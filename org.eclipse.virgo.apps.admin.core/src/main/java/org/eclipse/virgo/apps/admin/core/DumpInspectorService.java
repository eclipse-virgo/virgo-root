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
import java.util.List;

/**
 * <code>DumpManagerService</code> defines a simple service API for viewing dumps within the server.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations should be thread-safe.
 * 
 */
public interface DumpInspectorService {

    /**
     * @return a list of {@link File} of the dumps available for inspection.
     */
    public List<File> findAvaliableDumps();

    /**
     * @param dumpID as returned by a call to {@link DumpInspectorService#findAvaliableDumps}.
     * @return list of Strings
     */
    public List<String> getDumpEntries(String dumpID);

    /**
     * Read the contents of a dump entry and return it as a string
     * @param dumpID of dump
     * @param entryName in dump
     * 
     * @return the raw contents of the requested dump entry as a String
     */
    public String getDumpEntry(String dumpID, String entryName);

}
