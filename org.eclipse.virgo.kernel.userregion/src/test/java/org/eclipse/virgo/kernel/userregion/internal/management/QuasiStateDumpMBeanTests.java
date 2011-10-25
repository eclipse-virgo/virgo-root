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
public class QuasiStateDumpMBeanTests {

	private final QuasiStateDumpMBean quasiStateDumpMBean;
	
	public QuasiStateDumpMBeanTests() {
		quasiStateDumpMBean = new QuasiStateDumpMBean(new QuasiFrameworkFactory() {
			
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
	public void testGetSummary(){
		String[] summary = this.quasiStateDumpMBean.getSummary("src/test/resources/testDump");
		for (String string : summary) {
			System.out.println(string);
		}
		assertEquals(4, summary.length);
		assertEquals("Bundle: foo-0.0.0", summary[0]);
		assertEquals("    Description", summary[1]);
		assertEquals("", summary[2]);
		assertEquals("", summary[3]);
	}
	
}
