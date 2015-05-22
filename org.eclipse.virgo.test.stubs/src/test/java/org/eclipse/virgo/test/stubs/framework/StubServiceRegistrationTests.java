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

package org.eclipse.virgo.test.stubs.framework;

import static org.eclipse.virgo.test.stubs.AdditionalAsserts.assertContains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.util.Dictionary;
import java.util.Hashtable;

import org.junit.Test;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

public class StubServiceRegistrationTests {

    private StubServiceRegistration<Object> reg = new StubServiceRegistration<Object>(new StubBundleContext(new StubBundle()));

    @Test
    public void initialState() {
        assertNotNull(this.reg.getReference().getBundle());
        assertEquals(3, this.reg.getProperties().size());
        assertNotNull(this.reg.getReference());
    }

    @Test
    public void getCustomReference() {
        StubServiceReference<Object> ref = new StubServiceReference<Object>(1L, 1, reg);
        this.reg.setServiceReference(ref);
        assertSame(ref, this.reg.getReference());
    }

    @Test(expected = IllegalStateException.class)
    public void getReferenceUnregistered() {
        this.reg.unregister();
        this.reg.getReference();
    }

    @Test
    public void getCustomProperties() {
        TestServiceListener listener = new TestServiceListener();
        this.reg.getBundleContext().addServiceListener(listener);
        Dictionary<String, String> testDictionary = new Hashtable<String, String>();
        testDictionary.put("testKey", "testValue");
        this.reg.setProperties(testDictionary);
        assertNotNull(listener.getEvent());
        assertEquals(ServiceEvent.MODIFIED, listener.getEvent().getType());
        assertEquals(4, this.reg.getProperties().size());
        assertEquals("testValue", this.reg.getProperties().get("testKey"));
    }

    @Test
    public void setPropertiesNull() {
        Dictionary<String, Object> initial = this.reg.getProperties();
        this.reg.setProperties(null);
        assertEquals(initial, this.reg.getProperties());
    }

    @Test
    public void unregister() throws BundleException {
        this.reg.getBundleContext().getContextBundle().start();
        TestServiceListener listener = new TestServiceListener();
        this.reg.getBundleContext().addServiceListener(listener);
        ServiceReference<?> ref = this.reg.getReference();
        this.reg.unregister();
        assertNotNull(listener.getEvent());
        assertEquals(ServiceEvent.UNREGISTERING, listener.getEvent().getType());
        assertNull(ref.getBundle());
    }

    @Test(expected = IllegalStateException.class)
    public void unregisterUnregistered() {
        this.reg.unregister();
        this.reg.unregister();
    }

    @Test
    public void testToString() {
        String toString = reg.toString();
        assertContains("object classes", toString);
        assertContains("unregistered", toString);
        assertContains("properties", toString);
    }

    private static class TestServiceListener implements ServiceListener {

        private ServiceEvent event = null;

        public void serviceChanged(ServiceEvent event) {
            this.event = event;
        }

        public ServiceEvent getEvent() {
            return this.event;
        }
    }

}
