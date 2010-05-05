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

import java.io.File;
import java.util.Set;

import org.eclipse.virgo.repository.ArtifactBridge;
import org.eclipse.virgo.repository.UriMapper;
import org.eclipse.virgo.repository.internal.PersistentRepository;


/**
 * Abstract superclass for the configuration of {@link PersistentRepository}
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe
 * 
 */
public abstract class PersistentRepositoryConfiguration extends LocalRepositoryConfiguration {

    private final File indexLocation;

    protected PersistentRepositoryConfiguration(String name, File indexLocation, Set<ArtifactBridge> artifactBridges, String mBeanDomain) {
        super(name, artifactBridges, mBeanDomain);
        this.indexLocation = indexLocation;
    }

    protected PersistentRepositoryConfiguration(String name, File indexLocation, Set<ArtifactBridge> artefactBridges, UriMapper uriMapper,
        String mBeanDomain) {
        super(name, artefactBridges, uriMapper, mBeanDomain);
        this.indexLocation = indexLocation;
    }

    /**
     * The location at which the repository should store its index
     * 
     * @return The index storage location
     */
    public File getIndexLocation() {
        return this.indexLocation;
    }
}
