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

import static org.junit.Assert.assertEquals;

import org.eclipse.virgo.apps.admin.core.BundleHolder;
import org.eclipse.virgo.apps.admin.core.state.StandardFailedResolutionHolder;
import org.eclipse.virgo.apps.admin.core.stubs.StubModuleContextAccessor;
import org.eclipse.virgo.apps.admin.core.stubs.StubQuasiResolutionFaliure;
import org.junit.Before;
import org.junit.Test;


/**
 */
public class StandardFailedResolutionHolderTests {

    private StandardFailedResolutionHolder standardFailedResolutionHolder;
   
    @Before
    public void setUp(){
        this.standardFailedResolutionHolder = new StandardFailedResolutionHolder(new StubQuasiResolutionFaliure("test", 5), new StubModuleContextAccessor());
    }
    
    @Test
    public void testGetDescription() {
        String result = this.standardFailedResolutionHolder.getDescription();
        assertEquals("test", result);
    }

    @Test
    public void testGetUnresolvedBundle() {
        BundleHolder unresolvedBundle = this.standardFailedResolutionHolder.getUnresolvedBundle();
        assertEquals(new Long(5), unresolvedBundle.getBundleId());
    }
    
}
