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
 * Represents the <code>Export-Package</code> header in a {@link BundleManifest}.
 * 
 * <strong>Concurrent Semantics</strong><br />
 * May not be thread-safe.
 */
public interface ExportPackage extends Parseable {

    /**
     * Returns a <code>List</code> of the packages that are exported. Returns an empty <code>List</code> if no packages
     * are exported.
     * 
     * @return the exported packages.
     */
    List<ExportedPackage> getExportedPackages();

    /**
     * Adds an export of the package with the supplied name to this <code>Export-Package</code> header.
     * 
     * @param packageName The name of the exported package.
     * @return the newly-created <code>ExportedPackage</code>.
     */
    ExportedPackage addExportedPackage(String packageName);
}
