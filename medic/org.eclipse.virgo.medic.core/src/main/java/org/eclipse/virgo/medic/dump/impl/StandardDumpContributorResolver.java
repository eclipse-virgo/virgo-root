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

import org.eclipse.virgo.medic.dump.DumpContributor;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

public final class StandardDumpContributorResolver implements
		DumpContributorResolver {

	private final ServiceTracker<DumpContributor, DumpContributor> serviceTracker;

	private final DumpContributorTracker dumpContributorTracker;

	public StandardDumpContributorResolver(BundleContext bundleContext) {
		this.dumpContributorTracker = new DumpContributorTracker(bundleContext);

		this.serviceTracker = new ServiceTracker<DumpContributor, DumpContributor>(
				bundleContext, DumpContributor.class.getName(),
				this.dumpContributorTracker);
		this.serviceTracker.open();
	}

	public List<DumpContributor> getDumpContributors() {
		return this.dumpContributorTracker.getDumpContributors();
	}

	public void close() {
		this.serviceTracker.close();
	}
}
