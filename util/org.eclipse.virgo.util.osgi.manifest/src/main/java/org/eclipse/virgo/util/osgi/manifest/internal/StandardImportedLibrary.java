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
import org.eclipse.virgo.util.osgi.manifest.Sharing;
import org.eclipse.virgo.util.osgi.manifest.parse.HeaderDeclaration;
import org.eclipse.virgo.util.osgi.manifest.parse.HeaderParser;


/**
 * <strong>Concurrent Semantics</strong><br />
 * Not thread-safe.
 * 
 */
public class StandardImportedLibrary extends BaseImported implements ImportedLibrary {
    
    private static final String SHARING_SHARE = "share";
    
    private static final String SHARING_CLONE = "clone";
    
    private static final String SHARING_DIRECTIVE = "sharing";

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

    /** 
     * {@inheritDoc}
     */
    public Sharing getSharing() {
        String value = getDirectives().get(SHARING_DIRECTIVE);
        if (SHARING_SHARE.equals(value)) {
            return Sharing.SHARE;
        } else if (SHARING_CLONE.equals(value)) {
            return Sharing.CLONE;
        } else {
            return Sharing.AUTOMATIC;
        }
    }

    /** 
     * {@inheritDoc}
     */
    public void setSharing(Sharing sharing) {
        if (sharing == null) {
            getDirectives().remove(SHARING_DIRECTIVE);
            return;
        }
        
        switch (sharing) {
            case CLONE: 
                getDirectives().put(SHARING_DIRECTIVE, SHARING_CLONE);
                break;
            case SHARE:
                getDirectives().put(SHARING_DIRECTIVE, SHARING_SHARE);
                break;
            default:
                getDirectives().remove(SHARING_DIRECTIVE);
                break;       
        }   
    }
}
