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

package org.eclipse.virgo.apps.repository.core.internal;

import java.io.File;

import org.junit.Test;

import org.eclipse.virgo.apps.repository.core.HostedRepositoryInfo;
import org.eclipse.virgo.apps.repository.core.internal.ExportingArtifactDescriptorPersister;
import org.eclipse.virgo.apps.repository.core.internal.HostedRepository;
import org.eclipse.virgo.apps.repository.core.internal.HostedRepositoryObjectNameFactory;
import org.eclipse.virgo.apps.repository.core.internal.HostedRepositoryUriMapper;
import org.eclipse.virgo.repository.Repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

/**
 * Tests for {@link HostedRepository} class
 * 
 */
public class HostedRepositoryTests {

    @Test
    public void createHostedRepository() throws Exception {
        Repository repository = createMock(Repository.class);
        expect(repository.getName()).andReturn("testHostRepoName").atLeastOnce();
        replay(repository);

        final File dummyIndexFile = new File("dummyIndexFile");
        ExportingArtifactDescriptorPersister persister = createMock(ExportingArtifactDescriptorPersister.class);
        expect(persister.exportIndexFile()).andReturn(dummyIndexFile).atLeastOnce();
        replay(persister);
        
        final HostedRepositoryUriMapper uriMapper = new HostedRepositoryUriMapper(0, repository.getName());
        
        HostedRepository hr = new HostedRepository(repository, persister, uriMapper,
            new HostedRepositoryObjectNameFactory("testHostedRepo"));
        assertNotNull("No hosted repository created", hr);
        assertNotNull("No index for Hosted repository", hr.getRepositoryIndex());
        assertEquals("Hosted repository misnamed.", "testHostRepoName", hr.getName());
        HostedRepositoryInfo mBean = hr.createMBean();
        assertEquals("Incorrect MBean name", "testHostRepoName", mBean.getLocalRepositoryName());
        assertEquals("Uri prefix not same as mapper",uriMapper.getUriPrefix(), mBean.getUriPrefix());
    }

}
