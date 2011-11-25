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

package org.eclipse.virgo.kernel.stubs;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.Map;

import org.osgi.framework.Bundle;

import org.eclipse.virgo.kernel.install.artifact.ArtifactIdentityDeterminer;
import org.eclipse.virgo.kernel.install.artifact.BundleInstallArtifact;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiBundle;

import org.eclipse.virgo.kernel.artifact.fs.ArtifactFS;
import org.eclipse.virgo.util.osgi.manifest.BundleManifest;

/**
 * <code>StubBundleInstallArtifact</code> implements {@link BundleInstallArtifact} interface for testing.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * thread-safe
 * 
 */
public class StubBundleInstallArtifact extends StubGraphAssociableInstallArtifact implements BundleInstallArtifact {

    private final Bundle bundle;

    private final BundleManifest bundleManifest;

    private QuasiBundle quasiBundle;

    public StubBundleInstallArtifact(ArtifactFS artifactFS, Bundle bundle, BundleManifest bundleManifest) {
        super(artifactFS, ArtifactIdentityDeterminer.BUNDLE_TYPE, bundleManifest.getBundleSymbolicName().getSymbolicName(),
            bundleManifest.getBundleVersion());
        this.bundle = bundle;
        this.bundleManifest = bundleManifest;
    }

    /**
     * {@inheritDoc}
     */
    public Bundle getBundle() {
        return this.bundle;
    }

    /**
     * {@inheritDoc}
     */
    public BundleManifest getBundleManifest() throws IOException {
        return this.bundleManifest;
    }

    /**
     * {@inheritDoc}
     */
    public QuasiBundle getQuasiBundle() {
        return quasiBundle;
    }

    /**
     * {@inheritDoc}
     */
    public void setQuasiBundle(QuasiBundle quasiBundle) {
        this.quasiBundle = quasiBundle;
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, String> getDeploymentProperties() {
        return Collections.<String, String> emptyMap();
    }

    public void deleteEntry(String targetPath) {
    }

    public void updateEntry(URI inputPath, String targetPath) {
    }
}
