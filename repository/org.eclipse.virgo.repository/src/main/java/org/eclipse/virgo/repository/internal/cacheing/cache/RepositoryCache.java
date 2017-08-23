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

package org.eclipse.virgo.repository.internal.cacheing.cache;

import java.net.URI;

import org.eclipse.virgo.repository.RepositoryAwareArtifactDescriptor;


/**
 * {@link RepositoryCache} is an internal interface to a repository cache implementation.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations of this interface must be thread safe.
 * 
 */
public interface RepositoryCache {

    /**
     * Returns the URI of the possibly cached artifact for the given artifact descriptor. If the cached artifact is out
     * of date, it is refreshed first. If the artifact is not cached for any reason, returns the URI from the
     * repository.
     * 
     * @param artifactDescriptor the artifact required
     * @return the URI of the possibly cached artifact
     */
    URI getUri(RepositoryAwareArtifactDescriptor artifactDescriptor);

}
