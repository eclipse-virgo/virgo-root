/*******************************************************************************
 * Copyright (c) 2012 SAP AG
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   SAP AG - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.web.enterprise.openwebbeans.initialiser;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.apache.catalina.Container;
import org.apache.catalina.ContainerEvent;
import org.apache.catalina.ContainerListener;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Service;
import org.apache.catalina.core.ContainerBase;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardEngine;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.core.StandardServer;
import org.apache.naming.ContextAccessController;
import org.apache.tomcat.InstanceManager;
import org.apache.webbeans.web.tomcat.TomcatUtil;

/**
 * Virgo Context lifecycle listener. Adapted from OWB and updated.
 * 
 */
public class VirgoContextLifecycleListener implements PropertyChangeListener, LifecycleListener, ContainerListener {

    private StandardServer standardServer;

    public VirgoContextLifecycleListener() {
    }

    @Override
    public void lifecycleEvent(LifecycleEvent event) {
        try {
            if (event.getSource() instanceof StandardServer) {
                if (event.getType().equals(Lifecycle.START_EVENT)) {
                    this.standardServer = (StandardServer) event.getSource();
                    start();
                }
            } else if (event.getSource() instanceof StandardContext) {
                StandardContext context = (StandardContext) event.getSource();

                if (event.getType().equals(Lifecycle.CONFIGURE_START_EVENT)) {
                    context.addContainerListener(this);
                    // Registering ELResolver with JSP container
                    System.setProperty("org.apache.webbeans.application.jsp", "true");

                    String[] oldListeners = context.findApplicationListeners();
                    addWebBeansConfigListenerAtFront(context, oldListeners);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void addWebBeansConfigListenerAtFront(StandardContext context, String[] oldListeners) {
        LinkedList<String> listeners = new LinkedList<String>();
        listeners.addFirst("org.apache.webbeans.servlet.WebBeansConfigurationListener");
        for (String listener : oldListeners) {
            listeners.add(listener);
            context.removeApplicationListener(listener);
        }
        for (String listener : listeners) {
            context.addApplicationListener(listener);
        }
    }

    @Override
    public void containerEvent(ContainerEvent event) {
        StandardContext context = null;

        try {
            if (event.getSource() instanceof StandardContext) {
                context = (StandardContext) event.getSource();

                if (event.getType().equals("beforeContextInitialized")) {
                    ClassLoader loader = context.getLoader().getClassLoader();
                    Object listener = event.getData();

                    if (listener.getClass().getName().equals("org.apache.webbeans.servlet.WebBeansConfigurationListener")) {
                        ContextAccessController.setWritable(context.getNamingContextListener().getName(), context);
                        return;
                    } else {
                        TomcatUtil.inject(listener, loader);
                    }

                } else if (event.getType().equals("afterContextInitialized")) {
                    ClassLoader loader = context.getLoader().getClassLoader();
                    Object listener = event.getData();

                    if (listener.getClass().getName().equals("org.apache.webbeans.servlet.WebBeansConfigurationListener")) {
                        InstanceManager processor = context.getInstanceManager();
                        InstanceManager custom = new VirgoInstanceManager(context.getLoader().getClassLoader(), processor);
                        context.setInstanceManager(custom);

                        context.getServletContext().setAttribute(InstanceManager.class.getName(), custom);

                        ContextAccessController.setReadOnly(context.getNamingContextListener().getName());

                        Object[] listeners = context.getApplicationEventListeners();
                        for (Object instance : listeners) {
                            if (!instance.getClass().getName().equals("org.apache.webbeans.servlet.WebBeansConfigurationListener")) {
                                TomcatUtil.inject(instance, loader);
                            }
                        }
                    }
                } else if (event.getType().equals("beforeContextDestroyed")) {
                    Object listener = event.getData();
                    if (listener.getClass().getName().equals("org.apache.webbeans.servlet.WebBeansConfigurationListener")) {
                        ContextAccessController.setWritable(context.getNamingContextListener().getName(), context);
                    }
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void start() {
        // hook the hosts so we get notified before contexts are started
        this.standardServer.addPropertyChangeListener(this);
        this.standardServer.addLifecycleListener(this);
        for (Service service : this.standardServer.findServices()) {
            serviceAdded(service);
        }
    }

    public void stop() {
        this.standardServer.removePropertyChangeListener(this);
    }

    private void serviceAdded(Service service) {
        Container container = service.getContainer();
        if (container instanceof StandardEngine) {
            StandardEngine engine = (StandardEngine) container;
            engineAdded(engine);
        }
    }

    private void serviceRemoved(Service service) {
        Container container = service.getContainer();
        if (container instanceof StandardEngine) {
            StandardEngine engine = (StandardEngine) container;
            engineRemoved(engine);
        }
    }

    private void engineAdded(StandardEngine engine) {
        addContextListener(engine);
        for (Container child : engine.findChildren()) {
            if (child instanceof StandardHost) {
                StandardHost host = (StandardHost) child;
                hostAdded(host);
            }
        }
    }

    private void engineRemoved(StandardEngine engine) {
        for (Container child : engine.findChildren()) {
            if (child instanceof StandardHost) {
                StandardHost host = (StandardHost) child;
                hostRemoved(host);
            }
        }
    }

    private void hostAdded(StandardHost host) {
        addContextListener(host);
        host.addLifecycleListener(this);
        for (Container child : host.findChildren()) {
            if (child instanceof StandardContext) {
                StandardContext context = (StandardContext) child;
                contextAdded(context);
            }
        }
    }

    private void hostRemoved(StandardHost host) {
        for (Container child : host.findChildren()) {
            if (child instanceof StandardContext) {
                StandardContext context = (StandardContext) child;
                contextRemoved(context);
            }
        }
    }

    private void contextAdded(StandardContext context) {
        // put this class as the first listener so we can process the
        // application before any classes are loaded
        forceFirstLifecycleListener(context);
    }

    private void forceFirstLifecycleListener(StandardContext context) {
        LifecycleListener[] listeners = context.findLifecycleListeners();

        // if we are already first return
        if (listeners.length > 0 && listeners[0] == this) {
            return;
        }

        // remove all of the current listeners
        for (LifecycleListener listener : listeners) {
            context.removeLifecycleListener(listener);
        }

        // add this class (as first)
        context.addLifecycleListener(this);
        context.addContainerListener(this);

        // add back all listeners
        for (LifecycleListener listener : listeners) {
            if (listener != this) {
                context.addLifecycleListener(listener);
            }
        }
    }

    private void contextRemoved(StandardContext context) {

    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if ("service".equals(event.getPropertyName())) {
            Object oldValue = event.getOldValue();
            Object newValue = event.getNewValue();
            if (oldValue == null && newValue instanceof Service) {
                serviceAdded((Service) newValue);
            }
            if (oldValue instanceof Service && newValue == null) {
                serviceRemoved((Service) oldValue);
            }
        }
        if ("children".equals(event.getPropertyName())) {
            Object source = event.getSource();
            Object oldValue = event.getOldValue();
            Object newValue = event.getNewValue();
            if (source instanceof StandardEngine) {
                if (oldValue == null && newValue instanceof StandardHost) {
                    hostAdded((StandardHost) newValue);
                }
                if (oldValue instanceof StandardHost && newValue == null) {
                    hostRemoved((StandardHost) oldValue);
                }
            }
            if (source instanceof StandardHost) {
                if (oldValue == null && newValue instanceof StandardContext) {
                    contextAdded((StandardContext) newValue);
                }
                if (oldValue instanceof StandardContext && newValue == null) {
                    contextRemoved((StandardContext) oldValue);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void addContextListener(ContainerBase containerBase) {
        try {
            Field field = (Field) AccessController.doPrivileged(new PrivilegedActionForClass(ContainerBase.class, "children"));
            AccessController.doPrivileged(new PrivilegedActionForAccessibleObject(field, true));
            Map<Object, Object> children = (Map<Object, Object>) field.get(containerBase);
            if (children instanceof VirgoContextLifecycleListener.MoniterableHashMap) {
                return;
            }
            children = new VirgoContextLifecycleListener.MoniterableHashMap(children, containerBase, "children", this);
            field.set(containerBase, children);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public static class MoniterableHashMap extends HashMap<Object, Object> {

        private static final long serialVersionUID = 1L;

        private final Object source;

        private final String propertyName;

        private final PropertyChangeListener listener;

        public MoniterableHashMap(Map<Object, Object> m, Object source, String propertyName, PropertyChangeListener listener) {
            super(m);
            this.source = source;
            this.propertyName = propertyName;
            this.listener = listener;
        }

        @Override
        public Object put(Object key, Object value) {
            Object oldValue = super.put(key, value);
            PropertyChangeEvent event = new PropertyChangeEvent(this.source, this.propertyName, null, value);
            this.listener.propertyChange(event);
            return oldValue;
        }

        @Override
        public Object remove(Object key) {
            Object value = super.remove(key);
            PropertyChangeEvent event = new PropertyChangeEvent(this.source, this.propertyName, value, null);
            this.listener.propertyChange(event);
            return value;
        }
    }

    protected static class PrivilegedActionForAccessibleObject implements PrivilegedAction<Object> {

        AccessibleObject object;

        boolean flag;

        protected PrivilegedActionForAccessibleObject(AccessibleObject object, boolean flag) {
            this.object = object;
            this.flag = flag;
        }

        @Override
        public Object run() {
            this.object.setAccessible(this.flag);
            return null;
        }
    }

    protected static class PrivilegedActionForClass implements PrivilegedAction<Object> {

        Class<?> clazz;

        Object parameters;

        protected PrivilegedActionForClass(Class<?> clazz, Object parameters) {
            this.clazz = clazz;
            this.parameters = parameters;
        }

        @Override
        public Object run() {
            try {
                return this.clazz.getDeclaredField((String) this.parameters);
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
        }

    }

}
