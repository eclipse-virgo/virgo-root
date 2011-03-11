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

package org.eclipse.virgo.kernel.shell.model.helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.management.ManagementFactory;
import java.util.List;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.eclipse.virgo.kernel.shell.model.helper.ArtifactAccessor;
import org.eclipse.virgo.kernel.shell.model.helper.ArtifactAccessorPointer;
import org.eclipse.virgo.kernel.shell.model.helper.RamAccessorHelper;
import org.eclipse.virgo.kernel.shell.model.helper.StandardRamAccessorHelper;
import org.junit.Before;
import org.junit.Test;


/**
 */
public class StandardRamAccessorHelperTests {

    private static final String TYPE = "test.type";

    private static final String NAME = "test.name";

    private static final String VERSION = "test.version";

    private static final String TYPE_EXISTS = "test.type.exists";

    private static final String NAME_EXISTS = "test.name.exists";

    private static final String VERSION_EXISTS = "test.version.exists";

    private static final String ARTIFACT_MBEAN_FORMAT = "org.eclipse.virgo.kernel:type=Model,artifact-type=%s,name=%s,version=%s";
    
    private RamAccessorHelper ramAccessorHelper;
    
    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        ObjectName objectName = new ObjectName(String.format(ARTIFACT_MBEAN_FORMAT, TYPE_EXISTS, NAME_EXISTS, VERSION_EXISTS));
        if(!mBeanServer.isRegistered(objectName)) {
            DummyManagableArtifact dummyManagableArtifact = new DummyManagableArtifact(TYPE_EXISTS, NAME_EXISTS, VERSION_EXISTS);
            mBeanServer.registerMBean(dummyManagableArtifact, objectName);
            AttributeList attributeList = new AttributeList();
            attributeList.add(new Attribute("type", TYPE_EXISTS));
            attributeList.add(new Attribute("name", NAME_EXISTS));
            attributeList.add(new Attribute("version", VERSION_EXISTS));
            mBeanServer.setAttributes(objectName, attributeList);
        }
        ramAccessorHelper = new StandardRamAccessorHelper();
    }

    /**
     * Test method for {@link RamAccessorHelper#start(String, String, String)}.
     */
    @Test
    public void testStart() {        
        String message = this.ramAccessorHelper.start(TYPE_EXISTS, NAME_EXISTS, VERSION_EXISTS);
        assertNotNull(message);
        assertTrue(message.contains("successful"));
        assertTrue(message.contains("start"));
    }

    /**
     * Test method for {@link RamAccessorHelper#stop(String, String, String)}.
     */
    @Test
    public void testStop() {
        String message = this.ramAccessorHelper.stop(TYPE_EXISTS, NAME_EXISTS, VERSION_EXISTS);
        assertNotNull(message);
        assertTrue(message.contains("successful"));
        assertTrue(message.contains("stop"));
    }

    /**
     * Test method for {@link RamAccessorHelper#uninstall(String, String, String)}.
     */
    @Test
    public void testUninstall() {
        String message = this.ramAccessorHelper.uninstall(TYPE_EXISTS, NAME_EXISTS, VERSION_EXISTS);
        assertNotNull(message);
        assertTrue(message.contains("successful"));
        assertTrue(message.contains("uninstall"));
    }

    /**
     * Test method for {@link RamAccessorHelper#refresh(String, String, String)}.
     */
    @Test
    public void testUpdateAndRefresh() {
        String message = this.ramAccessorHelper.refresh(TYPE_EXISTS, NAME_EXISTS, VERSION_EXISTS);
        assertNotNull(message);
        assertTrue(message.contains("successful"));
        assertTrue(message.contains("refresh"));
    }

    /**
     * Test method for {@link RamAccessorHelper#start(String, String, String)}.
     */
    @Test
    public void testStartFail() {
        String message = this.ramAccessorHelper.start(TYPE, NAME, VERSION);
        assertNotNull(message);
        assertTrue(message.contains("error"));
        assertTrue(message.contains("start"));
    }

    /**
     * Test method for {@link RamAccessorHelper#stop(String, String, String)}.
     */
    @Test
    public void testStopFail() {
        String message = this.ramAccessorHelper.stop(TYPE, NAME, VERSION);
        assertNotNull(message);
        assertTrue(message.contains("error"));
        assertTrue(message.contains("stop"));
    }

    /**
     * Test method for {@link RamAccessorHelper#uninstall(String, String, String)}.
     */
    @Test
    public void testUninstallFail() {
        String message = this.ramAccessorHelper.uninstall(TYPE, NAME, VERSION);
        assertNotNull(message);
        assertTrue(message.contains("error"));
        assertTrue(message.contains("uninstall"));
    }

    /**
     * Test method for {@link RamAccessorHelper#refresh(String, String, String)}.
     */
    @Test
    public void testUpdateAndRefreshFail() {
        String message = this.ramAccessorHelper.refresh(TYPE, NAME, VERSION);
        assertNotNull(message);
        assertTrue(message.contains("error"));
        assertTrue(message.contains("refresh"));
    }
    
    @Test
    public void testGetTypes() {
        List<String> types = this.ramAccessorHelper.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        assertEquals(TYPE_EXISTS, types.get(0));
    }

    @Test
    public void testGetArtifactsOfTypeExists() {
        List<ArtifactAccessorPointer> artifactsOfType = this.ramAccessorHelper.getArtifactsOfType(TYPE_EXISTS);
        assertNotNull(artifactsOfType);
        assertEquals(1, artifactsOfType.size());
        assertEquals(TYPE_EXISTS, artifactsOfType.get(0).getType());
    }

    @Test
    public void testGetArtifactsOfTypeNotExist() {
        List<ArtifactAccessorPointer> artifactsOfType = this.ramAccessorHelper.getArtifactsOfType(TYPE);
        assertNotNull(artifactsOfType);
        assertEquals(0, artifactsOfType.size());
    }

    @Test
    public void testGetArtifactExist() {
        ArtifactAccessor artifact = this.ramAccessorHelper.getArtifact(TYPE_EXISTS, NAME_EXISTS, VERSION_EXISTS);
        assertNotNull(artifact);
        assertEquals(TYPE_EXISTS, artifact.getType());
        assertEquals(NAME_EXISTS, artifact.getName());
        assertEquals(VERSION_EXISTS, artifact.getVersion());
    }

    @Test
    public void testGetArtifactNotExist() {
        ArtifactAccessor artifact = this.ramAccessorHelper.getArtifact(TYPE, NAME, VERSION);
        assertNotNull(artifact);
        // This accommodates the workaround to bug 337211.
        assertEquals("Region", artifact.getType());
        assertEquals(NAME, artifact.getName());
        assertEquals(VERSION, artifact.getVersion());
    }
    
}
