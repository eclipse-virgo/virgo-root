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

package org.eclipse.virgo.kernel.shell.model.helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.virgo.kernel.shell.model.helper.ArtifactAccessorPointer;
import org.eclipse.virgo.kernel.shell.model.helper.StandardArtifactAccessor;
import org.junit.Before;
import org.junit.Test;


/**
 * Tests for {@link StandardArtifactAccessorTests}
 *
 */
public class StandardArtifactAccessorTests {

    private static final String TYPE_ATTRIBUTE = "Type";
    
    private static final String NAME_ATTRIBUTE = "Name";
    
    private static final String VERSION_ATTRIBUTE = "Version";
    
    private static final String STATE_ATTRIBUTE = "State";
    
    private StandardArtifactAccessor standardArtifactAccessor;
    
    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put(TYPE_ATTRIBUTE, TYPE_ATTRIBUTE);
        attributes.put(NAME_ATTRIBUTE, NAME_ATTRIBUTE);
        attributes.put(VERSION_ATTRIBUTE, VERSION_ATTRIBUTE);
        attributes.put(STATE_ATTRIBUTE, STATE_ATTRIBUTE);
        attributes.put("extra", "extra");
        this.standardArtifactAccessor = new StandardArtifactAccessor(attributes, new HashMap<String, String>(), new HashSet<ArtifactAccessorPointer>());
    }

    /**
     * Test method for {@link StandardArtifactAccessor#getType()}.
     */
    @Test
    public void testGetType() {
        assertEquals(TYPE_ATTRIBUTE, this.standardArtifactAccessor.getType());
    }

    /**
     * Test method for {@link StandardArtifactAccessor#getName()}.
     */
    @Test
    public void testGetName() {
        assertEquals(NAME_ATTRIBUTE, this.standardArtifactAccessor.getName());
    }

    /**
     * Test method for {@link StandardArtifactAccessor#getVersion()}.
     */
    @Test
    public void testGetVersion() {
        assertEquals(VERSION_ATTRIBUTE, this.standardArtifactAccessor.getVersion());
    }

    /**
     * Test method for {@link StandardArtifactAccessor#getAttributes()}.
     */
    @Test
    public void testGetAditionalAttributes() {
        assertNotNull(this.standardArtifactAccessor.getAttributes());
        assertEquals(2, this.standardArtifactAccessor.getAttributes().size());
        assertTrue(this.standardArtifactAccessor.getAttributes().containsKey("extra"));
        assertTrue(this.standardArtifactAccessor.getAttributes().containsKey("State"));
    }

    /**
     * Test method for {@link StandardArtifactAccessor#getProperties()}.
     */
    @Test
    public void testGetProperties() {
        assertNotNull(this.standardArtifactAccessor.getProperties());
    }

    /**
     * Test method for {@link StandardArtifactAccessor#getDependents()}.
     */
    @Test
    public void testGetDependents() {
        assertNotNull(this.standardArtifactAccessor.getDependents());
    }

}
