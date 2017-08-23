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

package org.eclipse.virgo.kernel.deployer.test;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.jar.JarFile;

import org.eclipse.virgo.nano.deployer.api.core.DeploymentIdentity;
import org.osgi.framework.ServiceRegistration;

import org.eclipse.virgo.kernel.module.ModuleContextEvent;
import org.eclipse.virgo.kernel.module.ModuleContextEventListener;
import org.eclipse.virgo.kernel.module.ModuleContextFailedEvent;

/**
 */
public abstract class AbstractParTests extends AbstractDeployerIntegrationTest {

    protected DeploymentIdentity deploy(File file) throws Throwable {
        String appSymbolicName = getApplicationSymbolicName(file);

        FailureTrackingListener listener = new FailureTrackingListener(appSymbolicName);
        ServiceRegistration<ModuleContextEventListener> registration = this.context.registerService(ModuleContextEventListener.class, listener, null);

        try {
            DeploymentIdentity deploymentIdentity = this.deployer.deploy(file.toURI());
            if (listener.cause != null) {
                throw listener.cause;
            }
            return deploymentIdentity;
        } finally {
            if (registration != null) {
                registration.unregister();
            }
        }
    }

    private String getApplicationSymbolicName(File par) throws Exception {
        JarFile jar = null;
        try {
            jar = new JarFile(par);
            String name = jar.getManifest().getMainAttributes().getValue("Application-SymbolicName");
            if (name == null) {
                name = jar.getManifest().getMainAttributes().getValue("Bundle-SymbolicName");
            }
            jar.close();
            assertTrue("Application-SymbolicName or Bundle-SymbolicName cannot be found for file: " + par, name != null && name.length() > 0);
            return name;
        } finally {
            if (jar != null) {
                jar.close();
            }
        }
    }

    private final class FailureTrackingListener implements ModuleContextEventListener {

        private final String appName;

        private Throwable cause;

        private FailureTrackingListener(String appName) {
            this.appName = appName;
        }

        public void onEvent(ModuleContextEvent moduleContextEvent) {
            String symbolicName = moduleContextEvent.getBundle().getSymbolicName();
            if (symbolicName.startsWith(this.appName) && moduleContextEvent instanceof ModuleContextFailedEvent) {
                this.cause = ((ModuleContextFailedEvent) moduleContextEvent).getFailureCause();
            }
        }
    }
}
