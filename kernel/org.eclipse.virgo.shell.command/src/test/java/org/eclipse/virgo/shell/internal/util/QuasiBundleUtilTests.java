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
package org.eclipse.virgo.shell.internal.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.eclipse.virgo.kernel.osgi.quasi.QuasiBundle;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiFrameworkFactory;
import org.eclipse.virgo.shell.internal.util.QuasiBundleUtil;
import org.eclipse.virgo.shell.stubs.StubQuasiFrameworkFactory;
import org.eclipse.virgo.test.stubs.framework.StubBundle;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Version;

public class QuasiBundleUtilTests {


    private QuasiBundleUtil quasiBundleUtil;

    private QuasiFrameworkFactory stubQuasiFrameworkFactory;
    
    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
    	StubBundle stubBundle = new StubBundle(4l, "test.symbolic.name", new Version("1.2.3"), "location");
        this.stubQuasiFrameworkFactory = new StubQuasiFrameworkFactory(stubBundle);
        this.quasiBundleUtil = new QuasiBundleUtil(this.stubQuasiFrameworkFactory);
    }

    @Test
    public void getAllBundlesNullDump() {
        List<QuasiBundle> result = this.quasiBundleUtil.getAllBundles();
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    public void getBundleNullDumpExists() {
        QuasiBundle quasiBundle = this.quasiBundleUtil.getBundle(4);
        assertNotNull(quasiBundle);
        assertEquals("test.symbolic.name", quasiBundle.getSymbolicName());
    }

    @Test
    public void getBundleNullDumpNoExists() {
        QuasiBundle quasiBundle = this.quasiBundleUtil.getBundle(5);
        assertNull(quasiBundle);
    }
    
}
