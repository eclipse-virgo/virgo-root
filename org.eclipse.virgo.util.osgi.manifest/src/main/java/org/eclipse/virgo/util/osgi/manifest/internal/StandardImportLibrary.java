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

import java.util.List;

import org.eclipse.virgo.util.osgi.manifest.ImportLibrary;
import org.eclipse.virgo.util.osgi.manifest.ImportedLibrary;
import org.eclipse.virgo.util.osgi.manifest.parse.HeaderDeclaration;
import org.eclipse.virgo.util.osgi.manifest.parse.HeaderParser;



/**
 * <strong>Concurrent Semantics</strong><br />
 * Not thread-safe
 */
public class StandardImportLibrary extends CompoundParseable<ImportedLibrary> implements ImportLibrary {

    StandardImportLibrary(HeaderParser parser) {
        super(parser);        
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    List<HeaderDeclaration> parse(String parseString) {
        return this.parser.parseImportLibraryHeader(parseString);        
    }

    /** 
     * {@inheritDoc}
     */
    public ImportedLibrary addImportedLibrary(String importedLibrary) {
        return add(importedLibrary);
    }

    /** 
     * {@inheritDoc}
     */
    public List<ImportedLibrary> getImportedLibraries() {
        return this.components;
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    ImportedLibrary newEntry(String name) {
        return new StandardImportedLibrary(this.parser, name);
    }
}
