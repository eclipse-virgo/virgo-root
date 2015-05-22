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

package org.eclipse.virgo.server.smoketest;

import static org.junit.Assert.assertEquals;

import org.eclipse.virgo.test.tools.AbstractSmokeTests;
import org.eclipse.virgo.test.tools.JmxUtils;
import org.eclipse.virgo.test.tools.ServerUtils;
import org.eclipse.virgo.test.tools.UrlWaitLatch;
import org.eclipse.virgo.test.tools.VirgoServerShutdownThread;
import org.eclipse.virgo.test.tools.VirgoServerStartupThread;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class JettyServerSmokeTests extends AbstractSmokeTests {

    private static final String OEV_JETTY_SAMPLE_TAGS_JAR = "org.eclipse.virgo.jetty.sample.tags.jar";
    private static final String VIRGO_FLAVOR = "jetty-server";

    @BeforeClass
    public static void initJmxConnection() {
        JmxUtils.virgoHome = ServerUtils.getHome(VIRGO_FLAVOR);
    }

    @Before
    public void startServer() throws Exception {
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
    public void testKernelStatus() throws Exception {
        assertEquals(JmxUtils.STATUS_STARTED, JmxUtils.getKernelStatus());
    }

    @Test
    public void connectToSplashScreen() throws Exception {
        UrlWaitLatch.waitFor("http://localhost:8080/");
    }

    @Test
    public void testAdminScreen() {
        UrlWaitLatch.waitFor("http://localhost:8080/admin", "admin", "admin");
    }

    @Test
    public void testTagLibsScreen() {
        deployTestBundles(VIRGO_FLAVOR, OEV_JETTY_SAMPLE_TAGS_JAR);
        UrlWaitLatch.waitFor("http://localhost:8080/taglibs/app/sample.htm");
        undeployTestBundles(VIRGO_FLAVOR, OEV_JETTY_SAMPLE_TAGS_JAR);
    }

}
