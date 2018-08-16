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

import static org.eclipse.virgo.util.osgi.BundleUtils.getExportedPackages;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Set;

import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;

import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentIdentity;
import org.eclipse.virgo.kernel.model.management.ManageableArtifact;
import org.eclipse.virgo.util.io.PathReference;

/**
 * Tests for refreshing a bundle via its entry in the runtime artifact model (RAM).
 * <p />
 *
 */
public class RAMBundleRefreshTests extends AbstractRAMIntegrationTests {

    @Test
    public void refresh() throws DeploymentException {
        PathReference copyToDeploy = new PathReference("target/initial.jar");
        if (copyToDeploy.exists() && !copyToDeploy.delete(true)) {
            fail("Failed to delete " + copyToDeploy);
        }
        
        PathReference original = new PathReference("src/test/resources/ram-bundle-refresh/initial.jar");
        original.copy(copyToDeploy, true);
        
        DeploymentIdentity deployed = this.deployer.deploy(copyToDeploy.toURI());                      
        
        assertStateOfInitialBundle();
        
        PathReference refresh = new PathReference("src/test/resources/ram-bundle-refresh/new-entry.jar");
        copyToDeploy.delete(true);
        refresh.copy(copyToDeploy, true);
        
        ManageableArtifact artifact = getManageableArtifact(deployed, new StubRegion("org.eclipse.virgo.region.user"));
        assertTrue(artifact.refresh());        
        assertEquals("ACTIVE", artifact.getState());
        
        assertStateOfNewEntryBundle();
        
        refresh = new PathReference("src/test/resources/ram-bundle-refresh/new-export.jar");
        copyToDeploy.delete(true);
        refresh.copy(copyToDeploy, true);
        
        assertTrue(artifact.refresh());             
        assertEquals("ACTIVE", artifact.getState());
        assertStateOfNewExportBundle();
        
        refresh = new PathReference("src/test/resources/ram-bundle-refresh/new-name.jar");
        copyToDeploy.delete(true);
        refresh.copy(copyToDeploy, true);
        
        assertFalse(artifact.refresh());
        assertEquals("ACTIVE", artifact.getState());
        
        refresh = new PathReference("src/test/resources/ram-bundle-refresh/new-version.jar");
        copyToDeploy.delete(true);
        refresh.copy(copyToDeploy, true);
        
        assertFalse(artifact.refresh());
        assertEquals("ACTIVE", artifact.getState());

        this.deployer.undeploy(deployed);
        
        assertNull(getBundle("refresh", new Version(1,0,0)));
    }  
  
    @Test
    public void refreshWithinAnUnscopedPlan() throws DeploymentException, InterruptedException {
        PathReference watchedRepository = new PathReference("target/watched");
        if (watchedRepository.exists() && ! watchedRepository.delete(true)) {
            fail("Failed to delete watched repository");
        }
        
        watchedRepository.createDirectory();
        
        PathReference copyToDeploy = new PathReference("target/watched/refresh.jar");
        PathReference original = new PathReference("src/test/resources/ram-bundle-refresh/initial.jar");
        original.copy(copyToDeploy, true);
        
        Thread.sleep(2000);
        
        DeploymentIdentity deployed = this.deployer.deploy(new File("src/test/resources/ram-bundle-refresh/test.plan").toURI());    
        
        assertStateOfInitialBundle();
        
        PathReference refresh = new PathReference("src/test/resources/ram-bundle-refresh/new-entry.jar");        
        copyToDeploy.delete(true);
        refresh.copy(copyToDeploy, true);
        
        ManageableArtifact artifact = getManageableArtifact("bundle", "refresh", new Version(1,0,0), new StubRegion("org.eclipse.virgo.region.user"));
        assertTrue(artifact.refresh());
        assertEquals("ACTIVE", artifact.getState());
        
        assertStateOfNewEntryBundle();
        
        refresh = new PathReference("src/test/resources/ram-bundle-refresh/new-export.jar");        
        copyToDeploy.delete(true);
        refresh.copy(copyToDeploy, true);
        
        assertTrue(artifact.refresh());
        assertEquals("ACTIVE", artifact.getState());
        
        assertStateOfNewExportBundle();
        
        this.deployer.undeploy(deployed);
        assertNull(getBundle("refresh", new Version(1,0,0)));
    }
    
