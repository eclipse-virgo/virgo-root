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

package org.eclipse.virgo.kernel.deployer.core.internal;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.net.URL;

import org.junit.Test;
import org.osgi.framework.Version;

import org.eclipse.virgo.kernel.osgi.quasi.QuasiFramework;

import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.kernel.deployer.core.internal.ManifestUpgrader;
import org.eclipse.virgo.kernel.install.artifact.BundleInstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.install.environment.InstallEnvironment;
import org.eclipse.virgo.kernel.install.environment.InstallLog;
import org.eclipse.virgo.repository.Repository;
import org.eclipse.virgo.util.osgi.manifest.BundleManifest;
import org.eclipse.virgo.util.osgi.manifest.internal.StandardBundleManifest;

public class ManifestUpgraderTests {

    private static final String BUNDLE_SYMBOLIC_NAME = "testBundleName";

    private static final Version BUNDLE_VERSION = Version.emptyVersion;

    private final ManifestUpgrader upgrader = new ManifestUpgrader();

    private final BundleInstallArtifact installArtifact = createMock(BundleInstallArtifact.class);

    private final InstallEnvironment installEnvironment = new StubInstallEnvironment();

    @Test
    public void notBundleInstallArtifact() throws DeploymentException {
        this.upgrader.operate(null, null);

        InstallArtifact mock = createMock(InstallArtifact.class);
        replay(mock);
        this.upgrader.operate(mock, this.installEnvironment);
        verify(mock);
    }

    @Test(expected = DeploymentException.class)
    public void noAppManifest() throws DeploymentException, IOException {
        expect(this.installArtifact.getBundleManifest()).andThrow(new IOException());
        expect(this.installArtifact.getName()).andReturn(BUNDLE_SYMBOLIC_NAME);
        expect(this.installArtifact.getVersion()).andReturn(BUNDLE_VERSION);
        replay(this.installArtifact);
        this.upgrader.operate(this.installArtifact, this.installEnvironment);
    }

    @Test
    public void noUpgradeManifest() throws IOException, DeploymentException {
        BundleManifest bundleManifest = new StandardBundleManifest(null);
        bundleManifest.setBundleManifestVersion(2);
        bundleManifest.getBundleSymbolicName().setSymbolicName(BUNDLE_SYMBOLIC_NAME);
        expect(this.installArtifact.getBundleManifest()).andReturn(bundleManifest);
        replay(this.installArtifact);
        this.upgrader.operate(this.installArtifact, this.installEnvironment);
        verify(this.installArtifact);
    }

    @Test
    public void upgradeManifest() throws IOException, DeploymentException {
        BundleManifest bundleManifest = new StandardBundleManifest(null);
        bundleManifest.getBundleSymbolicName().setSymbolicName(BUNDLE_SYMBOLIC_NAME);
        expect(this.installArtifact.getBundleManifest()).andReturn(bundleManifest);
        replay(this.installArtifact);
        this.upgrader.operate(this.installArtifact, this.installEnvironment);
        verify(this.installArtifact);
        assertEquals(2, bundleManifest.getBundleManifestVersion());
        assertEquals(BUNDLE_SYMBOLIC_NAME, bundleManifest.getBundleSymbolicName().getSymbolicName());
    }

    @Test
    public void upgradeManifestNoSymbolicName() throws IOException, DeploymentException {
        BundleManifest bundleManifest = new StandardBundleManifest(null);
        expect(this.installArtifact.getBundleManifest()).andReturn(bundleManifest);
        expect(this.installArtifact.getName()).andReturn(BUNDLE_SYMBOLIC_NAME);
        replay(this.installArtifact);
        this.upgrader.operate(this.installArtifact, this.installEnvironment);
        verify(this.installArtifact);
        assertEquals(2, bundleManifest.getBundleManifestVersion());
        assertEquals(BUNDLE_SYMBOLIC_NAME, bundleManifest.getBundleSymbolicName().getSymbolicName());
    }

    @Test(expected = DeploymentException.class)
    public void invalidManifest() throws IOException, DeploymentException {
        BundleManifest bundleManifest = new StandardBundleManifest(null);
        bundleManifest.setBundleManifestVersion(2);
        expect(this.installArtifact.getBundleManifest()).andReturn(bundleManifest);
        expect(this.installArtifact.getName()).andReturn(BUNDLE_SYMBOLIC_NAME);
        expect(this.installArtifact.getVersion()).andReturn(BUNDLE_VERSION);
        replay(this.installArtifact);
        this.upgrader.operate(this.installArtifact, this.installEnvironment);
    }

    @Test
    public void validManifest() throws IOException, DeploymentException {
        BundleManifest bundleManifest = new StandardBundleManifest(null);
        bundleManifest.setBundleManifestVersion(2);
        bundleManifest.getBundleSymbolicName().setSymbolicName(BUNDLE_SYMBOLIC_NAME);
        expect(this.installArtifact.getBundleManifest()).andReturn(bundleManifest);
        replay(this.installArtifact);
        this.upgrader.operate(this.installArtifact, this.installEnvironment);
        verify(this.installArtifact);
    }

    @Test
    public void bundleUpdateLocation() throws IOException, DeploymentException {
        BundleManifest bundleManifest = new StandardBundleManifest(null);
        bundleManifest.setBundleUpdateLocation(new URL("file:///"));
        expect(this.installArtifact.getBundleManifest()).andReturn(bundleManifest);
        expect(this.installArtifact.getName()).andReturn(BUNDLE_SYMBOLIC_NAME).anyTimes();
        expect(this.installArtifact.getVersion()).andReturn(BUNDLE_VERSION).anyTimes();
        replay(this.installArtifact);
        this.upgrader.operate(this.installArtifact, this.installEnvironment);
        verify(this.installArtifact);
        assertNull(bundleManifest.getBundleUpdateLocation());
    }

    @Test
    public void noBundleUpdateLocation() throws IOException, DeploymentException {
        BundleManifest bundleManifest = new StandardBundleManifest(null);
        bundleManifest.setBundleManifestVersion(2);
        bundleManifest.getBundleSymbolicName().setSymbolicName(BUNDLE_SYMBOLIC_NAME);
        expect(this.installArtifact.getBundleManifest()).andReturn(bundleManifest);
        replay(this.installArtifact);
        this.upgrader.operate(this.installArtifact, this.installEnvironment);
        verify(this.installArtifact);
    }

    private static class StubInstallEnvironment implements InstallEnvironment {

        public InstallLog getInstallLog() {
            return createMock(InstallLog.class);
        }

        public QuasiFramework getQuasiFramework() {
            throw new UnsupportedOperationException();
        }

        public Repository getRepository() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void destroy() {
            throw new UnsupportedOperationException();
        }

    }
}
