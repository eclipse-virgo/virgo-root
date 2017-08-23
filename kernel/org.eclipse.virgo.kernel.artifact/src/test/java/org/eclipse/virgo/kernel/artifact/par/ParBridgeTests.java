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

package org.eclipse.virgo.kernel.artifact.par;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;

import org.junit.Test;
import org.osgi.framework.Version;

import org.eclipse.virgo.kernel.artifact.StubHashGenerator;
import org.eclipse.virgo.kernel.artifact.par.ParBridge;
import org.eclipse.virgo.repository.ArtifactDescriptor;
import org.eclipse.virgo.repository.ArtifactGenerationException;

public class ParBridgeTests {

    private final ParBridge parBridge = new ParBridge(new StubHashGenerator());

    @Test
    public void descriptorGeneration() throws ArtifactGenerationException {
        ArtifactDescriptor descriptor = this.parBridge.generateArtifactDescriptor(new File("src/test/resources/pars/basic.par"));
        assertDescriptor(descriptor, "par", "basic", new Version(1,2,3), "Basic Par", "A basic PAR file for the unit tests");
    }

    @Test
    public void generationWithNoManifest() throws ArtifactGenerationException {
        ArtifactDescriptor descriptor = this.parBridge.generateArtifactDescriptor(new File("src/test/resources/pars/no-manifest.par"));
        assertDescriptor(descriptor, "par", "no-manifest", Version.emptyVersion, null, null);
    }

    @Test
    public void generationWithNoApplicationSymbolicName() throws ArtifactGenerationException {
        ArtifactDescriptor descriptor = this.parBridge.generateArtifactDescriptor(new File("src/test/resources/pars/no-asn.par"));
        assertNull(descriptor);
    }

    @Test
    public void generationWithNoApplicationVersion() throws ArtifactGenerationException {
        ArtifactDescriptor descriptor = this.parBridge.generateArtifactDescriptor(new File("src/test/resources/pars/no-version.par"));
        assertDescriptor(descriptor, "par", "basic", Version.emptyVersion, "Basic Par", "A basic PAR file for the unit tests");
    }

    @Test
    public void generationWithNoName() throws ArtifactGenerationException {
        ArtifactDescriptor descriptor = this.parBridge.generateArtifactDescriptor(new File("src/test/resources/pars/no-name.par"));
        assertDescriptor(descriptor, "par", "basic", new Version(1,2,3), null, "A basic PAR file for the unit tests");
    }

    @Test
    public void generationWithNoDescription() throws ArtifactGenerationException {
        ArtifactDescriptor descriptor = this.parBridge.generateArtifactDescriptor(new File("src/test/resources/pars/no-description.par"));
        assertDescriptor(descriptor, "par", "basic", new Version(1,2,3), "Basic Par", null);
    }

    @Test
    public void generationWithIllegalApplicationSymbolicName() throws Exception {
        try {
            this.parBridge.generateArtifactDescriptor(new File("src/test/resources/pars/illegal-asn.par"));
            fail("Illegal Application-SymbolicName did not throw an ArtifactGenerationException");
        } catch (ArtifactGenerationException age) {
            assertEquals("Application-SymbolicName '.@$%' contains illegal characters", age.getMessage());
        }
    }

    @Test
    public void generationWithIllegalApplicationVersion() throws Exception {
        try {
            this.parBridge.generateArtifactDescriptor(new File("src/test/resources/pars/illegal-version.par"));
            fail("Illegal Application-Version did not throw an IllegalArgumentException");
        } catch (IllegalArgumentException iae) {
            assertEquals("Version 'alpha' is ill-formed", iae.getMessage());
        }
    }

    @Test(expected=ArtifactGenerationException.class)
    public void generationWithMissingPar() throws ArtifactGenerationException {
        this.parBridge.generateArtifactDescriptor(new File("src/test/resources/pars/not-there.par"));
    }

    private void assertDescriptor(ArtifactDescriptor descriptor, String type, String symbolicName, Version version, String name, String description) {
        assertNotNull(descriptor);
        assertEquals(type, descriptor.getType());
        assertEquals(symbolicName, descriptor.getName());
        assertEquals(version, descriptor.getVersion());
        if (name != null) {
            assertEquals(name, descriptor.getAttribute("Application-Name").iterator().next().getValue());
        } else {
            assertTrue(descriptor.getAttribute("Application-Name").isEmpty());
        }

        if (description != null) {
            assertEquals(description, descriptor.getAttribute("Application-Description").iterator().next().getValue());
        } else {
            assertTrue(descriptor.getAttribute("Application-Description").isEmpty());
        }
    }
}
