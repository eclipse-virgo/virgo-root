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

import org.eclipse.virgo.util.osgi.manifest.ExportPackage;
import org.eclipse.virgo.util.osgi.manifest.ExportedPackage;
import org.eclipse.virgo.util.osgi.manifest.parse.HeaderDeclaration;
import org.eclipse.virgo.util.osgi.manifest.parse.HeaderParser;
import org.osgi.framework.Constants;



/**
 * <strong>Concurrent Semantics</strong><br />
 * Not thread-safe.
 */
class StandardExportPackage extends CompoundParseable<ExportedPackage> implements ExportPackage {

    StandardExportPackage(HeaderParser parser) {
        super(parser);
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    List<HeaderDeclaration> parse(String parseString) {
        return this.parser.parsePackageHeader(parseString, Constants.EXPORT_PACKAGE);        
    }

    /** 
     * {@inheritDoc}
     */
    public ExportedPackage addExportedPackage(String exportedPackage) {
        return add(exportedPackage);        
    }

    /** 
     * {@inheritDoc}
     */
    public List<ExportedPackage> getExportedPackages() {
        return this.components;        
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    ExportedPackage newEntry(String name) {
        return new StandardExportedPackage(this.parser, name);
    }
}
