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

import org.eclipse.osgi.service.resolver.BaseDescription;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.osgi.framework.Version;


/**
 * TODO Document StubBaseDescription
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * TODO Document concurrent semantics of StubBaseDescription
 *
 */
public class StubBaseDescription implements BaseDescription {

    private BundleDescription supplier;

    /** 
     * {@inheritDoc}
     */
    public String getName() {
        throw new UnsupportedOperationException();
    }

    /** 
     * {@inheritDoc}
     */
    public BundleDescription getSupplier() {
        return this.supplier;
    }

    /** 
     * {@inheritDoc}
     */
    public Version getVersion() {
        throw new UnsupportedOperationException();
    }
    
    public void setSupplier(BundleDescription supplier) {
        this.supplier = supplier;
    }

}
