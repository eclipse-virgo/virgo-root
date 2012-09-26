/*******************************************************************************
 * Copyright (c) 2012 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.util.common;

import static org.easymock.EasyMock.createMock;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

public class IteratorToEnumerationAdapterTests {

    private static final String TEST_STRING = "TEST";
    private Iterator<String> mockIterator;
    private IteratorToEnumerationAdapter<String> adapter;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws Exception {
        this.mockIterator = createMock(Iterator.class);
        this.adapter = new IteratorToEnumerationAdapter<String>(this.mockIterator);
    }

    @Test
    public void testHasMoreElements() {
        EasyMock.expect(this.mockIterator.hasNext()).andReturn(true).once();
        EasyMock.replay(this.mockIterator);
        assertTrue(this.adapter.hasMoreElements());
        EasyMock.verify(this.mockIterator);
    }

    @Test
    public void testNextElement() {
        EasyMock.expect(this.mockIterator.next()).andReturn(TEST_STRING).once();
        EasyMock.replay(this.mockIterator);
        assertEquals(TEST_STRING, this.adapter.nextElement());
        EasyMock.verify(this.mockIterator);
    }

}
