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

package org.eclipse.virgo.kernel.osgi.test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.junit.*;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.Version;

import org.eclipse.virgo.kernel.osgi.framework.OsgiFrameworkUtils;
import org.eclipse.virgo.kernel.osgi.framework.OsgiServiceHolder;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiBundle;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiExportPackage;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiFramework;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiFrameworkFactory;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiImportPackage;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiPackageResolutionFailure;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiRequiredBundle;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiResolutionFailure;
import org.eclipse.virgo.kernel.test.AbstractKernelIntegrationTest;
import org.eclipse.virgo.util.osgi.manifest.BundleManifest;
import org.eclipse.virgo.util.osgi.manifest.BundleManifestFactory;
import org.eclipse.virgo.util.osgi.manifest.RequireBundle;

public class QuasiFrameworkIntegrationTests extends AbstractKernelIntegrationTest {

    private static final String EXPORTER_BSN = "exporter";

    private static final String IMPORTER_BSN = "importer";
    
    private static final String REQUIRING_BSN = "requirer";

    private static final String IMPORTER_JAR_PATH = "src/test/resources/quasi/simpleimporter.jar";
    
    private static final String EXPORTER_JAR_PATH = "src/test/resources/quasi/simpleexporter.jar";

    private static final String QUASI_TEST_PACKAGE = "quasi.test";

    private static final Version BUNDLE_VERSION = new Version("2.3");

    private QuasiFramework quasiFramework;

    @Before
    public void setUp() {
        OsgiServiceHolder<QuasiFrameworkFactory> holder = OsgiFrameworkUtils.getService(this.kernelContext, QuasiFrameworkFactory.class);
        QuasiFrameworkFactory quasiFrameworkFactory = holder.getService();
        Assert.assertNotNull(quasiFrameworkFactory);
        this.quasiFramework = quasiFrameworkFactory.create();
        Assert.assertNotNull(this.quasiFramework);
    }
    
    @After
    public void tearDown() {
        this.quasiFramework.destroy();
    }

    @Test
    public void testInstall() throws Exception {
        BundleManifest bundleManifest = getBundleManifest("test");

        QuasiBundle quasiBundle = this.quasiFramework.install(new URI("test"), bundleManifest);
        Assert.assertEquals("test", quasiBundle.getSymbolicName());
        Assert.assertEquals(BUNDLE_VERSION, quasiBundle.getVersion());
        Assert.assertFalse(quasiBundle.isResolved());
        Assert.assertNull(quasiBundle.getBundle());
        
        long bundleId = quasiBundle.getBundleId();
        Assert.assertFalse(bundleId == 0);
        Assert.assertEquals(quasiBundle, this.quasiFramework.getBundle(bundleId));
    }

    @Test
    public void testBasicResolve() throws Exception {
        QuasiBundle exporterQuasiBundle = installExporterBundle();

        List<QuasiResolutionFailure> resolutionFailures = this.quasiFramework.resolve();
        Assert.assertNotNull(resolutionFailures);
        Assert.assertEquals(0, resolutionFailures.size());
        Assert.assertTrue(exporterQuasiBundle.isResolved());
    }

    @Test
    public void testWiringResolve() throws Exception {
        QuasiBundle importerQuasiBundle = installImporterBundle();
        QuasiBundle exporterQuasiBundle = installExporterBundle();

        List<QuasiResolutionFailure> resolutionFailures = this.quasiFramework.resolve();
        Assert.assertNotNull(resolutionFailures);
        Assert.assertEquals(0, resolutionFailures.size());
        Assert.assertTrue(importerQuasiBundle.isResolved());
        Assert.assertTrue(exporterQuasiBundle.isResolved());
    }

    @Test
    public void testFailedResolve() throws Exception {
        QuasiBundle importerQuasiBundle = installImporterBundle();

        List<QuasiResolutionFailure> resolutionFailures = this.quasiFramework.resolve();
        Assert.assertNotNull(resolutionFailures);
        Assert.assertEquals(1, resolutionFailures.size());
        QuasiResolutionFailure failure = resolutionFailures.get(0);
        Assert.assertTrue(failure instanceof QuasiPackageResolutionFailure);
        QuasiPackageResolutionFailure qprFailure = (QuasiPackageResolutionFailure) failure;
        Assert.assertEquals(QUASI_TEST_PACKAGE, qprFailure.getPackage());
        Assert.assertFalse(importerQuasiBundle.isResolved());
    }

