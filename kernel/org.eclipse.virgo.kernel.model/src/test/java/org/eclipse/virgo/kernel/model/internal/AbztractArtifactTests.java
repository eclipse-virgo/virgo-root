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

package org.eclipse.virgo.kernel.model.internal;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import org.eclipse.equinox.region.Region;
import org.eclipse.virgo.kernel.model.Artifact;
import org.eclipse.virgo.kernel.model.ArtifactState;
import org.eclipse.virgo.kernel.model.StubCompositeArtifact;
import org.eclipse.virgo.nano.serviceability.Assert.FatalAssertionException;
import org.eclipse.virgo.test.stubs.framework.StubBundleContext;
import org.eclipse.virgo.test.stubs.region.StubRegion;
import org.eclipse.virgo.test.stubs.support.TrueFilter;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Version;

public class AbztractArtifactTests {

    private final StubBundleContext bundleContext;

    private final AbstractArtifact artifact;

    private final StubRegion region = new StubRegion("test-region", null);

    {
        bundleContext = new StubBundleContext();
        String filterString = String.format("(&(objectClass=%s)(artifactType=test-type))", DependencyDeterminer.class.getCanonicalName());
        bundleContext.addFilter(filterString, new TrueFilter(filterString));
        artifact = new StubArtifact(bundleContext, "test-type", "test-name", Version.emptyVersion, region);
    }

    @Test(expected = FatalAssertionException.class)
    public void nullBundleContext() {
        new StubArtifact(null, "type", "name", Version.emptyVersion, region);
    }

    @Test(expected = FatalAssertionException.class)
    public void nullType() {
        new StubArtifact(new StubBundleContext(), null, "name", Version.emptyVersion, region);
    }

    @Test(expected = FatalAssertionException.class)
    public void nullName() {
        new StubArtifact(new StubBundleContext(), "type", null, Version.emptyVersion, region);
    }

    @Test(expected = FatalAssertionException.class)
    public void nullVersion() {
        new StubArtifact(new StubBundleContext(), "type", "name", null, region);
    }

    @Test(expected = FatalAssertionException.class)
    public void nullRegion() {
        new StubArtifact(new StubBundleContext(), "type", "name", Version.emptyVersion, null);
    }

    @Test(expected = RuntimeException.class)
    public void badFilter() {
        new StubArtifact(new StubBundleContext(), "type", "name", Version.emptyVersion, region);
    }

    @Test
    public void getType() {
        assertEquals("test-type", artifact.getType());
    }

    @Test
    public void getName() {
        assertEquals("test-name", artifact.getName());
    }

    @Test
    public void getVersion() {
        assertEquals(Version.emptyVersion, artifact.getVersion());
    }

    @Test
    public void getRegion() {
        assertEquals("test-region", artifact.getRegion().getName());
    }

    @Test
    public void getDependents() {
        assertEquals(0, artifact.getDependents().size());

        DependencyDeterminer determiner = createMock(DependencyDeterminer.class);
        Dictionary<String, String> properties = new Hashtable<String, String>();
        properties.put("artifactType", "test-type");
        bundleContext.registerService(DependencyDeterminer.class.getCanonicalName(), determiner, properties);

        expect(determiner.getDependents(artifact)).andReturn(getArtifacts());
        replay(determiner);
        assertEquals(3, artifact.getDependents().size());
        verify(determiner);
    }
    
    @Test
    public void getProperties() {
        assertEquals(0, artifact.getProperties().size());
    }

    private Set<Artifact> getArtifacts() {
        Set<Artifact> artifacts = new HashSet<Artifact>();
        for (int i = 0; i < 3; i++) {
            artifacts.add(new StubCompositeArtifact());
        }
        return artifacts;
    }

    private static class StubArtifact extends AbstractArtifact {

        public StubArtifact(BundleContext bundleContext) {
            super(bundleContext, "test-type", "test-name", Version.emptyVersion, new StubRegion("test-region", null));
        }

        public StubArtifact(BundleContext bundleContext, String type, String name, Version version, Region region) {
            super(bundleContext, type, name, version, region);
        }

        public ArtifactState getState() {
            throw new UnsupportedOperationException();
        }

        public boolean refresh() {
            throw new UnsupportedOperationException();
        }

        public void start() {
            throw new UnsupportedOperationException();
        }

        public void stop() {
            throw new UnsupportedOperationException();
        }

        public void uninstall() {
            throw new UnsupportedOperationException();
        }
    }
}
