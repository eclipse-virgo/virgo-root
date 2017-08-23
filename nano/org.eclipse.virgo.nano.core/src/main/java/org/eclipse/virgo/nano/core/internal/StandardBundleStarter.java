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

package org.eclipse.virgo.nano.core.internal;

import org.eclipse.virgo.nano.core.AbortableSignal;
import org.eclipse.virgo.nano.core.BundleStarter;
import org.eclipse.virgo.nano.core.BundleUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;


/**
 * Standard implementation of {@link BundleStarter} that starts the bundle and delegates to a {@link BundleStartTracker}
 * to track any asynchronous portion of the bundle's startup.
 * <p/>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread-safe.
 * 
 */
final class StandardBundleStarter implements BundleStarter {

    private final BundleStartTracker bundleStartTracker;

    private static final int DEFAULT_START_OPTIONS = 0;

    public StandardBundleStarter(BundleStartTracker bundleStartTracker) {
        this.bundleStartTracker = bundleStartTracker;
    }

    /**
     * {@inheritDoc}
     */
    public void start(Bundle bundle, AbortableSignal signal) throws BundleException {
        start(bundle, DEFAULT_START_OPTIONS, signal);
    }

    /**
     * {@inheritDoc}
     */
    public void start(Bundle bundle, int options, AbortableSignal signal) throws BundleException {
        
        trackStart(bundle, signal);
        
        if (bundleNeedsStarting(bundle)) {
            try {
                bundle.start(options);
            } catch (BundleException be) {
                this.bundleStartTracker.cleanup(bundle, false, be);
                throw be;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void trackStart(Bundle bundle, AbortableSignal signal) {
        if (BundleUtils.isFragmentBundle(bundle)) {
            throw new IllegalArgumentException("A fragment bundle cannot be started and so start cannot be tracked");
        }
        
        if (signal != null) {
            this.bundleStartTracker.trackStart(bundle, signal);
        }
    }

    private static boolean bundleNeedsStarting(Bundle bundle) {
        if (bundle != null) {
            int bundleState = bundle.getState();
            return (bundleState != Bundle.STARTING && bundleState != Bundle.ACTIVE);
        }
        return false;
    }
    
}
