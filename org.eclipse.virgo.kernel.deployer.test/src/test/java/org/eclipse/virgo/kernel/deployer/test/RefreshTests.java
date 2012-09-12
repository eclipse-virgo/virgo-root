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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Set;

import org.eclipse.virgo.nano.deployer.api.core.ApplicationDeployer;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentIdentity;
import org.eclipse.virgo.util.io.JarUtils;
import org.eclipse.virgo.util.io.PathReference;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * Test refreshing individual modules of a deployed application.
 * <p />
 * 
 */
public class RefreshTests extends AbstractDeployerIntegrationTest {

    private final String TEST_IMPORTER_BUNDLE_SYMBOLIC_NAME = "RefreshTest-1-RefreshImporter";

    private final String TEST_IMPORT_BUNDLE_IMPORTER_BUNDLE_SYMBOLIC_NAME = "RefreshTest-Import-Bundle-" + TEST_APPS_VERSION + "-RefreshImporter";

    private ServiceReference<ApplicationDeployer> appDeployerServiceReference;

    private ApplicationDeployer appDeployer;

    private PathReference explodedPar;

    private PathReference par;

    private PathReference parImportBundle;

    @Before
    public void setUp() throws Exception {
        PathReference pr = new PathReference("./target/org.eclipse.virgo.kernel");
        pr.delete(true);
        pr.createDirectory();

        this.appDeployerServiceReference = this.context.getServiceReference(ApplicationDeployer.class);
        this.appDeployer = this.context.getService(this.appDeployerServiceReference);

        explodedPar = new PathReference("./target/refresh-test/refresh.par");
        explodedPar.delete(true);

        par = new PathReference("src/test/resources/refresh.par");
        JarUtils.unpackTo(par, explodedPar);

        parImportBundle = new PathReference("src/test/resources/refresh-import-bundle.par");
    }

    @After
    public void tearDown() throws Exception {
        if (this.appDeployerServiceReference != null) {
            this.context.ungetService(this.appDeployerServiceReference);
        }
    }

    @Test
    public void testBasicRefresh() throws DeploymentException, InterruptedException, IOException {
        explodedPar.delete(true);
        JarUtils.unpackTo(par, explodedPar);

        this.appDeployer.deploy(explodedPar.toURI());
        // Check that the test bundle's application contexts are created.
        assertNotNull(ApplicationContextUtils.getApplicationContext(this.context, TEST_IMPORTER_BUNDLE_SYMBOLIC_NAME));        

        checkV1Classes();

        PathReference exporter = new PathReference("./target/refresh-test/refresh.par/RefreshExporter.jar");
        PathReference v2 = new PathReference("./target/refresh-test/refresh.par/build/RefreshExporterv2.jar");
        exporter.delete();
        v2.copy(exporter);
        this.appDeployer.refresh(explodedPar.toURI(), "RefreshExporter");
        Thread.sleep(1000); // wait for appCtx to be properly closed before waiting for it to be created.
        ApplicationContextUtils.awaitApplicationContext(this.context, TEST_IMPORTER_BUNDLE_SYMBOLIC_NAME, 10);
        
        checkV2Classes();

        PathReference v3 = new PathReference("./target/refresh-test/refresh.par/build/RefreshExporterv3.jar");
        exporter.delete();
        v3.copy(exporter);
        this.appDeployer.refresh(explodedPar.toURI(), "RefreshExporter");
        Thread.sleep(1000); // wait for appCtx to be properly closed before waiting for it to be created.
        ApplicationContextUtils.awaitApplicationContext(this.context, TEST_IMPORTER_BUNDLE_SYMBOLIC_NAME, 10);

        checkV3Classes();
    }

    @Test(expected=DeploymentException.class)
    public void testRefreshOfUndeployedApplication() throws DeploymentException, IOException {
        explodedPar.delete(true);
        JarUtils.unpackTo(par, explodedPar);

        // Ensure application isn't already deployed.
        this.appDeployer.undeploy("par", "RefreshTest", "1");

        this.appDeployer.refresh(explodedPar.toURI(), "RefreshExporter");
    }

    @Test
    public void testRefreshWithImportBundle() throws DeploymentException, InterruptedException, IOException {
        explodedPar.delete(true);
        JarUtils.unpackTo(parImportBundle, explodedPar);

        this.appDeployer.deploy(explodedPar.toURI());
        // Check that the test bundle's application contexts are created.
        ApplicationContextUtils.awaitApplicationContext(this.context, TEST_IMPORT_BUNDLE_IMPORTER_BUNDLE_SYMBOLIC_NAME, 10);        

        checkV1Classes();

        this.appDeployer.refresh(explodedPar.toURI(), "RefreshImporter");
        Thread.sleep(1000); // wait for appCtx to be properly closed before waiting for it to be created.
        ApplicationContextUtils.awaitApplicationContext(this.context, TEST_IMPORT_BUNDLE_IMPORTER_BUNDLE_SYMBOLIC_NAME, 10);        

        checkV1Classes();
    }


