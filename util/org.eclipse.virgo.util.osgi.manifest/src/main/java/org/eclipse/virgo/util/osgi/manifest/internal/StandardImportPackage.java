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

import org.eclipse.virgo.util.osgi.manifest.ImportPackage;
import org.eclipse.virgo.util.osgi.manifest.ImportedPackage;
import org.eclipse.virgo.util.osgi.manifest.parse.HeaderDeclaration;
import org.eclipse.virgo.util.osgi.manifest.parse.HeaderParser;
import org.osgi.framework.Constants;


/**
 * <strong>Concurrent Semantics</strong><br />
 * Thread-safe.
 */
public class StandardImportPackage extends CompoundParseable<ImportedPackage> implements ImportPackage {

    /**
     * @param parser
     */
    StandardImportPackage(HeaderParser parser) {
        super(parser);
    }

    /** 
     * {@inheritDoc}
     */
    public ImportedPackage addImportedPackage(String importedPackage) {
        return add(importedPackage);
    }

    /** 
     * {@inheritDoc}
     */
    public List<ImportedPackage> getImportedPackages() {
        return this.components;
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    List<HeaderDeclaration> parse(String parseString) {
        return this.parser.parsePackageHeader(parseString, Constants.IMPORT_PACKAGE);        
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    ImportedPackage newEntry(String name) {
        return new StandardImportedPackage(this.parser, name);
    }
}
