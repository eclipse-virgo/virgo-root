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


public interface FragmentHost extends Parameterised {

    /**
     * Enumeration of the valid values for the header's <code>extension</code> directive
     * that applies when a fragment attaches to the system bundle.
     */    
    public enum Extension {
        /**
         * The fragment is a framework extension bundle.
         */
        FRAMEWORK, 
        /**
         * The fragment is a boot classpath extension bundle.
         */
        BOOTCLASSPATH
    }

    /**
     * Returns the bundle symbolic name specified in the header, or <code>null</code> if no <code>Fragment-Host</code> is specified.
     * 
     * @return the bundle symbolic name
     */
    String getBundleSymbolicName();

    /**
     * Set the bundle symbolic name in the header
     * 
     * @param hostName the bundle symbolic name of the fragment's host
     */
    void setBundleSymbolicName(String hostName);    

    /**
     * Returns the value of the header's <code>extension</code> directive, or <code>null</code> if no such directive is specified.
     * 
     * @return the extension directive
     */
    Extension getExtension();

    /**
     * Set the value of the header's <code>extension</code> directive.
     * @param extension the value for the extension directive
     */
    void setExtension(Extension extension);
    
    /**
     * Returns the value of the header's <code>bundle-version</code> attribute. If no such attribute is specified, returns the
     * default version range of [0, infinity).
     * 
     * @return the value of the <code>bundle-version</code> attribute.
     */
    VersionRange getBundleVersion();
    
    /**
     * Sets the value of the header's <code>bundle-version</code> directive.
     * 
     * @param versionRange the value for the <code>bundle-version</code> directive.
     */
    void setBundleVersion(VersionRange versionRange);
}
