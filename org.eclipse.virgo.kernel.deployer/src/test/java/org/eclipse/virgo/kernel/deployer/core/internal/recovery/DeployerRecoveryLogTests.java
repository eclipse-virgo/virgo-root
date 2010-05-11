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

import org.eclipse.virgo.kernel.deployer.core.ApplicationDeployer;
import org.eclipse.virgo.kernel.deployer.core.ApplicationDeployer.DeploymentOptions;
import org.eclipse.virgo.kernel.deployer.core.internal.recovery.DeployerRecoveryLog;
import org.eclipse.virgo.util.io.PathReference;

public class DeployerRecoveryLogTests {
        
    private PathReference deployArea = new PathReference("target/deployArea");
    
    @Before
    public void cleanup() {
        deployArea.delete(true);
        deployArea.createDirectory();
    }
    
    @Test
    public void emptyLog() {
        DeployerRecoveryLog log = new DeployerRecoveryLog(deployArea);
        assertEquals(0, log.getRecoveryState().size());
    } 
    
    @Test
    public void recovery() {
        DeployerRecoveryLog log = new DeployerRecoveryLog(deployArea);
        URI app1 = new File("app/one").toURI();
        log.add(app1, new ApplicationDeployer.DeploymentOptions(true, true, true));
        URI app2 = new File("app/two").toURI();
        log.add(app2, new ApplicationDeployer.DeploymentOptions(true, false, true));
        
        log = new DeployerRecoveryLog(deployArea);
             
        Map<URI, DeploymentOptions> recoveryState = log.getRecoveryState();
        assertEquals(2, recoveryState.size());
        
        DeploymentOptions app1Options = recoveryState.remove(app1);       
        assertNotNull(app1Options);
        assertTrue(app1Options.getDeployerOwned());
        assertTrue(app1Options.getRecoverable());
        
        DeploymentOptions app2Options = recoveryState.remove(app2);       
        assertNotNull(app2Options);
        assertFalse(app2Options.getDeployerOwned());
        assertTrue(app2Options.getRecoverable());
        
        assertEquals(0, recoveryState.size());
    }
    
    @Test
    public void rewrite() {
        DeployerRecoveryLog log = new DeployerRecoveryLog(deployArea);
        URI app1 = new File("app/one").toURI();
        for (int i = 0; i < 100; i++) {           
            log.add(app1, new  ApplicationDeployer.DeploymentOptions(true, true, true));
            if (i < 99) {
                log.remove(app1);
            }
        }
        assertEquals(1, log.getRecoveryState().size());
    }
}
