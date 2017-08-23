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

package org.eclipse.virgo.kernel.artifact.fs;

import java.io.File;

/**
 * An abstraction that represents the filesystem location of an artifact
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations must be threadsafe
 * 
 */
public interface ArtifactFS {

    /**
     * Gets an entry from this artifact
     * 
     * @param name The name of the entry to retrieve
     * @return An {@link ArtifactFSEntry} that represents the entry in the artifact
     */
    ArtifactFSEntry getEntry(String name);

    /**
     * Gets the file upon which this {@link ArtifactFS} is built
     * 
     * @return the underlying file for this {@link ArtifactFS}
     */
    File getFile();
}