    @Test
    public void testPackageWiringExploration() throws Exception {
        QuasiBundle importerQuasiBundle = installImporterBundle();
        QuasiBundle exporterQuasiBundle = installExporterBundle();
        
        List<QuasiResolutionFailure> resolutionFailures = this.quasiFramework.resolve();
        Assert.assertNotNull(resolutionFailures);
        Assert.assertEquals(0, resolutionFailures.size());
        Assert.assertTrue(importerQuasiBundle.isResolved());
        Assert.assertTrue(exporterQuasiBundle.isResolved());
        
        checkGetBundles(importerQuasiBundle, exporterQuasiBundle);
        
        checkDependents(importerQuasiBundle, exporterQuasiBundle);
        
        QuasiImportPackage qip = checkImportPackages(importerQuasiBundle, exporterQuasiBundle);
        
        QuasiExportPackage qep = checkExportPackages(importerQuasiBundle, exporterQuasiBundle);
        
        checkConsumers(qep, qip);
        
        checkProvider(qip, qep);
    }
    
    @Test
    public void testRequiredBundleExploration() throws Exception {
        QuasiBundle requiringQuasiBundle = installRequiringBundle();
        QuasiBundle exporterQuasiBundle = installExporterBundle();
        
        List<QuasiResolutionFailure> resolutionFailures = this.quasiFramework.resolve();
        Assert.assertNotNull(resolutionFailures);
        Assert.assertEquals(0, resolutionFailures.size());
        Assert.assertTrue(requiringQuasiBundle.isResolved());
        Assert.assertTrue(exporterQuasiBundle.isResolved());
        
        checkGetBundles(requiringQuasiBundle, exporterQuasiBundle);
        
        List<QuasiRequiredBundle> requiredBundles = requiringQuasiBundle.getRequiredBundles();
        Assert.assertEquals(1, requiredBundles.size());
        QuasiRequiredBundle quasiRequiredBundle = requiredBundles.get(0);
        QuasiBundle provider = quasiRequiredBundle.getProvider();
        Assert.assertEquals(exporterQuasiBundle, provider);
        
    }

    private void checkProvider(QuasiImportPackage qip, QuasiExportPackage qep) {
        QuasiExportPackage provider = qip.getProvider();
        Assert.assertEquals(qep, provider);
    }

    private void checkConsumers(QuasiExportPackage qep, QuasiImportPackage qip) {
        List<QuasiImportPackage> consumers = qep.getConsumers();
        Assert.assertEquals(1, consumers.size());
        QuasiImportPackage consumer = consumers.get(0);
        Assert.assertEquals(qip, consumer);
    }

    private QuasiImportPackage checkImportPackages(QuasiBundle importerQuasiBundle, QuasiBundle exporterQuasiBundle) {
        List<QuasiImportPackage> exporterImportPackages = exporterQuasiBundle.getImportPackages();
        Assert.assertEquals(0, exporterImportPackages.size());
        
        List<QuasiImportPackage> importPackages = importerQuasiBundle.getImportPackages();
        Assert.assertEquals(1, importPackages.size());
        QuasiImportPackage qip = importPackages.get(0);
        Assert.assertEquals("quasi.test", qip.getPackageName());
        return qip;
    }

    private QuasiExportPackage checkExportPackages(QuasiBundle importerQuasiBundle, QuasiBundle exporterQuasiBundle) {
        Assert.assertEquals(0, importerQuasiBundle.getExportPackages().size());
        
        List<QuasiExportPackage> exportPackages = exporterQuasiBundle.getExportPackages();
        Assert.assertEquals(1, exportPackages.size());
        QuasiExportPackage qep = exportPackages.get(0);
        Assert.assertEquals("quasi.test", qep.getPackageName());
        return qep;
    }

    private void checkDependents(QuasiBundle importerQuasiBundle, QuasiBundle exporterQuasiBundle) {
        List<QuasiBundle> dependents = exporterQuasiBundle.getDependents();
        Assert.assertEquals(1, dependents.size());
        Assert.assertEquals(importerQuasiBundle, dependents.get(0));
    }

    private void checkGetBundles(QuasiBundle importerQuasiBundle, QuasiBundle exporterQuasiBundle) {
        List<QuasiBundle> quasiBundles = this.quasiFramework.getBundles();
        
        boolean foundImporter = false;
        boolean foundExporter = false;
        
        for (QuasiBundle quasiBundle : quasiBundles) {
            if (quasiBundle.equals(importerQuasiBundle)) {
                foundImporter = true;
            }
            if (quasiBundle.equals(exporterQuasiBundle)) {
                foundExporter = true;
            }
            
        }
        
        Assert.assertTrue(foundImporter);
        Assert.assertTrue(foundExporter);
    }
    
