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

import java.util.List;

/**
 * <p>
 * PackagesCollection holds an exclusive collection of packages sorted by imports and exports.
 * </p>
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * PackagesCollection implementations should be thread-safe
 *
 */
public interface PackagesCollection {


    /**
     * The name of the package this {@link PackagesCollection} represents.
     * Will never be null. 
     * 
     * @return packageName
     */
    public String getPackageName();
    
    /**
     * A list of all the {@link ImportedPackageHolder}s that are imports of this package.
     * 
     * @return list of imports
     */
    public List<ImportedPackageHolder> getImported();
    
    /**
     * A list of all the {@link ExportedPackageHolder}s that are exports of this package.
     * 
     * @return list of exports
     */
    public List<ExportedPackageHolder> getExported();
    
}
