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

package org.eclipse.virgo.kernel.install.artifact.internal.bundle;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.isNull;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;

import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import org.eclipse.virgo.kernel.osgi.quasi.QuasiBundle;

import org.eclipse.virgo.kernel.artifact.fs.ArtifactFS;
import org.eclipse.virgo.kernel.artifact.fs.internal.DirectoryArtifactFS;
import org.eclipse.virgo.kernel.deployer.core.DeploymentException;
import org.eclipse.virgo.kernel.install.artifact.ArtifactIdentity;
import org.eclipse.virgo.kernel.install.artifact.ArtifactIdentityDeterminer;
import org.eclipse.virgo.kernel.install.artifact.ArtifactStorage;
import org.eclipse.virgo.kernel.install.artifact.BundleInstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.internal.ArtifactStateMonitor;
import org.eclipse.virgo.kernel.install.artifact.internal.StandardArtifactStateMonitor;
import org.eclipse.virgo.kernel.install.artifact.internal.StubInstallArtifactRefreshHandler;
import org.eclipse.virgo.kernel.install.artifact.internal.bundle.BundleDriver;
import org.eclipse.virgo.kernel.install.artifact.internal.bundle.StandardBundleInstallArtifact;
import org.eclipse.virgo.medic.test.eventlog.MockEventLogger;
import org.eclipse.virgo.teststubs.osgi.framework.StubBundle;
import org.eclipse.virgo.teststubs.osgi.framework.StubBundleContext;
import org.eclipse.virgo.util.common.ThreadSafeArrayListTree;
import org.eclipse.virgo.util.common.Tree;
import org.eclipse.virgo.util.io.IOUtils;
import org.eclipse.virgo.util.osgi.manifest.BundleManifest;
import org.eclipse.virgo.util.osgi.manifest.BundleManifestFactory;

/**
 */
public class StandardBundleInstallArtifactTests {

    private final BundleDriver bundleDriver = createMock(BundleDriver.class);
    
    private final ArtifactIdentityDeterminer identityDeterminer = createMock(ArtifactIdentityDeterminer.class);

    private final StubInstallArtifactRefreshHandler refreshHandler = new StubInstallArtifactRefreshHandler();

    private final File bundleFile = new File("src/test/resources/org/eclipse/virgo/kernel/install/artifact/internal/sbiat-bundle");

    private final BundleContext bundleContext = new StubBundleContext();

    private final ArtifactStateMonitor artifactStateMonitor = new StandardArtifactStateMonitor(this.bundleContext);

    private BundleInstallArtifact createInstallArtifact(ArtifactStorage artifactStorage, ArtifactStateMonitor artifactStateMonitor)
        throws IOException {
        FileReader reader = null;
        try {
            reader = new FileReader(new File(bundleFile, "META-INF/MANIFEST.MF"));
            BundleManifest bundleManifest = BundleManifestFactory.createBundleManifest(reader);
            return new StandardBundleInstallArtifact(new ArtifactIdentity(ArtifactIdentityDeterminer.BUNDLE_TYPE,
                bundleManifest.getBundleSymbolicName().getSymbolicName(), bundleManifest.getBundleVersion(), null), bundleManifest, artifactStorage,
                bundleDriver, artifactStateMonitor, this.refreshHandler, null, new MockEventLogger(), identityDeterminer);
        } finally {
            IOUtils.closeQuietly(reader);
        }
    }

    protected ArtifactStorage createArtifactStorage() {
        return new ArtifactStorage() {

            private final ArtifactFS artifactFS = new DirectoryArtifactFS(bundleFile);

            public void synchronize(URI sourceUri) {
            }

            public void synchronize() {
            }

            public ArtifactFS getArtifactFS() {
                return this.artifactFS;
            }

            public void delete() {
            }

            public void rollBack() {
            }
        };
    }

    @Test
    public void updateAndRefreshWhenNotPartOfAPlan() throws IOException, DeploymentException {
        
        StandardBundleInstallArtifact artifact = (StandardBundleInstallArtifact) createInstallArtifact(createArtifactStorage(), artifactStateMonitor);
        
        ArtifactIdentity identity = new ArtifactIdentity(artifact.getType(), artifact.getName(), artifact.getVersion(), artifact.getScopeName());
        expect(this.identityDeterminer.determineIdentity(isA(File.class), (String)isNull())).andReturn(identity);
        replay(this.identityDeterminer);

        QuasiBundle quasiBundle = createMock(QuasiBundle.class);
        StubBundle bundle = new StubBundle();
        bundle.setState(Bundle.INSTALLED);
        expect(quasiBundle.getBundle()).andReturn(bundle);
        replay(quasiBundle);

        artifact.setQuasiBundle(quasiBundle);

        Tree<InstallArtifact> tree = new ThreadSafeArrayListTree<InstallArtifact>(artifact);
        artifact.setTree(tree);

        expect(this.bundleDriver.update(isA(BundleManifest.class))).andReturn(true);
        this.bundleDriver.refreshBundle();
        replay(this.bundleDriver);

        this.refreshHandler.setRefreshOutcome(artifact, true);
        assertTrue(artifact.refresh());
        this.refreshHandler.assertRefreshed(artifact);
    }

    @Test
    public void testDeploymentProperties() throws Exception {
        BundleInstallArtifact installArtifact = createInstallArtifact(createArtifactStorage(), artifactStateMonitor);

        installArtifact.getDeploymentProperties().put("foo", "bar");
        assertEquals("bar", installArtifact.getDeploymentProperties().get("foo"));
    }
}
