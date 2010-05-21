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

package org.eclipse.virgo.apps.admin.core.state;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipException;

import org.junit.Before;
import org.junit.Test;

import org.eclipse.virgo.apps.admin.core.state.StateDumpExtractor;
import org.eclipse.virgo.apps.admin.core.stubs.StubDumpPathLocator;
import org.eclipse.virgo.apps.admin.core.stubs.StubWorkArea;
import org.eclipse.virgo.kernel.services.work.WorkArea;

/**
 */
public class StateDumpExtractorTests {
    
    private final String DUMP_FOLDER_CONTENT = "testDumpWithContent"; 

    private final String DUMP_FOLDER_NO_CONTENT = "testDumpWithNoContent"; 
    
    private final String DUMP_FOLDER_NOT_EXIST = "notHere"; 
    
    private StateDumpExtractor stateDumpExtractor;
    
    private WorkArea workArea = new StubWorkArea();
    
    @Before
    public void setUp() {            
        StubDumpPathLocator stubDumpPathLocator = new StubDumpPathLocator();
        this.stateDumpExtractor = new StateDumpExtractor(workArea, stubDumpPathLocator);
    }
    
    @Test
    public void getStateDump() throws ZipException, IOException {
        File result = this.stateDumpExtractor.getStateDump(DUMP_FOLDER_CONTENT);
        assertNotNull(result);
        assertEquals("state", result.getName());
    }
    
    @Test(expected=IOException.class)
    public void getStateDumpNoContent() throws ZipException, IOException {
        this.stateDumpExtractor.getStateDump(DUMP_FOLDER_NO_CONTENT);
    }
    
    @Test(expected=IOException.class)
    public void getStateDumpNotExists() throws ZipException, IOException {
        this.stateDumpExtractor.getStateDump(DUMP_FOLDER_NOT_EXIST);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void getStateDumpNull() throws ZipException, IOException {
        this.stateDumpExtractor.getStateDump(null);
    }
    
}
