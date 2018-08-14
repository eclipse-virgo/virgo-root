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

package org.eclipse.virgo.kernel.osgi.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import org.eclipse.equinox.region.Region;
import org.eclipse.virgo.kernel.test.AbstractKernelIntegrationTest;

public class RegionTests extends AbstractKernelIntegrationTest {

    @Test
    public void testGetUserRegion() throws Exception {
        ServiceReference<?>[] serviceReferences = lookupRegionServices("org.eclipse.virgo.region.user");
        assertRegion(serviceReferences);
    }

    private void assertRegion(ServiceReference<?>[] serviceReferences) {
        assertEquals(1, serviceReferences.length);
        
        Region userRegion = (Region) this.kernelContext.getService(serviceReferences[0]);
        assertNotNull(userRegion);
    }

    private ServiceReference<?>[] lookupRegionServices(String name) throws InvalidSyntaxException {
        return this.kernelContext.getServiceReferences(Region.class.getName(), String.format("(org.eclipse.virgo.kernel.region.name=%s)",name));
    }
}
