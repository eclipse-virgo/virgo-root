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

package org.eclipse.virgo.repository;

/**
 * An extension to the {@link ArtifactDescriptor} interface that contains information about the repository this artifact
 * was returned from.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations should be threadsafe
 * 
 */
public interface RepositoryAwareArtifactDescriptor extends ArtifactDescriptor {

    /**
     * Get the descriptive name of the repository from which the artifact is loaded. This is null if the artifact is not
     * loaded or no repository is known.
     * 
     * @return the name of the repository
     */
    String getRepositoryName();
}
