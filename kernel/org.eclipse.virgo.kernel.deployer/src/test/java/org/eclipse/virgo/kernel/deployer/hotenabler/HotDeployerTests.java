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

package org.eclipse.virgo.kernel.deployer.hotenabler;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.Map;

import org.eclipse.virgo.nano.deployer.api.core.ApplicationDeployer;
import org.eclipse.virgo.nano.deployer.api.core.DeployerConfiguration;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentIdentity;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentOptions;
import org.eclipse.virgo.medic.test.eventlog.MockEventLogger;
import org.eclipse.virgo.util.io.PathReference;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.service.event.Event;

public class HotDeployerTests {

    private static final PathReference PICKUP_DIR = new PathReference("build/pickup");

    private ApplicationDeployer deployer;

    @Before
    public void setUp() {
        PICKUP_DIR.createDirectory();
    }

    @After
    public void cleanUp() {
        PICKUP_DIR.delete(true);
    }

    @Test
    public void deploy() throws Exception {
        PathReference sourceFile = new PathReference("src/test/resources/test/dummy.txt");
        assertTrue(sourceFile.exists());

        this.deployer = createMock(ApplicationDeployer.class);
        DeploymentIdentity deploymentIdentity = new DeploymentIdentity() {

            private static final long serialVersionUID = 1L;

            public String getType() {
                return null;
            }

            public String getSymbolicName() {
                return null;
            }

            public String getVersion() {
                return null;
            }

        };
        this.deployer.deploy(isA(URI.class), isA(DeploymentOptions.class));
        expectLastCall().andReturn(deploymentIdentity);
        replay(this.deployer);

        DeployerConfiguration deployerConfiguration = createMock(DeployerConfiguration.class);
        expect(deployerConfiguration.getDeploymentPickupDirectory()).andReturn(new PathReference("build/pickup"));
        expect(deployerConfiguration.getScanIntervalMillis()).andReturn(1000);

        replay(deployerConfiguration);

        HotDeployerEnabler enabler = new HotDeployerEnabler(deployerConfiguration, this.deployer, new MockEventLogger());
        enabler.handleEvent(new Event("org/eclipse/virgo/kernel/deployer/recovery/COMPLETED", (Map<String, ?>)null));

        // Deployer.start() is asynchronous: sleep long enough for it to have started up
        sleep(2000);

        PathReference copy = sourceFile.copy(PICKUP_DIR);
        pauseOnCreate(copy, 4000, 4000);
        enabler.stop();

        verify(this.deployer);
    }

    private void pauseOnCreate(PathReference pr, long pause, long timeout) {
        long start = System.currentTimeMillis();

        while (!pr.exists() && System.currentTimeMillis() - start < timeout) {
            sleep(100);
        }
        if (pr.exists()) {
            sleep(pause);
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {
        }
    }
}
