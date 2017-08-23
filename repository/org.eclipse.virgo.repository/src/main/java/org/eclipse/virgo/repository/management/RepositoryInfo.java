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

package org.eclipse.virgo.repository.management;

import java.util.Set;

import javax.management.MXBean;

import org.eclipse.virgo.repository.ArtifactDescriptor;


/**
 * A management interface for a repository. Useful for exposing the contents of the repository via JMX.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations must be threadsafe
 * 
 */
@MXBean
public interface RepositoryInfo {
    
    /**
     * Gets the type of the repository
     * @return the type
     * @deprecated The type field in the MBean's descriptor should be used instead
     */
    String getType();

    /**
     * Gets the name of the repository
     * @return the name
     */
    String getName();

    /**
     * Gets a collection of the type, name, and version of all {@link ArtifactDescriptor}s in the repository
     * @return set of summary descriptors
     */
    Set<ArtifactDescriptorSummary> getAllArtifactDescriptorSummaries();
 
}
