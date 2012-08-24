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

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

import org.eclipse.virgo.util.common.Assert;
import org.eclipse.virgo.util.math.Sets;
import org.slf4j.Logger;


/**
 * Checks a directory on the file system for modifications. Maintains a known state of the files to determine if the
 * changes are new files, modified files or deleted files.
 * <p/>
 * 
 * Modification notifications ({@link FileSystemEvent}s) are published to a set of configured {@link FileSystemListener
 * FileSystemListeners}. New listeners can be safely added at runtime.
 * <p/>
 * 
 * By default, all files in the directory are monitored. Files can be excluded from monitoring using a regex pattern.
 * <p/>
 * <strong>Concurrent Semantics</strong><br/>
 * Thread-safe.
 * 
 */
public final class FileSystemChecker {

    private final File checkDir;

    private final Logger logger;
    private final Object checkLock = new Object();

    /**
     * The files we know about -- with their last modified date (as a Long) are in <code>fileState</code>.<br/>
     * Files that are changing or are new are monitored in <code>monitorRecords</code>.
     * <p/>
     * <strong>Invariant:</strong> all monitored files are in <code>fileState</code>.<br/>
     * As soon as a file is notified (to listeners) it is no longer monitored. Thus files in <code>fileState</code> and
     * not monitored can be assumed to have been notified about already.
     */
    private final Map<String, Long> fileState = new HashMap<String, Long>(32);

    private final Map<String, MonitorRecord> monitorRecords = new HashMap<String, MonitorRecord>(16);

    private final List<FileSystemListener> listeners = new CopyOnWriteArrayList<FileSystemListener>();

    private final FilenameFilter includeFilter;

    private static boolean WINDOWS = System.getProperty("os.name").startsWith("Windows");

    /**
     * Creates a new <code>FileSystemChecker</code>. Identifies changes on all files in <code>checkDir</code>.
     * 
     * @param checkDir the directory to check.
     */
    public FileSystemChecker(File checkDir) {
        this(checkDir, null, null);
    }

    /**
     * Creates a new <code>FileSystemChecker</code>. Identifies changes on all files in <code>checkDir</code>, except those that match
     * <code>excludePattern</code>. No diagnostics logging.
     * 
     * @param checkDir the directory to check.
     * @param excludePattern regular expression for files to exclude.
     */
    public FileSystemChecker(File checkDir, String excludePattern) {
        this(checkDir, excludePattern, null);
    }
    
    /**
     * Creates a new <code>FileSystemChecker</code>. Identifies changes on all files in <code>checkDir</code>. Diagnostics to logger.
     * 
     * @param checkDir the directory to check.
     * @param logger where to log diagnostics -- can be null
     */
    public FileSystemChecker(File checkDir, Logger logger) {
        this(checkDir, null, logger);
    }

