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

package org.eclipse.virgo.apps.admin.core.state;

import java.util.Map;

import org.eclipse.virgo.apps.admin.core.BundleHolder;
import org.eclipse.virgo.apps.admin.core.RequiredBundleHolder;
import org.eclipse.virgo.kernel.module.ModuleContextAccessor;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiRequiredBundle;
import org.eclipse.virgo.util.osgi.VersionRange;


/**
 * <p>
 * StandardRequiredBundleHolder is the standard implementation of {@link RequiredBundleHolder}.
 * </p>
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * StandardRequiredBundleHolder is thread-safe
 *
 */
final class StandardRequiredBundleHolder implements RequiredBundleHolder {

    private final QuasiRequiredBundle quasiRequiredBundle;
    
    private final ModuleContextAccessor moduleContextAccessor;

    public StandardRequiredBundleHolder(QuasiRequiredBundle quasiRequiredBundle, ModuleContextAccessor moduleContextAccessor) {
        this.quasiRequiredBundle = quasiRequiredBundle;
        this.moduleContextAccessor = moduleContextAccessor;
    }

    /** 
     * {@inheritDoc}
     */
    public BundleHolder getProvider() {
        return new StandardBundleHolder(this.quasiRequiredBundle.getProvider(), this.moduleContextAccessor);
    }

    /** 
     * {@inheritDoc}
     */
    public String getRequiredBundleName() {
        return this.quasiRequiredBundle.getRequiredBundleName();
    }

    /** 
     * {@inheritDoc}
     */
    public BundleHolder getRequiringBundle() {
        return new StandardBundleHolder(this.quasiRequiredBundle.getRequiringBundle(), this.moduleContextAccessor);
    }

    /** 
     * {@inheritDoc}
     */
    public String getVersionConstraint() {
        VersionRange versionConstraint = this.quasiRequiredBundle.getVersionConstraint();
        if(versionConstraint == null) {
            versionConstraint = VersionRange.NATURAL_NUMBER_RANGE;
        }
        return versionConstraint.toString().replace(", oo]", ", &infin;]").replace(", oo)", ", &infin;)");
    }
    
    /** 
     * {@inheritDoc}
     */
    public Map<String, String> getAttributes() {
        return ObjectFormatter.formatMapValues(this.quasiRequiredBundle.getAttributes());
    }

    /** 
     * {@inheritDoc}
     */
    public Map<String, String> getDirectives() {
        return ObjectFormatter.formatMapValues(this.quasiRequiredBundle.getDirectives());
    } 

    /** 
     * {@inheritDoc}
     */
    public boolean isResolved() {
        return this.quasiRequiredBundle.isResolved();
    }

}
