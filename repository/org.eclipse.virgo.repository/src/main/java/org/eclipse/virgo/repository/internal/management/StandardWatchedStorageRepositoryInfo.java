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

package org.eclipse.virgo.repository.internal.management;

import java.util.Set;

import org.eclipse.virgo.repository.WatchableRepository;
import org.eclipse.virgo.repository.internal.ArtifactDescriptorDepository;
import org.eclipse.virgo.repository.management.RepositoryInfo;
import org.eclipse.virgo.repository.management.WatchedStorageRepositoryInfo;



/**
 * Implementation of {@link RepositoryInfo} for a watched storage repository.
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread-safe
 *
 */
public class StandardWatchedStorageRepositoryInfo extends AbstractRepositoryInfo implements WatchedStorageRepositoryInfo {

    private static final String TYPE = "watched";
    private final WatchableRepository repository;

    public StandardWatchedStorageRepositoryInfo(String name, ArtifactDescriptorDepository depository, WatchableRepository watchableRepository) {
        super(name, depository);
        this.repository = watchableRepository;
    }
    
    public String getType() {
        return TYPE;
    }
    
    public void forceCheck() throws RuntimeException {
        try {
            this.repository.forceCheck();
        } catch (Exception e) {
            throw new RuntimeException("Exception returned from repository", e);
        }
    }

    public Set<String> getArtifactLocations(String filename) {
        return this.repository.getArtifactLocations(filename);
    }
}
