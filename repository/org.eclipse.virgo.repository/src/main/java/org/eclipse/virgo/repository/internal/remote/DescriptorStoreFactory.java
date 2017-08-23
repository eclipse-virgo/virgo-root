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

package org.eclipse.virgo.repository.internal.remote;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.virgo.util.io.FileCopyUtils;
import org.eclipse.virgo.util.io.FileSystemUtils;

/**
 * {@link DescriptorStoreFactory} is a utility for creating and persisting a {@link DescriptorStore} instance from an
 * input stream and for recovering such an instance.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is thread safe.
 * 
 */
public class DescriptorStoreFactory {

    private static final long EPOCH = 0L;

    private static final String REPOSITORY_NAME_ETAG_SEPARATOR = "-";

    private static final String DESCRIPTOR_STORE_FILE_SUFFIX = ".descriptors";

    private final Object monitor = new Object();

    private final String repositoryName;

    private final File descriptorStoreDirectory;

    public DescriptorStoreFactory(String repositoryName, File descriptorStoreDirectory) {
        this.repositoryName = repositoryName;
        this.descriptorStoreDirectory = descriptorStoreDirectory;
    }

    public DescriptorStore createDescriptorStore(InputStream storeStream, String etag) throws IOException, FileNotFoundException {
        synchronized (this.monitor) {
            File newStoreLocation = createStoreFile(etag);
            FileCopyUtils.copy(storeStream, new FileOutputStream(newStoreLocation));
            return new DescriptorStore(etag, newStoreLocation);            
        }
    }

    private File createStoreFile(String etag) {
        return new File(this.descriptorStoreDirectory, this.repositoryName + REPOSITORY_NAME_ETAG_SEPARATOR + etag + DESCRIPTOR_STORE_FILE_SUFFIX);
    }

    public DescriptorStore recoverDescriptorStore() {
        synchronized (this.monitor) {
            File descriptorStoreFile = recoverDescriptorStoreFile();
            if (descriptorStoreFile != null) {
                String eTag = parseETagFromDescriptorStoreFileName(descriptorStoreFile.getName());
                if (eTag != null) {
                    return new DescriptorStore(eTag, descriptorStoreFile);
                }
            }
            return null;
        }
    }

    private File recoverDescriptorStoreFile() {
        return getMostRecentFile(getDescriptorStoreFiles());
    }

    private String parseETagFromDescriptorStoreFileName(String name) {
        String eTag = null;
        if (isValidDescriptorStoreFileName(name)) {
            eTag = name.substring(descriptorStoreFileNamePrefix().length(), name.length() - DESCRIPTOR_STORE_FILE_SUFFIX.length());
        }
        return eTag;
    }

    private boolean isValidDescriptorStoreFileName(String name) {
        return name.startsWith(descriptorStoreFileNamePrefix()) && name.endsWith(DESCRIPTOR_STORE_FILE_SUFFIX);
    }

    private String descriptorStoreFileNamePrefix() {
        return this.repositoryName + REPOSITORY_NAME_ETAG_SEPARATOR;
    }

    private File[] getDescriptorStoreFiles() {
        FilenameFilter filter = new FilenameFilter() {

            public boolean accept(File dir, String name) {
                return isValidDescriptorStoreFileName(name);
            }

        };
        return FileSystemUtils.listFiles(this.descriptorStoreDirectory, filter);
    }

    /**
     * Returns the most recent file from the given array of files. If there is more than one file with the same last
     * modified time, returns the last one of those files in the array. This isn't crucial as the index will be
     * refreshed when an old etag is recovered.
     */
    private static File getMostRecentFile(File[] files) {
        File mostRecentFileile = null;
        if (files != null) {
            Long mostRecentLastModified = EPOCH;
            for (File file : files) {
                long lastModified = file.lastModified();
                if (lastModified >= mostRecentLastModified) {
                    mostRecentLastModified = lastModified;
                    mostRecentFileile = file;
                }
            }
        }
        return mostRecentFileile;
    }

}