    @Test
    public void refreshWithinAPar() throws DeploymentException {
        PathReference copyToDeploy = new PathReference("target/bundle-refresh.par");
        if (copyToDeploy.exists() && !copyToDeploy.delete(true)) {
            fail("Failed to delete " + copyToDeploy);
        }
        
        PathReference original = new PathReference("src/test/resources/ram-bundle-refresh/bundle-refresh.par");
        original.copy(copyToDeploy, true);
        
        DeploymentIdentity deployed = this.deployer.deploy(copyToDeploy.toURI());                   
        
        assertStateOfScopedInitialBundle();
        
        PathReference refreshSource = new PathReference("src/test/resources/ram-bundle-refresh/new-entry.jar");
        PathReference refreshTarget = new PathReference("target/bundle-refresh.par/initial.jar");
        refreshTarget.delete(true);
        refreshSource.copy(refreshTarget, true);
        
        ManageableArtifact artifact = getManageableArtifact("bundle", "bundle-refresh-1-refresh", new Version(1,0,0), new StubRegion("org.eclipse.virgo.region.user"));
        assertTrue(artifact.refresh());
        assertEquals("ACTIVE", artifact.getState());
        
        assertStateOfScopedNewEntryBundle();   
        
        refreshSource = new PathReference("src/test/resources/ram-bundle-refresh/new-export.jar");
        refreshTarget.delete(true);
        refreshSource.copy(refreshTarget, true);
        
        assertFalse(artifact.refresh());
        assertEquals("ACTIVE", artifact.getState());
        
        this.deployer.undeploy(deployed);
        assertNull(getBundle("refresh", new Version(1,0,0)));
    }
    
    @Test
    public void refreshWithDefaultSymbolicNameAndVersion() throws DeploymentException {        
        PathReference original = new PathReference("src/test/resources/ram-bundle-refresh/no-bsn-no-version.jar");
        
        DeploymentIdentity deployed = this.deployer.deploy(original.toURI());                      
        assertEquals("no-bsn-no-version", deployed.getSymbolicName());
        assertEquals("0.0.0", deployed.getVersion());
                        
        ManageableArtifact artifact = getManageableArtifact(deployed, new StubRegion("org.eclipse.virgo.region.user"));
        assertTrue(artifact.refresh());        
        assertEquals("ACTIVE", artifact.getState());
        
        this.deployer.undeploy(deployed);
    }
    
    @Test
    public void refreshWithinAnUnscopedPlanWithAScopedParent() throws DeploymentException, InterruptedException {
        PathReference watchedRepository = new PathReference("target/watched");
        if (watchedRepository.exists() && ! watchedRepository.delete(true)) {
            fail("Failed to delete watched repository");
        }
        
        watchedRepository.createDirectory();
        
        PathReference copyToDeploy = new PathReference("target/watched/refresh.jar");
        PathReference original = new PathReference("src/test/resources/ram-bundle-refresh/initial.jar");
        original.copy(copyToDeploy, true);
        
        new PathReference("src/test/resources/ram-bundle-refresh/test.plan").copy(new PathReference("target/watched/test.plan"));
        
        Thread.sleep(2000);
        
        DeploymentIdentity deployed = this.deployer.deploy(new File("src/test/resources/ram-bundle-refresh/parent-test.plan").toURI());    
        
        assertStateOfScopedInitialBundle();
        
        PathReference refresh = new PathReference("src/test/resources/ram-bundle-refresh/new-entry.jar");        
        copyToDeploy.delete(true);
        refresh.copy(copyToDeploy, true);
        
        ManageableArtifact artifact = getManageableArtifact("bundle", "bundle-refresh-1-refresh", new Version(1,0,0), new StubRegion("org.eclipse.virgo.region.user"));
        assertTrue(artifact.refresh());
        assertEquals("ACTIVE", artifact.getState());
        
        assertStateOfScopedNewEntryBundle();
        
        refresh = new PathReference("src/test/resources/ram-bundle-refresh/new-export.jar");        
        copyToDeploy.delete(true);
        refresh.copy(copyToDeploy, true);
        
        assertFalse(artifact.refresh());
        assertEquals("ACTIVE", artifact.getState());
        
        this.deployer.undeploy(deployed);
        assertNull(getBundle("refresh", new Version(1,0,0)));
    }
    
    private void assertStateOfNewExportBundle() {
        Bundle bundle = getBundle("refresh", new Version(1,0,0));
        Set<String> exportedPackages = getExportedPackages(bundle);
        
        assertEquals(2, exportedPackages.size());
    }

    private void assertStateOfNewEntryBundle() {
        Bundle bundle = getBundle("refresh", new Version(1,0,0));
        assertStateOfNewEntryBundle(bundle);        
    }
    
    private void assertStateOfScopedNewEntryBundle() {
        Bundle bundle = getBundle("bundle-refresh-1-refresh", new Version(1,0,0));
        assertStateOfNewEntryBundle(bundle);
    }
    
    private void assertStateOfNewEntryBundle(Bundle bundle) {        
        assertNotNull(bundle);
        assertNotNull(bundle.getEntry("one/foo.txt"));
        
        Set<String> exportedPackages = getExportedPackages(bundle);
        
        assertEquals(1, exportedPackages.size());
    }

    private void assertStateOfInitialBundle() {
        Bundle bundle = getBundle("refresh", new Version(1,0,0));
        assertStateOfInitialBundle(bundle);
    }
    
    private void assertStateOfScopedInitialBundle() {
        Bundle bundle = getBundle("bundle-refresh-1-refresh", new Version(1,0,0));
        assertStateOfInitialBundle(bundle);
    } 
    
    private void assertStateOfInitialBundle(Bundle initialBundle) {
        assertNotNull(initialBundle);
        assertNull(initialBundle.getEntry("one/foo.txt"));

        Set<String> exportedPackages = getExportedPackages(initialBundle);
        assertEquals(1, exportedPackages.size());
    }
}
