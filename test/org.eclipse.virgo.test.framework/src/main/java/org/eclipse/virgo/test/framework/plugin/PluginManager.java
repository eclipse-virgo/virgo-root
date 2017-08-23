/*******************************************************************************
 * Copyright (c) 2008, 2010 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.test.framework.plugin;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class PluginManager {

    private final Plugin[] plugins;

    private final Plugin pluginDelegate;

    public PluginManager(Class<?> testClass) {
        this.plugins = createPlugins(testClass);
        this.pluginDelegate = (Plugin) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] { Plugin.class },
            new PluginDelegateInvocationHandler(this.plugins));
    }

    public Plugin[] getConfiguredPlugins() {
        Plugin[] copy = new Plugin[this.plugins.length];
        System.arraycopy(this.plugins, 0, copy, 0, copy.length);
        return copy;
    }

    public Plugin getPluginDelegate() {
        return this.pluginDelegate;
    }

    private Plugin[] createPlugins(Class<?> testClass) {
        Plugins annotation = testClass.getAnnotation(Plugins.class);
        if (annotation == null) {
            return new Plugin[0];
        } else {
            return doCreatePlugins(annotation.value());
        }

    }

    private Plugin[] doCreatePlugins(Class<? extends Plugin>[] pluginClasses) {
        Plugin[] result = new Plugin[pluginClasses.length];
        for (int x = 0; x < result.length; x++) {
            Class<? extends Plugin> cls = pluginClasses[x];
            try {
                result[x] = cls.newInstance();
            } catch (InstantiationException e) {
                throw new PluginException("Unable to instantiate plugin '" + cls.getName() + "'", e);
            } catch (IllegalAccessException e) {
                throw new PluginException("Unable to create plugin '" + cls.getName() + "'. Constructor not accessible.", e);
            }
        }
        return result;
    }

    private static class PluginDelegateInvocationHandler implements InvocationHandler {

        private final Plugin[] plugins;

        public PluginDelegateInvocationHandler(Plugin[] plugins) {
            this.plugins = plugins;
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            for (Plugin p : this.plugins) {
                try {
                    method.invoke(p, args);
                } catch (InvocationTargetException e) {
                    Throwable targetException = e.getTargetException();
                    throw targetException;
                }
            }
            return null;
        }
    }
}
