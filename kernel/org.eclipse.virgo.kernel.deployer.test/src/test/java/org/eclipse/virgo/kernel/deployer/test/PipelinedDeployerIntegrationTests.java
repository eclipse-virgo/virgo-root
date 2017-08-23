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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.ServiceRegistration;

import org.eclipse.virgo.kernel.install.artifact.InstallArtifactLifecycleListener;

import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentIdentity;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentOptions;
import org.eclipse.virgo.repository.WatchableRepository;
import org.eclipse.virgo.util.io.FileCopyUtils;
import org.eclipse.virgo.util.io.FileSystemUtils;
import org.eclipse.virgo.util.io.PathReference;

/**
 * Test the generic deployer using a test deployer. This doesn't check OSGi behaviour but will detect clashes in the
 * file system during deployment.
 * 
 */
public class PipelinedDeployerIntegrationTests extends AbstractDeployerIntegrationTest {

    private static final int POLLING_INTERVAL_MILLIS = 100;

    private final PathReference pickup = new PathReference("./build/pickup");

    private final PathReference target = new PathReference("./build");

    private StubInstallArtifactLifecycleListener lifecycleListener;

    private ServiceRegistration<InstallArtifactLifecycleListener> lifecycleListenerRegistration;

    private StubWatchableRepository watchableRepository;

    private ServiceRegistration<WatchableRepository> watchableRepositoryRegistration;

    private DeploymentIdentity deploymentIdentity;
    
    @Before
    public void setUp() throws Exception {
        PathReference pr = new PathReference("./build/deployer");
        pr.delete(true);
        pr.createDirectory();

        clearPickup();

        this.lifecycleListener = new StubInstallArtifactLifecycleListener();
        this.lifecycleListenerRegistration = this.kernelContext.registerService(InstallArtifactLifecycleListener.class, this.lifecycleListener, null);
        
        this.watchableRepository = new StubWatchableRepository();
        this.watchableRepositoryRegistration = this.kernelContext.registerService(WatchableRepository.class, this.watchableRepository, null);
    }

    private void clearPickup() {
        for (File file : FileSystemUtils.listFiles(this.pickup.toFile())) {
            file.delete();
        }
    }

    @After
    public void tearDown() throws Exception {
        undeploy();
        clearPickup();
        if (this.lifecycleListenerRegistration != null) {
            this.lifecycleListenerRegistration.unregister();
        }
        if (this.watchableRepository != null) {
            this.watchableRepositoryRegistration.unregister();
        }
    }

    private void undeploy() throws DeploymentException {
        if (this.deploymentIdentity != null) {
            this.deployer.undeploy(this.deploymentIdentity);
            this.deploymentIdentity = null;
        }
    }

    private void undeploy(boolean deleted) throws DeploymentException {
        if (this.deploymentIdentity != null) {
            this.deployer.undeploy(this.deploymentIdentity, deleted);
            this.deploymentIdentity = null;
        }
    }

    @Test
    public void testDeployer() throws Exception {
        File file = new File("src/test/resources/dummy.jar");
        this.lifecycleListener.assertLifecycleCounts(0, 0, 0, 0);
        this.deploymentIdentity = this.deployer.deploy(file.toURI());
        this.lifecycleListener.assertLifecycleCounts(1, 1, 0, 0);
        this.deployer.undeploy(this.deploymentIdentity);
        this.deploymentIdentity = null;
        this.lifecycleListener.assertLifecycleCounts(1, 1, 1, 1);
        assertEquals(1, this.watchableRepository.getCheckCount());
    }

    @Test
    public void testDeployerCleanup() throws Exception {
        File file = new File("src/test/resources/dummy.jar");
        this.lifecycleListener.assertLifecycleCounts(0, 0, 0, 0);
        this.deploymentIdentity = this.deployer.deploy(file.toURI());
        this.lifecycleListener.assertLifecycleCounts(1, 1, 0, 0);
        undeploy();
        this.lifecycleListener.assertLifecycleCounts(1, 1, 1, 1);

        File workDir = new File("build/work/org.eclipse.virgo.kernel/Module/dummy.jar-0");
        assertFalse(workDir.exists());
        assertEquals(1, this.watchableRepository.getCheckCount());
    }

    @Test
    public void testAwaitRecovery() throws Exception {
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        ObjectName objectName = ObjectName.getInstance("org.eclipse.virgo.kernel:category=Control,type=RecoveryMonitor");
        assertTrue(server.isRegistered(objectName));
        assertTrue((Boolean) server.getAttribute(objectName, "RecoveryComplete"));
        assertEquals(0, this.watchableRepository.getCheckCount());
    }

