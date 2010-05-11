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

import java.util.List;

import org.eclipse.virgo.util.osgi.manifest.BundleManifest;

/**
 * {@link ImportExpander} is an interface for expanding library and bundle imports into package imports and propagating
 * promoted imports across a collection of bundles. <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations of this interface should be thread safe.
 * 
 */
public interface ImportExpander {

    public interface ImportPromotionVector {
    }

    /**
     * Modifies the supplied {@link BundleManifest bundle manifests} by replacing all of the <code>Import-Library</code>
     * and <code>Import-Bundle</code> header entries with the equivalent <code>Import-Package</code> header entries and
     * propagating promoted imports across the supplied bundle manifests.
     * 
     * @param bundleManifests the manifests to perform <code>Import-Library</code> and <code>Import-Bundle</code>
     *        expansion upon.
     * @return vector of imports promoted
     * @throws UnableToSatisfyDependenciesException if a manifest's dependencies cannot be satisfied
     * @throws ImportMergeException if there was a clash between some of the imports being merged
     */
    ImportPromotionVector expandImports(List<BundleManifest> bundleManifests) throws UnableToSatisfyDependenciesException, ImportMergeException;

}
