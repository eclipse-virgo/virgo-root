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

package org.eclipse.virgo.test.framework;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URI;

import org.eclipse.virgo.test.framework.BundleLocationLocator;
import org.junit.Test;
import org.osgi.framework.launch.Framework;

public class BundleLocationLocatorTests {

	@Test
	public void testFileLocation() throws Exception {
		String location = BundleLocationLocator.determineBundleLocation(BundleLocationLocatorTests.class);
		URI uri = new URI(location);
		assertTrue(new File(uri).exists());
	}
	
	@Test
	public void testJarLocation() throws Exception {
		String location = BundleLocationLocator.determineBundleLocation(Framework.class);
		URI uri = new URI(location);
		assertTrue(new File(uri).exists());
	}
}
