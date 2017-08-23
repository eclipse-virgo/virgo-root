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

package org.eclipse.virgo.util.osgi.manifest;



/**
 * Represents a single entry in a bundle's <code>DynamicImport-Package</code> header.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * May not be thread safe.
 */
public interface DynamicallyImportedPackage extends Parameterised {
    
    /**
     * Returns the (possibly wildcarded) name of the package in this entry.
     * 
     * @return the package name
     */
    String getPackageName();
    
    /**
     * Sets the name of the package in this entry.
     * @param packageName the package's name.
     */
    void setPackageName(String packageName);
    
    /**
     * Returns the value of the import's <code>bundle-version</code> attribute. If no such attribute is specified
     * the default version range of [0, infinity) is returned.
     * 
     * @return The value of the <code>bundle-version</code> attribute.
     */
    VersionRange getBundleVersion();
    
    /**
     * Sets the value of the import's <code>bundle-version</code> attribute.
     * @param versionRange the value of the <code>bundle-version</code> attribute.
     */
    void setBundleVersion(VersionRange versionRange);
    
    /**
     * Returns the value of the import's <code>bundle-symbolic-name</code> attribute. If no such attribute is
     * specified <code>null</code> is returned.
     * 
     * @return The value of the import's <code>bundle-symbolic-name</code> attribute.
     */
    String getBundleSymbolicName();
     
    /**
     * Sets the value of the import's <code>bundle-symbolic-name</code> attribute.
     * 
     * @param bundleSymbolicName the value of the <code>bundle-symbolic-name</code> attribute.
     */
    void setBundleSymbolicName(String bundleSymbolicName);
    
    /**
     * Returns the version range of the import. If no <code>version</code> attribute is specified
     * the default range of [0, infinity) is returned.
     * 
     * @return the import's version range
     */
    VersionRange getVersion();
    
    /**
     * Sets the version range of the import.
     * 
     * @param versionRange the import's version range.
     */
    void setVersion(VersionRange versionRange);
}
