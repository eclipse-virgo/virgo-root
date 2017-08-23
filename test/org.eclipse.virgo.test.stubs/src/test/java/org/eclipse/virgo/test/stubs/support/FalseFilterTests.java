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

package org.eclipse.virgo.test.stubs.support;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Dictionary;
import java.util.Map;

import org.junit.Test;
import org.osgi.framework.ServiceReference;

import org.eclipse.virgo.test.stubs.support.FalseFilter;

public class FalseFilterTests {

    private final FalseFilter filter = new FalseFilter();

    @Test
    public void match() {
        assertFalse(filter.match((Dictionary<String, ?>) null));
        assertFalse(filter.match((ServiceReference<?>) null));
        assertFalse(filter.matchCase(null));
        assertFalse(filter.matches((Map<String, ?>) null));
    }

    @Test
    public void testToString() {
        assertEquals("", new FalseFilter().getFilterString());
        assertEquals("testFilterString", new FalseFilter("testFilterString").toString());
    }
    
    @Test
    public void hashCodeEqualsToStringsHashCode() {
        FalseFilter falseFilter = new FalseFilter("testFilterString");
        assertEquals(falseFilter.hashCode(), falseFilter.toString().hashCode());
    }
}
