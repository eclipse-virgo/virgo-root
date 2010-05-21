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

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import org.eclipse.virgo.apps.admin.core.dump.StandardDumpPathLocator;
import org.eclipse.virgo.teststubs.osgi.service.cm.StubConfiguration;
import org.eclipse.virgo.teststubs.osgi.service.cm.StubConfigurationAdmin;

/**
 */
public class StandardDumpPathLocatorTests {

    private final static String FILE_SEPARATOR = System.getProperty("file.separator");
    
    private static final String CONFIG_POINT = "org.eclipse.virgo.medic";
    
    private static final String CONFIG_PROPERTY = "dump.root.directory";
    
    private static final String TEST_DUMPS_FOLDER = "serviceability" + FILE_SEPARATOR + "dumps";
    
    private static final String TEST_DUMP_CONTENT = "testDumpWithContent";
    
    private static final String TEST_DUMP_NO_CONTENT = "testDumpWithNoContent";
    
    private static final String TEST_DUMP_CONTENT_ITEM = "dumpItem2.foo";
    
    private static final String TEST_DUMP_CONTENT_STATE = "osgi.zip";
    
    private StandardDumpPathLocator standardDumpPathLocator;
    
    @Before
    public void setUp() throws Exception {
        StubConfigurationAdmin stubConfigurationAdmin = new StubConfigurationAdmin();
        StubConfiguration createConfiguration = stubConfigurationAdmin.createConfiguration(CONFIG_POINT);
        createConfiguration.addProperty(CONFIG_PROPERTY, TEST_DUMPS_FOLDER);
        this.standardDumpPathLocator = new StandardDumpPathLocator(stubConfigurationAdmin);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testNullConstructor(){
        this.standardDumpPathLocator = new StandardDumpPathLocator(null);
    }

    @Test
    public void testGetDumpDirectory() {
        File dumpDirectory = this.standardDumpPathLocator.getDumpDirectory();
        assertNotNull(dumpDirectory);
        assertEquals(TEST_DUMPS_FOLDER, dumpDirectory.getPath());
    }

    @Test
    public void testGetDumpDirectoryNoConfig() {
        this.standardDumpPathLocator = new StandardDumpPathLocator(new StubConfigurationAdmin());
        File dumpDirectory = this.standardDumpPathLocator.getDumpDirectory();
        assertNull(dumpDirectory);
    }

    @Test
    public void testGetDumpFolder() {
        File dumpFolder = this.standardDumpPathLocator.getDumpFolder(TEST_DUMP_CONTENT);
        assertNotNull(dumpFolder);
        assertEquals(TEST_DUMPS_FOLDER + FILE_SEPARATOR + TEST_DUMP_CONTENT, dumpFolder.getPath());
    }

    @Test
    public void testGetDumpFolderNotExist() {
        File dumpFolder = this.standardDumpPathLocator.getDumpFolder("Im not a folder");
        assertNull(dumpFolder);
    }

    @Test
    public void testGetDumpFolderNull() {
        File dumpFolder = this.standardDumpPathLocator.getDumpFolder(null);
        assertNull(dumpFolder);
    }

    @Test
    public void testGetDumpEntryFileContent() {
        File dumpEntryFile = this.standardDumpPathLocator.getDumpEntryFile(TEST_DUMP_CONTENT, TEST_DUMP_CONTENT_ITEM);
        assertNotNull(dumpEntryFile);
    }

    @Test
    public void testGetDumpEntryFileNoContent() {
        File dumpEntryFile = this.standardDumpPathLocator.getDumpEntryFile(TEST_DUMP_NO_CONTENT, TEST_DUMP_CONTENT_STATE);
        assertNull(dumpEntryFile);
    }

    @Test
    public void testGetDumpEntryFileNullFolder() {
        File dumpEntryFile = this.standardDumpPathLocator.getDumpEntryFile(null, TEST_DUMP_CONTENT);
        assertNull(dumpEntryFile);
    }

    @Test
    public void testGetDumpEntryFile() {
        File dumpEntryFile = this.standardDumpPathLocator.getDumpEntryFile(TEST_DUMPS_FOLDER, null);
        assertNull(dumpEntryFile);
    }

}
