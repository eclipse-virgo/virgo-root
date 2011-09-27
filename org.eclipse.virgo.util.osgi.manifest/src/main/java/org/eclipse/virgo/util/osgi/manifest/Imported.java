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


/**
 * A common interface for the entries in the three import headers: <code>Import-Package</code>, <code>Import-Bundle</code>, and
 * <code>Import-Library</code>.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * May not be thread-safe.
 * 
 * @see ImportedPackage
 * @see ImportedBundle
 * @see ImportedLibrary
 */
public interface Imported extends Parameterised {
    
    /**
     * Returns the version range of the import. If no <code>version</code> attribute is specified
     * the default range of [0, infinity) is returned.
     * 
     * @return the import's version range
     */
    VersionRange getVersion();
    
    /**
     * Sets the version range of the import.
     * 
     * @param versionRange the import's version range.
     */
    void setVersion(VersionRange versionRange);

    /**
     * Returns the value of the import's <code>resolution</code> directive. If no such directive is specified the
     * default value of {@link Resolution#MANDATORY} is returned.
     * 
     * @return the value of the import's resolution directive.
     */
    Resolution getResolution();
    
    /**
     * Sets the value of the import's <code>resolution</code> directive.
     * 
     * @param resolution The import's resolution directive
     */
    void setResolution(Resolution resolution);
}
