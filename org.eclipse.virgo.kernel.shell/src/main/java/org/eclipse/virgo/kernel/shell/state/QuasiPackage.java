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

package org.eclipse.virgo.kernel.shell.state;

import java.util.List;

import org.eclipse.virgo.kernel.osgi.quasi.QuasiExportPackage;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiFramework;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiImportPackage;


/**
 * <p>
 * A representation of a single package within the {@link QuasiFramework} 
 * </p>
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * Implementations should be thread-safe
 *
 */
public interface QuasiPackage {

    /**
     * The name of the package this {@link QuasiPackage} represents.
     * Will never be null. 
     * 
     * @return packageName
     */
    public String getPackageName();
    
    /**
     * A list of all the {@link QuasiImportPackage}s that are imports of this package.
     * 
     * @return list of imports
     */
    public List<QuasiImportPackage> getImporters();
    
    /**
     * A list of all the {@link QuasiExportPackage}s that are exports of this package.
     * 
     * @return list of exports
     */
    public List<QuasiExportPackage> getExporters();
    
}
