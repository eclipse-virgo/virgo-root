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

package org.eclipse.virgo.apps.admin.web;

import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.eclipse.virgo.apps.admin.web.DumpController;
import org.eclipse.virgo.apps.admin.web.internal.DumpListFormatterUtil;
import org.eclipse.virgo.apps.admin.web.stubs.StubDumpInspectorService;
import org.eclipse.virgo.apps.admin.web.stubs.StubDumpListFormatterUtil;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import org.eclipse.virgo.apps.admin.core.DumpInspectorService;

/**
 *
 */
public class DumpControllerTests {

	private DumpController dumpManagerController;
	
	private DumpInspectorService stubDumpManagerService = new StubDumpInspectorService();
	
	private DumpListFormatterUtil stubDumpListFormatterUtil = new StubDumpListFormatterUtil();
	
	private MockHttpServletRequest request;
	
	
	@Before
	public void setup(){
		this.dumpManagerController = new DumpController(this.stubDumpManagerService, this.stubDumpListFormatterUtil);
		this.request = new MockHttpServletRequest();	
	}
	
	/**
	 * Custom handler for undeploying an application.
	 */
	@Test
	public void testDump() {
		ModelAndView mav = this.dumpManagerController.dump();
		Map<String, Object> model = mav.getModel();

		assertTrue(model.containsKey("dumps"));
		assertTrue(model.containsKey("selectedDump"));
		assertTrue(model.containsKey("entries"));
		assertTrue(model.containsKey("selectedEntry"));
		assertTrue(model.containsKey("inspection"));
	}

	/**
	 * Custom handler for undeploying an application.
	 */
	@Test
	public void testDumpEntry() {
		ModelAndView mav = this.dumpManagerController.dumpEntry(request);
		Map<String, Object> model = mav.getModel();

		assertTrue(model.containsKey("dumps"));
		assertTrue(model.containsKey("selectedDump"));
		assertTrue(model.containsKey("entries"));
		assertTrue(model.containsKey("selectedEntry"));
		assertTrue(model.containsKey("inspection"));
	}

}
