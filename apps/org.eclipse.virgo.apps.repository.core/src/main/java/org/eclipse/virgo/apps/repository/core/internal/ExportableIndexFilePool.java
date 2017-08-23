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

package org.eclipse.virgo.apps.repository.core.internal;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.virgo.util.io.FileSystemUtils;

/**
 * A pool of files managed to keep index files that are 'exported' on demand.
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 * thread-safe
 *
 */
final public class ExportableIndexFilePool implements FilePool {

    private static final int INDEX_NUMBER_LENGTH = 4;
    private static final int TRIAL_TIMES = 10;  // must not be exceed ten consecutive numbered files
    private static final int POOL_SIZE = 10;  // must be greater than zero
    private static final String INDEX_SUFFIX = ".index";
    private static final String INDEX_FILENAME_SUFFIX_FORMAT = INDEX_SUFFIX + "%04d"; // last four digits assumed in set-up
    private final String fileFormat;
    private final File indexLocationDir;

    private final Object monitorIndexPool = new Object(); // protects all following private state
        private final List<File> exportedIndexes;
        private final List<File> toBeDeleted;
        private volatile int indexFileNameNumber;
        private final List<File> generatedIndexFiles;


    public ExportableIndexFilePool(File indexLocationDir, String fileNameBase) {
        this.indexLocationDir = indexLocationDir;
        this.fileFormat = fileNameBase + INDEX_FILENAME_SUFFIX_FORMAT;
        
        final String filePrefix = fileNameBase + INDEX_SUFFIX;
        this.exportedIndexes = new ArrayList<File>(POOL_SIZE);
        collectExportedIndexesAndDetermineLastIndexValue(this.indexLocationDir, this.exportedIndexes, filePrefix);
        
        this.indexFileNameNumber = determineLastIndexValue(this.exportedIndexes); 
        
        this.toBeDeleted = new ArrayList<File>(0);
        this.generatedIndexFiles = new ArrayList<File>(0);
        clearOldIndexes();
    }

    public File generateNextPoolFile() throws FilePoolException {
        synchronized (monitorIndexPool) {
            for (int i=0; i<TRIAL_TIMES; ++i) {
                if (++this.indexFileNameNumber > 9999) {
                    this.indexFileNameNumber = 0;
                }
                final File indexFile = new File(this.indexLocationDir, String.format(this.fileFormat, this.indexFileNameNumber));
                try {
                    final File canonicalIndexFile = indexFile.getCanonicalFile();
                    if (!this.generatedIndexFiles.contains(canonicalIndexFile)) {
                        if (canonicalIndexFile.createNewFile()) {
                            this.generatedIndexFiles.add(canonicalIndexFile);
                            return canonicalIndexFile;
                        }
                    }
                } catch (IOException e) {
                    throw new FilePoolException("Cannot generate new pool file in '" + this.indexLocationDir + "'.", e);
                }
            }
        }
        throw new FilePoolException("Cannot generate new pool file for '" + this.indexLocationDir + "'.");
    }
    
    public void putFileInPool(File indexFile) throws FilePoolException {
        synchronized (monitorIndexPool) {
            try {
                File canonicalFile = indexFile.getCanonicalFile();
                if (this.generatedIndexFiles.contains(canonicalFile)) {
                    this.exportedIndexes.add(0, canonicalFile);
                }
            } catch (IOException e) {
                throw new FilePoolException("Cannot put file in pool", e);
            }
            clearOldIndexes();
        }
    }
    
    public File getMostRecentPoolFile() throws FilePoolException {
        synchronized (monitorIndexPool) {
            if (this.exportedIndexes.isEmpty()) {
                throw new FilePoolException("No file in exportable index file pool in '" + this.indexLocationDir + "'.");
            }
            File indexFile = this.exportedIndexes.get(0);
            clearOldIndexes();
            return indexFile;
        }
    }

    private static int collectExportedIndexesAndDetermineLastIndexValue(File indexLocationDir, List<File> exportedIndexes, final String filePrefix) {
        if (indexLocationDir==null || (indexLocationDir.exists() && !indexLocationDir.isDirectory())) {
            throw new IllegalArgumentException("Index location '" + indexLocationDir + "' for index pool must be a directory.");
        }
        if (!indexLocationDir.exists()) {
            if (!indexLocationDir.mkdirs()) {
                throw new IllegalArgumentException("Index location '" + indexLocationDir + "' cannot be created.");
            }
        }

        try {
            fillAndOrderExportedIndexes(indexLocationDir, exportedIndexes, filePrefix);
        } catch (IOException e) {
            throw new IllegalArgumentException("Directory '" + indexLocationDir + "' cannot be used for indexes.");
        }
        
        return determineLastIndexValue(exportedIndexes);
    }

