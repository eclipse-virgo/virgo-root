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

import java.io.InputStream;
import java.io.OutputStream;

/**
 * An abstraction that represents an entry inside of an artifact
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe
 * 
 * @see ArtifactFS
 */
public interface ArtifactFSEntry {

    /**
     * Gets the path of this entry, relative to the root {@link ArtifactFS}
     * 
     * @return the path of this entry
     */
    String getPath();

    /**
     * Gets the name of this entry
     * 
     * @return the name of this entry
     */
    String getName();

    /**
     * Deletes this entry from the artifact
     * 
     * @return whether the deletion of this artifact was successful
     */
    boolean delete();

    /**
     * Whether this entry is a directory
     * 
     * @return whether this entry is a directory
     */
    boolean isDirectory();

    /**
     * Gets an {@link InputStream} for reading from this artifact. This method is not supported for directory artifacts.
     * 
     * @return an {@link InputStream} for reading from this artifact
     */
    InputStream getInputStream();

    /**
     * Gets an {@link OutputStream} for writing to this artifact. The artifact is created if it does not already exist
     * and is overwritten if it does. This method is not supported for directory artifacts.
     * 
     * @return an {@link OutputStream} for writing to this artifact
     */
    OutputStream getOutputStream();

    /**
     * Returns a list of children entries for this entry. This method is only supported for directory artifacts.
     * 
     * @return a list of children entries.
     */
    ArtifactFSEntry[] getChildren();

    /**
     * Returns a new {@link ArtifactFS} that is rooted at this entry
     * 
     * @return a new {@link ArtifactFS}
     */
    ArtifactFS getArtifactFS();

    /**
     * Returns <code>true</code> if this entry exists, otherwise <code>false</code>
     * @return <code>true</code> if this entry exists, otherwise <code>false</code>
     */
    boolean exists();
}
