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

import org.eclipse.virgo.util.osgi.manifest.DynamicImportPackage;
import org.eclipse.virgo.util.osgi.manifest.DynamicallyImportedPackage;
import org.eclipse.virgo.util.osgi.manifest.parse.HeaderDeclaration;
import org.eclipse.virgo.util.osgi.manifest.parse.HeaderParser;


/**
 * <strong>Concurrent Semantics</strong><br />
 * Not thread-safe.
 */
class StandardDynamicImportPackage extends CompoundParseable<DynamicallyImportedPackage> implements DynamicImportPackage {

    /**
     * @param parser
     */
    StandardDynamicImportPackage(HeaderParser parser) {
        super(parser);
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    List<HeaderDeclaration> parse(String parseString) {
        return this.parser.parseDynamicImportPackageHeader(parseString);
    }

    /** 
     * {@inheritDoc}
     */
    public DynamicallyImportedPackage addDynamicallyImportedPackage(String dynamicallyImportedPackage) {
        return add(dynamicallyImportedPackage);
    }

    /** 
     * {@inheritDoc}
     */
    public List<DynamicallyImportedPackage> getDynamicallyImportedPackages() {
        return this.components;
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    DynamicallyImportedPackage newEntry(String name) {
        return new StandardDynamicallyImportedPackage(this.parser, name);        
    }
}
