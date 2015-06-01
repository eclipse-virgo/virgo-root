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

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.http.HttpStatus.SC_OK;
import static org.eclipse.virgo.test.tools.UrlWaitLatch.waitFor;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;

import org.eclipse.virgo.test.tools.AbstractSmokeTests;
import org.eclipse.virgo.test.tools.JmxUtils;
import org.eclipse.virgo.test.tools.ServerUtils;
import org.eclipse.virgo.test.tools.UrlWaitLatch;
import org.junit.BeforeClass;
import org.junit.Test;

public class JettyServerSmokeTests extends AbstractSmokeTests {

    private static final String OEV_JETTY_SAMPLE_TAGS_JAR = "org.eclipse.virgo.jetty.sample.tags.jar";

    private static final String VIRGO_FLAVOR = "jetty-server";

    @Override
    protected String getVirgoFlavor() {
        return VIRGO_FLAVOR;
    }

    @BeforeClass
    public static void initJmxConnection() {
        JmxUtils.virgoHome = ServerUtils.getHome(VIRGO_FLAVOR);
    }

    @Test
    public void jettyServerShouldBeStarted() throws Exception {
        assertTrue(JmxUtils.isKernelStarted());
    }

    @Test
    public void splashScreenShouldBeAccessable() throws Exception {
        assertEquals(SC_OK, waitFor("http://localhost:8080/"));
    }

    @Test
    public void adminScreenShouldBeAccessableWithDefaultCredentials() {
        assertEquals(SC_OK, waitFor("http://localhost:8080/admin/content", "admin", "admin"));
    }

    @Test
    public void testTagLibsScreen() throws Exception {
        deployTestBundles(VIRGO_FLAVOR, OEV_JETTY_SAMPLE_TAGS_JAR);
        // allow some more time for this deployment - test fails on Eclipse.org Hudson only.
        SECONDS.sleep(5);
        UrlWaitLatch.waitFor("http://localhost:8080/taglibs/app/sample.htm");
        undeployTestBundles(VIRGO_FLAVOR, OEV_JETTY_SAMPLE_TAGS_JAR);
    }

}
