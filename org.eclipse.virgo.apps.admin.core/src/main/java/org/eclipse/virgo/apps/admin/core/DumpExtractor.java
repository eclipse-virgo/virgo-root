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

package org.eclipse.virgo.apps.admin.core;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipException;

/**
 * <p>
 * DumpExtractor provides an interface for clients to obtain state dump zips as produced by equinox.
 * </p>
 *
 * <strong>Concurrent Semantics</strong><br/>
 *
 * Implementations of DumpExtractor must be threadsafe
 *
 */
public interface DumpExtractor {

    /**
     * A zip file from the folder with a name from the {@link DumpInspectorService#getDumpEntries(String)} method.
     * 
     * @param dump name
     * @return the zip file
     * @throws ZipException when zipping
     * @throws IOException when reading
     */
    public File getStateDump(String dump) throws ZipException, IOException;

}
