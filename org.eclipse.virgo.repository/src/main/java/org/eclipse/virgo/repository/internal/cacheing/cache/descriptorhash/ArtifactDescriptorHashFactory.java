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

package org.eclipse.virgo.repository.internal.cacheing.cache.descriptorhash;

import org.eclipse.virgo.repository.RepositoryAwareArtifactDescriptor;


/**
 * {@link ArtifactDescriptorHashFactory} creates {@link ArtifactDescriptorHash} instances.
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * Implementations of this interface must be thread safe.
 *
 */
public interface ArtifactDescriptorHashFactory {
    
    ArtifactDescriptorHash createArtifactDescriptorHash(RepositoryAwareArtifactDescriptor artifactDescriptor);

}
