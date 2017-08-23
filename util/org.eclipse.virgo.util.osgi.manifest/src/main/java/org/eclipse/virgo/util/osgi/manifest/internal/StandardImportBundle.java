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

import org.eclipse.virgo.util.osgi.manifest.ImportBundle;
import org.eclipse.virgo.util.osgi.manifest.ImportedBundle;
import org.eclipse.virgo.util.osgi.manifest.parse.HeaderDeclaration;
import org.eclipse.virgo.util.osgi.manifest.parse.HeaderParser;



/**
 * <strong>Concurrent Semantics</strong><br />
 * Not thread-safe.
 */
class StandardImportBundle extends CompoundParseable<ImportedBundle> implements ImportBundle {

    StandardImportBundle(HeaderParser parser) {
        super(parser);
    }

    /** 
     * {@inheritDoc}
     */
    public ImportedBundle addImportedBundle(String importedBundle) {
        return add(importedBundle);
    }

    /** 
     * {@inheritDoc}
     */
    public List<ImportedBundle> getImportedBundles() {
        return this.components;
    }

    @Override
    List<HeaderDeclaration> parse(String parseString) {
        return this.parser.parseImportBundleHeader(parseString);        
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    ImportedBundle newEntry(String name) {
        return new StandardImportedBundle(this.parser, name);
    }

}
