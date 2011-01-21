/*
 * This file is part of the Eclipse Virgo project.
 *
 * Copyright (c) 2011 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    VMware Inc. - initial contribution
 */

package org.eclipse.virgo.kernel.osgi.region.hook;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.virgo.kernel.osgi.region.RegionDigraph;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.hooks.bundle.EventHook;
import org.osgi.framework.hooks.bundle.FindHook;

/**
 * {@link RegionBundleEventHook} manages the visibility of bundle events across regions according to the
 * {@link RegionDigraph}.
 * <p>
 * The current implementation delegates to {@link RegionBundleFindHook}. This is likely to perform adequately
 * because of the low frequency of bundle events and the typically small number of bundle listeners.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * Thread safe.
 */
final class RegionBundleEventHook implements EventHook {

    private final FindHook bundleFindHook;

    RegionBundleEventHook(FindHook bundleFindBook) {
        this.bundleFindHook = bundleFindBook;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void event(BundleEvent event, Collection<BundleContext> contexts) {
        Bundle eventBundle = event.getBundle();
        Iterator<BundleContext> i = contexts.iterator();
        while (i.hasNext()) {
            if (!find(i.next(), eventBundle)) {
                i.remove();
            }
        }
    }

    private boolean find(BundleContext finderBundleContext, Bundle candidateBundle) {
        Set<Bundle> candidates = new HashSet<Bundle>();
        candidates.add(candidateBundle);
        this.bundleFindHook.find(finderBundleContext, candidates);
        return !candidates.isEmpty();
    }

}
