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
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.virgo.util.io.StubLogger.StubLogEntry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 */
public class FileSystemCheckerTests {

    private final File checkDir = new File("build", "work");

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

            @Override
            public void onChange(String file, FileSystemEvent event) {
                if (file.endsWith(fileName) && FileSystemEvent.CREATED.equals(event)) {
                    eventReceived.set(true);
                }
            }

            @Override
            public void onInitialEvent(List<String> paths) {
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

            @Override
            public void onChange(String file, FileSystemEvent event) {
                if (file.endsWith(fileName) && FileSystemEvent.CREATED.equals(event)) {
                    eventReceived.set(true);
                }
            }

            @Override
            public void onInitialEvent(List<String> paths) {
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

            @Override
            public void onChange(String file, FileSystemEvent event) {
                if (file.endsWith(fileName) && FileSystemEvent.CREATED.equals(event)) {
                    eventReceived.set(true);
                }
            }

            @Override
            public void onInitialEvent(List<String> paths) {
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

            @Override
            public void onChange(String file, FileSystemEvent event) {
                if (file.endsWith(fileName) && FileSystemEvent.MODIFIED.equals(event)) {
                    eventReceived.set(true);
                }
            }

            @Override
            public void onInitialEvent(List<String> paths) {
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

        private final AtomicInteger eA;

        private final AtomicInteger eI;

        private final AtomicInteger eC;

        private final AtomicInteger eD;

        private final AtomicInteger eM;

        public TestFileSystemListener(String fileName, int all, int ini, int cre, int del, int mod) {
            this.fileName = fileName;
            this.eA = new AtomicInteger(all);
            this.eI = new AtomicInteger(ini);
            this.eC = new AtomicInteger(cre);
            this.eD = new AtomicInteger(del);
            this.eM = new AtomicInteger(mod);
        }

        @Override
        public void onChange(String file, FileSystemEvent event) {
            if (file.endsWith(this.fileName)) {
                this.eA.incrementAndGet(); // count all notifications for this file
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

        @Override
        public void onInitialEvent(List<String> paths) {
        }

        public boolean checkEvents(int all, int ini, int cre, int del, int mod) {
            return all == this.eA.get() && ini == this.eI.get() && cre == this.eC.get() && del == this.eD.get() && mod == this.eM.get();
        }
    }

    @Test
    public void complexUpdateFile() throws Exception {
        FileSystemChecker checker = new FileSystemChecker(this.checkDir);

        final String updFileName = "fileToUpdate.txt";
        final String delFileName = "fileToDelete.txt";

        File delFile = new File(this.checkDir, delFileName);
        delFile.createNewFile();

        TestFileSystemListener tlUpd = new TestFileSystemListener(updFileName, 0, 0, 0, 0, 0);
        TestFileSystemListener tlDel = new TestFileSystemListener(delFileName, 0, 0, 0, 0, 0);

        checker.addListener(tlUpd);
        checker.addListener(tlDel);

        File updateFile = new File(this.checkDir, updFileName);
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

        TestFileSystemListener delListener = new TestFileSystemListener(fileName, 0, 0, 0, 0, 0);
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

            @Override
            public void onChange(String file, FileSystemEvent event) {
                if (FileSystemEvent.INITIAL.equals(event)) {
                    initialEvents.incrementAndGet();
                }
            }

            @Override
            public void onInitialEvent(List<String> paths) {
            }
        });
        checker.check();
        assertEquals("Expected 2 INITIAL events", 2, initialEvents.get());

        checker.check();
        assertEquals("Too many INITIAL events", 2, initialEvents.get());
    }

    @Test
    public void initialStateDebug() throws Exception {
        StubLogger stubLogger = new StubLogger();
        new File(this.checkDir, "a.txt").createNewFile();
        new File(this.checkDir, "b.txt").createNewFile();
        FileSystemChecker checker = new FileSystemChecker(this.checkDir, stubLogger);
        final AtomicInteger initialEvents = new AtomicInteger(0);
        checker.addListener(new FileSystemListener() {

            @Override
            public void onChange(String file, FileSystemEvent event) {
                if (FileSystemEvent.INITIAL.equals(event)) {
                    initialEvents.incrementAndGet();
                }
            }

            @Override
            public void onInitialEvent(List<String> paths) {
            }
        });
        checker.check();
        assertEquals("Expected 2 INITIAL events", 2, initialEvents.get());

        checker.check();
        assertEquals("Too many INITIAL events", 2, initialEvents.get());

        List<StubLogEntry> entries = stubLogger.getEntries();
        assertTrue("There should be five states debugged.", 5 == entries.size());

        String initialStateHeader = "work - initial state:";
        String beforeStateHeader = "work - before check:";
        String afterStateHeader = "work - after check:";

        assertTrue("Initial state not output in correct place.", entries.get(0).getString().contains(initialStateHeader));
        assertTrue("Initial state not reporting a.txt and b.txt files.", entries.get(0).getString().contains("FileList():  [a.txt, b.txt]")
            || entries.get(0).getString().contains("FileList():  [b.txt, a.txt]"));
        assertTrue("Before state not output in correct place.", entries.get(1).getString().contains(beforeStateHeader));
        assertTrue("Before state not reporting a.txt and b.txt files.", entries.get(1).getString().contains("FileList():  [a.txt, b.txt]")
            || entries.get(1).getString().contains("FileList():  [b.txt, a.txt]"));
        assertTrue("After state not output in correct place.", entries.get(2).getString().contains(afterStateHeader));
        assertTrue("Before state not output in correct place.", entries.get(3).getString().contains(beforeStateHeader));
        assertTrue("Before state not reporting a.txt and b.txt files.", entries.get(3).getString().contains("FileList():  [a.txt, b.txt]")
            || entries.get(3).getString().contains("FileList():  [b.txt, a.txt]"));
        assertTrue("After state not output in correct place.", entries.get(4).getString().contains(afterStateHeader));

        // for (StubLogEntry sle : entries) {
        // System.out.println(sle);
        // }
    }

    @Test
    public void onInitialEventTest() throws Exception {
        try {
            // enables onInitialFSChanges notifications
            System.setProperty("org.eclipse.virgo.fschecker.initialEventMode", "bulk");
            File f1 = new File(this.checkDir, "a.txt");
            File f2 = new File(this.checkDir, "b.txt");
            f1.createNewFile();
            f2.createNewFile();
            final AtomicInteger onChangeEventsCounter = new AtomicInteger(0);
            final AtomicInteger onInitialEventsCounter = new AtomicInteger(0);
            final AtomicBoolean eventFilesCheckFlag = new AtomicBoolean(true);
            FileSystemChecker checker = new FileSystemChecker(this.checkDir);
            checker.addListener(new FileSystemListener() {

                @Override
                public void onChange(String file, FileSystemEvent event) {
                    onChangeEventsCounter.incrementAndGet();
                }

                @Override
                public void onInitialEvent(List<String> paths) {
                    onInitialEventsCounter.incrementAndGet();
                    if (paths.size() == 2) {
                        for (String s : paths) {
                            if (!(s.endsWith("a.txt") || s.endsWith("b.txt"))) {
                                eventFilesCheckFlag.set(false);
                            }
                        }
                    } else {
                        eventFilesCheckFlag.set(false);
                    }
                }

            });
            checker.check();
            assertEquals("Expected only 1 onInitialEvent event for the 2 files:", 1, onInitialEventsCounter.get());
            assertTrue("Expected onInitialFSChanges event for 2 files - a.txt and b.txt:", eventFilesCheckFlag.get());
            assertEquals("Expected no onChange events:", 0, onChangeEventsCounter.get());
            onInitialEventsCounter.set(0);
            onChangeEventsCounter.set(0);
            Thread.sleep(1000); // give the test chance to recognise the changed files
            f1.setLastModified(System.currentTimeMillis());
            f2.setLastModified(System.currentTimeMillis());
            checker.check();// here only marked for monitoring
            checker.check();// onChange events
            assertEquals("Expected no new onInitialEvent events:", 0, onInitialEventsCounter.get());
            assertEquals("Expected 2 onChange event for the 2 updated files:", 2, onChangeEventsCounter.get());
            onInitialEventsCounter.set(0);
            onChangeEventsCounter.set(0);

            new File(this.checkDir, "c.txt").createNewFile();
            checker.check();// here only marked for monitoring
            checker.check();// onChange events
            assertEquals("Expected no new onInitialEvent events:", 0, onInitialEventsCounter.get());
            assertEquals("Expected 1 onChange event for the new file:", 1, onChangeEventsCounter.get());
            onInitialEventsCounter.set(0);
            onChangeEventsCounter.set(0);

            createDir(); // clear dir
            checker.check();
            assertEquals("Expected no new onInitialEvent events:", 0, onInitialEventsCounter.get());
            assertEquals("Expected 3 onChange events for the 3 deleted files:", 3, onChangeEventsCounter.get());

        } finally {
            System.setProperty("org.eclipse.virgo.fschecker.initialEventMode", "singular");
        }
    }
    
    private static class RecursiveTestFileSystemListener implements FileSystemListener {
    	private int callCounter = 0;
    	private String filename;
    	
    	public RecursiveTestFileSystemListener(String filename) {
			this.filename = filename;
		}

		@Override
		public void onChange(String path, FileSystemEvent event) {
			if (path.endsWith(filename)) {
				if (FileSystemEvent.INITIAL == event || FileSystemEvent.CREATED == event) {				
					try {
						File file = new File(path);
						file.setLastModified(System.currentTimeMillis() + 1000);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				callCounter++;
			}
		}

		@Override
		public void onInitialEvent(List<String> paths) {	
		}
    	
    	public int getCallCounter() {
    		return callCounter;
    	}
    }
    
    @Test
    public void updateFileDuringHandling() throws Exception {
    	FileSystemChecker checker = new FileSystemChecker(this.checkDir);
        final String updFileName = "fileToUpdate.txt";
        
        RecursiveTestFileSystemListener listener = new RecursiveTestFileSystemListener(updFileName);
        
        checker.addListener(listener);

        File updateFile = new File(this.checkDir, updFileName);
        updateFile.createNewFile();
        
        checker.check();
        assertEquals("Expected 0 call to the listener", 0, listener.getCallCounter());
        
        checker.check();
        assertEquals("Expected 1 call to the listener", 1, listener.getCallCounter());
        
        checker.check();
        assertEquals("Expected 1 call to the listener", 1, listener.getCallCounter());
        
        checker.check();
        assertEquals("Expected 2 calls to the listener", 2, listener.getCallCounter());
    }
}
