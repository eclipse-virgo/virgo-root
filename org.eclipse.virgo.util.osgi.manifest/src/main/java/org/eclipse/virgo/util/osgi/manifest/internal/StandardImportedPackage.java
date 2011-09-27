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

import org.eclipse.virgo.util.osgi.manifest.ImportedPackage;
import org.eclipse.virgo.util.osgi.manifest.VersionRange;
import org.eclipse.virgo.util.osgi.manifest.parse.HeaderDeclaration;
import org.eclipse.virgo.util.osgi.manifest.parse.HeaderParser;
import org.osgi.framework.Constants;



/**
 * <strong>Concurrent Semantics</strong><br />
 * Not thread-safe.
 */
public class StandardImportedPackage extends BaseImported implements ImportedPackage {

    StandardImportedPackage(HeaderParser parser, String name) {
        super(parser, name);
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    HeaderDeclaration parse(HeaderParser parser, String parseString) {
        return parser.parsePackageHeader(parseString, Constants.IMPORT_PACKAGE).get(0);
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
    public String getPackageName() {
        return this.name;
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
        getAttributes().put(Constants.BUNDLE_VERSION_ATTRIBUTE, versionRange.toParseString());
    }

    /** 
     * {@inheritDoc}
     */
    public void setPackageName(String packageName) {
        if (packageName == null) {
            throw new IllegalArgumentException("packageName must not be null");
        }
        this.name = packageName;
    }
}
