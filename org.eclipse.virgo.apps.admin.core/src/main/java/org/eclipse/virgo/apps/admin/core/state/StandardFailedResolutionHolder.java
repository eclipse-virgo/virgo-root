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

import org.eclipse.virgo.apps.admin.core.BundleHolder;
import org.eclipse.virgo.apps.admin.core.FailedResolutionHolder;
import org.eclipse.virgo.kernel.module.ModuleContextAccessor;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiResolutionFailure;


/**
 * <p>
 * StandardFailedResolutionHolder represents a single cause of a bundle resolution 
 * failure. A bundle may fail to resolve for many reasons.
 * </p>
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * StandardFailedResolutionHolder is threadsafe
 *
 */
final class StandardFailedResolutionHolder implements FailedResolutionHolder {

    private final ModuleContextAccessor moduleContextAccessor;
    
    private final QuasiResolutionFailure quasiResolutionFailure;

    /**
     * 
     * @param quasiResolutionFailure to be held
     * @param moduleContextAccessor used for {@link BundleHolder} creation
     */
    public StandardFailedResolutionHolder(QuasiResolutionFailure quasiResolutionFailure, ModuleContextAccessor moduleContextAccessor) {
        this.quasiResolutionFailure = quasiResolutionFailure;
        this.moduleContextAccessor = moduleContextAccessor;
    }

    /** 
     * {@inheritDoc}
     */
    public String getDescription() {
        return this.quasiResolutionFailure.getDescription();
    }

    /** 
     * {@inheritDoc}
     */
    public BundleHolder getUnresolvedBundle() {
        return new StandardBundleHolder(this.quasiResolutionFailure.getUnresolvedQuasiBundle(), this.moduleContextAccessor);
    }

}
