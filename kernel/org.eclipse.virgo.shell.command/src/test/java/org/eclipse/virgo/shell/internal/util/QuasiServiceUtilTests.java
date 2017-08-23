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

import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.eclipse.virgo.kernel.osgi.quasi.QuasiFrameworkFactory;
import org.eclipse.virgo.shell.internal.util.QuasiServiceUtil;
import org.eclipse.virgo.shell.internal.util.ServiceHolder;
import org.eclipse.virgo.shell.stubs.StubQuasiFrameworkFactory;
import org.eclipse.virgo.test.stubs.framework.StubBundle;
import org.eclipse.virgo.test.stubs.framework.StubBundleContext;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Version;

/**
 * <p>
 * Tests for {@link StandardStateService}
 * </p>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Tests
 * 
 */
public class QuasiServiceUtilTests {

    private QuasiServiceUtil quasiServiceUtil;

    private StubBundleContext stubBundleContext;

    private QuasiFrameworkFactory stubQuasiFrameworkFactory;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        this.stubBundleContext = new StubBundleContext();
        
        StubBundle stubSystemBundle = new StubBundle(0L, "stub.system.bundle", Version.emptyVersion, "stubLocation");
        this.stubBundleContext.addInstalledBundle(stubSystemBundle);
        
        this.stubQuasiFrameworkFactory = new StubQuasiFrameworkFactory();
        this.quasiServiceUtil = new QuasiServiceUtil(this.stubBundleContext, this.stubQuasiFrameworkFactory);
    }

    @Test
    public void getAllServices() {
        List<ServiceHolder> allServices = this.quasiServiceUtil.getAllServices();
        assertNotNull(allServices);
    }

}
