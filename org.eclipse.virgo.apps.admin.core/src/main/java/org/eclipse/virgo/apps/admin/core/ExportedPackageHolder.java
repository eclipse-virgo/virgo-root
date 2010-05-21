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
public interface ExportedPackageHolder {

    /**
     * 
     * @return the name of the package being exported
     */
    public String getPackageName();

    /**
     * 
     * @return the <code>Version</code> that the package is exported at as a String.
     */
    public String getVersion();
    
    /**
     * 
     * @return the {@link BundleHolder} that provides this <code>ExportedPackageHolder</code>
     */
    public BundleHolder getExportingBundle();
    
    /**
     * 
     * @return A list {@link ImportedPackageHolder}s that are consuming this export
     */
    public List<ImportedPackageHolder> getConsumers();
    
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
