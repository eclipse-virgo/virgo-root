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

package org.eclipse.virgo.apps.admin.web.stubs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.virgo.apps.admin.core.DumpInspectorService;

/**
 */
public class StubDumpInspectorService implements DumpInspectorService{

    private final String[] dummyFiles;
    
    public StubDumpInspectorService(String... dummyFiles) {
        this.dummyFiles = dummyFiles;
    }
    
    /**
     * {@inheritDoc}
     */
    public List<File> findAvaliableDumps() {
        List<File> dumps = new ArrayList<File>();
        for(String dummyFile : this.dummyFiles) {
            dumps.add(new File(dummyFile));
        }
        return dumps;
    }

    /**
     * {@inheritDoc}
     */
	public List<String> getDumpEntries(String dumpID) {
		List<String> entries = new ArrayList<String>();
		entries.add("bar");
		return entries;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getDumpEntry(String dumpID, String entryName) {
		return "baz";
	}
	
}
