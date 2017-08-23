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
package org.eclipse.virgo.kernel.userregion.internal.management;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.equinox.region.Region;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiBundle;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiFramework;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiResolutionFailure;
import org.eclipse.virgo.util.osgi.manifest.BundleManifest;
import org.osgi.framework.BundleException;
import org.osgi.framework.Version;

/**
 *
 */
public class StubQuasiFramework implements QuasiFramework {

	public static final String TEST_BUNDLE_NAME = "foo";
	
	@Override
	public QuasiBundle install(URI location, BundleManifest bundleManifest) throws BundleException {
		return null;
	}

	@Override
	public List<QuasiBundle> getBundles() {
		ArrayList<QuasiBundle> arrayList = new ArrayList<QuasiBundle>();
		arrayList.add(new StubQuasiBundle(TEST_BUNDLE_NAME, Version.emptyVersion, 5l));
		return arrayList;
	}

	@Override
	public QuasiBundle getBundle(long bundleId) {
		return new StubQuasiBundle(TEST_BUNDLE_NAME, Version.emptyVersion, bundleId);
	}

	@Override
	public List<QuasiResolutionFailure> resolve() {
		return new ArrayList<QuasiResolutionFailure>();
	}

	@Override
	public List<QuasiResolutionFailure> diagnose(long bundleId) {
		ArrayList<QuasiResolutionFailure> arrayList = new ArrayList<QuasiResolutionFailure>();
		arrayList.add(new StubQuasiResolutionFailure(new StubQuasiBundle(TEST_BUNDLE_NAME, Version.emptyVersion, bundleId)));
		return arrayList;
	}

	@Override
	public void commit() throws BundleException {
	}

	@Override
	public void destroy() {
	}

	@Override
	public Set<Region> getRegions() {
		return null;
	}

}
