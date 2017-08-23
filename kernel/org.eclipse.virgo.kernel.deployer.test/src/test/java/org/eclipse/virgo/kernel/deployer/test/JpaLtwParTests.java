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

import java.io.File;
import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.eclipse.virgo.nano.deployer.api.core.DeploymentIdentity;
import org.junit.Ignore;
import org.junit.Test;


/**
 */
@Ignore("[DMS-2881] Uses TopLink Essentials which leaks a file handle and breaks the build on Windows")
public class JpaLtwParTests extends AbstractParTests {

    @Test public void testDeployApp() throws Throwable {        
        doTest(new File("src/test/resources/jpa-ltw-sample-update.par"));
    }

    void doTest(File f) throws Throwable {
        DeploymentIdentity deploymentIdentity = deploy(f);
        try {
            ObjectName oname = ObjectName.getInstance("bean:name=addressSupport");
            MBeanServer server = ManagementFactory.getPlatformMBeanServer();
            int size = (Integer) server.getAttribute(oname, "AddressesSize");
            assertEquals(3, size);
        } finally {
            this.deployer.undeploy(deploymentIdentity);
        }
    }
}
