/*******************************************************************************
 * Copyright (c) 2012 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.kernel.install.artifact.internal;

import org.eclipse.virgo.util.io.PathReference;

/**
 * {@link ArtifactStore} manages the disk storage used to hold a working copy of an artifact. When updating the
 * artifact, the old version of the artifact is saved in case the update operation fails and the artifact needs to be
 * restored to the old version (in which case the new version is deleted). At most one saved version is maintained to
 * conserve disk space if the artifact is updated multiple times.
 * <p />
 * An implementation of this interface may expose a single current path and save the artifact by moving it, it may
 * create a series of paths without moving the artifact, or it may use another approach.
 * <p />
 * Note that instances of this interface may be stateful, so only a single instance should be used to manage the
 * disk storage for a particular artifact.
 * <p />
 * <strong>Concurrent Semantics</strong><br />
 * Implementations of this interface need not be thread safe.
 */
interface ArtifactStore {

    /**
     * Gets the current artifact path.
     * 
     * @return a {@link PathReference} to the current artifact path.
     */
    public PathReference getCurrentPath();

    /**
     * Saves the current artifact and prepares the current artifact path storage ready for the new artifact. Only one
     * saved version is kept: any previously saved version is deleted.
     */
    public void save();

    /**
     * Deletes the current artifact and restores the saved version. The new current artifact has the same path as when
     * it was last current.
     */
    public void restore();

}