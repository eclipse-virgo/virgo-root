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

package org.eclipse.virgo.medic.dump.impl;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.virgo.medic.dump.DumpContributor;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;


final class DumpContributorTracker implements ServiceTrackerCustomizer {

    private final List<DumpContributor> contributors = new CopyOnWriteArrayList<DumpContributor>();

    private final BundleContext bundleContext;

    DumpContributorTracker(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public Object addingService(ServiceReference reference) {
        Object service = bundleContext.getService(reference);
        contributors.add((DumpContributor) service);
        return service;
    }

    public void modifiedService(ServiceReference reference, Object service) {
    }

    public void removedService(ServiceReference reference, Object service) {
        this.contributors.remove(service);
        this.bundleContext.ungetService(reference);
    }

    List<DumpContributor> getDumpContributors() {
        return this.contributors;
    }
}
