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

package org.eclipse.virgo.kernel.osgi.framework;

import org.osgi.framework.Bundle;

import org.eclipse.virgo.util.osgi.manifest.BundleManifest;

/**
 * A ManifestTransformer can be used to return a transformed {@link BundleManifest} for a {@link Bundle}. <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations <strong>must</strong> be thread-safe.
 * 
 */
public interface ManifestTransformer {

    /**
     * Transform the supplied manifest, e.g. by expanding any Import-Library and Import-Bundle headers.
     * 
     * @param bundleManifest the manifest to transform
     * @return The transformed manifest, or the original manifest if no transformation is required.
     */
    public BundleManifest transform(BundleManifest bundleManifest);
}
