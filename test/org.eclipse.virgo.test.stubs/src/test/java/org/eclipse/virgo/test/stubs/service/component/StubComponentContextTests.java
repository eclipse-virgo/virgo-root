/*******************************************************************************
 * Copyright (c) 2008, 2010 SAP AG
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   SAP AG - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.test.stubs.service.component;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;

import org.eclipse.virgo.test.stubs.framework.StubBundleContext;

public class StubComponentContextTests {

    private StubBundleContext bundleContext = new StubBundleContext();

    private StubComponentContext componentContext = new StubComponentContext(this.bundleContext);

    @Test
    public void getProperties() {
        assertNotNull(this.componentContext.getProperties());
        assertTrue(0 != this.componentContext.getProperties().size());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void locateServiceWithName() {
        this.componentContext.locateService("testName");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void locateServiceWithNameAndServiceReference() {
        this.componentContext.locateService("testName", new ServiceReference<Object>() {

            public int compareTo(Object reference) {
                throw new UnsupportedOperationException();
            }

            public Bundle getBundle() {
                throw new UnsupportedOperationException();
            }

            public Object getProperty(String key) {
                return null;
            }

            public String[] getPropertyKeys() {
                throw new UnsupportedOperationException();
            }

            public Bundle[] getUsingBundles() {
                throw new UnsupportedOperationException();
            }

            public boolean isAssignableTo(Bundle bundle, String className) {
                throw new UnsupportedOperationException();
            }
        });
    }

    @Test(expected = UnsupportedOperationException.class)
    public void locateServices() {
        this.componentContext.locateServices("testName");
    }

    @Test
    public void getBundleContext() {
        assertEquals(this.bundleContext, this.componentContext.getBundleContext());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getUsingBundle() {
        this.componentContext.getUsingBundle();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getComponentInstance() {
        this.componentContext.getComponentInstance();
    }

    @Test
    public void enableComponent() {
        this.componentContext.enableComponent("testName");
    }

    @Test
    public void disableComponent() {
        this.componentContext.disableComponent("testName");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getServiceReference() {
        this.componentContext.getServiceReference();
    }

}
