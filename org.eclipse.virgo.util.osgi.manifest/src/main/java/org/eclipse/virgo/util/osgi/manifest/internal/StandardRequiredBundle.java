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

import org.eclipse.virgo.util.osgi.manifest.RequiredBundle;
import org.eclipse.virgo.util.osgi.manifest.Resolution;
import org.eclipse.virgo.util.osgi.manifest.VersionRange;
import org.eclipse.virgo.util.osgi.manifest.parse.HeaderDeclaration;
import org.eclipse.virgo.util.osgi.manifest.parse.HeaderParser;
import org.osgi.framework.Constants;


/**
 * <strong>Concurrent Semantics</strong><br />
 * Not thread-safe.
 */
public class StandardRequiredBundle extends BaseCompoundHeaderEntry implements RequiredBundle {

    StandardRequiredBundle(HeaderParser parser, String name) {
        super(parser, name);
    }

    public String getBundleSymbolicName() {
        return this.name;
    }

    /**
     * {@inheritDoc}
     */
    public Resolution getResolution() {
        String value = getDirectives().get(Constants.RESOLUTION_DIRECTIVE);
        if (Constants.RESOLUTION_OPTIONAL.equals(value)) {
            return Resolution.OPTIONAL;
        } else {
            return Resolution.MANDATORY;
        }
    }

    /**
     * {@inheritDoc}
     */
    public Visibility getVisibility() {
        String value = getDirectives().get(Constants.VISIBILITY_DIRECTIVE);
        if (Constants.VISIBILITY_REEXPORT.equals(value)) {
            return Visibility.REEXPORT;
        } else {
            return Visibility.PRIVATE;
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setBundleSymbolicName(String bundleSymbolicName) {
        if (bundleSymbolicName == null) {
            throw new IllegalArgumentException("bundleSymbolicName must not be null");
        }
        this.name = bundleSymbolicName;
    }

    /**
     * {@inheritDoc}
     */
    public void setResolution(Resolution resolution) {
        if (resolution == null) {
            getDirectives().remove(Constants.RESOLUTION_DIRECTIVE);
        } else {
            switch (resolution) {
                case OPTIONAL:
                    getDirectives().put(Constants.RESOLUTION_DIRECTIVE, Constants.RESOLUTION_OPTIONAL);
                    break;
                default:
                    getDirectives().remove(Constants.RESOLUTION_DIRECTIVE);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setVisibility(Visibility visibility) {
        if (visibility == null) {
            getDirectives().remove(Constants.VISIBILITY_DIRECTIVE);
        } else {
            switch (visibility) {
                case REEXPORT:
                    getDirectives().put(Constants.VISIBILITY_DIRECTIVE, Constants.VISIBILITY_REEXPORT);
                    break;
                default:
                    getDirectives().remove(Constants.VISIBILITY_DIRECTIVE);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    HeaderDeclaration parse(HeaderParser parser, String parseString) {
        return parser.parseRequireBundleHeader(parseString).get(0);
    }
    
    /** 
     * {@inheritDoc}
     */
    public VersionRange getBundleVersion() {
        return new VersionRange(getAttributes().get(Constants.BUNDLE_VERSION_ATTRIBUTE));        
    }

    /** 
     * {@inheritDoc}
     */
    public void setBundleVersion(VersionRange versionRange) {
        if (versionRange == null) {
            getAttributes().remove(Constants.BUNDLE_VERSION_ATTRIBUTE);
            
        } else {
            getAttributes().put(Constants.BUNDLE_VERSION_ATTRIBUTE, versionRange.toParseString());
        }        
    }
}
