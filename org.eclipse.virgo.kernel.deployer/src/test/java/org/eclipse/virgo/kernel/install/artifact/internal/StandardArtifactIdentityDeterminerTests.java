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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Version;

import org.eclipse.virgo.kernel.install.artifact.ArtifactIdentity;
import org.eclipse.virgo.kernel.install.artifact.ArtifactIdentityDeterminer;
import org.eclipse.virgo.kernel.install.artifact.internal.StandardArtifactIdentityDeterminer;
import org.eclipse.virgo.repository.ArtifactBridge;
import org.eclipse.virgo.repository.ArtifactDescriptor;
import org.eclipse.virgo.repository.ArtifactGenerationException;

/**
 */
public class StandardArtifactIdentityDeterminerTests {

    private ArtifactIdentityDeterminer artifactIdentityDeterminer;

    @Before
    public void setUp() {
        this.artifactIdentityDeterminer = new StandardArtifactIdentityDeterminer(Collections.<ArtifactBridge>emptySet());
    }

    @Test
    public void testJarFileType() {
        ArtifactIdentity identity = this.artifactIdentityDeterminer.determineIdentity(new File("test.jar"), null);
        assertEquals(ArtifactIdentityDeterminer.BUNDLE_TYPE, identity.getType());
        assertEquals("test", identity.getName());
        assertEquals(Version.emptyVersion, identity.getVersion());
    }

    @Test
    public void testPlanFileType() {
        ArtifactIdentity identity = this.artifactIdentityDeterminer.determineIdentity(new File("test.plan"), null);
        assertEquals(ArtifactIdentityDeterminer.PLAN_TYPE, identity.getType());
        assertEquals("test", identity.getName());
        assertEquals(Version.emptyVersion, identity.getVersion());
    }

    @Test
    public void testPropertiesFileType() {
        ArtifactIdentity identity = this.artifactIdentityDeterminer.determineIdentity(new File("test.properties"), null);
        assertEquals(ArtifactIdentityDeterminer.CONFIGURATION_TYPE, identity.getType());
        assertEquals("test", identity.getName());
        assertEquals(Version.emptyVersion, identity.getVersion());
    }

    @Test
    public void testParFileType() {
        ArtifactIdentity identity = this.artifactIdentityDeterminer.determineIdentity(new File("test.par"), null);
        assertEquals(ArtifactIdentityDeterminer.PAR_TYPE, identity.getType());
        assertEquals("test", identity.getName());
        assertEquals(Version.emptyVersion, identity.getVersion());
    }

    @Test
    public void testWarFileType() {
        assertNull(this.artifactIdentityDeterminer.determineIdentity(new File("test.war"), null));
    }

    @Test
    public void testNoFileType() {
        assertNull(this.artifactIdentityDeterminer.determineIdentity(new File("test"), null));
    }
    
    @Test
    public void identityFromBridge() throws ArtifactGenerationException {
        ArtifactDescriptor artifactDescriptor = createMock(ArtifactDescriptor.class);
        expect(artifactDescriptor.getType()).andReturn("foo");
        expect(artifactDescriptor.getName()).andReturn("bar");
        expect(artifactDescriptor.getVersion()).andReturn(new Version(1,2,3));
        
        ArtifactBridge bridge = createMock(ArtifactBridge.class);
        expect(bridge.generateArtifactDescriptor(new File("test"))).andReturn(artifactDescriptor);
        
        replay(artifactDescriptor, bridge);
        
        ArtifactIdentity artifactIdentity = new StandardArtifactIdentityDeterminer(new HashSet<ArtifactBridge>(Arrays.asList(bridge))).determineIdentity(new File("test"), null);
        assertEquals("foo", artifactIdentity.getType());
        assertEquals("bar", artifactIdentity.getName());
        assertEquals(new Version(1,2,3), artifactIdentity.getVersion());
        
        verify(artifactDescriptor, bridge);
    }
}
