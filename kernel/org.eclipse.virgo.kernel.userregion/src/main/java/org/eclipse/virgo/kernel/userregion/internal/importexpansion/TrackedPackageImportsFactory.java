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

package org.eclipse.virgo.kernel.userregion.internal.importexpansion;

import java.util.List;

import org.eclipse.virgo.kernel.osgi.framework.ImportMergeException;
import org.eclipse.virgo.util.osgi.manifest.BundleManifest;
import org.eclipse.virgo.util.osgi.manifest.ImportedPackage;

/**
 * {@link TrackedPackageImportsFactory} is an interface for creating {@link TrackedPackageImports} instances.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations of this interface must be thread safe.
 * 
 * @see TrackedPackageImports
 */
public interface TrackedPackageImportsFactory {

    /**
     * Create a {@link TrackedPackageImports} from the given {@link BundleManifest}.
     * <p />
     * This is typically used to take the imports of a bundle and merge in imports from other sources.
     * 
     * @param bundleManifest the bundle manifest
     * @return a <code>TrackedPackageImports</code> instance
     */
    TrackedPackageImports create(BundleManifest bundleManifest);

    /**
     * Create a {@link TrackedPackageImports} from the given list of {@link ImportedPackage}s and given source.
     * <p />
     * This is typically used to represent a collection of imports which are to be added to one or more bundles.
     * 
     * @param importedPackages a list of <code>ImportedPackage</code>s which must not contain duplicate packages
     * @param source the source of the given package imports
     * @return a <code>TrackedPackageImports</code> instance
     */
    TrackedPackageImports create(List<ImportedPackage> importedPackages, String source);

    /**
     * Create a {@link TrackedPackageImports} with no {@link ImportedPackage}s of its own.
     * <p />
     * This is typically used to gather together a collection of imports from multiple sources which are then to be
     * added to one or more bundles.
     * 
     * @return a <code>TrackedPackageImports</code> instance
     */
    TrackedPackageImports createCollector();

    /**
     * Create a <i>container</i> {@link TrackedPackageImports} which contains any {@link ImportedPackage}s which are
     * subsequently merged into it but which has no {@link ImportedPackage}s of its own. If the container is involved in
     * producing an {@link ImportMergeException}, the sources of the {@link ImportedPackage}s merged into the container
     * are wrapped in the given containing source.
     * <p />
     * This is typically used for named groups of imports such as the group of package imports corresponding to a
     * library (in which case the name of the group could be the library name).
     * @param containingSource 
     * @return a <code>TrackedPackageImports</code> instance
     */
    TrackedPackageImports createContainer(String containingSource);
    
    /**
     * Create an empty, immutable {@link TrackedPackageImports}.
     * 
     * @return a <code>TrackedPackageImports</code> instance
     */
    TrackedPackageImports createEmpty();

}
