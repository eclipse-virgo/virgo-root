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

package org.eclipse.virgo.apps.admin.core.state;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.zip.ZipException;

import org.eclipse.virgo.apps.admin.core.DumpExtractor;
import org.eclipse.virgo.apps.admin.core.dump.DumpPathLocator;
import org.eclipse.virgo.kernel.services.work.WorkArea;
import org.eclipse.virgo.util.io.FileSystemUtils;
import org.eclipse.virgo.util.io.PathReference;
import org.eclipse.virgo.util.io.ZipUtils;


/**
 * <p>
 * DumpStateExtractor can extract the osgi state from an equinox dump 
 * and place it in a staging location for use by clients of this class.
 * </p>
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * DumpStateExtractor is threadsafe
 *
 */
final class StateDumpExtractor implements DumpExtractor {
    
    private final File stagingDir;

    private final DumpPathLocator dumpPathLocator;
    
    /**
     * @param workArea to extract dumps to
     * @param dumpPathLocator to get dumps from
     */
    public StateDumpExtractor(WorkArea workArea, DumpPathLocator dumpPathLocator) {
        this.stagingDir = workArea.getWorkDirectory().newChild("extracted-state-dumps").createDirectory().toFile();
        this.dumpPathLocator = dumpPathLocator;     
    }    

    /** 
     * {@inheritDoc}
     */
    public File getStateDump(String dump) throws ZipException, IOException {
        File stateDump;
        if(dump == null){
            throw new IllegalArgumentException("Requested dump can not be null");
        }
        File dumpFolder = this.dumpPathLocator.getDumpFolder(dump);
        if(dumpFolder == null){
            throw new IOException(String.format("Requested dump does not exist or is not a folder '%s'", dump));
        }
        File[] stateDumpZipFiles = FileSystemUtils.listFiles(dumpFolder, new FilenameFilter() {

            public boolean accept(File dir, String name) {
                return name.equals("osgi.zip");
            }
        });
        if(stateDumpZipFiles.length != 1){
            throw new IOException(String.format("Exactly 1 state dump zip expected, found '%s'", stateDumpZipFiles.length));
        }
        File stateDumpZipFile = stateDumpZipFiles[0];

        stateDump = unzip(stateDumpZipFile);
        return stateDump;
    }
    
    private File unzip(File stateDumpZipFile) throws IOException {
        PathReference zipFile = new PathReference(stateDumpZipFile);
        PathReference dest = new PathReference(this.stagingDir);
        return new File(ZipUtils.unzipTo(zipFile, dest).toFile(), "state");
    }
    
}
