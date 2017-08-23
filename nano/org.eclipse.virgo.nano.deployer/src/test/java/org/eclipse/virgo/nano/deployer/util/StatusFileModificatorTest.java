/*******************************************************************************
 * Copyright (c) 2012 SAP AG
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   SAP AG - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.nano.deployer.util;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.eclipse.virgo.util.io.PathReference;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class StatusFileModificatorTest {

    private static final PathReference PICKUP_DIR = new PathReference("build/pickup");

    private static final PathReference STATE_DIR = new PathReference("build/pickup/.state");

    private static final String[] dummyStatusFiles = { "src/test/resources/test/testapp1.deploy.ok", "src/test/resources/test/testapp1.undeploy.ok",
        "src/test/resources/test/testapp1.deploy.error", "src/test/resources/test/testapp1.undeploy.error",
        "src/test/resources/test/testapp2.deploy.ok" };

    private static final String[] strangeStatusFiles = { "src/test/resources/test/test#strangesymbols.undeploy.ok", 
    	"src/test/resources/test/test#strangesymbols.deploy.ok", "src/test/resources/test/teststrangesymbols.undeploy.ok"};
    
    @Before
    public void setUp() {
        PICKUP_DIR.createDirectory();
    }

    @After
    public void cleanUp() {
        PICKUP_DIR.delete(true);
    }

    @Test
    public void deleteStatusFileTest() throws Exception {
        STATE_DIR.createDirectory();
        copyDummyStatusFiles();
        File stateFolder = STATE_DIR.toFile();
        File pickupFolder = PICKUP_DIR.toFile();
        assertTrue(stateFolder.list().length == 5);
        StatusFileModificator.deleteStatusFile("testapp1", pickupFolder);
        assertTrue(stateFolder.list().length == 1);
        assertTrue("Only 'testapp2.deploy.ok' file should be in .state folder : ", stateFolder.list()[0].equals("testapp2.deploy.ok"));
        StatusFileModificator.deleteStatusFile("testapp2", pickupFolder);
        assertTrue(stateFolder.list().length == 0);
    }
    
    @Test
    public void deleteStatusFileByPatternTest() throws Exception {
        STATE_DIR.createDirectory();
        copyDummyStatusFiles();
        File stateFolder = STATE_DIR.toFile();
        File pickupFolder = PICKUP_DIR.toFile();
        assertTrue(stateFolder.list().length == 5);
        String deleted = StatusFileModificator.deleteStatusFileByNamePattern("testapp2", pickupFolder);
        assertTrue(stateFolder.list().length == 4);
        assertTrue("Only 'testapp2' file should be deleted : ", deleted.equals("testapp2"));
        StatusFileModificator.deleteStatusFile("testapp1", pickupFolder);
        assertTrue(stateFolder.list().length == 0);
    }

    @Test
    public void deleteStatusFileByPatternStrangeSymbolsTest() throws Exception {
        STATE_DIR.createDirectory();
        copyStrangeStatusFiles();
        File stateFolder = STATE_DIR.toFile();
        File pickupFolder = PICKUP_DIR.toFile();
        assertTrue(stateFolder.list().length == 3);
        String deleted = StatusFileModificator.deleteStatusFileByNamePattern("test[^a-zA-Z0-9_-]strangesymbols", pickupFolder);
        assertTrue(stateFolder.list().length == 2);
        assertTrue("Only 'test#strangesymbols' file should be deleted: ", deleted.equals("test#strangesymbols"));
        assertTrue("Both undeploy status files should not be deleted: ",
            stateFolder.list()[0].contains(".undeploy.") && stateFolder.list()[1].contains(".undeploy."));
    }

    @Test
    public void createStatusFileOkTest() throws Exception {
        for (String st : new String[] { StatusFileModificator.OP_DEPLOY, StatusFileModificator.OP_UNDEPLOY }) {
            StatusFileModificator.createStatusFile("testApp3", PICKUP_DIR.toFile(), st, true, 33, 123456789);
            File stateDir = STATE_DIR.toFile();
            assertTrue(stateDir.exists());
            assertTrue("Invalid .state folder contents:", stateDir.list().length == 1 && stateDir.list()[0].equals("testApp3." + st + ".ok"));
            StatusFileModificator.deleteStatusFile("testApp3", PICKUP_DIR.toFile());
        }
    }

    @Test
    public void createStatusFileErrorTest() throws Exception {
        for (String st : new String[] { StatusFileModificator.OP_DEPLOY, StatusFileModificator.OP_UNDEPLOY }) {
            StatusFileModificator.createStatusFile("testApp4", PICKUP_DIR.toFile(), st, false, 33, 123456789);
            File stateDir = STATE_DIR.toFile();
            assertTrue(stateDir.exists());
            assertTrue("Invalid .state folder contents:", stateDir.list().length == 1 && stateDir.list()[0].equals("testApp4." + st + ".error"));
            StatusFileModificator.deleteStatusFile("testApp4", PICKUP_DIR.toFile());
        }
    }

    @Test
    public void getLastModifiedFromStatusFileTest() throws Exception {
        File pickupDir = PICKUP_DIR.toFile();
        StatusFileModificator.createStatusFile("testApp5", pickupDir, StatusFileModificator.OP_UNDEPLOY, true, 33, 123456789);
        assertTrue(StatusFileModificator.getLastModifiedFromStatusFile("testApp5", pickupDir) == -1);
        StatusFileModificator.createStatusFile("testApp6", pickupDir, StatusFileModificator.OP_UNDEPLOY, false, 33, 123456789);
        assertTrue(StatusFileModificator.getLastModifiedFromStatusFile("testApp6", pickupDir) == -1);
        StatusFileModificator.createStatusFile("testApp7", pickupDir, StatusFileModificator.OP_DEPLOY, true, 33, 123456789);
        assertTrue(StatusFileModificator.getLastModifiedFromStatusFile("testApp7", pickupDir) == 123456789);
        StatusFileModificator.createStatusFile("testApp8", pickupDir, StatusFileModificator.OP_DEPLOY, false, 33, 123456789);
        assertTrue(StatusFileModificator.getLastModifiedFromStatusFile("testApp8", pickupDir) == -1);
        assertTrue(StatusFileModificator.getLastModifiedFromStatusFile("testApp9", pickupDir) == -1);
    }

    private void copyDummyStatusFiles() {
    	copyStatusFiles(dummyStatusFiles);
    }
    
    private void copyStrangeStatusFiles() {
    	copyStatusFiles(strangeStatusFiles);
    }
    
    private void copyStatusFiles(String[] files) {
    	for (String statusFile : files) {
            PathReference sourceFile = new PathReference(statusFile);
            assertTrue(sourceFile.exists());
            PathReference copy = sourceFile.copy(STATE_DIR);
            assertTrue(copy.exists());
        }
    }

}