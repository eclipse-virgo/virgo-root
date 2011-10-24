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

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.virgo.medic.dump.DumpGenerator;
import org.eclipse.virgo.medic.impl.config.ConfigurationProvider;
import org.eclipse.virgo.util.io.FileSystemUtils;;

/**
 * 
 * Utility class for the viewing of produced Dumps.
 * 
 * This class is thread safe
 */
public class FileSystemDumpInspector implements DumpInspector {

    private final Logger logger = LoggerFactory.getLogger(FileSystemDumpInspector.class);

	private final DumpGenerator generator;

	private final ConfigurationProvider configurationProvider;
    
    /**
     * 
     * 
     */
    public FileSystemDumpInspector(DumpGenerator generator, ConfigurationProvider configurationProvider) {
		this.generator = generator;
		this.configurationProvider = configurationProvider;
	}
	
	@Override
	public String[] getDumps() throws IOException {
		File dumpDir = getDumpDirectory();
		if(dumpDir != null && dumpDir.exists() && dumpDir.isDirectory()){
			return FileSystemUtils.list(dumpDir, this.logger);
		} else {
			return new String[0];
		}
	}
	
	@Override
	public String[] getDumpEntries(String dumpId) throws IOException {
		if(dumpId == null){
			return new String[0];
		}
		File dumpDir = new File(getDumpDirectory(), dumpId);
		if(dumpDir != null && dumpDir.exists() && dumpDir.isDirectory()){
			return FileSystemUtils.list(dumpDir, this.logger);
		} else {
			return new String[0];
		}
	}

	@Override
	public String[] getDumpEntry(String dumpId, String entryName) {
		if(dumpId == null || entryName == null){
			return new String[0];
		}
		List<String> lines = new ArrayList<String>();
		File dumpDir = getDumpDirectory();
		if(dumpDir != null && dumpDir.exists() && dumpDir.isDirectory()){
			File dumpEntry = new File(dumpDir, dumpId + File.separatorChar + entryName);
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
		}
		return lines.toArray(new String[lines.size()]);
	}
	
	@Override
	public void createDump() {
		generator.generateDump("Generated via JMX");
	}

	@Override
	public void delete(String dumpId) {
		File dumpDir = getDumpDirectory();
		if(dumpDir.exists() && dumpDir.isDirectory()){
			File root = new File(dumpDir, dumpId);
			if(root.exists() && root.isDirectory()){
				FileSystemUtils.deleteRecursively(root);
			}
		}
	}

	private File getDumpDirectory(){
		String dumpDirectory = configurationProvider.getConfiguration().get(ConfigurationProvider.KEY_DUMP_ROOT_DIRECTORY);
		File dumpDir = new File(dumpDirectory);
		if(dumpDir.exists() && dumpDir.isDirectory()){
			return dumpDir;
		}
		return null;
	}
	
	private String escapeAngleBrackets(String unfriendlyMarkup) {
		String processed = unfriendlyMarkup.replace("<", "&#60;");
		processed = processed.replace(">", "&#62;");
		processed = processed.replace("¡", "&#8734;");
		return processed;
	}
	
}
