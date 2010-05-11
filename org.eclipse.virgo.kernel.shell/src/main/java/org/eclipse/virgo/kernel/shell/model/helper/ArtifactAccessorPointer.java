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

/**
 * <p>
 * Simple store for a type, name and version that can be used to retrieve the full 
 * {@link ArtifactAccessor} at a later time.
 * </p>
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * Implementations should be thread safe
 *
 */
public interface ArtifactAccessorPointer extends Comparable<ArtifactAccessorPointer> {
    
    /**
     * @return The type of the Artifact
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
     * @return the state of the artifact represented by this pointer
     */
    String getState();
    
}
