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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.virgo.test.framework.OsgiTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleReference;


@RunWith(OsgiTestRunner.class)
public class BasicOsgiEnvironmentTests {

	@Test
	public void testRunningInOsgi() {
		assertTrue((getClass().getClassLoader() instanceof BundleReference));
	}
	
	@Test
	public void testCanGetBundleContext() {
		BundleReference br = (BundleReference)getClass().getClassLoader();
		Bundle bundle = br.getBundle();
		
		assertNotNull(bundle);
		assertNotNull(bundle.getBundleContext());
	}
	
	@Test
	public void testCustomConfigurationFileApplied() {
		BundleReference br = (BundleReference)getClass().getClassLoader();
		Bundle bundle = br.getBundle();
		
		String property = bundle.getBundleContext().getProperty("test.property");
		assertEquals("foo", property);
	}
}
