/*******************************************************************************
 * Copyright (c) 2008, 2011 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *******************************************************************************/
package org.eclipse.virgo.kernel.userregion.internal.management;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipException;

import org.eclipse.virgo.kernel.osgi.quasi.QuasiFramework;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiFrameworkFactory;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * 
 *
 */
public class JMXQuasiStateDumpTests {

	private final JMXQuasiStateDump quasiStateDumpMBean;
	
	public JMXQuasiStateDumpTests() {
		quasiStateDumpMBean = new JMXQuasiStateDump(new QuasiFrameworkFactory() {
			
			@Override
			public QuasiFramework create(File stateDump) throws ZipException, IOException {
				return new StubQuasiFramework();
			}
			
			@Override
			public QuasiFramework create() {
				return new StubQuasiFramework();
			}
		});
	}
	
	@Test
	public void testGetUnresolvedBundleIds(){

	}
	
	@Test
	public void testListBundles(){

	}
	
	@Test
	public void testGetBundle(){

	}
	
}
