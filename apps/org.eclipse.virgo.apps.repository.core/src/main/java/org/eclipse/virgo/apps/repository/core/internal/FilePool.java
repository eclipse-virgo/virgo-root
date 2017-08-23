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

/**
 * Interface to manage a pool of files. Only the last file put in the pool is guaranteed to be present.
 * <p />
 *
 */
public interface FilePool {
    
    /**
     * Generates a new writable file which can be placed in the pool
     * @return newly created file
     * @throws FilePoolException if pool is full or cannot be written to
     */
    File generateNextPoolFile() throws FilePoolException;
    
    /**
     * Add file to pool.<br/>
     * Files not returned by {@link #generateNextPoolFile()} may be ignored.
     * @param indexFile to be placed in the pool
     * @throws FilePoolException if IOException occurs on File operations
     */
    void putFileInPool(File indexFile) throws FilePoolException;
    
    /**
     * @return the last file placed in the pool
     * @throws FilePoolException if the pool is empty
     */
    File getMostRecentPoolFile() throws FilePoolException;
}
