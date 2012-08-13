/*******************************************************************************
 * This file is part of the Eclipse Virgo project.
 *
 * Copyright (c) 2011 copyright_holder
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    cgfrost - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.kernel.model.internal;

import static org.easymock.EasyMock.createNiceMock;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.virgo.nano.serviceability.Assert.FatalAssertionException;
import org.eclipse.virgo.test.stubs.framework.StubBundle;
import org.eclipse.virgo.test.stubs.framework.StubBundleContext;
import org.eclipse.virgo.test.stubs.framework.StubFilter;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;
import org.springframework.context.ApplicationContext;

public class StandardSpringContextAccessorTests {
    
    private static final String FILTER = "(Bundle-SymbolicName=test-bundle)";

    private final StandardSpringContextAccessor standardSpringContextAccessor = new StandardSpringContextAccessor();

    private StubBundle bundle;
    
    private StubBundleContext bundleContext;
    
    @Before
    public void setUp(){
        this.bundle = new StubBundle("test-bundle", Version.emptyVersion);
        this.bundleContext = new StubBundleContext(bundle);
        this.bundle.setBundleContext(this.bundleContext);
        this.bundleContext.addFilter(FILTER, new TestFilter());
    }

    @Test(expected = FatalAssertionException.class)
    public void testNullBundle(){
        this.standardSpringContextAccessor.isSpringPowered(null);
    }
    
    @Test
    public void testSpringPowered(){
        Dictionary<String, String> properties = new Hashtable<String, String>();
        properties.put("Bundle-SymbolicName", "test-bundle");
        this.bundleContext.registerService(ApplicationContext.class, createNiceMock(ApplicationContext.class), properties);
        assertTrue(this.standardSpringContextAccessor.isSpringPowered(this.bundle));
    }

    @Test
    public void testNotSpringPowered(){
        assertFalse(this.standardSpringContextAccessor.isSpringPowered(this.bundle));
    }
    
    private class TestFilter implements StubFilter {

        @Override
        public boolean match(ServiceReference<?> reference) {
            return true;
        }

        @Override
        public boolean match(Dictionary<String, ?> dictionary) {
            return true;
        }

        @Override
        public boolean matchCase(Dictionary<String, ?> dictionary) {
            return true;
        }

        @Override
        public boolean matches(Map<String, ?> map) {
            return true;
        }

        @Override
        public String getFilterString() {
            return FILTER;
        }
        
    }
    
}
