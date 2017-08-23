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

package org.eclipse.virgo.repository.internal;

import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_ATTRIBUTE_NAME_ONE;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_ATTRIBUTE_VALUE_ONE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Set;

import org.eclipse.virgo.repository.internal.StandardAttribute;
import org.junit.Test;

/**
 * <p>
 * Unit tests for {@link org.eclipse.virgo.repository.internal.StandardAttribute}
 * </p>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe test case
 * 
 */
public class StandardAttributeTests {

    @Test(expected = IllegalArgumentException.class)
    public void testArtefactNullName() {
        new StandardAttribute(null, "foo", new HashMap<String, Set<String>>());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAttributeNullValue() {
        new StandardAttribute("foo", null, new HashMap<String, Set<String>>());
    }

    @Test
    public void testGetName() {
        assertEquals(TEST_ATTRIBUTE_NAME_ONE, TEST_ATTRIBUTE_NAME_ONE);
    }

    @Test
    public void testGetValue() {
        assertEquals(TEST_ATTRIBUTE_VALUE_ONE, TEST_ATTRIBUTE_VALUE_ONE);
    }

    @Test
    public void testToString() {
        assertTrue("Wrong toString returned for constructor args",
            new StandardAttribute(TEST_ATTRIBUTE_NAME_ONE, TEST_ATTRIBUTE_VALUE_ONE).toString().contains(TEST_ATTRIBUTE_NAME_ONE));
        assertTrue("Wrong toString returned for constructor args",
            new StandardAttribute(TEST_ATTRIBUTE_NAME_ONE, TEST_ATTRIBUTE_VALUE_ONE).toString().contains(TEST_ATTRIBUTE_VALUE_ONE));
    }

}
