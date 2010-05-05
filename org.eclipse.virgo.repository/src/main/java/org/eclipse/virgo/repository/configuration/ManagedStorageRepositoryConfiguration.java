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
import org.eclipse.virgo.repository.ArtifactDescriptor;
import org.eclipse.virgo.repository.Repository;


/**
 * Configuration for a {@link Repository} that manages the storage of the {@link ArtifactDescriptor ArtifactDescriptors}
 * that it contains.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * Thread-safe.
 * 
 */
public class ManagedStorageRepositoryConfiguration extends PersistentRepositoryConfiguration {

    private final File storageDirectory;

    /**
     * Creates configuration for a new <code>Repository</code> with managed storage. The <code>Repository</code> will
     * have the supplied <code>name</name> and will write its index to the supplied
     * <code>indexLocation</code>. The <code>Repository</code> will manage the storage of its artifacts, storing them
     * beneath the supplied <code>storageDirectory</code>. The structure of the storage beneath the supplied directory
     * is unspecified.
     * 
     * @param name The name of the repository
     * @param indexLocation The location to which the repository should write its index
     * @param artifactBridges The artifact bridges to be used to generate artifacts when items are added to the
     *        repository
     * @param storageDirectory The directory beneath which the repository should store its artifacts.
     * @param mBeanDomain the domain name of the management beans registered -- none registered if this is null
     */
    public ManagedStorageRepositoryConfiguration(String name, File indexLocation, Set<ArtifactBridge> artifactBridges, File storageDirectory,
        String mBeanDomain) {
        super(name, indexLocation, artifactBridges, mBeanDomain);
        this.storageDirectory = storageDirectory;
    }

    public File getStorageLocation() {
        return this.storageDirectory;
    }
}
