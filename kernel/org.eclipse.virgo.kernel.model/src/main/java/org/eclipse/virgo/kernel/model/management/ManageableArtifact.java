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

package org.eclipse.virgo.kernel.model.management;

import java.util.Map;

import javax.management.MXBean;
import javax.management.ObjectName;

import org.eclipse.equinox.region.Region;
import org.osgi.framework.Version;

/**
 * Represents an artifact in the runtime model of this system. Acts as a generic interface that delegates to the richer
 * {@Artifact} type and translates types that are JMX-unfriendly to types that are JMX-friendly.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations must be threadsafe
 * 
 */
@MXBean
public interface ManageableArtifact {

    /**
     * Start this {@link ManageableArtifact}
     */
    void start();

    /**
     * Stop this {@link ManageableArtifact}
     */
    void stop();

    /**
     * Update and refresh the contents of this {@link ManageableArtifact}
     * @return true if refresh is successful, false if refresh is not done
     */
    boolean refresh();

    /**
     * Uninstall this {@link ManageableArtifact}
     */
    void uninstall();

    /**
     * Get the {@link ManageableArtifact}s that this {@link ManageableArtifact} depends on. The dependency can be of any
     * kind and will be determined by the type of {@link ManageableArtifact} represented.
     * 
     * @return This {@link ManageableArtifact}'s dependents
     */
    ObjectName[] getDependents();

    /**
     * Get the type of this {@link ManageableArtifact}
     * 
     * @return The type of this {@link ManageableArtifact}
     */
    String getType();

    /**
     * Get the name of this {@link ManageableArtifact}
     * 
     * @return The name of this {@link ManageableArtifact}
     */
    String getName();

    /**
     * Get the {@link Version} of this {@link ManageableArtifact}
     * 
     * @return The @{link Version} of this {@link ManageableArtifact}
     */
    String getVersion();

    /**
     * Get the state of this {@link ManageableArtifact}
     * 
     * @return The state of this {@link ManageableArtifact}
     */
    String getState();
    
    /**
     * Get the {@link Region} of this {@link ManageableArtifact} or the empty string if this artifact does not belong in a region
     * 
     * @return the {@link Region} of this {@link ManageableArtifact} or the enpty string if this artifact does not belong in a region
     */
    String getRegion();

    /**
     * Get this {@link ManageableArtifact}'s properties. This map is free to hold any properties contributed by any
     * collaborator.
     * 
     * @return This {@link ManageableArtifact}'s properties
     */
    Map<String, String> getProperties();
}
