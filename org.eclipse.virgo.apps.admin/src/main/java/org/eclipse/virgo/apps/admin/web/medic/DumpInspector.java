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
package org.eclipse.virgo.apps.admin.web.medic;

import java.io.IOException;
import java.text.ParseException;

import javax.management.MXBean;

/**
 * 
 * Implementations should be thread safe
 *
 */
@MXBean
public interface DumpInspector {
	
	String[] getDumps() throws IOException, ParseException;
	
	String[] getDumpEntryNames(String dumpId) throws IOException;
	
	String[] getDumpEntry(String dumpId, String item);

	void createDump();
	
	void delete(String dumpId);
}
