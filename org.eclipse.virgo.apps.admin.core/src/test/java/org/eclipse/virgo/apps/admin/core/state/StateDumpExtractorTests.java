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

import org.eclipse.virgo.apps.admin.core.stubs.StubDumpPathLocator;
import org.junit.Before;
import org.junit.Test;

public class StateDumpExtractorTests {
    
    private final String DUMP_FOLDER_CONTENT = "testDumpWithContent"; 
    
    private final String DUMP_FOLDER_NOT_EXIST = "notHere"; 
    
    private StandardDumpLocator stateDumpExtractor;
    
    @Before
    public void setUp() {            
        StubDumpPathLocator stubDumpPathLocator = new StubDumpPathLocator();
        this.stateDumpExtractor = new StandardDumpLocator(stubDumpPathLocator);
    }
    
    @Test
    public void getStateDump() throws ZipException, IOException {
        File result = this.stateDumpExtractor.getDumpDir(DUMP_FOLDER_CONTENT);
        assertNotNull(result);
        assertEquals(DUMP_FOLDER_CONTENT, result.getName());
    }
    
    @Test(expected=IOException.class)
    public void getStateDumpNotExists() throws ZipException, IOException {
        this.stateDumpExtractor.getDumpDir(DUMP_FOLDER_NOT_EXIST);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void getStateDumpNull() throws ZipException, IOException {
        this.stateDumpExtractor.getDumpDir(null);
    }
    
}
