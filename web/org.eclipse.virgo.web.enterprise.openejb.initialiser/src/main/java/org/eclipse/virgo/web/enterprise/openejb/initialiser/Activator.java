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

package org.eclipse.virgo.web.enterprise.openejb.initialiser;

import java.io.File;
import java.util.Properties;

import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.core.ThreadContextListener;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.Assembler;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {
    
    private Assembler assembler = null;
    private ThreadContextListener virgoThreadContextListener = new VirgoThreadContextListener();

    public void start(BundleContext bundleContext) throws Exception {
        setDefaultProperty("openejb.deploymentId.format", "{appId}/{ejbName}");
        setDefaultProperty("openejb.jndiname.strategy.class", "org.apache.openejb.assembler.classic.JndiBuilder$TemplatedStrategy");
        setDefaultProperty("openejb.jndiname.format", "{ejbName}{interfaceType.annotationName}");
        setDefaultProperty("openejb.jndiname.failoncollision", "false");

        ClassLoader old = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(Assembler.class.getClassLoader());

            this.assembler = new VirgoOpenEjbAssembler();

            Properties env = new Properties();
            env.put("openejb.conf.file", System.getProperty("org.eclipse.virgo.kernel.config") + File.separator + "openejb.xml");
            assembler.init(env);
            assembler.build();
            ThreadContext.addThreadContextListener(virgoThreadContextListener);
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }

    }

    public void stop(BundleContext bundleContext) throws Exception {
    	ThreadContext.removeThreadContextListener(virgoThreadContextListener);
    	if (this.assembler != null) {
            assembler.destroy();
        }
    }

    private void setDefaultProperty(String key, String value) {
        SystemInstance systemInstance = SystemInstance.get();

        String format = systemInstance.getProperty(key);
        if (format == null) {
            systemInstance.setProperty(key, value);
        }
    }

}
