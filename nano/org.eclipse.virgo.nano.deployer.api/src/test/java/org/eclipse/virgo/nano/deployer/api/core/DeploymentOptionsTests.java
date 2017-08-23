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

package org.eclipse.virgo.nano.deployer.api.core;

import static org.junit.Assert.assertEquals;

import org.eclipse.virgo.nano.deployer.api.core.DeploymentOptions;
import org.junit.Test;


/**
 * <code>DeploymentOptionsTests</code> is a simple test of the {@link DeploymentOptions} class.
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 * thread-safe
 *
 */
public class DeploymentOptionsTests {
    
    @Test
    public void constructionDefault() throws Exception {
        DeploymentOptions deploymentOptions = new DeploymentOptions();
    
        assertOptions(deploymentOptions, "default", true, false, true);
    }

    private static void assertOptions(DeploymentOptions deploymentOptions, String name, boolean recoverable, boolean owned, boolean synchronous) {
        assertEquals(name + " recoverable option", recoverable, deploymentOptions.getRecoverable());
        assertEquals(name + " deployerOwned option", owned, deploymentOptions.getDeployerOwned());
        assertEquals(name + " synchronous option", synchronous, deploymentOptions.getSynchronous());
    }
    
    @Test
    public void constructionUnowned() throws Exception {
        DeploymentOptions deploymentOptions = new DeploymentOptions(true, false, true);
    
        assertOptions(deploymentOptions, "unowned", true, false, true);
    }
    
    @Test
    public void constructionOwned() throws Exception {
        DeploymentOptions deploymentOptions = new DeploymentOptions(true, true, true);
    
        assertOptions(deploymentOptions, "owned", true, true, true);
    }
    
    @Test
    public void constructionNonRecoverable() throws Exception {
        DeploymentOptions deploymentOptions = new DeploymentOptions(false, false, true);
    
        assertOptions(deploymentOptions, "non-recoverable", false, false, true);
    }
    

}
