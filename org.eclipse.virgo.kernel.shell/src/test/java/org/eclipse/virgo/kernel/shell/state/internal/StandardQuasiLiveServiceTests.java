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

package org.eclipse.virgo.kernel.shell.state.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.eclipse.virgo.kernel.osgi.quasi.QuasiBundle;
import org.eclipse.virgo.kernel.shell.internal.util.ServiceHolder;
import org.eclipse.virgo.kernel.shell.state.QuasiLiveService;
import org.eclipse.virgo.kernel.shell.stubs.StubQuasiFramework;
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
public class StandardQuasiLiveServiceTests {

    private QuasiLiveService standardQuasiLiveService;

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
        stubServiceReference.setBundle(new StubBundle(4l, "Name", new Version("1.2.3"), "Location"));
        stubServiceReference.addUsingBundles(new StubBundle(), new StubBundle());
        StubQuasiFramework stubQuasiFramework = new StubQuasiFramework();
        this.standardQuasiLiveService = new ServiceHolder(stubQuasiFramework, stubServiceReference);
    }

    /**
     * Test method for {@link ServiceHolder#getServiceId()}.
     */
    @Test
    public void testGetServiceId() {
        assertEquals(38l, this.standardQuasiLiveService.getServiceId());
    }

    /**
     * Test method for {@link ServiceHolder#getConsumers()}.
     */
    @Test
    public void testGetConsumers() {
        List<QuasiBundle> result = this.standardQuasiLiveService.getConsumers();
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    /**
     * Test method for {@link ServiceHolder#getProvider()}.
     */
    @Test
    public void testGetProvider() {
        assertEquals(4l, this.standardQuasiLiveService.getProvider().getBundleId());
    }

    /**
     * Test method for {@link ServiceHolder#getProperties()}.
     */
    @Test
    public void testGetProperties() {
        Map<String, Object> propertyMap = this.standardQuasiLiveService.getProperties();
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
        int compareTo = this.standardQuasiLiveService.compareTo(this.standardQuasiLiveService);
        assertEquals(0, compareTo);
    }

}
