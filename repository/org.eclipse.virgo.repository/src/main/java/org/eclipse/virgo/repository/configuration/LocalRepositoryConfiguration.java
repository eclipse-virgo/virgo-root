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

package org.eclipse.virgo.repository.configuration;

import java.util.Set;

import org.eclipse.virgo.repository.ArtifactBridge;
import org.eclipse.virgo.repository.Repository;
import org.eclipse.virgo.repository.UriMapper;
import org.eclipse.virgo.repository.internal.IdentityUriMapper;


/**
 * Abstract super class for the configuration of a local {@link Repository}.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * Thread-safe.
 * 
 */
public abstract class LocalRepositoryConfiguration extends RepositoryConfiguration {

    private final Set<ArtifactBridge> artifactBridges;

    private UriMapper uriMapper;

    protected LocalRepositoryConfiguration(String name, Set<ArtifactBridge> artifactBridges, String mBeanDomain) {
        this(name, artifactBridges, new IdentityUriMapper(), mBeanDomain);
    }

    protected LocalRepositoryConfiguration(String name, Set<ArtifactBridge> artefactBridges, UriMapper uriMapper, String mBeanDomain) {
        super(name, mBeanDomain);

        this.artifactBridges = artefactBridges;
        this.uriMapper = uriMapper;
    }

    /**
     * The <code>ArtifactBridge</code>s to be used to generate artifacts when items are added to the
     * <code>Repository</code>.
     * 
     * @return the repository's artefact bridges.
     */
    public Set<ArtifactBridge> getArtefactBridges() {
        return this.artifactBridges;
    }

    /**
     * The <code>UriMapper</code> to be used to map <code>URIs</code> stored in the repository's index.
     * 
     * @return the repository configuration's uri mapper.
     */
    public UriMapper getUriMapper() {
        return this.uriMapper;
    }

    /**
     * Set the <code>UriMapper</code> to be used to map <code>URIs</code> stored in the repository's index.
     * 
     * @param uriMapper mapper to be taken by repository.
     */
    public void setUriMapper(UriMapper uriMapper) {
        this.uriMapper = uriMapper;
    }

}
