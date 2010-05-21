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

package org.eclipse.virgo.apps.repository.core;

import java.io.IOException;
import java.io.InputStream;

/**
 * A <code>RepositoryManager</code> manages repositories, providing access to repository indices and artifacts.
 * 
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations <strong>must</strong> be thread-safe.
 * 
 */
public interface RepositoryManager {

    /**
     * Returns the {@link RepositoryIndex} of the repository identified by the supplied repository name.
     * 
     * @param repositoryName The name of the repository for which the index is required
     * 
     * @return The named repository's index, or <code>null</code> if a repository with the supplied name is not known.
     * @throws IOException if index cannot be accessed
     */
    RepositoryIndex getIndex(String repositoryName) throws IOException;

    /**
     * Returns an <code>InputStream</code> from which the artifact, stored in the repository identified by the supplied
     * repository name, and identified by the supplied type, name, and version, can be read.
     * 
     * @param repositoryName The name of the repository that holds the required artifact
     * @param type The type of the required artifact
     * @param name The name of the required artifact
     * @param version The version of the required artifact
     * 
     * @return The requested artifact, or <code>null</code> if the artifact does not exist.
     */
    InputStream getArtifact(String repositoryName, String type, String name, String version);
}