    private static void fillAndOrderExportedIndexes(File indexLocationDir, List<File> exportedIndexes, final String filePrefix) throws IOException {
        List<LastModifiedOrderableFile> orderableFileList = new ArrayList<LastModifiedOrderableFile>();
        final int indexNameLength = filePrefix.length() + INDEX_NUMBER_LENGTH; 
        for (File file : FileSystemUtils.listFiles
            ( indexLocationDir, 
                new FileFilter() 
                    { public boolean accept(File pathname) 
                        { 
                            if (pathname.isDirectory())
                                return false;
                            String name = pathname.getName();
                            if (name.length() == indexNameLength) {
                                if (name.startsWith(filePrefix) 
                                    && allDigits(name.substring(indexNameLength - INDEX_NUMBER_LENGTH, indexNameLength))) {
                                    return true;
                                }
                            }
                            return false;
                        }
                    }
                )
            ) {
            orderableFileList.add(new LastModifiedOrderableFile(file.getCanonicalFile()));
        }
         
        Collections.<LastModifiedOrderableFile>sort(orderableFileList);
        
        for (LastModifiedOrderableFile lmoFile : orderableFileList) {
            exportedIndexes.add(lmoFile.getFile());
        }
        
        orderableFileList.clear();
    }
    
    private static boolean allDigits(String string) {
        final char[] chars = string.toCharArray();
        for (char c : chars) {
            if ("0123456789".indexOf(c)==-1) return false;
        }
        return true;
    }

    private static class LastModifiedOrderableFile implements Comparable<LastModifiedOrderableFile> {
        private final File file;
        private final long lastModifiedValue;
        public LastModifiedOrderableFile(File file) {
            this.file = file;
            this.lastModifiedValue = file.lastModified();
        }
        public int compareTo(LastModifiedOrderableFile o) {
            long diff = o.lastModifiedValue - this.lastModifiedValue;
            return (diff<0) ? -1 : ((diff>0) ? +1 : 0);
        }
        public boolean equals(Object o) {
            if (o instanceof LastModifiedOrderableFile) {
                return ((LastModifiedOrderableFile) o).lastModifiedValue == this.lastModifiedValue;
            }
            return false;
        }
        
        public int hashCode() {
            assert false : "hashCode not designed";
            return 13; // any arbitrary constant will do 
        }
        
        public File getFile() {
            return this.file;
        }
    }

    /**
     * Precondition: exportedIndexes must be in newest-->oldest order.
     * @param exportedIndexes list of files
     * @return last index value found on newest file name
     */
    private static int determineLastIndexValue(List<File> exportedIndexes) {
        int lastIndexValue = 0;
        for (int i = exportedIndexes.size()-1; i>=0; --i) {    
            File file = exportedIndexes.get(i);
            try {
                String filename = file.getName();
                String lastFourChars = filename.substring(filename.length()-4, filename.length());
                int indexNumber = Integer.parseInt(lastFourChars);
                lastIndexValue = indexNumber;
            } catch (IndexOutOfBoundsException e) {
            } catch (NumberFormatException e) {
            }
        }
        return lastIndexValue;
    }
    
    private void clearOldIndexes() {
        if (this.exportedIndexes.size()>POOL_SIZE) {
            for (int i = this.exportedIndexes.size() - 1; i >= POOL_SIZE; --i) {
                File indexFile = this.exportedIndexes.remove(i);
                this.generatedIndexFiles.remove(indexFile);
                if (indexFile.exists() && !indexFile.delete()) {
                    this.toBeDeleted.add(indexFile);
                }
            }
        }
        if (!this.toBeDeleted.isEmpty()) {
            List<File> remnant = new ArrayList<File>(this.toBeDeleted.size());
            for (int i = 0; i < this.toBeDeleted.size(); ++i) {
                File file = this.toBeDeleted.get(i);
                if (file.exists() && !file.delete()) {
                    remnant.add(file);
                }
            }
            this.toBeDeleted.clear();
            this.toBeDeleted.addAll(remnant);
        }
    }
}
