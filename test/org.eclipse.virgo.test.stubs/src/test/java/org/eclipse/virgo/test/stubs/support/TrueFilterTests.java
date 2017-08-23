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
import static org.junit.Assert.assertTrue;

import java.util.Dictionary;
import java.util.Map;

import org.junit.Test;
import org.osgi.framework.ServiceReference;

public class TrueFilterTests {

    private final TrueFilter filter = new TrueFilter();

    @Test
    public void match() {
        assertTrue(filter.match((Dictionary<String, ?>) null));
        assertTrue(filter.match((ServiceReference<?>) null));
        assertTrue(filter.matchCase(null));
        assertTrue(filter.matches((Map<String, ?>) null));
    }

    @Test
    public void testToString() {
        assertEquals("", new TrueFilter().toString());
        assertEquals("testFilterString", new TrueFilter("testFilterString").toString());
    }

    @Test
    public void hashCodeEqualsToStringsHashCode() {
        assertEquals(this.filter.hashCode(), this.filter.toString().hashCode());
    }

}
