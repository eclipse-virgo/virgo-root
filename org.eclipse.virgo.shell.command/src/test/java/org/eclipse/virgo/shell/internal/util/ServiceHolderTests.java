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

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.eclipse.virgo.kernel.osgi.quasi.QuasiBundle;
import org.eclipse.virgo.shell.internal.util.ServiceHolder;
import org.eclipse.virgo.shell.stubs.StubQuasiFramework;
import org.eclipse.virgo.test.stubs.framework.StubBundle;
import org.eclipse.virgo.test.stubs.framework.StubBundleContext;
import org.eclipse.virgo.test.stubs.framework.StubServiceReference;
import org.eclipse.virgo.test.stubs.framework.StubServiceRegistration;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Constants;
import org.osgi.framework.Version;

/**
 */
public class ServiceHolderTests {

    private ServiceHolder serviceHolder;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        StubServiceRegistration<Object> serviceRegistration = new StubServiceRegistration<Object>(new StubBundleContext());
        Dictionary<String, Object> properties = new Hashtable<String, Object>();
        properties.put(Constants.OBJECTCLASS, new String[] { "one", "two" });
        properties.put("Random", "foo");
        serviceRegistration.setProperties(properties);
        StubServiceReference<Object> stubServiceReference = new StubServiceReference<Object>(38l, 6, serviceRegistration);
        
        StubBundle stubBundle = new StubBundle(4L, "test.symbolic.name", new Version("1.2.3"), "");
        stubServiceReference.setBundle(stubBundle);
        
        stubServiceReference.addUsingBundles(new StubBundle(), new StubBundle());
        StubQuasiFramework stubQuasiFramework = new StubQuasiFramework(stubBundle);
        this.serviceHolder = new ServiceHolder(stubQuasiFramework, stubServiceReference);
    }

    /**
     * Test method for {@link ServiceHolder#getServiceId()}.
     */
    @Test
    public void testGetServiceId() {
        assertEquals(38l, this.serviceHolder.getServiceId());
    }

    /**
     * Test method for {@link ServiceHolder#getConsumers()}.
     */
    @Test
    public void testGetConsumers() {
        List<QuasiBundle> result = this.serviceHolder.getConsumers();
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    /**
     * Test method for {@link ServiceHolder#getProvider()}.
     */
    @Test
    public void testGetProvider() {
        assertEquals(4l, this.serviceHolder.getProvider().getBundleId());
    }

    /**
     * Test method for {@link ServiceHolder#getProperties()}.
     */
    @Test
    public void testGetProperties() {
        Map<String, Object> propertyMap = this.serviceHolder.getProperties();
        assertNotNull(propertyMap);
        assertEquals(4, propertyMap.size());
        assertNotNull(propertyMap.get(Constants.SERVICE_ID));
        assertEquals("foo", propertyMap.get("Random"));
    }

    /**
     * Test method for
     * {@link ServiceHolder#compareTo(QuasiLiveService)}
     * .
     */
    @Test
    public void testCompareTo() {
        int compareTo = this.serviceHolder.compareTo(this.serviceHolder);
        assertEquals(0, compareTo);
    }

}
