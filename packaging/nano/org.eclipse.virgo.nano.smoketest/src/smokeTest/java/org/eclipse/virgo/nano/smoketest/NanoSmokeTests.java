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

import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static org.eclipse.virgo.test.tools.JmxUtils.isDefaultJmxPortAvailable;
import static org.eclipse.virgo.test.tools.JmxUtils.isKernelStarted;
import static org.eclipse.virgo.test.tools.JmxUtils.waitForVirgoServerShutdownFully;
import static org.eclipse.virgo.test.tools.UrlWaitLatch.waitFor;
import static org.eclipse.virgo.test.tools.VirgoServerShutdownThread.shutdown;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.concurrent.TimeUnit;

import org.eclipse.virgo.test.tools.AbstractSmokeTests;
import org.eclipse.virgo.test.tools.JmxUtils;
import org.eclipse.virgo.test.tools.ServerUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class NanoSmokeTests extends AbstractSmokeTests {

    private static final String VIRGO_FLAVOR = "nano";

    private static final String JAVA_PROFILE_TESTER_1_0_0_JAR = "java.profile.tester_1.0.0.jar";

    @Override
    protected String getVirgoFlavor() {
        return VIRGO_FLAVOR;
    }

    @BeforeClass
    public static void initJmxConnection() {
        JmxUtils.virgoHome = ServerUtils.getHome(VIRGO_FLAVOR);
    }

    @Before
    public void startServer() throws Exception {
        // cleanup
        undeployTestBundles(VIRGO_FLAVOR, JAVA_PROFILE_TESTER_1_0_0_JAR);
        super.startServer();
    }

    @Test
    public void virgoNanoShouldBeStarted() throws Exception {
        assertTrue(isKernelStarted());
    }

    @Test
    public void testNanoJavaProfileSetCorrectly() throws Exception {
        assertTrue(isKernelStarted());

        // deploy bundle that should kill the server if the test is successful
        deployTestBundles(VIRGO_FLAVOR, JAVA_PROFILE_TESTER_1_0_0_JAR);

        // wait for deployment
        TimeUnit.SECONDS.sleep(10);
        if (isDefaultJmxPortAvailable()) {
            // success, we expect the server to be killed with this deployment unit
            return;
        }

        // the server is still running - shutdown and fail
        shutdown(ServerUtils.getBinDir(getVirgoFlavor()));
        assertTrue(waitForVirgoServerShutdownFully());
        fail("Virgo java profile not properly set - Nano was supposed to be killed if the test was successful.");
    }

    @Test
    @Ignore("Splash is not available in Virgo Nano")
    public void splashScreenShouldBeAccessable() throws Exception {
        assertEquals(SC_OK, waitFor("http://localhost:8080/"));
    }

    @Test
    public void adminScreenShouldBeDeniedWithWrongCredentials() {
        assertEquals(SC_UNAUTHORIZED, waitFor("http://localhost:8080/admin/content", "foo", "bar"));
    }

    @Test
    public void adminScreenShouldBeAccessableWithDefaultCredentials() {
        assertEquals(SC_OK, waitFor("http://localhost:8080/admin/content", "admin", "admin"));
    }

}
