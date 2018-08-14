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
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.net.URI;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.ServiceReference;

import org.eclipse.virgo.nano.deployer.api.core.ApplicationDeployer;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentIdentity;
import org.eclipse.virgo.util.io.PathReference;

/**
 * Test various usages of the deployer interface for edge cases.
 * <p />
 *
 */
public class DeployerEdgeTests extends AbstractDeployerIntegrationTest {

    private final String TEST_PAR_BUNDLE_SYMBOLIC_NAME = "MyApp-1-com.springsource.kernel.deployer.testbundle";

    private ServiceReference<ApplicationDeployer> appDeployerServiceReference;

    private ApplicationDeployer appDeployer;

    private PathReference parCopy1, parCopy2, parCopy3;

    private PathReference par;

    private PathReference jarCopy1, jarCopy2, jarCopy3;

    private PathReference jar;

    @Before
    public void setUp() {
        PathReference pr = new PathReference("./target/org.eclipse.virgo.kernel");
        pr.delete(true);
        pr.createDirectory();
        pr = new PathReference("./target/deployer-edge-test");
        pr.delete(true);
        pr.createDirectory();
        pr = new PathReference("./target/deployer-edge-test/other");
        pr.createDirectory();

        this.appDeployerServiceReference = this.context.getServiceReference(ApplicationDeployer.class);
        this.appDeployer = this.context.getService(this.appDeployerServiceReference);

        parCopy1 = new PathReference("./target/deployer-edge-test/app0.par");
        parCopy2 = new PathReference("./target/deployer-edge-test/app0copy.par");
        parCopy3 = new PathReference("./target/deployer-edge-test/other/app0.par");
        par = new PathReference("src/test/resources/app0.par");

        jarCopy1 = new PathReference("./target/deployer-edge-test/dummy.jar");
        jarCopy2 = new PathReference("./target/deployer-edge-test/dummycopy.jar");
        jarCopy3 = new PathReference("./target/deployer-edge-test/other/dummy.jar");
        jar = new PathReference("src/test/resources/dummy.jar");
    }

    @After
    public void tearDown() {
        if (this.appDeployerServiceReference != null) {
            this.context.ungetService(this.appDeployerServiceReference);
        }
        PathReference pr = new PathReference("./target/org.eclipse.virgo.kernel");
        pr.delete(true);
        pr = new PathReference("./target/deployer-edge-test");
        pr.delete(true);
    }

    @Test(expected = DeploymentException.class)
    public void testNonExistentFile() throws DeploymentException {
        PathReference noPar = new PathReference("./blah");

        this.appDeployer.deploy(noPar.toURI());
    }

    @Test(expected = DeploymentException.class)
    public void testNonExistentJar() throws DeploymentException {
        PathReference noJar = new PathReference("./blah.jar");

        this.appDeployer.deploy(noJar.toURI());
    }

    @Test(expected = DeploymentException.class)
    public void testDuplicateAppFromDifferentLocation() throws DeploymentException {
        parCopy1.delete(true);
        par.copy(parCopy1);

        this.appDeployer.deploy(parCopy1.toURI());
        // Check that the test bundle's application contexts are created.
        assertNotNull(ApplicationContextUtils.getApplicationContext(this.context, TEST_PAR_BUNDLE_SYMBOLIC_NAME));

        parCopy2.delete(true);
        par.copy(parCopy2);

        this.appDeployer.deploy(parCopy2.toURI());
    }

    @Test(expected = DeploymentException.class)
    public void testDuplicateJarFromDifferentLocation() throws DeploymentException {
        jarCopy1.delete(true);
        jar.copy(jarCopy1);

        this.appDeployer.deploy(jarCopy1.toURI());

        jarCopy2.delete(true);
        jar.copy(jarCopy2);

        this.appDeployer.deploy(jarCopy2.toURI());
    }

    @Test(expected = DeploymentException.class)
    public void testDuplicateAppSameFileNameDifferentLocation() throws DeploymentException {
        parCopy1.delete(true);
        par.copy(parCopy1);

        this.appDeployer.deploy(parCopy1.toURI());
        // Check that the test bundle's application contexts are created.
        assertNotNull(ApplicationContextUtils.getApplicationContext(this.context, TEST_PAR_BUNDLE_SYMBOLIC_NAME));

        parCopy3.delete(true);
        par.copy(parCopy3);

        this.appDeployer.deploy(parCopy3.toURI());
    }

