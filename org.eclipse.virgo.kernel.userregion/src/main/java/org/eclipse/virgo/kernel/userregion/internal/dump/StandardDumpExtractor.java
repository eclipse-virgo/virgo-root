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

package org.eclipse.virgo.kernel.userregion.internal.dump;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.zip.ZipException;

import org.eclipse.virgo.kernel.services.work.WorkArea;
import org.eclipse.virgo.kernel.userregion.internal.DumpExtractor;
import org.eclipse.virgo.util.io.FileSystemUtils;
import org.eclipse.virgo.util.io.PathReference;
import org.eclipse.virgo.util.io.ZipUtils;

/**
 * <p>
 * DumpStateExtractor can extract the OSGi state from a dump directory and place it in a staging location for use by
 * clients of this class. It can also locate the region digraph dump in a dump directory.
 * </p>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread safe.
 * 
 */
public final class StandardDumpExtractor implements DumpExtractor {

    private final File stagingDir;

    /**
     * @param workArea to extract dumps to
     */
    public StandardDumpExtractor(WorkArea workArea) {
        this.stagingDir = workArea.getWorkDirectory().newChild("extracted-state-dumps").createDirectory().toFile();
    }

    /**
     * {@inheritDoc}
     */
    public File getStateDump(File dump) throws ZipException, IOException {
        File stateDumpZipFile = getDumpFile(dump, STATE_DUMP_FILE_NAME);

        return unzip(stateDumpZipFile);
    }

    private File getDumpFile(File dumpDirectory, final String fileName) throws IOException {
        if (dumpDirectory == null) {
            throw new IllegalArgumentException("Requested dump cannot be null");
        }
        
        File[] dumpFiles = FileSystemUtils.listFiles(dumpDirectory, new FilenameFilter() {

            public boolean accept(File dir, String name) {
                return name.equals(fileName);
            }
        });
        if (dumpFiles.length != 1) {
            throw new IOException(String.format("Exactly one dump file with name '%s' expected, found '%s'", fileName, dumpFiles.length));
        }
        return dumpFiles[0];
    }

    private File unzip(File stateDumpZipFile) throws IOException {
        PathReference zipFile = new PathReference(stateDumpZipFile);
        PathReference dest = new PathReference(this.stagingDir);
        return new File(ZipUtils.unzipTo(zipFile, dest).toFile(), "state");
    }

    public File getRegionDigraphDump(File dump) throws IOException {
        return getDumpFile(dump, REGION_DIGRAPH_FILE_NAME);
    }

}
