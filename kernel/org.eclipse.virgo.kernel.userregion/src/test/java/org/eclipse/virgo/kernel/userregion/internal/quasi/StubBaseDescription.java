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

package org.eclipse.virgo.kernel.userregion.internal.quasi;

import java.util.Map;

import org.eclipse.osgi.service.resolver.BaseDescription;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.osgi.framework.Version;
import org.osgi.framework.wiring.BundleCapability;

public class StubBaseDescription implements BaseDescription {

    private BundleDescription supplier;

    /** 
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        throw new UnsupportedOperationException();
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public BundleDescription getSupplier() {
        return this.supplier;
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public Version getVersion() {
        throw new UnsupportedOperationException();
    }
    
    public void setSupplier(BundleDescription supplier) {
        this.supplier = supplier;
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> getDeclaredDirectives() {
        throw new UnsupportedOperationException();
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> getDeclaredAttributes() {
        throw new UnsupportedOperationException();
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public BundleCapability getCapability() {
        throw new UnsupportedOperationException();
    }

	@Override
	public Object getUserObject() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setUserObject(Object arg0) {
	}

}
