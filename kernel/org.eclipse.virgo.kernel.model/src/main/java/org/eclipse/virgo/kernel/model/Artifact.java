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

import java.util.Map;
import java.util.Set;

import org.eclipse.equinox.region.Region;
import org.osgi.framework.Version;

/**
 * Represents an artifact in the runtime model of this system. Acts as a generic interface that delegates to more
 * specific functionality in the running system. In all likelihood, there should be very few sub-interfaces of this
 * interface but quite a few implementations of this interface.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations must be threadsafe
 * 
 */
public interface Artifact {

    /**
     * Start this {@link Artifact}
     */
    void start();

    /**
     * Stop this {@link Artifact}
     */
    void stop();

    /**
     * Update and refresh the contents of this {@link Artifact}
     * @return true if the refresh is successful, false if refresh is not performed
     */
    boolean refresh();

    /**
     * Uninstall this {@link Artifact}
     */
    void uninstall();

    /**
     * Get the type of this {@link Artifact}
     * 
     * @return The type of this {@link Artifact}
     */
    String getType();

    /**
     * Get the name of this {@link Artifact}
     * 
     * @return The name of this {@link Artifact}
     */
    String getName();

    /**
     * Get the {@link Version} of this {@link Artifact}
     * 
     * @return The {@link Version} of this {@link Artifact}
     */
    Version getVersion();
    
    /**
     * Get the {@link Region} of this {@link Artifact} or <code>null</code> if this artifact does not belong in a region
     * 
     * @return the {@link Region} of this {@link Artifact} or <code>null</code> if this artifact does not belong in a region
     */
    Region getRegion();

    /**
     * Get the state of this {@link Artifact}
     * 
     * @return The state of this {@link Artifact}
     */
    ArtifactState getState();

    /**
     * Get the {@link Artifact}s that this {@link Artifact} depends on. The dependency can be of any kind and will be
     * determined by the type of {@link Artifact} represented.
     * 
     * @return This {@link Artifact}'s dependents
     */
    Set<Artifact> getDependents();

    /**
     * Get this {@link Artifact}'s properties. This map is free to hold any properties contributed by any collaborator.
     * 
     * @return This {@link Artifact}'s properties
     */
    Map<String, String> getProperties();

}
