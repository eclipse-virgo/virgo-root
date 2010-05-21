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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.virgo.apps.admin.web.internal.DumpListFormatterUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import org.eclipse.virgo.apps.admin.core.DumpInspectorService;

/**
 * <p>
 * DumpController handles all requests from the dump inspector page of the admin console
 * </p>
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * DumpController is thread safe
 *
 */
@Controller
public final class DumpController {

	private final DumpInspectorService dumpInspectorService;

    private final DumpListFormatterUtil dumpListFormatterUtil;
	
	private static final int DUMP_TYPE = 1;
	
	private static final int ENTRY_TYPE = 2;

	/**
	 * Simple constructor taking an {@link DumpInspectorService} instance to provide any data required to render requests
	 * @param dumpManagerService for request rendering
	 * @param dumpListFormatterUtil for general list formatting
	 */
	@Autowired
	public DumpController(DumpInspectorService dumpManagerService, DumpListFormatterUtil dumpListFormatterUtil) {
		this.dumpInspectorService = dumpManagerService;
		this.dumpListFormatterUtil = dumpListFormatterUtil;
	}

	/**
	 * Custom handler for displaying the list of available dumps
	 * @return ModelAndView to render
	 */
	@RequestMapping("/dump/inspector.htm")
	public ModelAndView dump()  {
		Map<String, String> dumps = this.dumpListFormatterUtil.getAvaliableDumps();
		List<String> dumpEntries = Collections.emptyList();
		String inspection = "";
		return createContextDumpModelAndView(dumps, null, dumpEntries, null, inspection);		
	}

	/**
	 * Custom handler for displaying the list of available dumps and dump entries
	 * @param request to limit response
	 * @return ModleAndView to render
	 */
	@RequestMapping("/dump/entry.htm")
	public ModelAndView dumpEntry(HttpServletRequest request) {
		String dumpID = ServletRequestUtils.getStringParameter(request, "dumpID", null);
		int requestType = getRequestType(request);
		Map<String, String> dumps = this.dumpListFormatterUtil.getAvaliableDumps();
		
		String dumpEntryName = null;
		List<String> dumpEntries = null;
		String inspection = null;
		
		if(requestType == ENTRY_TYPE || dumpID == null){
			dumpEntryName = ServletRequestUtils.getStringParameter(request, "dumpEntryName", null);
			dumpEntries = this.dumpInspectorService.getDumpEntries(dumpID);
			inspection = this.dumpInspectorService.getDumpEntry(dumpID, dumpEntryName);
		}
		return createContextDumpModelAndView(dumps, dumpID, dumpEntries, dumpEntryName, inspection);
	}

    private ModelAndView createContextDumpModelAndView(Map<String, String> dumps, String selectedDump, List<String> entries, String selectedEntry, String inspection) {
        String formattedSelectedDump = null;
        if(selectedDump != null) {
            formattedSelectedDump = dumps.get(selectedDump);
        }
        return new ModelAndView("dump-overview").addObject("dumps", dumps)
                                                .addObject("selectedDump", selectedDump)
                                                .addObject("formattedSelectedDump", formattedSelectedDump)
                                                .addObject("entries", entries)
                                                .addObject("selectedEntry", selectedEntry)
                                                .addObject("inspection", inspection);
    }
	
	private int getRequestType(HttpServletRequest request) {
		String buttonName;
		try {
			buttonName = ServletRequestUtils.getStringParameter(request, "Operation");
			if ("Select Dump".equals(buttonName)) {
				return DUMP_TYPE;
			} else {
				return ENTRY_TYPE;
			}
		} catch (ServletRequestBindingException e) {
			return DUMP_TYPE;
		}
	}

}
