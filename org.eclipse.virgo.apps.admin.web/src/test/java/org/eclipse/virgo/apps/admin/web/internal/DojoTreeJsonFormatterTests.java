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

package org.eclipse.virgo.apps.admin.web.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.eclipse.virgo.apps.admin.web.internal.DojoTreeFormatter;
import org.eclipse.virgo.apps.admin.web.internal.DojoTreeJsonFormatter;
import org.eclipse.virgo.apps.admin.web.stubs.StubArtifactAccessorAndPointer;
import org.eclipse.virgo.kernel.shell.model.helper.ArtifactAccessor;
import org.eclipse.virgo.kernel.shell.model.helper.ArtifactAccessorPointer;


/**
 */
public class DojoTreeJsonFormatterTests {

    private static final String TEST_PARENT = "aParent";

    public static final String TEST_TYPE = "bundle"; //To get a link to be generated
    
    public static final String TEST_NAME = "aName";
    
    public static final String TEST_VERSION = "aVersion";
    
    public static final String TEST_STATE = "aState";

    private List<String> testTypes = new ArrayList<String>();
    
    private List<ArtifactAccessorPointer> testAccessorPointers;
    
    private ArtifactAccessor testArtifactAccessor;
    
    private DojoTreeFormatter dojoTreeJsonFormatter;

    @Before
    public void setUp() {
        this.testTypes.add(TEST_TYPE);
        this.testAccessorPointers = new ArrayList<ArtifactAccessorPointer>();
        this.testAccessorPointers.add(new StubArtifactAccessorAndPointer(TEST_TYPE, TEST_NAME, TEST_VERSION, TEST_STATE));
        this.testArtifactAccessor = new StubArtifactAccessorAndPointer(TEST_TYPE, TEST_NAME, TEST_VERSION, TEST_STATE);
        dojoTreeJsonFormatter = new DojoTreeJsonFormatter();
    }

    @Test
    public void testGetTypes() {
        String types = this.dojoTreeJsonFormatter.formatTypes(this.testTypes);
        assertNotNull(types);
        assertTrue(types.contains(TEST_TYPE));
    }

    @Test
    public void testGetTypesEmpty() {
        String types = this.dojoTreeJsonFormatter.formatTypes(new ArrayList<String>());
        assertNotNull(types);
        assertEquals(0, types.length());
    }

    @Test
    public void testGetArtifactsOfTypeNotExist() {
        String artifactsOfType = this.dojoTreeJsonFormatter.formatArtifactsOfType("foo", new ArrayList<ArtifactAccessorPointer>());
        assertNotNull(artifactsOfType);
        assertEquals(0, artifactsOfType.length());
    }

    @Test
    public void testGetArtifactsOfTypeNullParent() {
        String artifactsOfType = this.dojoTreeJsonFormatter.formatArtifactsOfType(null, new ArrayList<ArtifactAccessorPointer>());
        assertNotNull(artifactsOfType);
        assertEquals(0, artifactsOfType.length());
    }

    @Test
    public void testGetArtifactsOfTypeNullPointers() {
        String artifactsOfType = this.dojoTreeJsonFormatter.formatArtifactsOfType("foo", null);
        assertNotNull(artifactsOfType);
        assertEquals(0, artifactsOfType.length());
    }
    
    @Test
    public void testGetArtifactsOfType() {
        String artifactsOfType = this.dojoTreeJsonFormatter.formatArtifactsOfType(TEST_PARENT, this.testAccessorPointers);
        assertNotNull(artifactsOfType);
        assertTrue(artifactsOfType.contains(TEST_TYPE));
        assertTrue(artifactsOfType.contains(TEST_NAME));
        assertTrue(artifactsOfType.contains(TEST_VERSION));
        assertTrue(artifactsOfType.contains("children"));
        assertFalse(artifactsOfType.contains("link"));
        assertFalse(artifactsOfType.contains("icon"));
        assertTrue(artifactsOfType.contains("state"));
    }

    @Test
    public void testGetArtifactDetailsNotExistNullParent() {
        String artifactDetails = this.dojoTreeJsonFormatter.formatArtifactDetails(null, new StubArtifactAccessorAndPointer("bar", "foo", "bar", "foo"));
        assertNotNull(artifactDetails);
        assertEquals(0, artifactDetails.length());
    }

    @Test
    public void testGetArtifactDetailsNotExistNullArtifact() {
        String artifactDetails = this.dojoTreeJsonFormatter.formatArtifactDetails("foo", null);
        assertNotNull(artifactDetails);
        assertEquals(0, artifactDetails.length());
    }

    @Test
    public void testGetArtifactDetails() {
        String artifactDetails = this.dojoTreeJsonFormatter.formatArtifactDetails(TEST_PARENT, this.testArtifactAccessor);
        assertNotNull(artifactDetails);
        assertTrue(artifactDetails.contains(TEST_TYPE));
        assertTrue(artifactDetails.contains(TEST_NAME));
        assertTrue(artifactDetails.contains(TEST_VERSION));
        assertTrue(artifactDetails.contains("link"));
        assertTrue(artifactDetails.contains("state"));
        assertTrue(artifactDetails.contains("icon"));
    }

}
