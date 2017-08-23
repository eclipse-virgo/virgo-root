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

package org.eclipse.virgo.test.test;

import static org.junit.Assert.assertTrue;

import org.eclipse.virgo.test.framework.BundleDependencies;
import org.eclipse.virgo.test.framework.BundleEntry;
import org.eclipse.virgo.test.framework.OsgiTestRunner;
import org.eclipse.virgo.test.framework.TestFrameworkUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;


@BundleDependencies(entries = {@BundleEntry("file:./src/test/resources/test-bundle")})
@RunWith(OsgiTestRunner.class)
public class BundleDependenciesTests {

	@Test
	public void testBundleInstalled() {
		BundleContext bundleContext = TestFrameworkUtils.getBundleContextForTestClass(getClass());
		Bundle[] bundles = bundleContext.getBundles();
		boolean found = false;
		for (Bundle bundle : bundles) {
			if("test.bundle".equals(bundle.getSymbolicName()))  {
			    found = true;
			    break;
			}
		}
		assertTrue(found);
	}
}
