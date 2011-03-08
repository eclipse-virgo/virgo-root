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

package org.eclipse.virgo.kernel.model.internal.bundle;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.Set;

import org.eclipse.virgo.kernel.model.Artifact;
import org.eclipse.virgo.kernel.model.RuntimeArtifactRepository;
import org.eclipse.virgo.kernel.model.StubArtifactRepository;
import org.eclipse.virgo.kernel.model.StubCompositeArtifact;
import org.eclipse.virgo.kernel.model.internal.bundle.BundleDependencyDeterminer;
import org.eclipse.virgo.kernel.serviceability.Assert.FatalAssertionException;
import org.junit.Test;

import org.eclipse.virgo.kernel.osgi.framework.PackageAdminUtil;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiBundle;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiFramework;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiFrameworkFactory;

public class BundleDependencyDeterminerTests {

    private final QuasiFrameworkFactory quasiFrameworkFactory = createMock(QuasiFrameworkFactory.class);

    private final RuntimeArtifactRepository artifactRepository = new StubArtifactRepository();
    
    private final PackageAdminUtil packageAdminUtil = createMock(PackageAdminUtil.class);

    private final BundleDependencyDeterminer determiner = new BundleDependencyDeterminer(quasiFrameworkFactory, artifactRepository, packageAdminUtil);

    @Test(expected = FatalAssertionException.class)
    public void nullFactory() {
        new BundleDependencyDeterminer(null, artifactRepository, packageAdminUtil);
    }

    @Test(expected = FatalAssertionException.class)
    public void nullRepository() {
        new BundleDependencyDeterminer(quasiFrameworkFactory, null, packageAdminUtil);
    }
    
    @Test(expected = FatalAssertionException.class)
    public void nullPackageAdminUtil() {
        new BundleDependencyDeterminer(quasiFrameworkFactory, artifactRepository, null);
    }

    @Test
    public void unknownBundle() {
        QuasiFramework framework = createMock(QuasiFramework.class);
        expect(quasiFrameworkFactory.create()).andReturn(framework);
        expect(framework.getBundles()).andReturn(Collections.<QuasiBundle> emptyList());
        replay(quasiFrameworkFactory, framework);

        Set<Artifact> dependents = this.determiner.getDependents(new StubCompositeArtifact());
        assertEquals(Collections.<Artifact> emptySet(), dependents);

        verify(quasiFrameworkFactory, framework);
    }

}
