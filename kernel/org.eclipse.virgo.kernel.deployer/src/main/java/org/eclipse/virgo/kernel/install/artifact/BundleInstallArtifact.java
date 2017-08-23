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

package org.eclipse.virgo.kernel.install.artifact;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import org.osgi.framework.Bundle;

import org.eclipse.virgo.kernel.osgi.quasi.QuasiBundle;
import org.eclipse.virgo.util.osgi.manifest.BundleManifest;

/**
 * {@link BundleInstallArtifact} is an {@link InstallArtifact} for a bundle.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is thread safe.
 * 
 */
public interface BundleInstallArtifact extends GraphAssociableInstallArtifact {

    /**
     * Returns the bundle manifest. This may differ from the bundle's original manifest file contents if transformations
     * have been performed in the installation pipeline. Note that such transformations are performed in memory and the
     * results are not written to a manifest file.
     * 
     * @return the {@link BundleManifest} abstract syntax tree
     * @throws IOException if an I/O error occurred while reading the bundle manifest
     */
    BundleManifest getBundleManifest() throws IOException;

    /**
     * Sets the {@link QuasiBundle} for this {@link BundleInstallArtifact}.
     * 
     * @param quasiBundle the <code>QuasiBundle</code>
     */
    void setQuasiBundle(QuasiBundle quasiBundle);

    /**
     * Gets the {@link QuasiBundle} for this {@link BundleInstallArtifact}.
     * 
     * @return the <code>QuasiBundle</code> or <code>null</code> if no <code>QuasiBundle</code> has been set
     */
    QuasiBundle getQuasiBundle();

    /**
     * Gets the OSGi {@link Bundle} for this {@link BundleInstallArtifact}.
     * 
     * @return the <code>Bundle</code> or <code>null</code> if this <code>BundleInstallArtifact</code> has not been
     *         installed in the OSGi framework
     */
    Bundle getBundle();

    /**
     * Returns the properties that are used to customize the deployment of the <code>BundleInstallArtifact</code>.
     * 
     * @return The deployment properties
     */
    Map<String, String> getDeploymentProperties();

    /**
     * Delete an entry within this bundle
     * 
     * @param targetPath The bundle relative path to delete
     */
    void deleteEntry(String targetPath);

    /**
     * Update an entry within this bundle. If the target path does not already exist, creates a new entry at that
     * location.
     * 
     * @param inputPath The path to read update from
     * @param targetPath The bundle relative path to write the update to
     */
    void updateEntry(URI inputPath, String targetPath);
}
