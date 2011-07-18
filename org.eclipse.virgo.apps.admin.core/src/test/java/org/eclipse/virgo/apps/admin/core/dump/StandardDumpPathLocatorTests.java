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
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MXBean;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.InvalidSyntaxException;

/**
 */
public class StandardDumpPathLocatorTests {

    private final static String FILE_SEPARATOR = System.getProperty("file.separator");
    
    private static final String CONFIG_PROPERTY = "dump.root.directory";

    private static final String MEDIC_MBEAN_QUERY = "org.eclipse.virgo.kernel:type=Configuration,name=org.eclipse.virgo.medic";

    private static final String TEST_DUMPS_FOLDER = "serviceability" + FILE_SEPARATOR + "dumps";
    
    private static final String TEST_DUMP_CONTENT = "testDumpWithContent";
    
    private static final String TEST_DUMP_NO_CONTENT = "testDumpWithNoContent";
    
    private static final String TEST_DUMP_CONTENT_ITEM = "dumpItem2.foo";
    
    private static final String TEST_DUMP_CONTENT_STATE = "osgi.zip";
    
    private StandardDumpPathLocator standardDumpPathLocator;
    
    @Before
    public void setUp() throws Exception {
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        if(!mBeanServer.isRegistered(new ObjectName(MEDIC_MBEAN_QUERY))){
        	mBeanServer.registerMBean(new MedicMXBean(), new ObjectName(MEDIC_MBEAN_QUERY));
        }
		this.standardDumpPathLocator = new StandardDumpPathLocator();
    }

    @Test
    public void testGetDumpDirectory() {
        File dumpDirectory = standardDumpPathLocator.getDumpDirectory();
        assertNotNull(dumpDirectory);
        assertEquals(TEST_DUMPS_FOLDER, dumpDirectory.getPath());
    }

    @Test
    public void testGetDumpDirectoryNoConfig() throws InvalidSyntaxException, MBeanRegistrationException, InstanceNotFoundException, MalformedObjectNameException, NullPointerException {
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        mBeanServer.unregisterMBean(new ObjectName(MEDIC_MBEAN_QUERY));
        standardDumpPathLocator = new StandardDumpPathLocator();
        File dumpDirectory = standardDumpPathLocator.getDumpDirectory();
        assertNull(dumpDirectory);
    }

    @Test
    public void testGetDumpFolder() {
        File dumpFolder = standardDumpPathLocator.getDumpFolder(TEST_DUMP_CONTENT);
        assertNotNull(dumpFolder);
        assertEquals(TEST_DUMPS_FOLDER + FILE_SEPARATOR + TEST_DUMP_CONTENT, dumpFolder.getPath());
    }

    @Test
    public void testGetDumpFolderNotExist() {
        File dumpFolder = standardDumpPathLocator.getDumpFolder("Im not a folder");
        assertNull(dumpFolder);
    }

    @Test
    public void testGetDumpFolderNull() {
        File dumpFolder = standardDumpPathLocator.getDumpFolder(null);
        assertNull(dumpFolder);
    }

    @Test
    public void testGetDumpEntryFileContent() {
        File dumpEntryFile = standardDumpPathLocator.getDumpEntryFile(TEST_DUMP_CONTENT, TEST_DUMP_CONTENT_ITEM);
        assertNotNull(dumpEntryFile);
    }

    @Test
    public void testGetDumpEntryFileNoContent() {
        File dumpEntryFile = standardDumpPathLocator.getDumpEntryFile(TEST_DUMP_NO_CONTENT, TEST_DUMP_CONTENT_STATE);
        assertNull(dumpEntryFile);
    }

    @Test
    public void testGetDumpEntryFileNullFolder() {
        File dumpEntryFile = standardDumpPathLocator.getDumpEntryFile(null, TEST_DUMP_CONTENT);
        assertNull(dumpEntryFile);
    }

    @Test
    public void testGetDumpEntryFile() {
        File dumpEntryFile = standardDumpPathLocator.getDumpEntryFile(TEST_DUMPS_FOLDER, null);
        assertNull(dumpEntryFile);
    }

    public static class MedicMXBean implements MedicMXBeanAPI {
    	public Map<String, String> getProperties(){
    		HashMap<String, String> hashMap = new HashMap<String, String>();
    		hashMap.put(CONFIG_PROPERTY, TEST_DUMPS_FOLDER);
			return hashMap;
    	}
    }

    @MXBean
    public static interface MedicMXBeanAPI {
    	public Map<String, String> getProperties();
    }
    
}
