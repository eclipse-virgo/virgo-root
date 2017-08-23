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

import java.util.Set;

import org.eclipse.virgo.kernel.model.Artifact;


/**
 * Interface used to provide the values of the dependents of a given {@link Artifact}. Implementations of this interface
 * should be registered in the OSGi Service Registry with a service property of <code>artifactType</code>. The value of
 * this property should indicate which type of artifact this {@link DependencyDeterminer} can be used for.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations should be threadsafe
 * 
 * @see AbstractArtifact
 */
public interface DependencyDeterminer {

    /**
     * Returns the collection of dependent {@link Artifact}s for a given root {@link Artifact}. Note that the artifacts
     * that are returned from this determiner should already be contained in the {@link org.eclipse.virgo.kernel.model.RuntimeArtifactRepository RuntimeArtifactRepository}. The
     * single instance of this may need to be injected into the {@link DependencyDeterminer} implementation.
     * 
     * @param rootArtifact The {@link Artifact} to find dependents for
     * @return The collection of dependent {@link Artifact}s
     */
    Set<Artifact> getDependents(Artifact rootArtifact);
}
