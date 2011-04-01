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

import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Version;


import org.eclipse.virgo.kernel.model.Artifact;
import org.eclipse.virgo.kernel.model.ArtifactState;
import org.eclipse.virgo.kernel.model.StubCompositeArtifact;
import org.eclipse.virgo.kernel.model.internal.AbstractArtifact;
import org.eclipse.virgo.kernel.model.internal.DependencyDeterminer;
import org.eclipse.virgo.kernel.serviceability.Assert.FatalAssertionException;
import org.eclipse.virgo.teststubs.osgi.framework.StubBundleContext;
import org.eclipse.virgo.teststubs.osgi.support.TrueFilter;

public class AbztractArtifactTests {

    private final StubBundleContext bundleContext;

    private final AbstractArtifact artifact;

    {
        bundleContext = new StubBundleContext();
        String filterString = String.format("(&(objectClass=%s)(artifactType=test-type))", DependencyDeterminer.class.getCanonicalName());
        bundleContext.addFilter(filterString, new TrueFilter(filterString));
        artifact = new StubArtifact(bundleContext, "test-type", "test-name", Version.emptyVersion);
    }

    @Test(expected = FatalAssertionException.class)
    public void nullBundleContext() {
        new StubArtifact(null, "type", "name", Version.emptyVersion);
    }

    @Test(expected = FatalAssertionException.class)
    public void nullType() {
        new StubArtifact(new StubBundleContext(), null, "name", Version.emptyVersion);
    }

    @Test(expected = FatalAssertionException.class)
    public void nullName() {
        new StubArtifact(new StubBundleContext(), "type", null, Version.emptyVersion);
    }

    @Test(expected = FatalAssertionException.class)
    public void nullVersion() {
        new StubArtifact(new StubBundleContext(), "type", "name", null);
    }

    @Test(expected = RuntimeException.class)
    public void badFilter() {
        new StubArtifact(new StubBundleContext(), "type", "name", Version.emptyVersion);
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

    @SuppressWarnings("unchecked")
    @Test
    public void getDependents() {
        assertEquals(0, artifact.getDependents().size());

        DependencyDeterminer determiner = createMock(DependencyDeterminer.class);
        Dictionary properties = new Hashtable();
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
            super(bundleContext, "test-type", "test-name", Version.emptyVersion, null);
        }

        public StubArtifact(BundleContext bundleContext, String type, String name, Version version) {
            super(bundleContext, type, name, version, null);
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
