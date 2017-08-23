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

package org.eclipse.virgo.kernel.model.management.internal;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.eclipse.virgo.kernel.model.Artifact;
import org.eclipse.virgo.kernel.model.ArtifactState;
import org.eclipse.virgo.kernel.model.StubCompositeArtifact;
import org.eclipse.virgo.kernel.model.management.RuntimeArtifactModelObjectNameCreator;
import org.eclipse.virgo.kernel.model.management.internal.DelegatingManageableArtifact;
import org.eclipse.virgo.nano.serviceability.Assert.FatalAssertionException;
import org.eclipse.virgo.test.stubs.region.StubRegion;
import org.junit.Test;
import org.osgi.framework.Version;


public class DelegatingManageableArtifactTests {

    @Test(expected = FatalAssertionException.class)
    public void nullCreator() {
        new DelegatingManageableArtifact(null, new StubCompositeArtifact());
    }

    @Test(expected = FatalAssertionException.class)
    public void nullArtifact() {
        new DelegatingManageableArtifact(createMock(RuntimeArtifactModelObjectNameCreator.class), null);
    }

    @Test
    public void success() throws MalformedObjectNameException, NullPointerException {
        RuntimeArtifactModelObjectNameCreator creator = createMock(RuntimeArtifactModelObjectNameCreator.class);
        Artifact artifact = createMock(Artifact.class);

        DelegatingManageableArtifact manageableArtifact = new DelegatingManageableArtifact(creator, artifact);

        expect(artifact.getDependents()).andReturn(getArtifacts());
        expect(creator.createArtifactModel(isA(Artifact.class))).andReturn(new ObjectName("domain:key=value1,region=global"));
        expect(creator.createArtifactModel(isA(Artifact.class))).andReturn(new ObjectName("domain:key=value2,region=global"));
        expect(creator.createArtifactModel(isA(Artifact.class))).andReturn(new ObjectName("domain:key=value3,region=global"));
        expect(creator.createArtifactModel(isA(Artifact.class))).andReturn(new ObjectName("domain:key=value3,region=other"));
        expect(artifact.getName()).andReturn("test-name");
        expect(artifact.getState()).andReturn(ArtifactState.ACTIVE);
        expect(artifact.getRegion()).andReturn(new StubRegion("test-region", null));
        expect(artifact.getType()).andReturn("test-type");
        expect(artifact.getVersion()).andReturn(Version.emptyVersion);
        expect(artifact.getProperties()).andReturn(Collections.<String, String> emptyMap());
        expect(artifact.refresh()).andReturn(true);
        artifact.start();
        artifact.stop();
        artifact.uninstall();
        replay(creator, artifact);

        assertEquals(4, manageableArtifact.getDependents().length);
        manageableArtifact.getName();
        assertEquals("ACTIVE", manageableArtifact.getState());
        assertEquals("test-region", manageableArtifact.getRegion());
        assertEquals("test-type", manageableArtifact.getType());
        assertEquals("0.0.0", manageableArtifact.getVersion());
        assertEquals(0, manageableArtifact.getProperties().size());
        assertTrue(manageableArtifact.refresh());
        manageableArtifact.start();
        manageableArtifact.stop();
        manageableArtifact.uninstall();

        verify(creator, artifact);
    }

    private Set<Artifact> getArtifacts() {
        Set<Artifact> artifacts = new HashSet<Artifact>();
        for (int i = 0; i < 3; i++) {
            artifacts.add(new StubCompositeArtifact());
        }
        artifacts.add(new StubCompositeArtifact("foo", "bar", new StubRegion("global", null)));
        return artifacts;
    }
}
