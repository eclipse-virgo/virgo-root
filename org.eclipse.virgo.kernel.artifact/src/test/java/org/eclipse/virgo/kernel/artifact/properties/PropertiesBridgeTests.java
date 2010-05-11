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

package org.eclipse.virgo.kernel.artifact.properties;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;

import org.junit.Test;


import org.eclipse.virgo.kernel.artifact.StubHashGenerator;
import org.eclipse.virgo.kernel.artifact.properties.PropertiesBridge;
import org.eclipse.virgo.repository.ArtifactDescriptor;
import org.eclipse.virgo.repository.ArtifactGenerationException;

/**
 */
public class PropertiesBridgeTests {

    @Test
    public void testGeneratePropertiesFile() throws ArtifactGenerationException {
        PropertiesBridge bridge = new PropertiesBridge(new StubHashGenerator());
        ArtifactDescriptor result = bridge.generateArtifactDescriptor(new File("src/test/resources/properties/foo.properties"));
        assertNotNull(result);
    }

    @Test(expected = ArtifactGenerationException.class)
    public void testFileDoesNotExist() throws ArtifactGenerationException {
        PropertiesBridge bridge = new PropertiesBridge(new StubHashGenerator());

        File file = new File("src/test/resources/properties/not.exist.properties");
        bridge.generateArtifactDescriptor(file);
    }

    @Test
    public void testGenerateNotPropertiesFile() throws ArtifactGenerationException {
        PropertiesBridge bridge = new PropertiesBridge(new StubHashGenerator());
        ArtifactDescriptor descriptor = bridge.generateArtifactDescriptor(new File("src/test/resources/bar.noterties"));
        assertNull(descriptor);
    }
}
