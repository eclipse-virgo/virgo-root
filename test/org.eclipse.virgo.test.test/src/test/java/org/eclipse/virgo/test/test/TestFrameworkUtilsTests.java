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

import static org.junit.Assert.assertNotNull;

import org.eclipse.virgo.test.framework.OsgiTestRunner;
import org.eclipse.virgo.test.framework.TestFrameworkUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.BundleContext;


@RunWith(OsgiTestRunner.class)
public class TestFrameworkUtilsTests {

	@Test
	public void testGetBundleContextForTestClass() {
		BundleContext bundleContext = TestFrameworkUtils.getBundleContextForTestClass(getClass());
		assertNotNull(bundleContext);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testGetBundleContextForNonTestClass() {
		TestFrameworkUtils.getBundleContextForTestClass(String.class);
	}
}
