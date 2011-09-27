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

package org.eclipse.virgo.apps.admin.core.stubs;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.virgo.kernel.osgi.quasi.QuasiBundle;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiRequiredBundle;
import org.eclipse.virgo.util.osgi.manifest.internal.VersionRange;

/**
 */
public class StubQuasiRequiredBundle implements QuasiRequiredBundle {

    private final long requirer;
    
    private final long provider;

    private final String bundleName;

    public StubQuasiRequiredBundle(String bundleName, long provider, long requirer) {
        this.bundleName = bundleName;
        this.provider = provider;
        this.requirer = requirer;
    }
    
    /** 
     * {@inheritDoc}
     */
    public QuasiBundle getProvider() {
        return new StubQuasiLiveBundle(this.provider, null);
    }

    /** 
     * {@inheritDoc}
     */
    public String getRequiredBundleName() {
        return this.bundleName;
    }

    /** 
     * {@inheritDoc}
     */
    public QuasiBundle getRequiringBundle() {
        return new StubQuasiLiveBundle(this.requirer, null);
    }

    /** 
     * {@inheritDoc}
     */
    public VersionRange getVersionConstraint() {
        return VersionRange.NATURAL_NUMBER_RANGE;
    }

    /** 
     * {@inheritDoc}
     */
    public boolean isResolved() {
        return false;
    }

    /** 
     * {@inheritDoc}
     */
    public Map<String, Object> getAttributes() {
        return new HashMap<String, Object>();
    }

    /** 
     * {@inheritDoc}
     */
    public Map<String, Object> getDirectives() {
        return new HashMap<String, Object>();
    }

}
