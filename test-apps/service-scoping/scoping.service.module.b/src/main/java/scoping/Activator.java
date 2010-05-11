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

package scoping;

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;


public class Activator implements BundleActivator {

    private final String OBJECT_NAME = "scoping.test:type=Listener";
    
    public void start(BundleContext context) throws Exception {
        Listener listener = new Listener(context);
        context.addServiceListener(listener, "(test-case=app-listener)");
        
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        server.registerMBean(listener, ObjectName.getInstance(OBJECT_NAME));
    }

    public void stop(BundleContext context) throws Exception {
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        server.unregisterMBean(ObjectName.getInstance(OBJECT_NAME));
    }

}
