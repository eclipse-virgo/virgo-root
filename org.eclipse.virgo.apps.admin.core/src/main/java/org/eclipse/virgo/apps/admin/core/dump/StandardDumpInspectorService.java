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

package org.eclipse.virgo.apps.admin.core.dump;

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.virgo.apps.admin.core.DumpInspectorService;
import org.eclipse.virgo.util.io.FileSystemUtils;



/**
 * <p>
 * StandardDumpInspectorService is an implementation of {@link DumpInspectorService} 
 * that uses a {@link DumpPathLocator} to find dumps that the system has produced.
 * </p>
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * StandardDumpInspectorService is threadsafe
 *
 */
final class StandardDumpInspectorService implements DumpInspectorService {
	
	private final static String LINE_SEPARATOR = System.getProperty("line.separator");
    
    private final DumpPathLocator dumpPathLocator;
	
    public StandardDumpInspectorService(DumpPathLocator dumpPathLocator) {
        this.dumpPathLocator = dumpPathLocator;

    }
	
	/**
	 * {@inheritDoc}
	 */
	public List<File> findAvaliableDumps() {
		List<File> dumps = new ArrayList<File>();
		File dumpDir = this.dumpPathLocator.getDumpDirectory();
		if(dumpDir != null){			
			File[] dumpFolders = FileSystemUtils.listFiles(dumpDir, new FileFilter(){

				public boolean accept(File pathname) {
					return pathname.isDirectory();
				}
				
			});
			if(dumpFolders.length > 0){
				for(File dumpFolder : dumpFolders){
					dumps.add(dumpFolder);
				}
			}
		}
		return dumps;
	}

    /**
     * {@inheritDoc}
     */
	public List<String> getDumpEntries(String dumpID) {
		if(dumpID == null){
			return Collections.emptyList();
		}
		List<String> dumpEntries = new ArrayList<String>();
		File dumpDir = this.dumpPathLocator.getDumpFolder(dumpID);
		if(dumpDir == null){
			dumpEntries.add(String.format("No Entries have been found for '%s'", dumpID));
		}else{
			String[] dumpEntriesArray = FileSystemUtils.list(dumpDir);
			if(dumpEntriesArray.length ==0){
				dumpEntries.add(String.format("No Entries have been found for '%s'", dumpID));
			} else {
				dumpEntries.addAll(Arrays.asList(dumpEntriesArray));
			}
		}
		return dumpEntries;
	}

    /**
     * {@inheritDoc}
     */
	public String getDumpEntry(String dumpID, String entryName) {
		if(dumpID == null || entryName == null){
			return "";
		}
		StringBuilder inspection = new StringBuilder();
		File dumpEntry = this.dumpPathLocator.getDumpEntryFile(dumpID, entryName);
		if(dumpEntry != null){
			LineNumberReader reader = null;
			try {
				reader = new LineNumberReader(new FileReader(dumpEntry));
				while (reader.ready()){
					String rawLine = reader.readLine();
					if(rawLine != null){
						inspection.append(this.escapeAngleBrackets(rawLine));
						inspection.append(LINE_SEPARATOR);
					}
				}
				reader.close();
			} catch (IOException e) {
				try {
					if(reader != null){
						reader.close();
					}
				} catch (IOException e1) {
					// no-op to close stream
				}
				// no-op just return the default null value and let the jsp deal with it
			}
		}
		return inspection.toString();
	}

	private String escapeAngleBrackets(String unfriendlyHTML) {
		String processed = unfriendlyHTML.replace("<", "&#60;");
		processed = processed.replace(">", "&#62;");
		processed = processed.replace("°", "&infin;");
		return processed;
	}

}
