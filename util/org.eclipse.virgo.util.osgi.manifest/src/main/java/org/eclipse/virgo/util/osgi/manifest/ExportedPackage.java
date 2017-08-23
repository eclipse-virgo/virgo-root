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

import org.osgi.framework.Version;

/**
 * Represents a single entry in a bundle's <code>Export-Package</code> header.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * May not be thread-safe.
 */
public interface ExportedPackage extends Parameterised {

    /**
     * Returns the name of the exported package, never <code>null</code>.
     * 
     * @return the package name
     */
    String getPackageName();
    
    /**
     * Sets the name of the exported package.
     * 
     * @param packageName the package name
     * @throws IllegalArgumentException if the given package name is <code>null</code>
     */
    void setPackageName(String packageName) throws IllegalArgumentException;

    /**
     * Returns the version of the exported package.
     * 
     * @return the version of the exported package. Returns the default version (0) if the export has no version.
     */
    Version getVersion();

    /**
     * Sets the version of the exported package
     * 
     * @param version The exported package's version
     */
    void setVersion(Version version);

    /**
     * Returns a <code>List</code> of the package names specified in the export's <code>uses</code> directive. Returns
     * an empty list if the export has no uses directive.
     * 
     * @return the names of the used packages.
     */
    List<String> getUses();
   
    /**
     * Returns a list of the attribute names specified in the export's <code>mandatory</code> directive.
     * 
     * @return the names of the mandatory attributes. Returns an empty list if the export has no mandatory directive.
     */
    List<String> getMandatory();       

    /**
     * Returns a list of the class names specified in the export's <code>include</code> directive. Returns an empty list
     * if the export has no <code>include</code> directive.
     * 
     * @return the list of inclusions
     */
    List<String> getInclude();

    /**
     * Returns a list of the class names specified in the export's <code>exclude</code> directive. Returns an empty list
     * if the export has no <code>exclude</code> directive.
     * 
     * @return the list of exclusions
     */
    List<String> getExclude();     
}