    /**
     * Creates a new <code>FileSystemChecker</code>. Identifies changes to on all files, except those that match
     * <code>excludePattern</code>.
     * 
     * @param checkDir the directory to check -- {@link File} must exist and be a directory
     * @param excludePattern regular expression for files to exclude.
     * @param logger where to log diagnostics -- can be null
     */
    public FileSystemChecker(File checkDir, String excludePattern, Logger logger) {
        Assert.isTrue(checkDir.isDirectory(), "Check directory '%s' must exist and must be a directory.", checkDir.getAbsolutePath());
        this.checkDir = checkDir;
        this.logger = logger;
        
        final Pattern compiledExcludePattern = (excludePattern==null) ? null : Pattern.compile(excludePattern);

        this.includeFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return compiledExcludePattern == null || !compiledExcludePattern.matcher(name).matches();
            }
        };

        populateInitialState(); // no notifications made yet
    }

    /**
     * Add a new {@link FileSystemListener} to this <code>FileSystemChecker</code>.
     * 
     * @param listener the listener to add.
     */
    public void addListener(FileSystemListener listener) {
        this.listeners.add(listener);
    }

    /**
     * Instructs this <code>FileSystemChecker</code> to check the configured directory and notifies any registered
     * listeners of changes to the directory files.
     */
    public void check() {
        synchronized (this.checkLock) {
            try {
                File[] currentFiles;
                try {
                    currentFiles = listCurrentDirFiles();
                } catch (Exception e) {
                    if (logger!=null) logger.warn("FileSystemChecker caught exception from listFiles()", e);
                    throw e;
                }
                
                debugState("before check:", currentFiles);
                
                Set<String> currentFileKeys = new HashSet<String>(currentFiles.length);
                for (File file : currentFiles) {
                    // remember seen files to allow comparison for delete
                    String keyFile = this.key(file);
                    currentFileKeys.add(keyFile);

                    if (!isKnown(file)) {
                        // not seen it before -- start monitoring it -- a potential newly created file
                        monitorRecords.put(keyFile, new MonitorRecord(file.length(), FileSystemEvent.CREATED));
                        setKnownFileState(file);
                    } else if (monitorRecords.containsKey(keyFile)) {
                        // we are monitoring this file
                        MonitorRecord monitorRecord = monitorRecords.get(keyFile);
                        long size = file.length();
                        if (size > monitorRecord.getSize()) {
                            // still being written? continue to track it
                            monitorRecord.setSize(size);
                        } else if (isUnlocked(file)){
                            // not changing anymore so if we can rename it we can announce it:
                            notifyListeners(this.key(file), monitorRecord.getEvent());
                            // do not monitor it anymore
                            monitorRecords.remove(keyFile);
                        }
                        setKnownFileState(file);
                    } else if (file.lastModified() > knownLastModified(file)) {
                        // we know about this file, we are not monitoring it, but it has changed
                        // start monitoring it until it stabilises
                        monitorRecords.put(keyFile, new MonitorRecord(file.length(), FileSystemEvent.MODIFIED));
                        setKnownFileState(file);
                    }
                }

                Set<String> deletedFiles = Sets.difference(this.fileState.keySet(), currentFileKeys);
                for (String deletedFile : deletedFiles) {
                    if (monitorRecords.containsKey(deletedFile)) {
                        // we were monitoring it when it disappeared
                        MonitorRecord monitorRecord = monitorRecords.get(deletedFile);
                        if (monitorRecord.getEvent().equals(FileSystemEvent.MODIFIED)) {
                            notifyListeners(deletedFile, FileSystemEvent.DELETED);
                        }
                    } else {
                        notifyListeners(deletedFile, FileSystemEvent.DELETED);
                    }
                    this.fileState.remove(deletedFile);
                    this.monitorRecords.remove(deletedFile);
                }
            } catch (Exception _) {
                // FatalIOException can arise from listCurrentDirFiles() which means that we cannot determine the list.
                // In this case we have already retried the list, and we can ignore this check().   
                // The check() then becomes a no-op which is better than assuming the directory is empty.
            } finally {
                
                debugState("after check:", null);
            
            }
        }
    }

    public boolean isUnlocked(File file) {
        // Heuristic check for the file not being locked on Windows. On *ix, assume the file is unlocked since we can't tell.
        return !WINDOWS || file.renameTo(file);
    }

    private void debugState(final String heading, File[] files) {
        if (logger!=null && logger.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder().append(this.checkDir).append(" - ").append(heading);
            if (files != null) {
                sb.append("\n\tFileList():  [");
                boolean first = true;
                for (File f : files) {
                    if (!first) sb.append(", ");
                    sb.append(f.getName());
                    first = false;
                }
                sb.append("]");
            }
            if (this.fileState != null) {
                sb.append("\n\tKnown files: [");
                boolean first = true;
                for (String s : this.fileState.keySet()) {
                    if (!first) sb.append(", ");
                    sb.append(s);
                    first = false;
                }
                sb.append("]");
            }
            if (this.monitorRecords != null) {
                sb.append("\n\tMonitored:   [");
                boolean first = true;
                for (String s : this.monitorRecords.keySet()) {
                    if (!first) sb.append(", ");
                    sb.append(s);
                    first = false;
                }
                sb.append("]");
            }
            logger.debug(sb.toString());
        }
    }
    
    private void notifyListeners(String file, FileSystemEvent event) {
        for (FileSystemListener listener : this.listeners) {
            try {
                listener.onChange(file, event);
            } catch (Throwable e) {
                if (logger!=null) {
                    logger.warn("Listener threw exception for event " + event, e);
                }
            }
        }
    }

    /**
     * Initialises known files (<code>fileState</code>) from the check directory and starts monitoring them.
     * @throws Exception 
     */
    private void populateInitialState() throws RuntimeException {
        File[] initialList;
        try {
            initialList = listCurrentDirFiles();
        } catch (RuntimeException e) {
            if (logger!=null) logger.warn("FileSystemChecker caught exception from listFiles()", e);
            throw e;
        }
        for (File file : initialList) {
            String keyFile = key(file);
            monitorRecords.put(keyFile, new MonitorRecord(file.length(), FileSystemEvent.INITIAL));
            setKnownFileState(file);
        }
        debugState("initial state:", initialList);
    }

    /**
     * Lists the {@link File Files} currently in the check directory.
     * 
     * @return the <code>Files</code> that are in the check directory.
     */
    private File[] listCurrentDirFiles() {
        return FileSystemUtils.listFiles(this.checkDir, this.includeFilter, this.logger);
    }

    /**
     * Sets the state of the supplied {@link File} into our known files map (<code>fileState</code>).
     * 
     * @param the <code>File</code> to record state for.
     */
    private void setKnownFileState(File file) {
        String key = key(file);
        long lastModified = file.lastModified();
        this.fileState.put(key, lastModified);
    }

    /**
     * Gets the recorded last modified timestamp for the supplied {@link File}.
     * 
     * @param file the <code>File</code> to check for.
     * @return the last modified timestamp, or <code>null</code> if no timestamp is recorded.
     */
    private Long knownLastModified(File file) {
        return this.fileState.get(key(file));
    }

    /**
     * Is file known to us? (In <code>fileState</code>.)
     * 
     * @param file the <code>File</code> to check for.
     * @return <code>true</code> if in the map (domain), <code>false</code> otherwise.
     */
    private boolean isKnown(File file) {
        return this.fileState.containsKey(key(file));
    }

    /**
     * Gets the record key for the supplied {@link File}.
     * 
     * @param file the <code>File</code> to get the key for.
     * @return the record key.
     */
    private String key(File file) {
        String key = file.getAbsolutePath();
        if (file.isDirectory()) {
            key += File.separator;
        }
        return key;
    }

    private static class MonitorRecord {

        private final FileSystemEvent event;

        private long size;

        public MonitorRecord(long size, FileSystemEvent event) {
            this.size = size;
            this.event = event;
        }

        public long getSize() {
            return size;
        }

        public void setSize(long size) {
            this.size = size;
        }

        public FileSystemEvent getEvent() {
            return event;
        }

    }
}
