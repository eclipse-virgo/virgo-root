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

package org.eclipse.virgo.kernel.osgi.quasi;

import org.eclipse.virgo.util.osgi.manifest.VersionRange;

/**
 * <p>
 * {@link QuasiImportPackage} is a representation of a imported package from a {@link QuasiBundle}.
 * </p>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations of this interface must be thread safe.
 * 
 */
public interface QuasiImportPackage extends QuasiParameterised {

    /**
     * The name of the package being imported.
     * 
     * @return The package name
     */
    public String getPackageName();

    /**
     * The version range that the exporting package must be within to satisfy this <code>QuasiImportPackage</code>.
     * 
     * @return The {@link VersionRange} constraint
     */
    public VersionRange getVersionConstraint();

    /**
     * Returns whether or not this import is resolved.
     * 
     * @return true if this import is resolved
     */
    public boolean isResolved();

    /**
     * If this import is resolved, return the {@link QuasiExportPackage} that satisfies it. If this import is not
     * resolved or if it is resolved but is an optional import that was not satisfied, return null.
     * 
     * @return any <code>QuasiExportPackage</code> that satisfies this import.
     */
    public QuasiExportPackage getProvider();

    /**
     * The {@link QuasiBundle} that specifies this import package.
     * 
     * @return The specifying <code>QuasiBundle</code>.
     */
    public QuasiBundle getImportingBundle();

}
