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

package org.eclipse.virgo.apps.admin.core.dump;

import java.io.File;


/**
 * <p>
 * DumpPathLocator is used internally to locate the location of the dumps 
 * folder, the dumps within it and the individual dump entries within those.
 * </p>
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * Implementations of DumpPathLocator should be threadsafe
 *
 */
public interface DumpPathLocator {
    
    /**
     * @return directory where the dumps are to be found.
     */
    File getDumpDirectory();
    
    /**
     * When given a folder name, this will return that dump as a directory
     * Will return null if there is no such dump.
     * 
     * @param folderName
     * @return dump directory
     */
    File getDumpFolder(String folderName);
    
    /**
     * When given a folder name and an entry name, this will return a file representing that dump entry.
     * Will return null if there is no such entry.
     * 
     * @param folderName
     * @param fileName
     * @return entry in dump
     */
    File getDumpEntryFile(String folderName, String fileName);
}
