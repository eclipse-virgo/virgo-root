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
import static org.junit.Assert.assertNull;

import java.io.File;

import org.eclipse.virgo.teststubs.osgi.framework.StubBundle;
import org.eclipse.virgo.teststubs.osgi.framework.StubBundleContext;
import org.eclipse.virgo.teststubs.osgi.service.cm.StubConfigurationAdmin;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.Version;
import org.osgi.service.cm.ConfigurationAdmin;

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

	private StubBundleContext context;

	private StubBundleContext systemBundleContext;

	private ServiceRegistration<ConfigurationAdmin> registerService;
    
    @Before
    public void setUp() throws Exception {
    	StubConfigurationAdmin stubConfigurationAdmin = new StubConfigurationAdmin();
        stubConfigurationAdmin.createConfiguration(CONFIG_POINT).addProperty(CONFIG_PROPERTY, TEST_DUMPS_FOLDER);
        
        this.context = new StubBundleContext();
        StubBundle systemBundle = new StubBundle(0l, "org.osgi.framework", new Version("4.2"),"");
        systemBundleContext = new StubBundleContext(systemBundle);
        systemBundle.setBundleContext(systemBundleContext);
        registerService = systemBundleContext.registerService(ConfigurationAdmin.class, stubConfigurationAdmin, null);
        
		this.context.addInstalledBundle(systemBundle);
		this.standardDumpPathLocator = new StandardDumpPathLocator(this.context);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testNullConstructor() throws InvalidSyntaxException{
        this.standardDumpPathLocator = new StandardDumpPathLocator(null);
    }

    @Test
    public void testGetDumpDirectory() {
        File dumpDirectory = this.standardDumpPathLocator.getDumpDirectory();
        assertNotNull(dumpDirectory);
        assertEquals(TEST_DUMPS_FOLDER, dumpDirectory.getPath());
    }

    @Test
    public void testGetDumpDirectoryNoConfig() throws InvalidSyntaxException {
    	this.systemBundleContext.removeRegisteredService(registerService);
        systemBundleContext.registerService(ConfigurationAdmin.class, new StubConfigurationAdmin(), null);
        this.standardDumpPathLocator = new StandardDumpPathLocator(this.context);
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
