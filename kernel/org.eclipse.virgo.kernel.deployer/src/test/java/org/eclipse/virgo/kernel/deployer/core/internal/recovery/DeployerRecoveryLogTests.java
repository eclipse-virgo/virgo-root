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

package org.eclipse.virgo.kernel.deployer.core.internal.recovery;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URI;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import org.eclipse.virgo.nano.deployer.api.core.DeploymentOptions;
import org.eclipse.virgo.kernel.deployer.core.internal.recovery.DeployerRecoveryLog;
import org.eclipse.virgo.util.io.PathReference;

public class DeployerRecoveryLogTests {

    private PathReference deployArea = new PathReference("build/deployArea");

    @Before
    public void cleanup() {
        deployArea.delete(true);
        deployArea.createDirectory();
    }

    @Test
    public void emptyLog() {
        DeployerRecoveryLog log = new DeployerRecoveryLog(deployArea);
        assertTrue(log.getRecoveryState().isEmpty());
    }

    @Test
    public void recovery() {
        DeployerRecoveryLog log = new DeployerRecoveryLog(deployArea);

        // all true
        URI app1 = new File("app/one").toURI();
        log.add(app1, new DeploymentOptions(true, true, true));

        // all false
        URI app2 = new File("app/two").toURI();
        log.add(app2, new DeploymentOptions(false, false, false));

        URI app3 = new File("app/three").toURI();
        log.add(app3, new DeploymentOptions(false, false, true));

        URI app4 = new File("app/four").toURI();
        log.add(app4, new DeploymentOptions(false, true, true));

        log = new DeployerRecoveryLog(deployArea);

        Map<URI, DeploymentOptions> recoveryState = log.getRecoveryState();
        assertEquals(4, recoveryState.size());

        // boolean recoverable, boolean deployerOwned, boolean synchronous
        DeploymentOptions deploymentOptions = recoveryState.remove(app1);
        assertNotNull(deploymentOptions);
        assertTrue(deploymentOptions.getRecoverable());
        assertTrue(deploymentOptions.getDeployerOwned());
        assertTrue(deploymentOptions.getSynchronous());

        deploymentOptions = recoveryState.remove(app2);
        assertNotNull(deploymentOptions);
        assertFalse(deploymentOptions.getRecoverable());
        assertFalse(deploymentOptions.getDeployerOwned());
        assertFalse(deploymentOptions.getSynchronous());

        deploymentOptions = recoveryState.remove(app3);
        assertNotNull(deploymentOptions);
        assertFalse(deploymentOptions.getRecoverable());
        assertFalse(deploymentOptions.getDeployerOwned());
        assertTrue(deploymentOptions.getSynchronous());

        deploymentOptions = recoveryState.remove(app4);
        assertNotNull(deploymentOptions);
        assertFalse(deploymentOptions.getRecoverable());
        assertTrue(deploymentOptions.getDeployerOwned());
        assertTrue(deploymentOptions.getSynchronous());

        assertTrue(recoveryState.isEmpty());
    }

    @Test
    public void rewrite() {
        DeployerRecoveryLog log = new DeployerRecoveryLog(deployArea);
        URI app1 = new File("app/one").toURI();
        for (int i = 0; i < 100; i++) {
            log.add(app1, new DeploymentOptions(true, true, true));
            if (i < 99) {
                log.remove(app1);
            }
        }
        assertEquals(1, log.getRecoveryState().size());
    }
}
