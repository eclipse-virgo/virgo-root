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

package org.eclipse.virgo.kernel.artifact.library;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;

import org.junit.Test;


import org.eclipse.virgo.kernel.artifact.StubHashGenerator;
import org.eclipse.virgo.kernel.artifact.library.LibraryBridge;
import org.eclipse.virgo.repository.ArtifactDescriptor;
import org.eclipse.virgo.repository.ArtifactGenerationException;

/**
 */
public class LibraryBridgeTests {

    @Test
    public void testValidLibraryFile() throws ArtifactGenerationException {
        LibraryBridge bridge = new LibraryBridge(new StubHashGenerator());

        ArtifactDescriptor descriptor = bridge.generateArtifactDescriptor(new File("src/test/resources/libraries/test.libd"));
        assertNotNull(descriptor);

        assertEquals("test.library", descriptor.getName());
        assertEquals("1.2.0", descriptor.getAttribute("Library-Version").iterator().next().getValue());
        assertEquals(3, descriptor.getAttribute("Import-Bundle").size());
    }

    @Test
    public void testNotALibraryFile() throws ArtifactGenerationException {
        LibraryBridge bridge = new LibraryBridge(new StubHashGenerator());

        ArtifactDescriptor descriptor = bridge.generateArtifactDescriptor(new File("src/test/resources/libraries/test.foo"));
        assertNull(descriptor);
    }

    @Test(expected = ArtifactGenerationException.class)
    public void testInvalidLibraryFile() throws ArtifactGenerationException {
        LibraryBridge bridge = new LibraryBridge(new StubHashGenerator());
        bridge.generateArtifactDescriptor(new File("src/test/resources/libraries/invalid.libd"));
    }
}
