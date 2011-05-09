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

package org.eclipse.virgo.apps.admin.core.state;

import static org.junit.Assert.*;

import org.eclipse.virgo.apps.admin.core.BundleHolder;
import org.eclipse.virgo.apps.admin.core.state.StandardRequiredBundleHolder;
import org.eclipse.virgo.apps.admin.core.stubs.StubModuleContextAccessor;
import org.eclipse.virgo.apps.admin.core.stubs.StubQuasiRequiredBundle;
import org.eclipse.virgo.apps.admin.core.stubs.StubStateService;
import org.junit.Before;
import org.junit.Test;


/**
 */
public class StandardRequiredBundleHolderTests {

    StandardRequiredBundleHolder standardRequiredBundleHolder;
    
    @Before
    public void setup(){
        this.standardRequiredBundleHolder = new StandardRequiredBundleHolder(new StubQuasiRequiredBundle("testBundle", 5, 6), new StubModuleContextAccessor(), new StubStateService());
    }
    
    /**
     * Test method for {@link org.eclipse.virgo.apps.admin.core.state.StandardRequiredBundleHolder#getProvider()}.
     */
    @Test
    public void testGetProvider() {
        BundleHolder provider = this.standardRequiredBundleHolder.getProvider();
        assertEquals(new Long(5), provider.getBundleId());
    }

    /**
     * Test method for {@link org.eclipse.virgo.apps.admin.core.state.StandardRequiredBundleHolder#getRequiredBundleName()}.
     */
    @Test
    public void testGetRequiredBundleName() {
        String requiredBundleName = this.standardRequiredBundleHolder.getRequiredBundleName();
        assertEquals("testBundle", requiredBundleName);
    }

    /**
     * Test method for {@link org.eclipse.virgo.apps.admin.core.state.StandardRequiredBundleHolder#getRequiringBundle()}.
     */
    @Test
    public void testGetRequiringBundle() {
        BundleHolder requiringBundle = this.standardRequiredBundleHolder.getRequiringBundle();
        assertEquals(new Long(6), requiringBundle.getBundleId());
    }

    /**
     * Test method for {@link org.eclipse.virgo.apps.admin.core.state.StandardRequiredBundleHolder#getVersionConstraint()}.
     */
    @Test
    public void testGetVersionConstraint() {
        String versionConstraint = this.standardRequiredBundleHolder.getVersionConstraint();
        assertEquals("[0.0.0, &infin;)", versionConstraint);
    }

    /**
     * Test method for {@link org.eclipse.virgo.apps.admin.core.state.StandardRequiredBundleHolder#isResolved()}.
     */
    @Test
    public void testIsResolved() {
        boolean resolved = this.standardRequiredBundleHolder.isResolved();
        assertFalse(resolved);
    }

}
