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
 * Represents a single entry in a bundle's <code>Import-Library</code> header.
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 * May not be thread-safe.
 * 
 */
public interface ImportedLibrary extends Imported {

    /**
     * Returns the symbolic name of the library that is imported, never <code>null</code>.
     * 
     * @return the imported library's symbolic name.
     */
    String getLibrarySymbolicName();
    
    /**
     * Sets the symbolic name of the library that is imported.
     * 
     * @param librarySymbolicName The imported library's symbolic name
     * @throws IllegalArgumentException if librarySymbolicName is <code>null</code>
     */
    void setLibrarySymbolicName(String librarySymbolicName) throws IllegalArgumentException;
    
    /**
     * Returns the value of the import's <code>sharing</code> directive. If no such directive is specified the
     * default value of {@link Sharing#AUTOMATIC} is returned.
     * 
     * @return the value of the import's sharing directive.
     */
    Sharing getSharing();
    
    /**
     * Sets the value of the import's <code>sharing</code> directive.
     * 
     * @param sharing the value of the import's sharing directive
     */
    void setSharing(Sharing sharing);
}
