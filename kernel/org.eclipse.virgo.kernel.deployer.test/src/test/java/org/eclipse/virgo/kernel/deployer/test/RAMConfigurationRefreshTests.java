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

package org.eclipse.virgo.kernel.deployer.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.Version;
import org.osgi.service.cm.Configuration;


import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentIdentity;
import org.eclipse.virgo.kernel.model.management.ManageableArtifact;
import org.eclipse.virgo.util.io.PathReference;


/**
 * Tests for refreshing a configuration artifact via the runtime artifact model (RAM).
 * <p />
 *
 */
public class RAMConfigurationRefreshTests extends AbstractRAMIntegrationTests {
    
    @Test
    public void refresh() throws DeploymentException, IOException, InvalidSyntaxException {
        PathReference copyToDeploy = new PathReference("target/test.properties");
        if (copyToDeploy.exists() && !copyToDeploy.delete()) {
            fail("Failed to delete " + copyToDeploy);
        }
        
        PathReference original = new PathReference("src/test/resources/test.properties");
        original.copy(copyToDeploy);
        
        DeploymentIdentity deployed = this.deployer.deploy(copyToDeploy.toURI());
        
        Configuration configuration = getConfiguration("test");
        assertNotNull(configuration);        
        assertEquals("bar", configuration.getProperties().get("foo"));
        
        PathReference refresh = new PathReference("src/test/resources/test-refresh.properties");
        copyToDeploy.delete();
        refresh.copy(copyToDeploy);
                
        ManageableArtifact artifact = getManageableArtifact(deployed, new StubRegion("global"));
        artifact.refresh();
        
        configuration = getConfiguration("test");
        assertNotNull(configuration);        
        assertEquals("bravo", configuration.getProperties().get("alpha"));
    }   
    
    @Test
    public void refreshWithinAPar() throws DeploymentException, IOException, InvalidSyntaxException {
        PathReference copyToDeploy = new PathReference("target/config-refresh.par");
        if (copyToDeploy.exists() && !copyToDeploy.delete(true)) {
            fail("Failed to delete " + copyToDeploy);
        }
        
        PathReference original = new PathReference("src/test/resources/ram-config-refresh/config-refresh.par");
        original.copy(copyToDeploy, true);
        
        DeploymentIdentity parIdentity = this.deployer.deploy(copyToDeploy.toURI());
        
        Configuration configuration = getConfiguration("test");
        assertNotNull(configuration);        
        assertEquals("bar", configuration.getProperties().get("foo"));
        
        PathReference refresh = new PathReference("src/test/resources/ram-config-refresh/test-refresh.properties");
        PathReference propertiesToRefresh = new PathReference(new File(copyToDeploy.toFile(), "test.properties"));
        propertiesToRefresh.delete();
        refresh.copy(propertiesToRefresh);                
        
        ManageableArtifact artifact = getManageableArtifact("configuration", "test", new Version(0,0,0), new StubRegion("global"));
        artifact.refresh();
        
        configuration = getConfiguration("test");
        assertNotNull(configuration);        
        assertEquals("bravo", configuration.getProperties().get("alpha"));
        
        this.deployer.undeploy(parIdentity);
    }
    
    @Test
    public void refreshWithinAPlan() throws DeploymentException, IOException, InvalidSyntaxException, InterruptedException {
        PathReference watchedRepository = new PathReference("target/watched");
        if (watchedRepository.exists() && ! watchedRepository.delete(true)) {
            fail("Failed to delete watched repository");
        }
        
        watchedRepository.createDirectory();
        
        PathReference copyToDeploy = new PathReference("target/watched/test.properties");
        if (copyToDeploy.exists() && !copyToDeploy.delete(true)) {
            fail("Failed to delete " + copyToDeploy);
        }                
        
        PathReference configurationCopy = new PathReference("target/watched/test.properties");
        new PathReference("src/test/resources/ram-config-refresh/test.properties").copy(configurationCopy);
        
        Thread.sleep(2000);
        
        this.deployer.deploy(new File("src/test/resources/ram-config-refresh/test.plan").toURI());
        
        Configuration configuration = getConfiguration("test");
        assertNotNull(configuration);        
        assertEquals("bar", configuration.getProperties().get("foo"));
        
        PathReference refresh = new PathReference("src/test/resources/test-refresh.properties");        
        configurationCopy.delete();
        refresh.copy(configurationCopy);
                
        ManageableArtifact artifact = getManageableArtifact("configuration", "test", new Version(0,0,0), new StubRegion("global"));
        artifact.refresh();
        
        configuration = getConfiguration("test");
        assertNotNull(configuration);        
        assertEquals("bravo", configuration.getProperties().get("alpha"));
    }
}
