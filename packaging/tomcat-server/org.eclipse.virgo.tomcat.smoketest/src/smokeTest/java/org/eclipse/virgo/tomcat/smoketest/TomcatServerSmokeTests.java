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

package org.eclipse.virgo.tomcat.smoketest;

import static org.apache.http.HttpStatus.SC_OK;
import static org.eclipse.virgo.test.tools.UrlWaitLatch.waitFor;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.virgo.test.tools.AbstractSmokeTests;
import org.eclipse.virgo.test.tools.JmxUtils;
import org.eclipse.virgo.test.tools.ServerUtils;
import org.eclipse.virgo.test.tools.UrlWaitLatch;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class TomcatServerSmokeTests extends AbstractSmokeTests {

    private static final String FORMTAGS_PAR = "formtags-par.par";

    private static final String VIRGO_FLAVOR = "tomcat-server";

    @Override
    protected String getVirgoFlavor() {
        return VIRGO_FLAVOR;
    }

    @BeforeClass
    public static void initJmxConnection() {
        JmxUtils.virgoHome = ServerUtils.getHome(VIRGO_FLAVOR);
    }

    @Test
    public void tomcatServerShouldBeStarted() throws Exception {
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

    private static final String BASE_URL = "http://localhost:8080/formtags-par/";

    @Test
    @Ignore("Fails after moving away from EBR")
    // TODO - check why taglib doesn't work anymore - migrate taglib recipe to VTS 3.7.x
    public void testFormTagsListScreen() throws Exception {
        deployTestBundles(VIRGO_FLAVOR, FORMTAGS_PAR);
        UrlWaitLatch.waitFor(BASE_URL + "list.htm");
        undeployTestBundles(VIRGO_FLAVOR, FORMTAGS_PAR);
    }

    @Test
    @Ignore("Fails after moving away from EBR")
    // TODO - check why taglib doesn't work anymore - migrate taglib recipe to VTS 3.7.x
    public void testFormTagsFormScreen() throws Exception {
        deployTestBundles(VIRGO_FLAVOR, FORMTAGS_PAR);
        UrlWaitLatch.waitFor(BASE_URL + "form.htm?id=1");
        undeployTestBundles(VIRGO_FLAVOR, FORMTAGS_PAR);
    }

}
