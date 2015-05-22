/*******************************************************************************
 * Copyright (c) 2008, 2012 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *   SAP AG - re-factoring
 *******************************************************************************/

package org.eclipse.virgo.nano.smoketest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.eclipse.virgo.test.tools.AbstractSmokeTests;
import org.eclipse.virgo.test.tools.JmxUtils;
import org.eclipse.virgo.test.tools.ServerUtils;
import org.eclipse.virgo.test.tools.UrlWaitLatch;
import org.eclipse.virgo.test.tools.VirgoServerShutdownThread;
import org.eclipse.virgo.test.tools.VirgoServerStartupThread;
import org.eclipse.virgo.util.io.NetUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class NanoSmokeTests extends AbstractSmokeTests {

    private static final String VIRGO_FLAVOR = "nano";

    private static final String JAVA_PROFILE_TESTER_1_0_0_JAR = "java.profile.tester_1.0.0.jar";

    @BeforeClass
    public static void initJmxConnection() {
        JmxUtils.virgoHome = ServerUtils.getHome(VIRGO_FLAVOR);
    }

    @Before
    public void startServer() throws Exception {
        // cleanup
        undeployTestBundles(VIRGO_FLAVOR, JAVA_PROFILE_TESTER_1_0_0_JAR);

        new Thread(new VirgoServerStartupThread(ServerUtils.getBinDir(VIRGO_FLAVOR))).start();
        JmxUtils.waitForVirgoServerStartFully();
        Thread.sleep(5000); // wait for startup to complete in case it fails

        assertEquals(JmxUtils.STATUS_STARTED, JmxUtils.getKernelStatus());
    }

    @After
    public void shutdownServer() throws Exception {
        new Thread(new VirgoServerShutdownThread(ServerUtils.getBinDir(VIRGO_FLAVOR))).start();
        JmxUtils.waitForVirgoServerShutdownFully();
    }

    @Test
    public void testNanoStartAndStop() throws Exception {
        assertEquals(JmxUtils.STATUS_STARTED, JmxUtils.getKernelStatus());
    }

    @Test
    public void testNanoJavaProfileSetCorrectly() throws Exception {
        assertEquals(JmxUtils.STATUS_STARTED, JmxUtils.getKernelStatus());

        // deploy bundle that should kill the server if the test is successful
        deployTestBundles(VIRGO_FLAVOR, JAVA_PROFILE_TESTER_1_0_0_JAR);

        Thread.sleep(10000); // wait for deployment

        if (NetUtils.isPortAvailable(9875)) {
            return;
        }

        // the server is still running - shutdown and fail
        new Thread(new VirgoServerShutdownThread(ServerUtils.getBinDir(VIRGO_FLAVOR))).start();
        JmxUtils.waitForVirgoServerShutdownFully();
        fail("Virgo java profile not properly set - Nano was supposed to be killed if the test was successful.");
    }

    @Test
    public void splashScreenShouldBeAccessable() throws Exception {
        UrlWaitLatch.waitFor("http://localhost:8080/");
    }

    @Test
    public void adminScreenShouldBeAccessableWithDefaultCredentials() {
        UrlWaitLatch.waitFor("http://localhost:8080/admin/content", "foo", "bar");
    }

}
