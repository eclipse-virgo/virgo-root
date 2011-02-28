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

package org.eclipse.virgo.kernel.install.artifact.internal;

import org.eclipse.virgo.kernel.install.artifact.ArtifactIdentity;
import org.eclipse.virgo.repository.ArtifactBridge;
import org.eclipse.virgo.repository.ArtifactDescriptor;
import org.eclipse.virgo.repository.ArtifactGenerationException;
import org.junit.Test;
import org.osgi.framework.Version;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

public class StandardArtifactIdentityDeterminerTests {

    private static final File TEST_FILE = new File("test");

    @Test
    public void identityWithoutBridge() {
        ArtifactIdentity identity = new StandardArtifactIdentityDeterminer(Collections.<ArtifactBridge>emptySet()).determineIdentity(TEST_FILE, null);
        assertNull(identity);
    }

    @Test
    public void identityFromBridge() throws ArtifactGenerationException {
        ArtifactDescriptor artifactDescriptor = createArtifactDescriptorMock();
        ArtifactBridge bridge = createArtifactBridgeMock(artifactDescriptor);

        checkIdentityDeterminer(bridge);

        verify(artifactDescriptor, bridge);
    }

    @Test
    public void identityFromBridgeThrowingException() throws ArtifactGenerationException {
        ArtifactDescriptor artifactDescriptor = createArtifactDescriptorMock();
        ArtifactBridge interestedBridge = createArtifactBridgeMock(artifactDescriptor);

        ArtifactBridge throwingBridge = createMock(ArtifactBridge.class);
        expect(throwingBridge.generateArtifactDescriptor(new File("test"))).andThrow(new ArtifactGenerationException("Illegal argument"));

        replay(throwingBridge);

        assertNull(new StandardArtifactIdentityDeterminer(new LinkedHashSet<ArtifactBridge>(Arrays.asList(throwingBridge, interestedBridge))).determineIdentity(new File("test"), null));
    }

    @Test
    public void identityFromSeveralBridges() throws ArtifactGenerationException {
        ArtifactDescriptor artifactDescriptor = createArtifactDescriptorMock();
        ArtifactBridge interestedBridge = createArtifactBridgeMock(artifactDescriptor);

        ArtifactBridge throwingBridge = createMock(ArtifactBridge.class);

        ArtifactBridge uninterestedBridge = createMock(ArtifactBridge.class);
        expect(uninterestedBridge.generateArtifactDescriptor(TEST_FILE)).andReturn(null);

        replay(throwingBridge, uninterestedBridge);

        checkIdentityDeterminer(uninterestedBridge,  interestedBridge, throwingBridge);

        verify(artifactDescriptor, throwingBridge, interestedBridge, uninterestedBridge);
    }

    private ArtifactDescriptor createArtifactDescriptorMock() throws ArtifactGenerationException {
        ArtifactDescriptor artifactDescriptor = createMock(ArtifactDescriptor.class);
        expect(artifactDescriptor.getType()).andReturn("foo");
        expect(artifactDescriptor.getName()).andReturn("bar");
        expect(artifactDescriptor.getVersion()).andReturn(new Version(1, 2, 3));

        replay(artifactDescriptor);

        return artifactDescriptor;
    }

    private ArtifactBridge createArtifactBridgeMock(ArtifactDescriptor descriptor) throws ArtifactGenerationException {
        ArtifactBridge bridge = createMock(ArtifactBridge.class);
        expect(bridge.generateArtifactDescriptor(new File("test"))).andReturn(descriptor);

        replay(bridge);

        return bridge;
    }

    private void checkIdentityDeterminer(ArtifactBridge... bridges) {
        ArtifactIdentity artifactIdentity = new StandardArtifactIdentityDeterminer(new LinkedHashSet<ArtifactBridge>(Arrays.asList(bridges))).determineIdentity(TEST_FILE, null);
        assertNotNull(artifactIdentity);
        assertEquals("foo", artifactIdentity.getType());
        assertEquals("bar", artifactIdentity.getName());
        assertEquals(new Version(1, 2, 3), artifactIdentity.getVersion());
    }

}
