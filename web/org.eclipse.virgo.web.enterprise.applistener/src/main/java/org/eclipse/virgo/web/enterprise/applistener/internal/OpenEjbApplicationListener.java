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

package org.eclipse.virgo.web.enterprise.applistener.internal;

import java.io.File;

import javax.servlet.ServletContext;

import org.apache.catalina.Context;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Loader;
import org.apache.catalina.core.StandardContext;
import org.eclipse.virgo.web.enterprise.openejb.deployer.VirgoDeployerEjb;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenEjbApplicationListener implements LifecycleListener {
    // globally synchronize deployment because of overlapping app data
    private static Object monitor = new Object();
    
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    public void deploy(StandardContext standardContext) throws Exception {
        ServletContext context = standardContext.getServletContext();
        VirgoDeployerEjb deployer = new VirgoDeployerEjb(context);
        try {
            String realPath = context.getRealPath("");
            synchronized (monitor) {
                if (realPath != null) {
                    deployer.deploy(realPath, standardContext);
                } else {
                    deployer.deploy(getAppModuleId(standardContext.getDocBase()), standardContext);
                }
            }
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Failed to initialise enterprise container for application with context path '" + context.getContextPath() + "'", e);
            }
            throw e;
        }
    }

    // no need to synchronize the undeploy operation as it is stateless
    public void undeploy(StandardContext standardContext) throws Exception {
        ServletContext context = standardContext.getServletContext();
        VirgoDeployerEjb deployer = new VirgoDeployerEjb(context);
        try {
            String realPath = context.getRealPath("");
            if (realPath != null) {
                deployer.undeploy(realPath);
            } else {
                deployer.undeploy(new File(getAppModuleId(standardContext.getDocBase())).getAbsolutePath());
            }
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Failed to destroy enterprise container for application with context path '" + context.getContextPath() + "'", e);
            }
            throw e;
        }
    }

    private String getAppModuleId(String docBase) {
        String appModuleId;

        File appLocation = new File(docBase);
        if (!appLocation.isAbsolute()) {
            appModuleId = System.getProperty("org.eclipse.virgo.kernel.home") + File.separator + docBase;
        } else {
            appModuleId = docBase;
        }

        return appModuleId;
    }

    @Override
    public void lifecycleEvent(LifecycleEvent event) {
        Object source = event.getSource();
        if (source instanceof StandardContext) {
            StandardContext standardContext = (StandardContext) source;
            if (Lifecycle.BEFORE_START_EVENT.equals(event.getType())) {
                Loader loader = standardContext.getLoader();
                if (loader != null && loader instanceof Lifecycle) {
                    ((Lifecycle) loader).addLifecycleListener(this);
                }
            } else if (Lifecycle.CONFIGURE_START_EVENT.equals(event.getType())) {
                try {
                    deploy(standardContext);
                } catch (Exception e) {
                    standardContext.setConfigured(false);
                }
            } else if (Lifecycle.CONFIGURE_STOP_EVENT.equals(event.getType())) {
                try {
                    undeploy(standardContext);
                } catch (Exception e) {
                    standardContext.setConfigured(false);
                }
            }
        } else if (source instanceof Loader) {
            if (Lifecycle.AFTER_START_EVENT.equals(event.getType())) {
                // This event is very important
                // It reorders the lifecycle listeners
                // so that this listener appears after NamingContextListener
                Context context = ((Loader) source).getContext();
                LifecycleListener[] listeners = ((StandardContext) context).findLifecycleListeners();
                for (int i = 0; listeners != null && i < listeners.length; i++) {
                    if (listeners[i].equals(this)) {
                        context.removeLifecycleListener(this);// remove the listener from its current position
                    }
                }
                context.addLifecycleListener(this);// add the listener at the end
            }
        }
    }
}
