/*******************************************************************************
 * Copyright (c) 2012 SAP AG
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Violeta Georgieva - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.web.enterprise.applistener.internal;

import org.apache.catalina.Context;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.tomcat.util.descriptor.web.ContextEjb;
import org.apache.tomcat.util.descriptor.web.ContextLocalEjb;
import org.apache.catalina.deploy.NamingResourcesImpl;

public class NamingResourcesListener implements LifecycleListener {

    @Override
    public void lifecycleEvent(LifecycleEvent event) {
        Object container = event.getLifecycle();

        if (!(container instanceof Context)) {
            return;
        }

        if (Lifecycle.CONFIGURE_START_EVENT.equals(event.getType())) {
            NamingResourcesImpl namingResources = ((Context) container).getNamingResources();

            ContextEjb[] ejbs = namingResources.findEjbs();
            for (int i = 0; ejbs != null && i < ejbs.length; i++) {
                namingResources.removeEjb(ejbs[i].getName());
            }

            ContextLocalEjb[] localEjbs = namingResources.findLocalEjbs();
            for (int i = 0; localEjbs != null && i < localEjbs.length; i++) {
                namingResources.removeLocalEjb(localEjbs[i].getName());
            }
        }
    }

}
