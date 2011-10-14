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

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Utility class for the viewing of produced Dumps.
 * 
 * This class is thread safe
 */
public class FileSystemDumpInspector implements DumpInspector {

    private final Logger logger = LoggerFactory.getLogger(FileSystemDumpInspector.class);
	
    private final File dumpDirectory;
    
    /**
     * 
     * 
     */
    public FileSystemDumpInspector(String dumpDir) {
		this.dumpDirectory = new File(dumpDir);
	}
	
	@Override
	public String[] getDumps() throws IOException {
		if(this.dumpDirectory.exists() && this.dumpDirectory.isDirectory()){
			return getFileNames(MedicMBeanExporter.listFiles(this.dumpDirectory, this.logger));
		} else {
			return new String[0];
		}
	}
	
	@Override
	public String[] getDumpEntryNames(String dumpId) throws IOException {
		File dumpDir = new File(this.dumpDirectory, dumpId);
		String[] items;
		if(dumpDir.exists() && dumpDir.isDirectory()){
			items = getFileNames(MedicMBeanExporter.listFiles(dumpDir, this.logger));
		} else {
			items = new String[0];
		}
		return items;
	}

	@Override
	public String[] getDumpEntry(String dumpId, String entryName) {
		if(dumpId == null || entryName == null){
			return new String[0];
		}
		List<String> lines = new ArrayList<String>();
		File dumpEntry = new File(this.dumpDirectory, dumpId + File.separatorChar + entryName);
		if(dumpEntry != null){
			LineNumberReader reader = null;
			try {
				reader = new LineNumberReader(new FileReader(dumpEntry));
				while (reader.ready()){
					String rawLine = reader.readLine();
					if(rawLine != null){
						lines.add(this.escapeAngleBrackets(rawLine));
					}
				}
				reader.close();
			} catch (IOException e) {
				logger.error("Error while reading dump file " + dumpEntry.getPath(), e);
				try {
					if(reader != null){
						reader.close();
					}
				} catch (IOException e1) {
					// no-op to close stream
				}
				// no-op just return the default null value and let the js deal with it
			}
		}
		return lines.toArray(new String[lines.size()]);
	}
	
	@Override
	public void createDump() {
	}

	@Override
	public void delete(String dumpId) {
		MedicMBeanExporter.doRecursiveDelete(new File(this.dumpDirectory, dumpId));
	}

	private String escapeAngleBrackets(String unfriendlyHTML) {
		String processed = unfriendlyHTML.replace("<", "&#60;");
		processed = processed.replace(">", "&#62;");
		processed = processed.replace("¡", "&infin;");
		return processed;
	}
	
	private String[] getFileNames(File[] files){
		String[] fileNames = new String[files.length];
		for(int i = 0; i<files.length; i++){
			fileNames[i] = files[i].getName();

		}
		return fileNames;
	}
	
}
