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

package org.eclipse.virgo.util.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.virgo.util.io.FileSystemChecker;
import org.eclipse.virgo.util.io.FileSystemEvent;
import org.eclipse.virgo.util.io.FileSystemListener;
import org.eclipse.virgo.util.io.FileSystemUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 */
public class FileSystemCheckerTests {

    private final File checkDir = new File("target", "work");

    @Before
    public void createDir() {
        if (this.checkDir.exists()) {
            deleteRecursively(this.checkDir);
        }
        this.checkDir.mkdir();
    }

    @After
    public void deleteDir() {
        deleteRecursively(this.checkDir);
    }

    private void deleteRecursively(File path) {
        FileSystemUtils.deleteRecursively(path);
    }

    @Test
    public void newFile() throws Exception {
        final String fileName = "new.txt";
        FileSystemChecker checker = new FileSystemChecker(this.checkDir);
        final AtomicBoolean eventReceived = new AtomicBoolean(false);
        checker.addListener(new FileSystemListener() {

            public void onChange(String file, FileSystemEvent event) {
                if (file.endsWith(fileName) && FileSystemEvent.CREATED.equals(event)) {
                    eventReceived.set(true);
                }
            }
        });
        File newFile = new File(this.checkDir, fileName);
        newFile.createNewFile();
        // First call finds the new file
        checker.check();
        assertFalse("Unexpected CREATED event.", eventReceived.get());
        // Second call sees that file size hasn't changed and notifies the listener
        checker.check();
        assertTrue("Expected CREATED event.", eventReceived.get());
        // Third call sees no changes
        eventReceived.set(false); // reset flag
        checker.check();
        assertFalse("Unexpected CREATED event.", eventReceived.get());
    }

    @Test
    public void newFileWithFilter() throws Exception {
        final String fileName = "new.jar";
        FileSystemChecker checker = new FileSystemChecker(this.checkDir, ".*\\.txt");
        final AtomicBoolean eventReceived = new AtomicBoolean(false);
        checker.addListener(new FileSystemListener() {

            public void onChange(String file, FileSystemEvent event) {
                if (file.endsWith(fileName) && FileSystemEvent.CREATED.equals(event)) {
                    eventReceived.set(true);
                }
            }
        });
        File newFile = new File(this.checkDir, fileName);
        newFile.createNewFile();
        // First call finds the new file
        checker.check();
        assertFalse("Unexpected CREATED event.", eventReceived.get());
        // Second call sees that file size hasn't changed and notifies the listener
        checker.check();
        assertTrue("Expected CREATED event.", eventReceived.get());
        // Third call sees no changes
        eventReceived.set(false); // reset flag
        checker.check();
        assertFalse("Unexpected CREATED event.", eventReceived.get());
    }

    @Test
    public void newFileWithFilterNoMatch() throws Exception {
        final String fileName = "new.txt";
        FileSystemChecker checker = new FileSystemChecker(this.checkDir, ".*\\.txt");
        final AtomicBoolean eventReceived = new AtomicBoolean(false);
        checker.addListener(new FileSystemListener() {

            public void onChange(String file, FileSystemEvent event) {
                if (file.endsWith(fileName) && FileSystemEvent.CREATED.equals(event)) {
                    eventReceived.set(true);
                }
            }
        });
        File newFile = new File(this.checkDir, fileName);
        newFile.createNewFile();
        // First call finds the new file
        checker.check();
        assertFalse("Unexpected CREATED event.", eventReceived.get());
        // Second call sees that file size hasn't changed
        checker.check();
        assertFalse("Unexpected CREATED event.", eventReceived.get());
        // Third call sees no changes
        eventReceived.set(false); // reset flag
        checker.check();
        assertFalse("Unexpected CREATED event.", eventReceived.get());
    }

    @Test
    public void updateFile() throws Exception {
        final String fileName = "update.txt";
        FileSystemChecker checker = new FileSystemChecker(this.checkDir);
        final AtomicBoolean eventReceived = new AtomicBoolean(false);
        checker.addListener(new FileSystemListener() {

            public void onChange(String file, FileSystemEvent event) {
                if (file.endsWith(fileName) && FileSystemEvent.MODIFIED.equals(event)) {
                    eventReceived.set(true);
                }
            }
        });
        File updateFile = new File(this.checkDir, fileName);
        updateFile.createNewFile();
        // First call triggers monitoring of the file
        checker.check();
        // Second call triggers CREATED notification
        checker.check();
        updateFile.setLastModified(System.currentTimeMillis() + 1000);
        // Third call sees that last modified has changed and starts monitoring the file
        checker.check();
        // Fourth call sees that last modified is unchanged and triggers MODIFIED event
        checker.check();
        assertTrue("Expected MODIFIED event.", eventReceived.get());
        // Fifth call is quiet
        eventReceived.set(false); // reset flag
        checker.check();
        assertFalse("Unexpected MODIFIED event.", eventReceived.get());
    }

