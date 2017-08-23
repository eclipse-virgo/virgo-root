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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URI;

import org.eclipse.virgo.nano.deployer.api.core.ApplicationDeployer;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentIdentity;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.ServiceReference;

import org.eclipse.virgo.kernel.deployer.test.modules.TesterModule;
import org.eclipse.virgo.kernel.deployer.test.modules.TesterModuleExport;
import org.eclipse.virgo.kernel.deployer.test.modules.TesterModuleImport;

/**
 * Test detection and diagnosis of transitive uses constraint failures.
 * 
 */
public class TransitiveConstraintFailureDiagnosisTests extends AbstractDeployerIntegrationTest {

    private ServiceReference<ApplicationDeployer> appDeployerServiceReference;

    private ApplicationDeployer appDeployer;

    @Before
    public void setUp() throws Exception {
        this.appDeployerServiceReference = this.context.getServiceReference(ApplicationDeployer.class);
        this.appDeployer = this.context.getService(this.appDeployerServiceReference);
    }
    
    @After
    public void tearDown() throws Exception {
        if (this.appDeployerServiceReference != null) {
            this.context.ungetService(this.appDeployerServiceReference);
        }
    }

    /**
     * Modules and their manifest headers.
     * A:   import r;version="[1,2)",p
     * B:   export p;uses="q"
     *      import q
     * C:   export q;uses="r",r;version=0
     * D:   export r;version=1
     * 
     * A should fail to resolve because the transitive uses constraint prevents wiring r to A from D 
     * because of the version clash with r from C exposed by the transitive uses directives.
     * @throws Exception if run-time errors
     */
    @Test
    public void listTesterModuleHeaders() throws Exception {
        TesterModule tmA = new TesterModule.Builder("a")
        .addImport(new TesterModuleImport.Builder("r").versionRange("[1,2)").build())
        .addImport(new TesterModuleImport.Builder("p").build())
        .build();
        
        TesterModule tmB = new TesterModule.Builder("b")
        .addExport(new TesterModuleExport.Builder("p").uses("q").build())
        .addImport(new TesterModuleImport.Builder("q").build())
        .build();
        
        TesterModule tmC = new TesterModule.Builder("c")
        .addExport(new TesterModuleExport.Builder("q").uses("r").build())
        .addExport(new TesterModuleExport.Builder("r").version("0").build())
        .build();
        
        TesterModule tmD = new TesterModule.Builder("d")
        .addExport(new TesterModuleExport.Builder("r").version("1").build())
        .build();

        printHeaders(tmA);
        printHeaders(tmB);
        printHeaders(tmC);
        printHeaders(tmD);
    }
    
    private static void printHeaders(TesterModule tm) {
        System.out.println("All headers for '" + tm.getName() + "'--------------");
        for (String hdr : tm.getAllHeaders()) {
            System.out.println(hdr);
        }
        System.out.println("-----------------------------------------\n");
    }

    @Test(expected=DeploymentException.class)
    public void testConstraint() throws Exception {
        
        DeploymentIdentity depIdD = deployJar("src/test/resources/transitiveconstraint/tmD.jar");
        assertEquals("d", depIdD.getSymbolicName());
        
        DeploymentIdentity depIdC = deployJar("src/test/resources/transitiveconstraint/tmC.jar");
        assertEquals("c", depIdC.getSymbolicName());
        
        DeploymentIdentity depIdB = deployJar("src/test/resources/transitiveconstraint/tmB.jar");
        assertEquals("b", depIdB.getSymbolicName());

            DeploymentIdentity depIdA = deployJar("src/test/resources/transitiveconstraint/tmA.jar");
            assertEquals("a", depIdA.getSymbolicName());
    }
    
    private DeploymentIdentity deployJar(String jarFilePath) throws DeploymentException {
        File file = new File(jarFilePath);
        URI jarURI = file.toURI();
        
        assertFalse("File " + file + " deployed before test!", this.appDeployer.isDeployed(jarURI));

        DeploymentIdentity depId = this.appDeployer.deploy(jarURI);
        
        assertTrue("File " + file + " not deployed.", this.appDeployer.isDeployed(jarURI));

        return depId;
    }
}
