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
 * Representation of a single entry in a <code>Require-Bundle</code> header.
 * 
 * <strong>Concurrent Semantics</strong><br />
 * May not be thread-safe.
 * 
 */
public interface RequiredBundle extends Parameterised {

    /**
     * Enumeration of the valid values for the header's <code>visibility</code> directive.
     */
    public enum Visibility {
        /**
         * Packages from the required bundle are not re-exported.
         */
        PRIVATE,

        /**
         * Packages from the required bundle are re-exported, providing transitive access to the packages.
         */
        REEXPORT
    }

    /**
     * Returns the symbolic name of the bundle that is required, never <code>null</code>.
     * 
     * @return the required bundle's symbolic name.
     */
    String getBundleSymbolicName();

    /**
     * Sets the symbolic name of the bundle that is required.
     * 
     * @param bundleSymbolicName the required bundle's symbolic name
     * @throws IllegalArgumentException if the given bundle symbolic name is <code>null</code>
     */
    void setBundleSymbolicName(String bundleSymbolicName);

    /**
     * Returns the value of the entry's <code>resolution</code> directive. If no such directive is specified, the
     * default value of {@link Resolution#MANDATORY} is returned.
     * 
     * @return the value of the resolution directive
     */
    Resolution getResolution();

    /**
     * Sets the value of the <code>resolution</code> directive. If <code>null</code> is given, sets the value to
     * {@link Resolution#MANDATORY}.
     * 
     * @param resolution The resolution directive
     */
    void setResolution(Resolution resolution);

    /**
     * Returns the value of the entry's <code>visibility</code> directive. If no such directive is specified, the
     * default value of {@link Visibility#PRIVATE} is returned.
     * 
     * @return the value of the visibility directive
     */
    Visibility getVisibility();

    /**
     * Sets the value of the <code>visibility</code> directive. If <code>null</code> is given, sets the value to
     * {@link Visibility#PRIVATE}.
     * 
     * @param visibility The value of the visibility directive.
     */
    void setVisibility(Visibility visibility);
    
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
