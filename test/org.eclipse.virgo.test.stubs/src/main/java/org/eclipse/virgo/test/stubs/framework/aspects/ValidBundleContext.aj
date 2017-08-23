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

package org.eclipse.virgo.test.stubs.framework.aspects;

import org.osgi.framework.Bundle;

import org.eclipse.virgo.test.stubs.framework.StubBundleContext;

/**
 * Ensures that a bundle context is in {@link Bundle#STARTING}, {@link Bundle#ACTIVE}, or {@link Bundle#STOPPING} before
 * method execution
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe
 * 
 */
public aspect ValidBundleContext {

    /**
     * Ensures that a {@link BundleContext} is in a valid state (i.e. {@link Bundle.STARTING}, {@link Bundle.ACTIVE}, or
     * {@link Bundle.STOPPING}) before allowing method invocation
     * 
     * @param bundleContext The {@link Bundle} to check
     * @throws IllegalStateException if the {@link BundleContext} is not in valid state
     */
    before(StubBundleContext bundleContext) : 
            this(bundleContext) &&
            within(org.eclipse.virgo.test.stubs.framework.StubBundleContext) &&
            execution(* org.osgi.framework.BundleContext.*(..)) {
        int state = bundleContext.getContextBundle().getState();
        if (state != Bundle.STARTING && state != Bundle.ACTIVE && state != Bundle.STOPPING) {
            throw new IllegalStateException("This context is no longer valid");
        }
    }
}
