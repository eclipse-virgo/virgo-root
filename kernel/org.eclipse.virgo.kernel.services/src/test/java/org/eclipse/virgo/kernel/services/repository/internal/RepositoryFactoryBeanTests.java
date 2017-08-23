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

package org.eclipse.virgo.kernel.services.repository.internal;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.junit.Test;

import org.eclipse.virgo.kernel.services.repository.internal.RepositoryFactoryBean;
import org.eclipse.virgo.medic.test.eventlog.MockEventLogger;
import org.eclipse.virgo.repository.ArtifactBridge;
import org.eclipse.virgo.repository.Repository;
import org.eclipse.virgo.repository.RepositoryFactory;


/**
 */
public class RepositoryFactoryBeanTests {

    private static MockEventLogger mockEventLogger = new MockEventLogger();
    
    @Test
    public void testCreateEmpty() throws Exception {
        mockEventLogger.reinitialise();
        Properties properties = new Properties();
        RepositoryFactory factory = createMock(RepositoryFactory.class);
        File work = new File("build");
        
        RepositoryFactoryBean bean = new RepositoryFactoryBean(properties, mockEventLogger, factory, work, Collections.<ArtifactBridge>emptySet(), null);
        Repository r = bean.getObject();
        assertNotNull(r);
        assertTrue("Event KS0001I was not logged.", mockEventLogger.isLogged("KS0001I"));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testCreate() throws Exception {
        File work = new File("build");
        mockEventLogger.reinitialise();
        
        Properties properties = new Properties();
        properties.put("bundles.type", "external");
        properties.put("bundles.searchPattern", "build/*.jar");
        properties.put("chain", "bundles");
        
        RepositoryFactory factory = createMock(RepositoryFactory.class);
        expect(factory.createRepository(isA(List.class))).andReturn(null);
        replay(factory);
        
        RepositoryFactoryBean bean = new RepositoryFactoryBean(properties, mockEventLogger, factory, work, Collections.<ArtifactBridge>emptySet(), null);
        bean.getObject();
        
        verify(factory);
        assertFalse("Events were logged.", mockEventLogger.getCalled());
    }
    
    @Test
    public void testBasicContract() {
        mockEventLogger.reinitialise();
        Properties properties = new Properties();
        RepositoryFactory factory = createMock(RepositoryFactory.class);
        File work = new File("build");
        
        RepositoryFactoryBean bean = new RepositoryFactoryBean(properties, mockEventLogger, factory, work, Collections.<ArtifactBridge>emptySet(), null);
        assertTrue(bean.isSingleton());
        assertEquals(Repository.class, bean.getObjectType());
        assertFalse("Events were logged.", mockEventLogger.getCalled());
    }
}
