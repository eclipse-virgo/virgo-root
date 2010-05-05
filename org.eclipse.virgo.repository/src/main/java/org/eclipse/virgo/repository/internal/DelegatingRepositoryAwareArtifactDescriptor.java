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

package org.eclipse.virgo.repository.internal;

import java.util.Set;

import org.eclipse.virgo.repository.ArtifactDescriptor;
import org.eclipse.virgo.repository.Attribute;
import org.eclipse.virgo.repository.RepositoryAwareArtifactDescriptor;
import org.eclipse.virgo.repository.UriMapper;
import org.osgi.framework.Version;


public final class DelegatingRepositoryAwareArtifactDescriptor implements RepositoryAwareArtifactDescriptor {

    private final ArtifactDescriptor delegate;

    private final String repositoryName;

    private final UriMapper mapper;

    public DelegatingRepositoryAwareArtifactDescriptor(ArtifactDescriptor delegate, String repositoryName, UriMapper mapper) {
        this.delegate = delegate;
        this.repositoryName = repositoryName;
        this.mapper = mapper;
    }

    public String getRepositoryName() {
        return this.repositoryName;
    }

    public Set<Attribute> getAttribute(String name) {
        return this.delegate.getAttribute(name);
    }

    public Set<Attribute> getAttributes() {
        return this.delegate.getAttributes();
    }

    public String getFilename() {
        return this.delegate.getFilename();
    }

    public String getName() {
        return this.delegate.getName();
    }

    public String getType() {
        return this.delegate.getType();
    }

    public java.net.URI getUri() {
        return this.mapper.map(this.delegate.getUri(), this.delegate.getType(), this.delegate.getName(), this.delegate.getVersion());
    }

    public Version getVersion() {
        return this.delegate.getVersion();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this.getClass().equals(obj.getClass())) {
            return this.delegate.equals(((DelegatingRepositoryAwareArtifactDescriptor) obj).delegate);
        } else if (this.delegate.getClass().isAssignableFrom(obj.getClass())) {
            return this.delegate.equals(obj);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.delegate.hashCode();
    }

    @Override
    public String toString() {
        return this.delegate.toString();
    }

}
