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

import org.eclipse.virgo.util.osgi.manifest.RequireBundle;
import org.eclipse.virgo.util.osgi.manifest.RequiredBundle;
import org.eclipse.virgo.util.osgi.manifest.parse.HeaderDeclaration;
import org.eclipse.virgo.util.osgi.manifest.parse.HeaderParser;



/**
 * <strong>Concurrent Semantics</strong><br />
 * Not thread-safe.
 */
public class StandardRequireBundle extends CompoundParseable<RequiredBundle> implements RequireBundle {

    StandardRequireBundle(HeaderParser parser) {
        super(parser);
    }

    /** 
     * {@inheritDoc}
     */
    public RequiredBundle addRequiredBundle(String requiredBundle) {
        return add(requiredBundle);
    }

    /** 
     * {@inheritDoc}
     */
    public List<RequiredBundle> getRequiredBundles() {
        return this.components;
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    List<HeaderDeclaration> parse(String parseString) {
        return this.parser.parseRequireBundleHeader(parseString);        
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    RequiredBundle newEntry(String name) {
        return new StandardRequiredBundle(this.parser, name);
    }

}
