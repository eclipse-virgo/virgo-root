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

package org.eclipse.virgo.web.core.internal;

import java.io.File;
import java.net.URI;

import org.osgi.framework.Version;

import org.eclipse.virgo.kernel.artifact.fs.ArtifactFS;
import org.eclipse.virgo.kernel.install.artifact.BundleInstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.stubs.StubArtifactFS;
import org.eclipse.virgo.kernel.stubs.StubBundleInstallArtifact;
import org.eclipse.virgo.kernel.stubs.StubInstallArtifact;
import org.eclipse.virgo.test.stubs.framework.StubBundle;
import org.eclipse.virgo.util.osgi.manifest.BundleManifest;

final class TestUtils {

    static InstallArtifact createInstallArtifact(String name, Version version, File location, URI sourceURI) {
        ArtifactFS artifactFS = new StubArtifactFS(sourceURI, location, name);
        return new StubInstallArtifact(artifactFS, name, version);
    }

    static BundleInstallArtifact createBundleInstallArtifact(URI sourceURI, File location, BundleManifest bundleManifest) {
        ArtifactFS artifactFS = new StubArtifactFS(sourceURI, location, bundleManifest.getBundleSymbolicName().getSymbolicName());
        return new StubBundleInstallArtifact(artifactFS, new StubBundle(), bundleManifest);
    }
}
