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
package org.eclipse.virgo.medic.management;

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
	
	/**
	 * Return the currently configured dump directory
	 * 
	 * @return 
	 */
	String getConfiguredDumpDirectory();
	
	/**
	 * An array of the available dump ids
	 * 
	 * @return array of dump ids
	 * @throws IOException
	 * @throws ParseException
	 */
	String[] getDumps() throws IOException, ParseException;
	
	/**
	 * An array of the available dump items for the given dump with each array 
	 * containing an array in the order of display name, bean to use for displaying 
	 * the item (optional). When not given, this bean should be used.
	 * 
	 * @param dumpId
	 * @return array of dump entries
	 * @throws IOException
	 */
	String[][] getDumpEntries(String dumpId) throws IOException;
	
	/**
	 * Return the requested dump item as an array of Strings, one per line
	 * 
	 * @param dumpId
	 * @param item
	 * @return array of lines from the dump file
	 */
	String[] getDumpEntry(String dumpId, String item);

	/**
	 * Create a new Dump
	 * 
	 */
	void createDump();
	
	/**
	 * Delete the given dump from the file system
	 * 
	 * @param dumpId
	 */
	void deleteDump(String dumpId);
}
