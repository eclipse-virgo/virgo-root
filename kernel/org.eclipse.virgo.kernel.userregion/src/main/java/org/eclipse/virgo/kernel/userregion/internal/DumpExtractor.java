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

package org.eclipse.virgo.kernel.userregion.internal;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipException;

/**
 * <p>
 * DumpExtractor provides an interface for clients to obtain state dumps and region digraph dumps.
 * </p>
 * 
 * <strong>Concurrent Semantics</strong><br/>
 * 
 * Implementations of this interface must be thread safe.
 * 
 */
public interface DumpExtractor {

	public final static String STATE_DUMP_FILE_NAME = "osgi.zip";
	
	public final static String REGION_DIGRAPH_FILE_NAME = "region.digraph";
	
    /**
     * Returns an unzipped version of the state dump zip file from the given dump directory.
     * 
     * @param dumpDir dump directory
     * @return the unzipped state dump file
     * @throws ZipException when unzipping
     * @throws IOException when reading
     */
    public File getStateDump(File dumpDir) throws ZipException, IOException;

    /**
     * Returns a region digraph dump file from the given dump directory.
     * 
     * @param dumpDir dump directory
     * @return the region digraph dump file
     * @throws IOException when reading
     */
    public File getRegionDigraphDump(File dumpDir) throws IOException;

}