    @Test(expected = DeploymentException.class)
    public void testDuplicateJarSameFileNameDifferentLocation() throws DeploymentException {
        jarCopy1.delete(true);
        jar.copy(jarCopy1);

        this.appDeployer.deploy(jarCopy1.toURI());

        jarCopy3.delete(true);
        jar.copy(jarCopy3);

        this.appDeployer.deploy(jarCopy3.toURI());
    }

    @Test
    public void testDifferentAppSameLocation() throws Exception {
        parCopy1.delete(true);
        par.copy(parCopy1);

        this.appDeployer.deploy(parCopy1.toURI());
        // Check that the test bundle's application contexts are created.
        assertNotNull(ApplicationContextUtils.getApplicationContext(this.context, TEST_PAR_BUNDLE_SYMBOLIC_NAME));

        parCopy1.delete(true);
        new PathReference("src/test/resources/app4.par").copy(parCopy1);

        this.appDeployer.deploy(parCopy1.toURI());
    }

    @Test
    public void testDifferentJarSameLocation() throws Exception {
        jarCopy1.delete(true);
        jar.copy(jarCopy1);

        this.appDeployer.deploy(jarCopy1.toURI());

        jarCopy1.delete(true);
        new PathReference("src/test/resources/ExporterC.jar").copy(jarCopy1);

        this.appDeployer.deploy(jarCopy1.toURI());
    }

    @Test(expected = DeploymentException.class)
    public void testDifferentAppSameFileNameDifferentLocation() throws Exception {
        parCopy1.delete(true);
        par.copy(parCopy1);

        this.appDeployer.deploy(parCopy1.toURI());
        // Check that the test bundle's application contexts are created.
        assertNotNull(ApplicationContextUtils.getApplicationContext(this.context, TEST_PAR_BUNDLE_SYMBOLIC_NAME));

        parCopy3.delete(true);
        new PathReference("src/test/resources/app4.par").copy(parCopy3);

        this.appDeployer.deploy(parCopy3.toURI());
    }

    @Test(expected = DeploymentException.class)
    public void testDifferentJarSameFileNameDifferentLocation() throws Exception {
        jarCopy1.delete(true);
        jar.copy(jarCopy1);

        this.appDeployer.deploy(jarCopy1.toURI());

        jarCopy3.delete(true);
        new PathReference("src/test/resources/ExporterC.jar").copy(jarCopy3);

        this.appDeployer.deploy(jarCopy3.toURI());
    }

    @Test
    public void testParWithoutManifest() throws Exception {
        File f = new File("src/test/resources/nomanifest.par");
        DeploymentIdentity deployed = this.appDeployer.deploy(f.toURI());

        assertEquals("par", deployed.getType());
        assertEquals("nomanifest", deployed.getSymbolicName());
        assertEquals("0.0.0", deployed.getVersion());
    }

    @Test
    public void testJarWithoutManifest() throws Exception {
        File f = new File("src/test/resources/nomanifest.jar");
        DeploymentIdentity deploymentIdentity = this.appDeployer.deploy(f.toURI());
        this.appDeployer.undeploy(deploymentIdentity);
    }

    @Test
    public void testParWithoutMetaInf() throws Exception {
        File f = new File("src/test/resources/nometainf.par");
        DeploymentIdentity deployed = this.appDeployer.deploy(f.toURI());

        assertEquals("par", deployed.getType());
        assertEquals("nometainf", deployed.getSymbolicName());
        assertEquals("0.0.0", deployed.getVersion());
    }

    @Test
    public void testJarWithoutMetaInf() throws Exception {
        File f = new File("src/test/resources/nometainf.jar");
        DeploymentIdentity deploymentIdentity = this.appDeployer.deploy(f.toURI());
        this.appDeployer.undeploy(deploymentIdentity);
    }

    @Test(expected = DeploymentException.class)
    public void testParWithClashingExports() throws Exception {
        File f = new File("src/test/resources/clashing.exports.in.a.scope.par");
        this.appDeployer.deploy(f.toURI());
    }

    @Test(expected = DeploymentException.class)
    public void testUnsupportedURIScheme() throws Exception {
        URI httpURI = new URI("http://www.springsource.com");

        this.appDeployer.deploy(httpURI);
    }

    @Test(expected = DeploymentException.class)
    public void testNonBundleDirectory() throws Exception {
        URI dirURI = new File("build").toURI();
        this.appDeployer.deploy(dirURI);
    }

}
