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

package org.eclipse.virgo.nano.deployer.hot;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import org.eclipse.virgo.nano.deployer.api.core.ApplicationDeployer;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentIdentity;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentOptions;
import org.eclipse.virgo.nano.deployer.hot.HotDeploymentFileSystemListener;
import org.eclipse.virgo.medic.test.eventlog.MockEventLogger;
import org.eclipse.virgo.util.io.FileSystemEvent;

public class HotDeployerFileSystemListenerTests {

    private HotDeploymentFileSystemListener listener;

    private ApplicationDeployer deployer;

    private DeploymentIdentity deploymentIdentity;

    @Before
    public void initialise() {
        deployer = createMock(ApplicationDeployer.class);
        listener = new HotDeploymentFileSystemListener(deployer, new MockEventLogger());
        deploymentIdentity = createMock(DeploymentIdentity.class);
    }

    @Test
    public void appCreated() throws Exception {
        File app = new File("path/to/app");
        expect(deployer.deploy(eq(app.toURI()), isA(DeploymentOptions.class))).andReturn(deploymentIdentity);
        replay(deployer);
        listener.onChange("path/to/app", FileSystemEvent.CREATED);
        verify(deployer);
    }

    @Test
    public void newAppDuringStartup() throws Exception {
        File app = new File("path/to/app");
        expect(deployer.isDeployed(app.toURI())).andReturn(false);
        expect(deployer.deploy(eq(app.toURI()), isA(DeploymentOptions.class))).andReturn(deploymentIdentity);
        replay(deployer);
        listener.onChange("path/to/app", FileSystemEvent.INITIAL);
        verify(deployer);
    }

    @Test
    public void existingAppDuringStartup() throws Exception {
        File app = new File("path/to/app");
        expect(deployer.isDeployed(app.toURI())).andReturn(true);
        replay(deployer);
        listener.onChange("path/to/app", FileSystemEvent.INITIAL);
        verify(deployer);
    }

    @Test
    public void appModified() throws Exception {
        File app = new File("path/to/app");
        expect(deployer.deploy(eq(app.toURI()), isA(DeploymentOptions.class))).andReturn(deploymentIdentity);
        replay(deployer);
        listener.onChange("path/to/app", FileSystemEvent.MODIFIED);
        verify(deployer);
    }

    @Test
    public void appDeleted() throws Exception {
        File app = new File("path/to/app");
        expect(deployer.getDeploymentIdentity(app.toURI())).andReturn(this.deploymentIdentity);
        deployer.undeploy(this.deploymentIdentity, true);

        replay(deployer);
        listener.onChange("path/to/app", FileSystemEvent.DELETED);
        verify(deployer);
    }

    @Test
    public void nonexistentAppDeleted() throws Exception {
        File app = new File("path/to/app");
        expect(deployer.getDeploymentIdentity(app.toURI())).andReturn(null);
        replay(deployer);
        listener.onChange("path/to/app", FileSystemEvent.DELETED);
        verify(deployer);
    }

}
