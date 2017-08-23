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
 * Represents the <code>Import-Library</code> header in a {@link BundleManifest}.
 * 
 * <strong>Concurrent Semantics</strong><br />
 * May not be thread-safe.
 */
public interface ImportLibrary extends Parseable {
    
    /**
     * Returns a <code>List</code> of the libraries that are imported. Returns an empty <code>List</code> if no libraries
     * are imported.
     * 
     * @return the imported libraries.
     */
    List<ImportedLibrary> getImportedLibraries();

    /**
     * Adds an import of the library with the supplied symbolic name to this <code>Import-Library</code> header.
     * 
     * @param librarySymbolicName The name of the imported library.
     * @return the newly-created <code>ImportedLibrary</code>.
     */
    ImportedLibrary addImportedLibrary(String librarySymbolicName);
}
