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

package org.eclipse.virgo.kernel.model.internal;

import org.eclipse.virgo.kernel.model.Artifact;

/**
 * A listener for changes in the {@link NotifyingRuntimeArtifactRepository}
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations must be threadsafe
 * 
 * @see NotifyingRuntimeArtifactRepository
 */
public interface ArtifactRepositoryListener {

    /**
     * Called when an {@link Artifact} is added to the repository
     * 
     * @param artifact The {@link Artifact} that was added to the repository
     */
    void added(Artifact artifact);

    /**
     * Called when an {@link Artifact} is removed from the repository
     * 
     * @param artifact The {@link Artifact} that was removed from the repository
     */
    void removed(Artifact artifact);
}
