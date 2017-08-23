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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertNotNull;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.virgo.repository.WatchableRepository;
import org.eclipse.virgo.repository.internal.ArtifactDescriptorDepository;
import org.eclipse.virgo.repository.internal.management.StandardWatchedStorageRepositoryInfo;
import org.eclipse.virgo.repository.management.RepositoryInfo;
import org.eclipse.virgo.repository.management.WatchedStorageRepositoryInfo;
import org.junit.Test;


public class StandardWatchedStorageRepositoryInfoTests extends AbstractRepositoryInfoTests {
    
    private static final String LOCATIONS_TEST_FILENAME = "a.jar";

    private final ArtifactDescriptorDepository depository = createMock(ArtifactDescriptorDepository.class);
    
    private final WatchableRepository repository = createMock(WatchableRepository.class);
    
    private final WatchedStorageRepositoryInfo repositoryInfo = getMyRepositoryInfo(this.repository, this.depository);
    
    /** 
     * {@inheritDoc}
     */
    @Override
    protected RepositoryInfo getRepositoryInfo(ArtifactDescriptorDepository depository) {
        return getMyRepositoryInfo(this.repository, depository);
    }

    private final static WatchedStorageRepositoryInfo getMyRepositoryInfo(WatchableRepository repository, ArtifactDescriptorDepository depository) {        
        StandardWatchedStorageRepositoryInfo repositoryInfo = new StandardWatchedStorageRepositoryInfo("unit-test", depository, repository);               
        return repositoryInfo;
    }

    @Test
    public void forceCheck() throws Exception {
        repositoryInfo.forceCheck();
    }
    
    @Test
    public void getArtifactLocations() throws Exception {
        expect(this.repository.getArtifactLocations(LOCATIONS_TEST_FILENAME)).andReturn(new HashSet<String>());
        replay(this.repository);
        
        Set<String> filepaths = repositoryInfo.getArtifactLocations(LOCATIONS_TEST_FILENAME);
        assertNotNull(filepaths);

        verify(this.repository);
    }
}
