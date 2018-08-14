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

import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentIdentity;
import org.junit.Assert;
import org.junit.Test;


/**
 * Test diagnostics for the merging of imports.
 * 
 */
public class ImportMergeDiagnosticsTests extends AbstractDeployerIntegrationTest {

    @Test
    public void testImportClashingBundles() throws DeploymentException {
        DeploymentIdentity p1Identity = this.deployer.deploy(new File("src/test/resources/importMergeDiagnostics/TestP1.jar").toURI());
        DeploymentIdentity p2Identity = this.deployer.deploy(new File("src/test/resources/importMergeDiagnostics/TestP2.jar").toURI());
        try {
            this.deployer.deploy(new File("src/test/resources/importMergeDiagnostics/TestImportP1P2.jar").toURI());
        } catch (DeploymentException e) {
            e.printStackTrace();
            Throwable ime = e.getCause();
            String message = ime.getMessage();
            Assert.assertEquals(
                "Incorrect message text",
                "cannot merge imports of package 'p' from sources 'Import-Bundle 'TestP1' version '0.0.0', Import-Bundle 'TestP2' version '0.0.0'' because of conflicting values 'TestP2', 'TestP1' of attribute 'bundle-symbolic-name'",
                message);
        } finally {
            this.deployer.undeploy(p1Identity);
            this.deployer.undeploy(p2Identity);
        }
    }

    @Test
    public void testImportClashingLibraries() throws DeploymentException {
        DeploymentIdentity p1Identity = this.deployer.deploy(new File("src/test/resources/importMergeDiagnostics/TestP1.jar").toURI());
        DeploymentIdentity p2Identity = this.deployer.deploy(new File("src/test/resources/importMergeDiagnostics/TestP2.jar").toURI());        
        try {
            this.deployer.deploy(new File("src/test/resources/importMergeDiagnostics/TestImportP1P2ViaLibraries.jar").toURI());
        } catch (DeploymentException e) {
            e.printStackTrace();
            Throwable ime = e.getCause();
            String message = ime.getMessage();
            Assert.assertEquals(
                "Incorrect message text",
                "cannot merge imports of package 'p' from sources 'Import-Library 'LibraryImportingP1' version '1.0.0'(Import-Bundle 'TestP1' version '0.0.0'), Import-Library 'LibraryImportingP2' version '1.0.0'(Import-Bundle 'TestP2' version '0.0.0')' because of conflicting values 'TestP2', 'TestP1' of attribute 'bundle-symbolic-name'",
                message);
        } finally {
            this.deployer.undeploy(p1Identity);
            this.deployer.undeploy(p2Identity);
        }
    }

}