    /**
     * Instrumented {@link FileSystemListener} to listen for all {@link FileSystemEvent}s for a specific file
     * <p />
     *
     * <strong>Concurrent Semantics</strong><br />
     * thread safe
     *
     */
    private final class TestFileSystemListener implements FileSystemListener {

        private final String fileName;

        private final AtomicInteger eP;

        private final AtomicInteger eI;

        private final AtomicInteger eC;

        private final AtomicInteger eD;

        private final AtomicInteger eM;

        public TestFileSystemListener(String fileName, int all, int ini, int cre, int del, int mod) {
            this.fileName = fileName;
            eP = new AtomicInteger(all);
            eI = new AtomicInteger(ini);
            eC = new AtomicInteger(cre);
            eD = new AtomicInteger(del);
            eM = new AtomicInteger(mod);
        }

        public void onChange(String file, FileSystemEvent event) {
            if (file.endsWith(this.fileName)) {
                this.eP.incrementAndGet(); // count all notifications for this file
                switch (event) {
                    case INITIAL:
                        this.eI.incrementAndGet();
                        break;
                    case CREATED:
                        this.eC.incrementAndGet();
                        break;
                    case DELETED:
                        this.eD.incrementAndGet();
                        break;
                    case MODIFIED:
                        this.eM.incrementAndGet();
                        break;
                }
            }
        }

        public boolean checkEvents(int all, int ini, int cre, int del, int mod) {
            return all == this.eP.get() && ini == this.eI.get() && cre == this.eC.get() && del == this.eD.get() && mod == this.eM.get();
        }
    }

    @Test
    public void complexUpdateFile() throws Exception {
        FileSystemChecker checker = new FileSystemChecker(this.checkDir);

        final String updFileName = "fileToUpdate.txt";
        final String delFileName = "fileToDelete.txt";

        File delFile = new File(this.checkDir, delFileName);
        delFile.createNewFile();

        TestFileSystemListener tlUpd = new TestFileSystemListener(updFileName,0,0,0,0,0);
        TestFileSystemListener tlDel = new TestFileSystemListener(delFileName,0,0,0,0,0);
        
        checker.addListener(tlUpd);
        checker.addListener(tlDel);

        File updateFile = new File(checkDir, updFileName);
        updateFile.createNewFile();

        // First call triggers monitoring of the file
        checker.check();
        assertTrue("Unexpected notifications on first check for update file", tlUpd.checkEvents(0, 0, 0, 0, 0));
        assertTrue("Unexpected notifications on first check for delete file", tlDel.checkEvents(0, 0, 0, 0, 0));

        delFile.delete();

        // Second call triggers CREATED notification
        checker.check();
        assertTrue("Unexpected notifications on second check for update file", tlUpd.checkEvents(1, 0, 1, 0, 0));
        assertTrue("Unexpected notifications on second check for delete file", tlDel.checkEvents(0, 0, 0, 0, 0));

        // Third call sees that last modified has changed and starts monitoring the file
        updateFile.setLastModified(System.currentTimeMillis() + 1000);
        checker.check();
        assertTrue("Unexpected notifications on third check for update file", tlUpd.checkEvents(1, 0, 1, 0, 0));

        // Fourth call sees that last modified is unchanged and triggers MODIFIED event
        checker.check();
        assertTrue("Unexpected notifications on fourth check for update file", tlUpd.checkEvents(2, 0, 1, 0, 1));

        // Fifth call is quiet
        checker.check();
        assertTrue("Unexpected notifications on fifth check for update file", tlUpd.checkEvents(2, 0, 1, 0, 1));
    }

    @Test
    public void deleteFile() throws Exception {
        final String fileName = "delete.txt";
        FileSystemChecker checker = new FileSystemChecker(this.checkDir);
       
        TestFileSystemListener delListener = new TestFileSystemListener(fileName,0,0,0,0,0);
        checker.addListener(delListener);
        
        File deleteFile = new File(this.checkDir, fileName);
        deleteFile.createNewFile();

        checker.check();
        checker.check();
        assertTrue("Expected CREATED event.", delListener.checkEvents(1, 0, 1, 0, 0));

        deleteFile.delete();
        checker.check();
        assertTrue("Expected DELETED event.", delListener.checkEvents(2, 0, 1, 1, 0));
    }

    @Test
    public void initialState() throws Exception {
        new File(this.checkDir, "a.txt").createNewFile();
        new File(this.checkDir, "b.txt").createNewFile();
        FileSystemChecker checker = new FileSystemChecker(this.checkDir);
        final AtomicInteger initialEvents = new AtomicInteger(0);
        checker.addListener(new FileSystemListener() {

            public void onChange(String file, FileSystemEvent event) {
                if (FileSystemEvent.INITIAL.equals(event)) {
                    initialEvents.incrementAndGet();
                }
            }
        });
        checker.check();
        assertEquals("Expected 2 INITIAL events", 2, initialEvents.get());

        checker.check();
        assertEquals("Too many INITIAL events", 2, initialEvents.get());
    }
}
