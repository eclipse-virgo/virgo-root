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

import org.eclipse.virgo.kernel.osgi.framework.ManifestTransformer;
import org.eclipse.virgo.util.osgi.manifest.BundleManifest;

/**
 * {@link BundleDriverManifestTransformer} is a {@link ManifesTransformer} that transforms a bundle manifest by replacing it with an in-memory version.
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * This class is thread safe.
 *
 */
final class BundleDriverManifestTransformer implements ManifestTransformer {

    private final BundleManifest bundleManifest;

    public BundleDriverManifestTransformer(BundleManifest bundleManifest) {
        this.bundleManifest = bundleManifest;
    }

    /**
     * {@inheritDoc}
     */
    public BundleManifest transform(BundleManifest bundleManifest) {
        return this.bundleManifest;
    }
}
