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

package org.eclipse.virgo.kernel.model;

import java.util.Set;

import org.osgi.framework.Version;

/**
 * A single in-memory repository with representations of all artifacts in the running system. Operations are generally
 * executed against the artifacts themselves rather than the repository.
 * <p />
 * Bundle artifacts may reside in a number of {@link Region Regions}, but certain repository operations such as remove
 * and get operate only on bundle artifacts in the user region. This is to preserve backward compatibility with Virgo
 * 2.1.0 for the RuntimeArtifactRepository interface (and for JMX).
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations must be threadsafe
 * 
 */
public interface RuntimeArtifactRepository {

    /**
     * Add an {@link Artifact} to this repository
     * 
     * @param artifact The {@link Artifact} to add
     * @return <code>true</code> if this repository did not already contain the specified {@link Artifact}
     * @see Set
     */
    boolean add(Artifact artifact);

    /**
     * Remove an {@link Artifact} from this repository. If the artifact is a bundle, it is only removed if the bundle is
     * in the user region.
     * 
     * @param type The type of the {@link Artifact} to remove
     * @param name The name of the {@link Artifact} to remove
     * @param version The {@link Version} of the {@link Artifact} to remove
     * @return <code>true</code> if this repository contained the specified {@link Artifact}
     */
    boolean remove(String type, String name, Version version);

    /**
     * Returns the entire collection of {@link Artifact}s contained within this repository. The returned collection
     * should not be mutated by this repository when it changes. Only a subsequent call to this method will show changes
     * to the contents of the repository.
     * 
     * @return The entire collection of artifacts contained within this repository
     */
    Set<Artifact> getArtifacts();

    /**
     * Gets a specific {@link Artifact} from this repository. If the artifact is a bundle, it is only returned if the
     * bundle is in the user region.
     * 
     * @param type The type of the {@link Artifact} to get
     * @param name The name of the {@link Artifact} to get
     * @param version The {@link Version} of the {@link Artifact} to get
     * @return The {@link Artifact} represented by this type, name, and version if it is in the repository, otherwise
     *         <code>null</code>
     */
    Artifact getArtifact(String type, String name, Version version);
}
