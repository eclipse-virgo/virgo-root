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
 * Represents the <code>Import-Bundle</code> header in a {@link BundleManifest}.
 * 
 * <strong>Concurrent Semantics</strong><br />
 * May not be thread-safe.
 */
public interface ImportBundle extends Parseable {

    /**
     * Returns a <code>List</code> of the bundles that are imported. Returns an empty <code>List</code> if no bundles
     * are imported.
     * 
     * @return the imported bundles.
     */
    List<ImportedBundle> getImportedBundles();

    /**
     * Adds an import of the bundle with the supplied symbolic name to this <code>Import-Bundle</code> header.
     * 
     * @param bundleSymbolicName The name of the imported bundle
     * @return the newly-created <code>ImportBundle</code>.
     */
    ImportedBundle addImportedBundle(String bundleSymbolicName);
}
