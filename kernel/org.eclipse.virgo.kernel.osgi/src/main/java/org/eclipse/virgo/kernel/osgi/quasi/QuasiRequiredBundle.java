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

package org.eclipse.virgo.kernel.osgi.quasi;

import org.eclipse.virgo.util.osgi.manifest.VersionRange;

/**
 * <p>
 * {@link QuasiRequiredBundle} is a representation of a bundle required from another bundle in a {@link QuasiFramework}.
 * </p>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations of this class must be thread safe.
 * 
 */
public interface QuasiRequiredBundle extends QuasiParameterised {

    /**
     * @return The symbolic name of the Bundle required.
     */
    public String getRequiredBundleName();

    /**
     * The version range that must be satisfied by any matching bundles.
     * 
     * @return the <code>VersionRange</code> constraint
     */
    public VersionRange getVersionConstraint();

    /**
     * Returns whether this require bundle is resolved.
     * 
     * @return true if this require bundle is resolved
     */
    public boolean isResolved();

    /**
     * If this require bundle is resolved, return any specific {@link QuasiBundle} that satisfies it. If this require
     * bundle is not resolved or if it is resolved but is optional and was not satisfied, return null.
     * 
     * @return any <code>QuasiBundle</code> that satisfies this <code>QuasiRequiredBundle</code>.
     */
    public QuasiBundle getProvider();

    /**
     * The {@link QuasiBundle} that specifies this <code>QuasiRequiredBundle</code> clause.
     * 
     * @return The requiring QuasiBundle.
     */
    public QuasiBundle getRequiringBundle();

}
