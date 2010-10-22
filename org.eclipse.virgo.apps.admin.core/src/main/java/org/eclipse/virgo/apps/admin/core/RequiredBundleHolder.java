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

package org.eclipse.virgo.apps.admin.core;

import java.util.Map;

/**
 * <p>
 * RequiredBundleHolder represents a requirement from one bundle to another.
 * </p>
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * RequiredBundleHolder implementations should be thread-safe
 *
 */
public interface RequiredBundleHolder {

    /**
     * If this require bundle is resolved, return any specific {@link BundleHolder} that satisfies it. If this require
     * bundle is not resolved or if it is resolved but is optional and was not satisfied, return null.
     * 
     * @return any <code>BundleHolder</code> that satisfies this <code>RequiredBundleHolder</code>
     */
    public BundleHolder getProvider();
    
    /**
     * @return The symbolic name of the Bundle required.
     */
    public String getRequiredBundleName();

    /**
     * The version range that must be satisfied by any matching bundles.
     * 
     * @return the <code>VersionRange</code> constraint as a String
     */
    public String getVersionConstraint();

    /**
     * Returns whether this require bundle is resolved.
     * 
     * @return true if this require bundle is resolved
     */
    public boolean isResolved();

    /**
     * Returns the directives for a header.
     * 
     * @return a map containing the directives
     */
    Map<String, String> getDirectives();

    /**
     * Returns the attributes for a header.
     * 
     * @return a map containing the attributes
     */
    Map<String, String> getAttributes();
    
    /**
     * The {@link BundleHolder} that specifies this <code>RequiredBundleHolder</code> clause.
     * 
     * @return The requiring QuasiBundle
     */
    public BundleHolder getRequiringBundle();
    
}
