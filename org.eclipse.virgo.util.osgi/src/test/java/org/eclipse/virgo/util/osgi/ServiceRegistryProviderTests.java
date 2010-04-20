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

package org.eclipse.virgo.util.osgi;

import static org.junit.Assert.*;

import java.util.Set;

import org.eclipse.virgo.util.osgi.ServiceRegistryProvider;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;


/**
 * TODO Document ServiceRegistryProviderTests
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * TODO Document concurrent semantics of ServiceRegistryProviderTests
 *
 */
public class ServiceRegistryProviderTests {

    private ServiceRegistryProvider<ServiceObject> serviceRegistryProvider;
    
    private BundleContext bundleContext;
    
    @Before
    public void setUp() throws Exception {
        this.bundleContext = new StubBundleContext();
        this.serviceRegistryProvider = new ServiceRegistryProvider<ServiceObject>(this.bundleContext, ServiceObject.class);
    }

    /**
     * Test method for {@link org.eclipse.virgo.util.osgi.ServiceRegistryProvider#getSet()}.
     */
    @Test
    public void testGetServices() {
        Set<ServiceObject> services = this.serviceRegistryProvider.getSet();
        assertNotNull(services);
        //System.out.println(services);
        assertEquals(1, services.size());
    }

    /**
     * Test method for {@link org.eclipse.virgo.util.osgi.ServiceRegistryProvider#close()}.
     */
    @Test
    public void testClose() {
        this.serviceRegistryProvider.close();
        Set<ServiceObject> services = this.serviceRegistryProvider.getSet();
        assertNotNull(services);
        assertEquals(0, services.size());
    }

}
