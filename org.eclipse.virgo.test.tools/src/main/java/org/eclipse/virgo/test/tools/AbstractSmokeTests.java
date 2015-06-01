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

package org.eclipse.virgo.test.tools;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.eclipse.virgo.test.tools.JmxUtils.isDefaultJmxPortAvailable;
import static org.eclipse.virgo.test.tools.JmxUtils.waitForVirgoServerShutdownFully;
import static org.eclipse.virgo.test.tools.JmxUtils.waitForVirgoServerStartFully;
import static org.eclipse.virgo.test.tools.VirgoServerShutdownThread.shutdown;
import static org.eclipse.virgo.test.tools.VirgoServerStartupThread.startup;
import static org.eclipse.virgo.util.io.FileCopyUtils.copy;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;

public abstract class AbstractSmokeTests {

    private String srcDir = "src/smokeTest/resources";

    private File bundlesDir = null;

    protected abstract String getVirgoFlavor();

    @Before
    public void startServer() throws Exception {
        if (!isDefaultJmxPortAvailable()) {
            System.out.println("Port not available. Waiting for a few seconds.");
            TimeUnit.SECONDS.sleep(10);
            if (!isDefaultJmxPortAvailable()) {
                System.out.println("Port still not available. Trying to shutdown running Virgo.");
                shutdown(ServerUtils.getBinDir(getVirgoFlavor()));
                if (!isDefaultJmxPortAvailable()) {
                    System.out.println("Port still not available. Giving up.");
                }
            }
        }
        assertTrue("Default JMX port in use. Is another Virgo server still up and running?!", isDefaultJmxPortAvailable());
        startup(ServerUtils.getBinDir(getVirgoFlavor()));
        assertTrue("Server '" + getVirgoFlavor() + "' not started properly.", waitForVirgoServerStartFully());
    }

    @After
    public void shutdownServer() throws Exception {
        shutdown(ServerUtils.getBinDir(getVirgoFlavor()));
        assertTrue("Server '" + getVirgoFlavor() + "' not shut down properly.", waitForVirgoServerShutdownFully());
    }

    private File setupBundleResourcesDir() {
        if (bundlesDir == null) {
            File testExpanded = new File("./" + srcDir);
            bundlesDir = new File(testExpanded, "bundles");
        }
        return bundlesDir;
    }

    public void deployTestBundles(String flavor, String bundleName) throws Exception {
        setupBundleResourcesDir();
        copy(new File(bundlesDir, bundleName), new File(ServerUtils.getPickupDir(flavor), bundleName));
        // allow the Server to finish the deployment
        SECONDS.sleep(10);
    }

    public void undeployTestBundles(String flavor, String bundleName) throws Exception {
        setupBundleResourcesDir();
        File file = new File(ServerUtils.getPickupDir(flavor), bundleName);
        if (file.exists()) {
            file.delete();
            // allow the Server to finish the undeployment
            SECONDS.sleep(5);
        }
    }

}
