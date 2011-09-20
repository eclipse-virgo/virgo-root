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

package org.eclipse.virgo.util.osgi.manifest.internal;

import org.eclipse.virgo.util.osgi.manifest.ImportedLibrary;
import org.eclipse.virgo.util.osgi.manifest.parse.HeaderDeclaration;
import org.eclipse.virgo.util.osgi.manifest.parse.HeaderParser;


/**
 * <strong>Concurrent Semantics</strong><br />
 * Not thread-safe.
 * 
 */
public class StandardImportedLibrary extends BaseImported implements ImportedLibrary {
    
     StandardImportedLibrary(HeaderParser parser, String name) {
        super(parser, name);
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    HeaderDeclaration parse(HeaderParser parser, String parseString) {
        return parser.parseImportLibraryHeader(parseString).get(0);
    }

    /** 
     * {@inheritDoc}
     */
    public String getLibrarySymbolicName() {
        return this.name;
    }

    /** 
     * {@inheritDoc}
     */
    public void setLibrarySymbolicName(String librarySymbolicName) {
        if (librarySymbolicName == null) {
            throw new IllegalArgumentException("librarySymbolicName must not be null");
        }
        this.name = librarySymbolicName;
    }

}
