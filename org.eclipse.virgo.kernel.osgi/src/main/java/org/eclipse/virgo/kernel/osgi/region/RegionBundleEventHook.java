/*******************************************************************************
 * Copyright (c) 2010 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.kernel.osgi.region;

import java.util.Collection;
import java.util.Iterator;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.hooks.bundle.EventHook;

/**
 * TODO Document RegionBundleEventHook
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread safe.
 * 
 */
final class RegionBundleEventHook implements EventHook {

    private final RegionMembership regionMembership;

    public RegionBundleEventHook(RegionMembership regionMembership) {
        this.regionMembership = regionMembership;
    }

    @Override
    public void event(BundleEvent event, Collection<BundleContext> contexts) {
        Bundle sourceBundle = event.getBundle();
        if (sourceBundle.getBundleId() != 0L) {
            boolean eventInRegion = this.regionMembership.contains(sourceBundle);
            Iterator<BundleContext> i = contexts.iterator();
            while (i.hasNext()) {
                Bundle targetBundle = i.next().getBundle();
                if (this.regionMembership.contains(targetBundle) != eventInRegion && targetBundle.getBundleId() != 0L) {
                    i.remove();
                }
            }
        }
    }
}