    @Test
    public void testRedeployOfDependentBundles() throws DeploymentException, IOException {
        explodedPar.delete(true);
        JarUtils.unpackTo(par, explodedPar);

        PathReference exporterJar = explodedPar.newChild("RefreshExporter.jar");
        PathReference explodedExporterJar = explodedPar.newChild("explode").newChild("RefreshExporter.jar");
        explodedExporterJar.delete(true);
        JarUtils.unpackTo(exporterJar, explodedExporterJar);

        PathReference importerJar = explodedPar.newChild("RefreshImporter.jar");
        PathReference explodedImporterJar = explodedPar.newChild("explode").newChild("RefreshImporter.jar");
        explodedImporterJar.delete(true);
        JarUtils.unpackTo(importerJar, explodedImporterJar);

        // Ensure bundles are not already deployed.
        try {
            this.appDeployer.undeploy("bundle", "RefreshExporter.jar", TEST_APPS_VERSION);
        } catch (DeploymentException e) {
        }
        try {
            this.appDeployer.undeploy("bundle", "RefreshImporter.jar", TEST_APPS_VERSION);
        } catch (DeploymentException e) {
        }

        DeploymentIdentity diExporter = this.appDeployer.deploy(explodedExporterJar.toURI());
        DeploymentIdentity diImporter = this.appDeployer.deploy(explodedImporterJar.toURI());

        assertDeploymentIdentityEquals(diExporter, "RefreshExporter", "bundle", "RefreshExporter", TEST_APPS_VERSION);
        assertDeploymentIdentityEquals(diImporter, "RefreshImporter", "bundle", "RefreshImporter", TEST_APPS_VERSION);
        
        // Check that the test bundle's application contexts are created.
        ApplicationContextUtils.awaitApplicationContext(this.context, "RefreshImporter", 10);

        // Save the importer's manifest and overwrite it with the exporter's
        PathReference importerManifest = explodedImporterJar.newChild("META-INF").newChild("MANIFEST.MF");
        PathReference importerManifestSave = explodedImporterJar.newChild("META-INF").newChild("MANIFEST.MF.SAVED");
        importerManifest.copy(importerManifestSave);
        PathReference exporterManifest = explodedExporterJar.newChild("META-INF").newChild("MANIFEST.MF");
        importerManifest.delete();
        exporterManifest.copy(importerManifest);
        
        // Redeploy the importer which will fail because of a duplicate bundle.
        try {
            this.appDeployer.deploy(explodedImporterJar.toURI());
            assertFalse("deploy should fail because of a duplicate bundle", true);
        } catch (DeploymentException e) {
            assertTrue("Message did not report clash: " + e.getMessage(), e.getMessage().contains("clashes"));
        }

        // Restore the importer's manifest
        importerManifest.delete();
        importerManifestSave.copy(importerManifest);
        
        // Redeploy the importer which should now succeed.
        DeploymentIdentity deploymentIdentity = this.appDeployer.deploy(explodedImporterJar.toURI());
        assertDeploymentIdentityEquals(deploymentIdentity, "RefreshImporter", "bundle", "RefreshImporter", TEST_APPS_VERSION);
    }
    
    private void checkV1Classes() {
        LoadableClasses loadableClasses = (LoadableClasses) getApplicationBundleContext().getService(
            getApplicationBundleContext().getServiceReference("org.eclipse.virgo.kernel.deployer.test.LoadableClasses"));
        Set<String> loadableClassNames = loadableClasses.getLoadableClasses();
        assertTrue(loadableClassNames.contains("refresh.exporter.b1.B11"));
        assertFalse(loadableClassNames.contains("refresh.exporter.b1.B12"));
        assertFalse(loadableClassNames.contains("refresh.exporter.b2.B21"));
    }
    
    private void checkV2Classes() {
        LoadableClasses loadableClasses = (LoadableClasses) getApplicationBundleContext().getService(
            getApplicationBundleContext().getServiceReference("org.eclipse.virgo.kernel.deployer.test.LoadableClasses"));
        Set<String> loadableClassNames = loadableClasses.getLoadableClasses();
        assertTrue(loadableClassNames.contains("refresh.exporter.b1.B11"));
        assertTrue(loadableClassNames.contains("refresh.exporter.b1.B12"));
        assertFalse(loadableClassNames.contains("refresh.exporter.b2.B21"));
    }
    
    private void checkV3Classes() {
        LoadableClasses loadableClasses = (LoadableClasses) getApplicationBundleContext().getService(
            getApplicationBundleContext().getServiceReference("org.eclipse.virgo.kernel.deployer.test.LoadableClasses"));
        Set<String> loadableClassNames = loadableClasses.getLoadableClasses();
        assertTrue(loadableClassNames.contains("refresh.exporter.b1.B11"));
        assertTrue(loadableClassNames.contains("refresh.exporter.b1.B12"));
        assertTrue(loadableClassNames.contains("refresh.exporter.b2.B21"));
    }
    
    private BundleContext getApplicationBundleContext() {
        Bundle[] bundles = this.context.getBundles();
        for (Bundle bundle : bundles) {
            String symbolicName = bundle.getSymbolicName();
            if (symbolicName.contains("RefreshTest")) {
                System.out.println(bundle.getSymbolicName());
                return bundle.getBundleContext();
            }
        }
        fail("Cannot find bundle context");
        return null;
    }
}