    @Test
    public void testCommit() throws Exception {
        QuasiBundle importerQuasiBundle = installImporterBundle();
        QuasiBundle exporterQuasiBundle = installExporterBundle();

        List<QuasiResolutionFailure> resolutionFailures = this.quasiFramework.resolve();
        Assert.assertNotNull(resolutionFailures);
        Assert.assertEquals(0, resolutionFailures.size());
        this.quasiFramework.commit();
        
        Assert.assertTrue(importerQuasiBundle.isResolved());
        Bundle importerBundle = importerQuasiBundle.getBundle();
        Assert.assertNotNull(importerBundle);
        Assert.assertEquals(IMPORTER_BSN, importerBundle.getSymbolicName());
        
        Assert.assertTrue(exporterQuasiBundle.isResolved());
        Bundle exporterBundle = exporterQuasiBundle.getBundle();
        Assert.assertNotNull(exporterBundle);
        Assert.assertEquals(EXPORTER_BSN, exporterBundle.getSymbolicName());
        
        // Tidy up since test methods may run in any order in Java 7 
        importerBundle.uninstall();
        exporterBundle.uninstall();
    }
    
    private QuasiBundle installExporterBundle() throws BundleException, URISyntaxException {
        QuasiBundle exporterQuasiBundle;
        BundleManifest exporterBundleManifest = getBundleManifest(EXPORTER_BSN);
        exporterBundleManifest.getExportPackage().addExportedPackage(QUASI_TEST_PACKAGE);

        exporterQuasiBundle = this.quasiFramework.install(new URI(EXPORTER_JAR_PATH), exporterBundleManifest);
        Assert.assertEquals(EXPORTER_BSN, exporterQuasiBundle.getSymbolicName());
        Assert.assertEquals(BUNDLE_VERSION, exporterQuasiBundle.getVersion());
        Assert.assertFalse(exporterQuasiBundle.isResolved());
        Assert.assertNull(exporterQuasiBundle.getBundle());
        return exporterQuasiBundle;
    }

    private QuasiBundle installImporterBundle() throws BundleException, URISyntaxException {
        QuasiBundle importerQuasiBundle;
        BundleManifest importerBundleManifest = getBundleManifest(IMPORTER_BSN);
        importerBundleManifest.getImportPackage().addImportedPackage(QUASI_TEST_PACKAGE);

        importerQuasiBundle = this.quasiFramework.install(new URI(IMPORTER_JAR_PATH), importerBundleManifest);
        Assert.assertEquals(IMPORTER_BSN, importerQuasiBundle.getSymbolicName());
        Assert.assertEquals(BUNDLE_VERSION, importerQuasiBundle.getVersion());
        Assert.assertFalse(importerQuasiBundle.isResolved());
        Assert.assertNull(importerQuasiBundle.getBundle());
        return importerQuasiBundle;
    }
    
    private QuasiBundle installRequiringBundle() throws BundleException, URISyntaxException {
        QuasiBundle requiringQuasiBundle;
        BundleManifest requiringBundleManifest = getBundleManifest(REQUIRING_BSN);
        RequireBundle requireBundle = requiringBundleManifest.getRequireBundle();
        requireBundle.addRequiredBundle(EXPORTER_BSN);

        requiringQuasiBundle = this.quasiFramework.install(new URI(IMPORTER_JAR_PATH), requiringBundleManifest);
        Assert.assertEquals(REQUIRING_BSN, requiringQuasiBundle.getSymbolicName());
        Assert.assertEquals(BUNDLE_VERSION, requiringQuasiBundle.getVersion());
        Assert.assertFalse(requiringQuasiBundle.isResolved());
        Assert.assertNull(requiringQuasiBundle.getBundle());
        return requiringQuasiBundle;
    }

    private BundleManifest getBundleManifest(String symbolicName) {
        BundleManifest bundleManifest;
        bundleManifest = BundleManifestFactory.createBundleManifest();
        bundleManifest.setBundleManifestVersion(2);
        bundleManifest.getBundleSymbolicName().setSymbolicName(symbolicName);
        bundleManifest.setBundleVersion(BUNDLE_VERSION);
        return bundleManifest;
    }

}
