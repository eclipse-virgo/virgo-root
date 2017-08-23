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

import org.eclipse.virgo.util.osgi.manifest.FragmentHost;
import org.eclipse.virgo.util.osgi.manifest.VersionRange;
import org.eclipse.virgo.util.osgi.manifest.parse.HeaderDeclaration;
import org.eclipse.virgo.util.osgi.manifest.parse.HeaderParser;
import org.osgi.framework.Constants;



/**
 * <strong>Concurrent Semantics</strong><br />
 * Not thread-safe.
 */
class StandardFragmentHost extends BaseParameterised implements FragmentHost {

    StandardFragmentHost(HeaderParser parser) {
        super(parser);
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    HeaderDeclaration parse(HeaderParser parser, String parseString) {        
        return parser.parseFragmentHostHeader(parseString);
    }

    /** 
     * {@inheritDoc}
     */
    public String getBundleSymbolicName() {
        return this.name;
    }

    /** 
     * {@inheritDoc}
     */
    public Extension getExtension() {
        String value = getDirectives().get(Constants.EXTENSION_DIRECTIVE);
        if (Constants.EXTENSION_BOOTCLASSPATH.equals(value)) {
            return Extension.BOOTCLASSPATH;
        } else if (Constants.EXTENSION_FRAMEWORK.equals(value)) {
            return Extension.FRAMEWORK;
        }
        return null;
    }

    /** 
     * {@inheritDoc}
     */
    public void setBundleSymbolicName(String hostName) {
        this.name = hostName;        
    }

    /** 
     * {@inheritDoc}
     */
    public void setExtension(Extension extension) {
        if (extension == null) {
            getDirectives().remove(Constants.EXTENSION_DIRECTIVE);
            return;
        }
        
        switch (extension) {
            case BOOTCLASSPATH:
                getDirectives().put(Constants.EXTENSION_DIRECTIVE, Constants.EXTENSION_BOOTCLASSPATH);
                break;
            case FRAMEWORK:
                getDirectives().put(Constants.EXTENSION_DIRECTIVE, Constants.EXTENSION_FRAMEWORK);
                break;            
        }
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
