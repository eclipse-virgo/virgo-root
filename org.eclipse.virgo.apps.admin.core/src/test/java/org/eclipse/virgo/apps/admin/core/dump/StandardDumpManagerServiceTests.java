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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.List;

import org.eclipse.virgo.apps.admin.core.dump.StandardDumpInspectorService;
import org.eclipse.virgo.apps.admin.core.stubs.StubDumpPathLocator;
import org.junit.Before;
import org.junit.Test;


/**
 */
public final class StandardDumpManagerServiceTests {

	private StandardDumpInspectorService dumpInspectorService;
	
	private final String DUMP_FOLDER_CONTENT = "testDumpWithContent"; 
	
	private final String DUMP_FOLDER_NO_CONTENT = "testDumpWithNoContent"; 

    private final String DUMP_FOLDER_NOT_EXIST = "notHere"; 
	
	private final String DUMP_ITEM_ONE = "dumpItem1.txt"; 

    private final String DUMP_ITEM_TWO = "dumpItem2.foo"; 
    
    private final String DUMP_ITEM_FAKE = "Not_Here";
	
	private final static String LINE_SEPARATOR = System.getProperty("line.separator");
    
	@Before
	public void setup() {
		this.dumpInspectorService = new StandardDumpInspectorService(new StubDumpPathLocator());
	}
	
	@Test
	public void testFindAvaliableDumps(){
		List<File> result = this.dumpInspectorService.findAvaliableDumps();
		assertNotNull(result);
		assertEquals(2, result.size());
	}
    
    @Test
    public void testGetDumpEntriesNoFolder() {
        List<String> result = this.dumpInspectorService.getDumpEntries(DUMP_FOLDER_NOT_EXIST);
        assertNotNull(result);
        assertEquals(1, result.size());
    }
    
    @Test
    public void testGetDumpEntriesNotExists() {
        List<String> result = this.dumpInspectorService.getDumpEntries(DUMP_FOLDER_NO_CONTENT);
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    public void testGetDumpEntriesExists() {
        List<String> result = this.dumpInspectorService.getDumpEntries(DUMP_FOLDER_CONTENT);
        assertNotNull(result);
        assertEquals(3, result.size());
    }
    
    @Test
    public void testGetDumpEntriesWithNullFolderName() {
        List<String> result = this.dumpInspectorService.getDumpEntries(null);
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testGetEntryNoFolder() {
        String result = this.dumpInspectorService.getDumpEntry(DUMP_FOLDER_NOT_EXIST, DUMP_ITEM_TWO);
        assertEquals("", result); 
    }
    
    @Test
    public void testGetEntryNotExists() {
        String result = this.dumpInspectorService.getDumpEntry(DUMP_FOLDER_NO_CONTENT, DUMP_ITEM_TWO);
        assertEquals("", result); 
    }

    @Test
    public void testGetEntryNotExists2() {
        String result = this.dumpInspectorService.getDumpEntry(DUMP_FOLDER_CONTENT, DUMP_ITEM_FAKE);
        assertEquals("", result); 
    }

    @Test
    public void testGetEntryExists() {
        String result = this.dumpInspectorService.getDumpEntry(DUMP_FOLDER_CONTENT, DUMP_ITEM_ONE);
        assertEquals("Line1" + LINE_SEPARATOR + "Line2" + LINE_SEPARATOR + "Line3" + LINE_SEPARATOR, result); 
    }

    @Test
    public void testGetEntryNullDump() {
        String result = this.dumpInspectorService.getDumpEntry(null, DUMP_ITEM_ONE);
        assertEquals("", result); 
    }

    @Test
    public void testGetEntryNullItem() {
        String result = this.dumpInspectorService.getDumpEntry(DUMP_FOLDER_CONTENT, null);
        assertEquals("", result); 
    }
	
}
