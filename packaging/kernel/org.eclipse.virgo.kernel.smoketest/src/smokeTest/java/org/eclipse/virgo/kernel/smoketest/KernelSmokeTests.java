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

package org.eclipse.virgo.kernel.smoketest;

import static org.apache.http.HttpStatus.SC_OK;
import static org.eclipse.virgo.test.tools.JmxUtils.isKernelStarted;
import static org.eclipse.virgo.test.tools.UrlWaitLatch.waitFor;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.virgo.test.tools.AbstractSmokeTests;
import org.eclipse.virgo.test.tools.JmxUtils;
import org.eclipse.virgo.test.tools.ServerUtils;
import org.junit.BeforeClass;
import org.junit.Test;

public class KernelSmokeTests extends AbstractSmokeTests {

    private static final String VIRGO_FLAVOR = "kernel";

    @Override
    protected String getVirgoFlavor() {
        return VIRGO_FLAVOR;
    }

    @BeforeClass
    public static void initJmxConnection() {
        JmxUtils.virgoHome = ServerUtils.getHome(VIRGO_FLAVOR);
    }

    @Test
    public void virgoKernelShouldBeStarted() throws Exception {
        assertTrue(isKernelStarted());
    }

    @Test
    public void adminScreenShouldBeAccessableWithDefaultCredentials() {
        assertEquals(SC_OK, waitFor("http://localhost:8080/admin/content", "admin", "admin"));
    }

}
