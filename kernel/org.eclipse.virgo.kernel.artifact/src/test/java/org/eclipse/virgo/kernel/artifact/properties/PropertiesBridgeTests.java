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

package org.eclipse.virgo.kernel.artifact.properties;

import static org.junit.Assert.*;
import static org.easymock.EasyMock.*;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.junit.Test;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import org.easymock.EasyMock;
import org.eclipse.virgo.kernel.artifact.StubHashGenerator;
import org.eclipse.virgo.kernel.artifact.properties.PropertiesBridge;
import org.eclipse.virgo.repository.ArtifactDescriptor;
import org.eclipse.virgo.repository.ArtifactGenerationException;
import org.eclipse.virgo.repository.Attribute;

/**
 */
public class PropertiesBridgeTests {

    @Test
    public void testGeneratePropertiesFile() throws ArtifactGenerationException {
        PropertiesBridge bridge = new PropertiesBridge(new StubHashGenerator(), EasyMock.createMock(ConfigurationAdmin.class));
        ArtifactDescriptor result = bridge.generateArtifactDescriptor(new File("src/test/resources/properties/foo.properties"));
        assertNotNull(result);
    }

    @Test(expected = ArtifactGenerationException.class)
    public void testFileDoesNotExist() throws ArtifactGenerationException {
        PropertiesBridge bridge = new PropertiesBridge(new StubHashGenerator(), EasyMock.createMock(ConfigurationAdmin.class));

        File file = new File("src/test/resources/properties/not.exist.properties");
        bridge.generateArtifactDescriptor(file);
    }

    @Test
    public void testGenerateNotPropertiesFile() throws ArtifactGenerationException {
        PropertiesBridge bridge = new PropertiesBridge(new StubHashGenerator(), createMock(ConfigurationAdmin.class));
        ArtifactDescriptor descriptor = bridge.generateArtifactDescriptor(new File("src/test/resources/bar.noterties"));
        assertNull(descriptor);
    }

    @Test
    public void testGenerateWithFactoryPid() throws ArtifactGenerationException {
        final String factoryPid = "test.factory.pid";
        final String propertiesFile = "src/test/resources/properties/factoryPid.properties";

        ConfigurationAdmin mockConfigAdmin = createMock(ConfigurationAdmin.class);
        Configuration mockConfiguration = createMock(Configuration.class);

        try {
            expect(mockConfigAdmin.createFactoryConfiguration(factoryPid, null)).andReturn(mockConfiguration);
        } catch (IOException e) {
            fail(e.getMessage());
        }
        expect(mockConfiguration.getPid()).andReturn("1");

        replay(mockConfigAdmin, mockConfiguration);
        
        PropertiesBridge bridge = new PropertiesBridge(new StubHashGenerator(), mockConfigAdmin);
        ArtifactDescriptor descriptor = bridge.generateArtifactDescriptor(new File(propertiesFile));
        
        verify(mockConfigAdmin, mockConfiguration);
        
        // asserts
        assertNotNull(descriptor);
        assertEquals("1", descriptor.getName());
        // only expect one attribute
        Set<Attribute> attrSet = descriptor.getAttribute(ConfigurationAdmin.SERVICE_FACTORYPID);
        assertEquals(1, attrSet.size());
        Attribute attr = attrSet.iterator().next();
        assertNotNull(factoryPid, attr.getValue());
    }
    
    @Test(expected = ArtifactGenerationException.class)
    public void testGenerateWithFactoryPidAndIoExceptionFromConfigAdmin() throws ArtifactGenerationException {
        final String factoryPid = "test.factory.pid";
        final String propertiesFile = "src/test/resources/properties/factoryPid.properties";

        ConfigurationAdmin mockConfigAdmin = createMock(ConfigurationAdmin.class);
        
        try {
            expect(mockConfigAdmin.createFactoryConfiguration(factoryPid, null)).andThrow(new IOException("exception from configadmin"));
        } catch (IOException e) {
            // I really hate checked exceptions.
        }
        
        replay(mockConfigAdmin);
        PropertiesBridge bridge = new PropertiesBridge(new StubHashGenerator(), mockConfigAdmin);
        bridge.generateArtifactDescriptor(new File(propertiesFile));
        verify(mockConfigAdmin);
    }
    
    @Test
    public void makeSureThatServicePidIsTakenFromTheFileProvidedProperties() throws ArtifactGenerationException {
        final String name = "service.pid.in.the.file";
        PropertiesBridge bridge = new PropertiesBridge(new StubHashGenerator(), EasyMock.createMock(ConfigurationAdmin.class));
        ArtifactDescriptor result = bridge.generateArtifactDescriptor(new File("src/test/resources/properties/with-service-pid.properties"));
        assertNotNull(result);
        assertEquals(name, result.getName());
    }
}
