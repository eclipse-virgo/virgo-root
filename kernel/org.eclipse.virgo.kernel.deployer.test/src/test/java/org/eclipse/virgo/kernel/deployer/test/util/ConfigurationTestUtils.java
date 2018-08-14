/*
 * This file is part of the Eclipse Virgo project.
 *
 * Copyright (c) 2011 Chariot Solutions LLC
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    dsklyut - initial contribution
 */

package org.eclipse.virgo.kernel.deployer.test.util;

import static org.junit.Assert.fail;

import java.io.IOException;

import org.eclipse.virgo.nano.deployer.api.core.ApplicationDeployer;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentIdentity;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

/**
 * Utilities to share between configuration and factory configuration tests.
 * <p />
 * 
 */
public final class ConfigurationTestUtils {

    public static void pollUntilInDeploymentIdentities(ApplicationDeployer appDeployer, String type, String name, String version)
        throws InterruptedException {
        long start = System.currentTimeMillis();

        while (!isInDeploymentIdentities(appDeployer, type, name, version)) {
            long delta = System.currentTimeMillis() - start;
            if (delta > 60000) {
                fail("Deployment identity was not available within 60 seconds");
            }
            Thread.sleep(100);
        }
    }

    public static void pollUntilNotInDeploymentIdentities(ApplicationDeployer appDeployer, String type, String name, String version)
        throws InterruptedException {
        long start = System.currentTimeMillis();

        while (isInDeploymentIdentities(appDeployer, type, name, version)) {
            long delta = System.currentTimeMillis() - start;
            if (delta > 60000) {
                fail("Deployment identity was still available after 60 seconds");
            }
            Thread.sleep(100);
        }
    }

    public static boolean isInDeploymentIdentities(ApplicationDeployer appDeployer, DeploymentIdentity deploymentIdentity) {
        for (DeploymentIdentity id : appDeployer.getDeploymentIdentities()) {
            if (deploymentIdentity.equals(id)) {
                return true;
            }
        }
        return false;

    }

    static boolean isInDeploymentIdentities(ApplicationDeployer appDeployer, String type, String name, String version) {
        for (DeploymentIdentity id : appDeployer.getDeploymentIdentities()) {
            if (id.getType().equals(type) && id.getSymbolicName().equals(name) && id.getVersion().equals(version)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isInConfigurationAdmin(ConfigurationAdmin configAdmin, String pid) throws IOException, InvalidSyntaxException {
        Configuration[] configurations = configAdmin.listConfigurations(null);
        for (Configuration configuration : configurations) {
            if (pid.equals(configuration.getPid())) {
                return true;
            }
        }

        return false;
    }

    static boolean isFactoryInConfigurationAdmin(ConfigurationAdmin configAdmin, String factoryPid) throws IOException, InvalidSyntaxException {
        Configuration[] configurations = configAdmin.listConfigurations(null);
        for (Configuration configuration : configurations) {
            if (factoryPid.equals(configuration.getFactoryPid())) {
                return true;
            }
        }

        return false;
    }

    public static void pollUntilFactoryInConfigurationAdmin(ConfigurationAdmin configAdmin, String factoryPid) throws Exception {
        long start = System.currentTimeMillis();

        while (!isFactoryInConfigurationAdmin(configAdmin, factoryPid)) {
            long delta = System.currentTimeMillis() - start;
            if (delta > 60000) {
                fail("Deployment identity was not available within 60 seconds");
            }
            Thread.sleep(100);
        }
    }

    public static void pollUntilFactoryNotInConfigurationAdmin(ConfigurationAdmin configAdmin, String factoryPid) throws Exception {
        long start = System.currentTimeMillis();

        while (isFactoryInConfigurationAdmin(configAdmin, factoryPid)) {
            long delta = System.currentTimeMillis() - start;
            if (delta > 60000) {
                fail("Deployment identity was still available after 60 seconds");
            }
            Thread.sleep(100);
        }
    }
}