    @Test
    public void testRepeatDeployment() throws Exception {
        File file = new File("src/test/resources/dummy.jar");
        this.lifecycleListener.assertLifecycleCounts(0, 0, 0, 0);
        this.deploymentIdentity = this.deployer.deploy(file.toURI());
        this.lifecycleListener.assertLifecycleCounts(1, 1, 0, 0);
        undeploy();
        this.lifecycleListener.assertLifecycleCounts(1, 1, 1, 1);
        this.deploymentIdentity = this.deployer.deploy(file.toURI());
        this.lifecycleListener.assertLifecycleCounts(2, 2, 1, 1);
        undeploy();
        this.lifecycleListener.assertLifecycleCounts(2, 2, 2, 2);
        assertEquals(2, this.watchableRepository.getCheckCount());
    }

    @Test
    public void testDuplicateDeployment() throws Exception {
        File file = new File("src/test/resources/dummy.jar");
        this.lifecycleListener.assertLifecycleCounts(0, 0, 0, 0);
        this.deploymentIdentity = this.deployer.deploy(file.toURI());
        this.lifecycleListener.assertLifecycleCounts(1, 1, 0, 0);
        this.deployer.deploy(file.toURI());
        this.lifecycleListener.assertLifecycleCounts(2, 2, 1, 1);
        undeploy();
        this.lifecycleListener.assertLifecycleCounts(2, 2, 2, 2);
        assertEquals(1, this.watchableRepository.getCheckCount());
    }

    @Test
    public void testHotDeploy() throws Exception {
        PathReference dummy = new PathReference("src/test/resources/dummy.jar");
        this.lifecycleListener.assertLifecycleCounts(0, 0, 0, 0);
        PathReference deployed = dummy.copy(this.pickup);
        assertLifecycleCountsAfterWait(1, 1, 0, 0, 10000);
        deployed.delete();
        assertLifecycleCountsAfterWait(1, 1, 1, 1, 6000);
        assertEquals(1, this.watchableRepository.getCheckCount());
    }

    private void assertLifecycleCountsAfterWait(int starting, int started, int stopping, int stopped, long waitMillis) throws InterruptedException {
        long remainingWait = waitMillis;
        while (remainingWait > 0 && !this.lifecycleListener.checkLifecycleCounts(starting, started, stopping, stopped)) {
            Thread.sleep(POLLING_INTERVAL_MILLIS);
            remainingWait -= POLLING_INTERVAL_MILLIS;
        }
        this.lifecycleListener.assertLifecycleCounts(starting, started, stopping, stopped);
    }

    @Test
    public void testDeployerOwned() throws Exception {
        File dummyCopy = new File(this.target.toFile(), "dummy.jar");
        if (dummyCopy.exists()) {
            dummyCopy.delete();
        }

        PathReference dummy = new PathReference("src/test/resources/dummy.jar");
        PathReference copy = dummy.copy(this.target);
        assertTrue(copy.exists());
        this.lifecycleListener.assertLifecycleCounts(0, 0, 0, 0);
        this.deploymentIdentity = this.deployer.deploy(copy.toURI(), new DeploymentOptions(false, true, true));
        this.lifecycleListener.assertLifecycleCounts(1, 1, 0, 0);
        undeploy();
        this.lifecycleListener.assertLifecycleCounts(1, 1, 1, 1);
        assertFalse(copy.exists());

        copy = dummy.copy(this.target);
        this.deploymentIdentity = this.deployer.deploy(copy.toURI(), new DeploymentOptions(false, true, true));
        copy.delete();
        undeploy();

        // Check artifact not deleted when undeploy specifies deleted=true.
        dummy.copy(this.target);
        assertTrue(copy.exists());
        this.deploymentIdentity = this.deployer.deploy(copy.toURI(), new DeploymentOptions(false, true, true));
        undeploy(true);
        assertTrue(copy.exists());
        copy.delete();
        assertEquals(3, this.watchableRepository.getCheckCount());
    }

    @Test
    public void testRedeployDeployerOwned() throws Exception {
        File dummyCopy = new File(this.target.toFile(), "dummy.jar");
        if (dummyCopy.exists()) {
            dummyCopy.delete();
        }

        PathReference dummy = new PathReference("src/test/resources/dummy.jar");
        PathReference copy = dummy.copy(this.target);
        assertTrue(copy.exists());
        this.lifecycleListener.assertLifecycleCounts(0, 0, 0, 0);
        this.deploymentIdentity = this.deployer.deploy(copy.toURI(), new DeploymentOptions(false, true, true));
        this.lifecycleListener.assertLifecycleCounts(1, 1, 0, 0);

        File dummyModifiedFile = (new PathReference("src/test/resources/dummymodified.jar")).toFile().getAbsoluteFile();
        FileCopyUtils.copy(dummyModifiedFile, copy.toFile().getAbsoluteFile()); // force direct overwrite

        // simulate action on MODIFIED
        this.deploymentIdentity = this.deployer.deploy(copy.toURI(), new DeploymentOptions(false, true, true));

        this.lifecycleListener.assertLifecycleCounts(2, 2, 1, 1);
        assertTrue(copy.exists());

        undeploy();
        this.lifecycleListener.assertLifecycleCounts(2, 2, 2, 2);
        assertFalse(copy.exists());
        assertEquals(2, this.watchableRepository.getCheckCount());
    }
}
