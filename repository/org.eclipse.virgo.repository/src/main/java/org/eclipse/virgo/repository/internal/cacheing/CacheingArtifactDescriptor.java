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

package org.eclipse.virgo.repository.internal.cacheing;

import java.net.URI;
import java.util.Set;

import org.eclipse.virgo.repository.Attribute;
import org.eclipse.virgo.repository.RepositoryAwareArtifactDescriptor;
import org.eclipse.virgo.repository.internal.cacheing.cache.RepositoryCache;
import org.osgi.framework.Version;


/**
 * {@link CacheingArtifactDescriptor} maintains a local disk cache of remote artifacts keyed by type, name, and version
 * and delivers URIs of cached artifacts instead of the corresponding remote URIs.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is thread safe.
 * 
 */
public final class CacheingArtifactDescriptor implements RepositoryAwareArtifactDescriptor {
    
    private final RepositoryAwareArtifactDescriptor source;
    
    private final RepositoryCache cache;

    CacheingArtifactDescriptor(RepositoryAwareArtifactDescriptor source, RepositoryCache cache) {
        this.source = source;
        this.cache = cache;
    }

    /**
     * {@inheritDoc}
     */
    public Set<Attribute> getAttribute(String name) {
        return this.source.getAttribute(name);
    }

    /**
     * {@inheritDoc}
     */
    public Set<Attribute> getAttributes() {
        return this.source.getAttributes();
    }

    /**
     * {@inheritDoc}
     */
    public String getFilename() {
        return this.source.getFilename();
    }

    /**
     * {@inheritDoc}
     */
    public String getName() {
        return this.source.getName();
    }

    /** 
     * {@inheritDoc}
     */
    public String getRepositoryName() {
        return this.source.getRepositoryName();
    }
    
    /**
     * {@inheritDoc}
     */
    public String getType() {
        return this.source.getType();
    }

    /**
     * Returns the {@link URI} of the cached artifact. If the artifact cannot be downloaded, throws a <code>RuntimeException</code>.
     * 
     * @return the cached <code>URI</code>
     */
    public URI getUri() {
        return this.cache.getUri(this);
    }
    
    /**
     * Returns the remote (uncached) {@link URI} of the artifact.
     * 
     * @return the remote (uncached) <code>URI</code>
     */
    public URI getRemoteUri() {
        return this.source.getUri();
    }

    /**
     * {@inheritDoc}
     */
    public Version getVersion() {
        return this.source.getVersion();
    }

}
