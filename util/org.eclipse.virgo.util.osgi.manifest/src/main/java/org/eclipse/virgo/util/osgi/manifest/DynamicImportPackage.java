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

package org.eclipse.virgo.util.osgi.manifest;

import java.util.List;

/**
 * Represents the <code>DynamicImport-Package</code> header in a {@link BundleManifest}.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * Implementations may not be thread-safe.
 */
public interface DynamicImportPackage extends Parseable {

    /**
     * Returns a list of dynamically imported packages, one for each entry in the <code>DynamicImport-Package</code>
     * header. If no such header exists, an empty list is returned.
     * 
     * @return the list of dynamically imported packages.
     */
    List<DynamicallyImportedPackage> getDynamicallyImportedPackages();

    /**
     * Adds a dynamically imported package, with the supplied, possibly wild-carded, name, to this
     * <code>DynamicImport-Package</code> header.
     * 
     * @param packageName The, possibly wild-carded, name of the package
     * @return the newly-created <code>DynamicallyImportedPackage</code>.
     */
    DynamicallyImportedPackage addDynamicallyImportedPackage(String packageName);
}
