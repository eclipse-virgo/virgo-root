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

package org.eclipse.virgo.kernel.shell.model.helper;

import java.util.Map;
import java.util.Set;


/**
 * <p>
 * ArtifactAccessor is a representation of an Artifact from the Runtime Artifact Model.
 * </p>
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * Implementations should be thread safe.
 *
 */
public interface ArtifactAccessor {
    
    /**
     * @return the type of the Artifact
     */
    String getType();

    /**
     * @return The name of the Artifact
     */
    String getName();

    /**
     * @return The version of the Artifact as a String
     */
    String getVersion();
    
    /**
     * @return The state of the Artifact
     */
    String getState();
    
    /**
     * @return Any additional attributes that have been registered against the Artifact in the RAM
     */
    Map<String, Object> getAttributes();

    /**
     * @return a <code>Map&lt;String, String&gt;</code> of this artifact's properties
     */
    Map<String, String> getProperties();

    /**
     * @return A set of {@link ArtifactAccessorPointer}s of all the Artifacts that this Artifact depends on
     */
    Set<ArtifactAccessorPointer> getDependents();
}
