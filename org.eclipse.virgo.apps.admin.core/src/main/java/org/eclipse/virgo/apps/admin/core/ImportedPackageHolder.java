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

package org.eclipse.virgo.apps.admin.core;

import java.util.Map;

/**
 * <p>
 * PackageRelationHolder represents a single mapping of and exported 
 * </p>
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * Implementations of PackageRelationHolder should be threadsafe
 *
 */
public interface ImportedPackageHolder {

    /**
     * The name of the package being imported.
     * 
     * @return The package name
     */
    public String getPackageName();

    /**
     * The version range that the exporting package must be within to satisfy this <code>ImportedPackageHolder</code>.
     * 
     * @return The VersionRange constraint as a String
     */
    public String getVersionConstraint();

    /**
     * Returns whether or not this import is resolved.
     * 
     * @return true if this import is resolved
     */
    public boolean isResolved();

    /**
     * If this import is resolved, return the {@link ExportedPackageHolder} that satisfies it. If this import is not
     * resolved or if it is resolved but is an optional import that was not satisfied, return null.
     * 
     * @return The <code>ExportedPackageHolder</code> that satisfies this import.
     */
    public ExportedPackageHolder getProvider();

    /**
     * The {@link BundleHolder} that specifies this import package.
     * 
     * @return The specifying <code>BundleHolder</code>.
     */
    public BundleHolder getImportingBundle();
    
    /**
     * Returns the directives for a header.
     * 
     * @return a map containing the directives
     */
    Map<String, String> getDirectives();

    /**
     * Returns the attributes for a header.
     * 
     * @return a map containing the attributes
     */
    Map<String, String> getAttributes();
    
}
