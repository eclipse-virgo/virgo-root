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

import org.eclipse.virgo.util.osgi.manifest.DynamicallyImportedPackage;
import org.eclipse.virgo.util.osgi.manifest.VersionRange;
import org.eclipse.virgo.util.osgi.manifest.parse.HeaderDeclaration;
import org.eclipse.virgo.util.osgi.manifest.parse.HeaderParser;
import org.osgi.framework.Constants;



/**
 * <strong>Concurrent Semantics</strong><br />
 * Not thread-safe.
 */
class StandardDynamicallyImportedPackage extends BaseCompoundHeaderEntry implements DynamicallyImportedPackage {
    
    StandardDynamicallyImportedPackage(HeaderParser parser, String name) {
        super(parser, name);
    }

    @Override
    HeaderDeclaration parse(HeaderParser parser, String parseString) {
        List<HeaderDeclaration> headerDeclarations = parser.parseDynamicImportPackageHeader(parseString);
        return headerDeclarations.get(0);
    }

    /** 
     * {@inheritDoc}
     */
    public String getPackageName() {
        return this.name;
    }

    /** 
     * {@inheritDoc}
     */
    public void setPackageName(String packageName) {
        this.name = packageName;
    }
    
    /** 
     * {@inheritDoc}
     */
    public String getBundleSymbolicName() {
        return getAttributes().get(Constants.BUNDLE_SYMBOLICNAME_ATTRIBUTE);
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
    public VersionRange getVersion() {
        String value = getAttributes().get(Constants.VERSION_ATTRIBUTE);
        return new VersionRange(value);        
    }

    /** 
     * {@inheritDoc}
     */
    public void setVersion(VersionRange versionRange) {
        if (versionRange != null) {
            getAttributes().put(Constants.VERSION_ATTRIBUTE, versionRange.toParseString());
        } else {
            getAttributes().remove(Constants.VERSION_ATTRIBUTE);
        }
    }

    /** 
     * {@inheritDoc}
     */
    public void setBundleSymbolicName(String bundleSymbolicName) {
        getAttributes().put(Constants.BUNDLE_SYMBOLICNAME_ATTRIBUTE, bundleSymbolicName);        
    }

    /** 
     * {@inheritDoc}
     */
    public void setBundleVersion(VersionRange versionRange) {
        if (versionRange != null) {
            getAttributes().put(Constants.BUNDLE_VERSION_ATTRIBUTE, versionRange.toParseString());
        } else {
            getAttributes().remove(Constants.BUNDLE_VERSION_ATTRIBUTE);
        }
        
    }
}
