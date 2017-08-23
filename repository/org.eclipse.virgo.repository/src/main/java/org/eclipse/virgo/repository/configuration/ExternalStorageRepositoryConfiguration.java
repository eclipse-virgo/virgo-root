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
import org.eclipse.virgo.repository.Repository;
import org.eclipse.virgo.repository.UriMapper;
import org.eclipse.virgo.repository.internal.IdentityUriMapper;


/**
 * Configuration for a {@link Repository} that is populated by scanning the local filesystem and finding nodes that
 * match the supplied Ant-style path pattern.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * Thread-safe.
 * 
 */
public class ExternalStorageRepositoryConfiguration extends PersistentRepositoryConfiguration {

    private final String searchPattern;

    /**
     * Creates configuration for a new <code>Repository</code> with external storage. The <code>Repository</code> will
     * have the supplied <code>name</code> and will write its index to the supplied
     * <code>indexLocation</code>. The <code>Repository</code> will find artifacts by scanning the filesystem looking
     * for nodes that match the supplied Ant-style path pattern.
     * 
     * @param name The name of the repository
     * @param indexLocation The location to which the repository should write its index
     * @param artifactBridges The artifact bridges to be used to generate artifacts when items are added to the
     *        repository
     * @param searchPattern The Ant-style path pattern to use to identify artifacts
     * @param mBeanDomain domain name of repository management beans to register (none registered if null)
     */
    public ExternalStorageRepositoryConfiguration(String name, File indexLocation, Set<ArtifactBridge> artifactBridges, String searchPattern,
        String mBeanDomain) {
        this(name, indexLocation, artifactBridges, searchPattern, new IdentityUriMapper(), mBeanDomain);
    }

    /**
     * Creates configuration for a new <code>Repository</code> with external storage. The <code>Repository</code> will
     * have the supplied <code>name</code> and will write its index to the supplied <code>indexLocation</code>. The
     * <code>Repository</code> will find artifacts by scanning the filesystem looking for nodes that match the supplied
     * Ant-style path pattern.
     * 
     * @param name The name of the repository
     * @param indexLocation The location to which the repository should write its index
     * @param artifactBridges The artifact bridges to be used to generate artifacts when items are added to the
     *        repository
     * @param searchPattern The Ant-style path pattern to use to identify artifacts
     * @param uriMapper converter to externalise the URI exposed by this repository
     * @param mBeanDomain domain name of repository management beans to register (none registered if null)
     */
    public ExternalStorageRepositoryConfiguration(String name, File indexLocation, Set<ArtifactBridge> artifactBridges, String searchPattern,
        UriMapper uriMapper, String mBeanDomain) {
        super(name, indexLocation, artifactBridges, uriMapper, mBeanDomain);
        this.searchPattern = searchPattern;
    }

    /**
     * Returns the Ant-style path pattern to be used to search for artifacts.
     * 
     * @return The Ant-style path pattern
     */
    public String getSearchPattern() {
        return searchPattern;
    }
}
