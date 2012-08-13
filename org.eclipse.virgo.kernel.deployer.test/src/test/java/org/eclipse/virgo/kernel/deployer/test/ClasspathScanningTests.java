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

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.ServiceReference;

import org.eclipse.virgo.nano.deployer.api.core.ApplicationDeployer;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentIdentity;
import org.eclipse.virgo.util.io.PathReference;

public class ClasspathScanningTests extends AbstractDeployerIntegrationTest {

    private ServiceReference<ApplicationDeployer> appDeployerServiceReference;

    private ApplicationDeployer appDeployer;

    @Before
    public void setUp() throws Exception {
        cleanUp();

        this.appDeployerServiceReference = this.context.getServiceReference(ApplicationDeployer.class);
        this.appDeployer = (ApplicationDeployer) this.context.getService(this.appDeployerServiceReference);
    }

    @After
    public void tearDown() throws Exception {
        if (this.appDeployerServiceReference != null) {
            this.context.ungetService(this.appDeployerServiceReference);
        }
        cleanUp();
    }

    @Test
    public void testComponentAnnotatedClassesFromImportedPackagesArePresentInApplicationContext() throws Exception  {
        DeploymentIdentity identity = this.appDeployer.deploy(new File("src/test/resources/classpath-scanning.par").toURI());

        String symbolicName = "ClasspathScanning-1-com.foo.app";

        ApplicationContextUtils.assertApplicationContextContainsExpectedBeanDefinitions(ApplicationContextUtils.getApplicationContext(this.context,
            symbolicName), "dependencyOne", "dependencyTwo");
        
        this.appDeployer.undeploy(identity);
    }

    private void cleanUp() {
        cleanDirectory("./target/org.eclipse.virgo.kernel");
        cleanDirectory("./target/install");
        cleanDirectory("./target/locationCache");
    }

    private void cleanDirectory(String dir) {
        PathReference prd = new PathReference(dir);
        prd.delete(true);
        prd.createDirectory();
    }
}
