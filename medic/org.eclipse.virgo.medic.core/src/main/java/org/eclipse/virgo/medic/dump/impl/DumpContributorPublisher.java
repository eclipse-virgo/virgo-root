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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.virgo.medic.dump.DumpContributor;
import org.eclipse.virgo.medic.dump.impl.heap.HeapDumpContributor;
import org.eclipse.virgo.medic.dump.impl.summary.SummaryDumpContributor;
import org.eclipse.virgo.medic.dump.impl.thread.ThreadDumpContributor;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public final class DumpContributorPublisher {

    private static final String SUPPRESS_HEAP_DUMPS = "org.eclipse.virgo.suppress.heap.dumps";

    private final List<ServiceRegistration<DumpContributor>> contributorRegistrations = new ArrayList<ServiceRegistration<DumpContributor>>();

    private final BundleContext bundleContext;

    public DumpContributorPublisher(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public void publishDumpContributors() {
        publishDumpContributor(new SummaryDumpContributor());
        if("false".equalsIgnoreCase(this.bundleContext.getProperty(SUPPRESS_HEAP_DUMPS))){
            publishDumpContributor(new HeapDumpContributor());
        }
        publishDumpContributor(new ThreadDumpContributor());
    }

    @SuppressWarnings("unchecked")
	private void publishDumpContributor(DumpContributor dumpContributor) {
        ServiceRegistration<DumpContributor> registration = (ServiceRegistration<DumpContributor>)this.bundleContext.registerService(DumpContributor.class.getName(), dumpContributor, null);
        this.contributorRegistrations.add(registration);
    }

    public void retractDumpContributors() {        
        for (ServiceRegistration<DumpContributor> registration : this.contributorRegistrations) {
            registration.unregister();
        }
        
        this.contributorRegistrations.clear();
    }
}
