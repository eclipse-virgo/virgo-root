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

package org.eclipse.virgo.kernel.model.internal;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Version;


public class KernelRegionBundleTests {
    
    private KernelRegionBundle kernelRegionBundle;

    @Before
    public void setUp(){
        this.kernelRegionBundle =  new KernelRegionBundle(new Version("3.0.0"));
    }
    
    @Test
    public void kernelVersion() {
        assertEquals("3.0.0", this.kernelRegionBundle.getVersion().toString());
    }

}