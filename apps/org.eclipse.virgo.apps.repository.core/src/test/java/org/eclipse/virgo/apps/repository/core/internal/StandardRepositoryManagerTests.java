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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import org.eclipse.virgo.apps.repository.core.internal.HostedRepositoryObjectNameFactory;
import org.eclipse.virgo.apps.repository.core.internal.StandardRepositoryManager;
import org.eclipse.virgo.kernel.services.work.WorkArea;
import org.eclipse.virgo.medic.test.eventlog.MockEventLogger;
import org.eclipse.gemini.web.core.ConnectorDescriptor;
import org.eclipse.gemini.web.core.WebContainerProperties;
import org.eclipse.virgo.repository.ArtifactBridge;
import org.eclipse.virgo.repository.Repository;
import org.eclipse.virgo.repository.RepositoryFactory;
import org.eclipse.virgo.repository.configuration.ExternalStorageRepositoryConfiguration;
import org.eclipse.virgo.repository.configuration.RepositoryConfiguration;
import org.eclipse.virgo.repository.ArtifactDescriptorPersister;
import org.eclipse.virgo.util.io.PathReference;


import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests for {@link StandardRepositoryManager}
 */
public class StandardRepositoryManagerTests {
    
    private final MockEventLogger eventLogger = new MockEventLogger();

    
    private final Repository repository = createMock(Repository.class);
    {
        expect(this.repository.getName()).andReturn("testRepoName").atLeastOnce();
        replay(this.repository);
    }
    
    private final Map<String, RepositoryConfiguration> configurationMap = new HashMap<String, RepositoryConfiguration>();
    {
        final RepositoryConfiguration repositoryConfiguration 
        = new ExternalStorageRepositoryConfiguration("testRepoName", new File("build/indexDir"), new HashSet<ArtifactBridge>(), "testDirToWatch", "testHostRepoDomain");
        this.configurationMap.put("testRepoName", repositoryConfiguration);
    }
    
    private final WorkArea workArea = createMock(WorkArea.class);
    {
        PathReference pathRef = new PathReference("abc");
        expect(this.workArea.getWorkDirectory()).andReturn(pathRef).atLeastOnce();
        replay(this.workArea);
    }

    private final HostedRepositoryObjectNameFactory objectNameFactory = new HostedRepositoryObjectNameFactory("testDomain");
    

    @Test
    public void createOK() throws Exception {
        WebContainerProperties webContainerProperties = createMock(WebContainerProperties.class);
        Set<ConnectorDescriptor> mockConnectors = new HashSet<ConnectorDescriptor>();
        ConnectorDescriptor mockConnectorDescriptor = createMock(ConnectorDescriptor.class);
        expect(mockConnectorDescriptor.sslEnabled()).andReturn(false);
        expect(mockConnectorDescriptor.getScheme()).andReturn("http-8080");
        expect(mockConnectorDescriptor.getPort()).andReturn(8080);
        mockConnectors.add(mockConnectorDescriptor);
        expect(webContainerProperties.getConnectorDescriptors()).andReturn(mockConnectors);
        
        RepositoryFactory repositoryFactory = createMock(RepositoryFactory.class);
        expect(repositoryFactory.createRepository(isA(RepositoryConfiguration.class), isA(ArtifactDescriptorPersister.class))).andReturn(this.repository);
        
        replay(repositoryFactory, webContainerProperties, mockConnectorDescriptor);
        
        final StandardRepositoryManager srm = new StandardRepositoryManager(this.configurationMap, repositoryFactory, this.objectNameFactory, webContainerProperties, this.eventLogger);
        
        assertEquals("Events logged during creation", new ArrayList<String>(), this.eventLogger.getLoggedEvents());
        srm.start();
        srm.stop();
        assertEquals("Events were logged during start or stop", new ArrayList<String>(), this.eventLogger.getLoggedEvents());
        assertNotNull("Repository index not created", srm.getIndex("testRepoName"));
        
        verify(repositoryFactory, webContainerProperties, mockConnectorDescriptor);
    }
}
