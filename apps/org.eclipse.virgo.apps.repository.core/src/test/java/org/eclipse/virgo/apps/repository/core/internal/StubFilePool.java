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

import org.eclipse.virgo.apps.repository.core.internal.FilePool;
import org.eclipse.virgo.apps.repository.core.internal.FilePoolException;



/**
 * {@link FilePool} used for testing
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 * Immutable and thread-safe
 *
 */
class StubFilePool implements FilePool {

    private File soleFile;
    private File nextFile;
    
    /**
     * null parameters mean pool empty, or no next file respectively.
     * @param soleFile file in pool
     * @param nextFile next file for generate call
     */
    public StubFilePool(File soleFile, File nextFile) {
        this.soleFile = soleFile;
        this.nextFile = nextFile;
    }
    
    /** 
     * {@inheritDoc}
     */
    public File generateNextPoolFile() throws FilePoolException {
        if (this.nextFile==null) 
            throw new FilePoolException("Stub: no next file.");
        return this.nextFile;
    }

    /** 
     * {@inheritDoc}
     */
    public File getMostRecentPoolFile() throws FilePoolException {
        if (this.soleFile==null)
            throw new FilePoolException("Stub: pool empty");
        return this.soleFile;
    }

    /** 
     * {@inheritDoc}
     */
    public void putFileInPool(File indexFile) {
        if (indexFile != this.nextFile) 
            return;
        this.soleFile = indexFile;
        this.nextFile = null;
    }

    /**
     * For testing only
     * @param nextFile for generate call.
     */
    void setNextFile(File nextFile) {
        this.nextFile = nextFile;
    }
    
}
