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

package org.eclipse.virgo.kernel.userregion.internal.importexpansion;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import org.eclipse.virgo.kernel.osgi.framework.ImportMergeException;
import org.eclipse.virgo.kernel.userregion.internal.importexpansion.StandardTrackedPackageImportsFactory;
import org.eclipse.virgo.kernel.userregion.internal.importexpansion.TrackedPackageImports;
import org.eclipse.virgo.kernel.userregion.internal.importexpansion.TrackedPackageImportsFactory;
import org.eclipse.virgo.util.osgi.manifest.VersionRange;
import org.eclipse.virgo.util.osgi.manifest.BundleManifest;
import org.eclipse.virgo.util.osgi.manifest.BundleManifestFactory;
import org.eclipse.virgo.util.osgi.manifest.ImportedPackage;
import org.eclipse.virgo.util.osgi.manifest.Resolution;

/**
 */
public class TrackedPackageImportsTests {

    private static final String TEST_SOURCE = "test source";

    private final TrackedPackageImportsFactory trackedPackageImportsFactory = new StandardTrackedPackageImportsFactory();

    @Test public void testTrivialMerge() throws ImportMergeException, IOException {

        BundleManifest manifestA = BundleManifestFactory.createBundleManifest(new StringReader("bundle-symbolicname: A\nimport-package: p"));
        TrackedPackageImports tpiA = this.trackedPackageImportsFactory.create(manifestA);

        BundleManifest manifestB = BundleManifestFactory.createBundleManifest(new StringReader("bundle-symbolicname: B\nimport-package: p"));
        TrackedPackageImports tpiB = this.trackedPackageImportsFactory.create(manifestB);
        
        TrackedPackageImports empty = this.trackedPackageImportsFactory.createEmpty();
        
        Assert.assertTrue(empty.isEmpty());
        Assert.assertFalse(tpiB.isEmpty());
        Assert.assertTrue(tpiA.isEquivalent(tpiB));
        Assert.assertFalse(tpiA.isEquivalent(empty));

        tpiB.merge(tpiA);
        tpiB.merge(empty);
        Map<String, ImportedPackage> mergedImports = convertImportedPackageListToMap(tpiB.getMergedImports());
        ImportedPackage pImport = mergedImports.get("p");
        Assert.assertNotNull("Missing merged import", pImport);
    }

    @Test public void testOverlappingVersionRanges() throws ImportMergeException, IOException {

        BundleManifest manifestA = BundleManifestFactory.createBundleManifest(new StringReader(
            "bundle-symbolicname: A\nimport-package: p;version=\"[1,3]\""));
        TrackedPackageImports tpiA = this.trackedPackageImportsFactory.create(manifestA);

        BundleManifest manifestB = BundleManifestFactory.createBundleManifest(new StringReader(
            "bundle-symbolicname: B\nimport-package: p;version=\"2\""));
        TrackedPackageImports tpiB = this.trackedPackageImportsFactory.create(manifestB);

        tpiB.merge(tpiA);
        Map<String, ImportedPackage> mergedImports = convertImportedPackageListToMap(tpiB.getMergedImports());
        ImportedPackage pImport = mergedImports.get("p");
        Assert.assertNotNull("Missing merged import", pImport);
        VersionRange v = new VersionRange(pImport.getAttributes().get("version"));
        Assert.assertTrue("Incorrectly merged version", v.isFloorInclusive() && v.isCeilingInclusive() && v.getFloor().getMajor() == 2
            && v.getCeiling().getMajor() == 3);
    }

    @Test public void testDisjointVersionRanges() throws IOException {
      
        BundleManifest manifestA = BundleManifestFactory.createBundleManifest(new StringReader(
            "bundle-symbolicname: A\nimport-package: p;version=\"[1,2]\""));
        TrackedPackageImports tpiA = this.trackedPackageImportsFactory.create(manifestA);

        BundleManifest manifestB = BundleManifestFactory.createBundleManifest(new StringReader(
            "bundle-symbolicname: B\nimport-package: p;version=\"[3,4]\""));
        TrackedPackageImports tpiB = this.trackedPackageImportsFactory.create(manifestB);

        try {
            tpiB.merge(tpiA);
            Assert.assertTrue("Exception should be thrown", false);
        } catch (ImportMergeException e) {
            System.out.println(e);
            Assert.assertEquals("Incorrect conflicting package name", "p", e.getConflictingPackageName());
            Assert.assertEquals("Incorrect sources", "bundle B, bundle A", e.getSources());
        }
    }

    @Test public void testOverlappingBundleVersionRanges() throws ImportMergeException, IOException {

        BundleManifest manifestA = BundleManifestFactory.createBundleManifest(new StringReader(
            "bundle-symbolicname: A\nimport-package: p;bundle-version=\"[1,3]\""));
        TrackedPackageImports tpiA = this.trackedPackageImportsFactory.create(manifestA);

        BundleManifest manifestB = BundleManifestFactory.createBundleManifest(new StringReader(
            "bundle-symbolicname: B\nimport-package: p;bundle-version=\"2\""));
        TrackedPackageImports tpiB = this.trackedPackageImportsFactory.create(manifestB);

        tpiB.merge(tpiA);
        Map<String, ImportedPackage> mergedImports = convertImportedPackageListToMap(tpiB.getMergedImports());
        ImportedPackage pImport = mergedImports.get("p");
        Assert.assertNotNull("Missing merged import", pImport);
        VersionRange v = new VersionRange(pImport.getAttributes().get("bundle-version"));
        Assert.assertTrue("Incorrectly merged version", v.isFloorInclusive() && v.isCeilingInclusive() && v.getFloor().getMajor() == 2
            && v.getCeiling().getMajor() == 3);
    }

    @Test public void testDisjointBundleVersionRanges() throws IOException {
        BundleManifest manifestA = BundleManifestFactory.createBundleManifest(new StringReader(
            "bundle-symbolicname: A\nimport-package: p;bundle-version=\"[1,2]\""));
        TrackedPackageImports tpiA = this.trackedPackageImportsFactory.create(manifestA);

        BundleManifest manifestB = BundleManifestFactory.createBundleManifest(new StringReader(
            "bundle-symbolicname: B\nimport-package: p;bundle-version=\"[3,4]\""));
        TrackedPackageImports tpiB = this.trackedPackageImportsFactory.create(manifestB);

        try {
            tpiB.merge(tpiA);
            Assert.assertTrue("Exception should be thrown", false);
        } catch (ImportMergeException e) {
            System.out.println(e);
            Assert.assertEquals("Incorrect conflicting package name", "p", e.getConflictingPackageName());
            Assert.assertEquals("Incorrect sources", "bundle B, bundle A", e.getSources());
        }
    }

    @Test public void testResolution() throws ImportMergeException, IOException {
        
        BundleManifest manifestA = BundleManifestFactory.createBundleManifest(new StringReader(
            "bundle-symbolicname: A\nimport-package: p;resolution:=optional"));
        TrackedPackageImports tpiA = this.trackedPackageImportsFactory.create(manifestA);

        BundleManifest manifestB = BundleManifestFactory.createBundleManifest(new StringReader(
            "bundle-symbolicname: B\nimport-package: p;resolution:=mandatory"));
        TrackedPackageImports tpiB = this.trackedPackageImportsFactory.create(manifestB);

        tpiB.merge(tpiA);
        Map<String, ImportedPackage> mergedImports = convertImportedPackageListToMap(tpiB.getMergedImports());
        ImportedPackage pImport = mergedImports.get("p");
        Assert.assertNotNull("Missing merged import", pImport);
        Assert.assertTrue("Incorrectly merged resolution", pImport.getResolution() == Resolution.MANDATORY);
    }

    @Test public void testConsistentAttributes() throws ImportMergeException, IOException {
        
        BundleManifest manifestA = BundleManifestFactory.createBundleManifest(new StringReader(
            "bundle-symbolicname: A\nimport-package: p;a1=v1;a2=v2"));
        TrackedPackageImports tpiA = this.trackedPackageImportsFactory.create(manifestA);

        BundleManifest manifestB = BundleManifestFactory.createBundleManifest(new StringReader(
            "bundle-symbolicname: B\nimport-package: p;a1=v1;a3=v3"));
        TrackedPackageImports tpiB = this.trackedPackageImportsFactory.create(manifestB);

        tpiB.merge(tpiA);
        Map<String, ImportedPackage> mergedImports = convertImportedPackageListToMap(tpiB.getMergedImports());
        ImportedPackage pImport = mergedImports.get("p");
        Assert.assertNotNull("Missing merged import", pImport);
        Map<String, String> pAttrs = pImport.getAttributes();
        Assert.assertEquals("Incorrectly merged attribute for a1", "v1", pAttrs.get("a1"));
        Assert.assertEquals("Incorrectly merged attribute for a2", "v2", pAttrs.get("a2"));
        Assert.assertEquals("Incorrectly merged attribute for a3", "v3", pAttrs.get("a3"));
    }

    @Test public void testInconsistentAttributes() throws ImportMergeException, IOException {
        
        BundleManifest manifestA = BundleManifestFactory.createBundleManifest(new StringReader("bundle-symbolicname: A\nimport-package: p;a1=v1"));
        TrackedPackageImports tpiA = this.trackedPackageImportsFactory.create(manifestA);

        BundleManifest manifestB = BundleManifestFactory.createBundleManifest(new StringReader("bundle-symbolicname: B\nimport-package: p;a1=v2"));
        TrackedPackageImports tpiB = this.trackedPackageImportsFactory.create(manifestB);

        BundleManifest manifestC = BundleManifestFactory.createBundleManifest(new StringReader("bundle-symbolicname: C\nimport-package: q"));
        TrackedPackageImports tpiC = this.trackedPackageImportsFactory.create(manifestC);
        tpiB.merge(tpiC);

        try {
            tpiB.merge(tpiA);
            Assert.assertTrue("Exception should be thrown", false);
        } catch (ImportMergeException e) {
            System.out.println(e);
            Assert.assertEquals("Incorrect conflicting package name", "p", e.getConflictingPackageName());
            Assert.assertEquals("Incorrect sources", "bundle B, bundle A", e.getSources());
        }
    }

    @Test public void testTrivialAddition() throws ImportMergeException, IOException {

        BundleManifest manifestA = BundleManifestFactory.createBundleManifest(new StringReader("bundle-symbolicname: A\nimport-package: p"));
        TrackedPackageImports tpiA = this.trackedPackageImportsFactory.create(manifestA);

        ImportedPackage packageImport = BundleManifestFactory.createBundleManifest().getImportPackage().addImportedPackage("p");
        List<ImportedPackage> packageImports = new ArrayList<ImportedPackage>();
        packageImports.add(packageImport);
        TrackedPackageImports tpiB = this.trackedPackageImportsFactory.create(packageImports, TEST_SOURCE);

        tpiA.merge(tpiB);
        Map<String, ImportedPackage> mergedImports = convertImportedPackageListToMap(tpiA.getMergedImports());
        ImportedPackage pImport = mergedImports.get("p");
        Assert.assertNotNull("Missing merged import", pImport);
    }

    @Test public void testClashingAddition() throws IOException {

        BundleManifest manifestA = BundleManifestFactory.createBundleManifest(new StringReader(
            "bundle-symbolicname: A\nimport-package: p;attr=x"));
        TrackedPackageImports tpiA = this.trackedPackageImportsFactory.create(manifestA);

        ImportedPackage packageImport = BundleManifestFactory.createBundleManifest().getImportPackage().addImportedPackage("p");
        packageImport.getAttributes().put("attr", "y");
        List<ImportedPackage> packageImports = new ArrayList<ImportedPackage>();
        packageImports.add(packageImport);
        TrackedPackageImports tpiB = this.trackedPackageImportsFactory.create(packageImports, TEST_SOURCE);

        try {
            tpiA.merge(tpiB);
            Assert.assertTrue("Exception should be thrown", false);
        } catch (ImportMergeException e) {
            System.out.println(e);
            Assert.assertEquals("Incorrect conflicting package name", "p", e.getConflictingPackageName());
            Assert.assertEquals("Incorrect sources", "bundle A, " + TEST_SOURCE, e.getSources());
        }
    }

    @Test public void testOverlappingVersionRangesInCollection() throws ImportMergeException, IOException {

        BundleManifest manifestA = BundleManifestFactory.createBundleManifest(new StringReader(
            "bundle-symbolicname: A\nimport-package: p;version=\"[1,3]\""));
        TrackedPackageImports tpiA = this.trackedPackageImportsFactory.create(manifestA);

        BundleManifest manifestB = BundleManifestFactory.createBundleManifest(new StringReader(
            "bundle-symbolicname: B\nimport-package: p;version=\"2\""));
        TrackedPackageImports tpiB = this.trackedPackageImportsFactory.create(manifestB);

        TrackedPackageImports tpiC = this.trackedPackageImportsFactory.createCollector();

        tpiC.merge(tpiB);
        tpiC.merge(tpiA);

        Map<String, ImportedPackage> mergedImports = convertImportedPackageListToMap(tpiC.getMergedImports());
        ImportedPackage pImport = mergedImports.get("p");
        Assert.assertNotNull("Missing merged import", pImport);
        VersionRange v = new VersionRange(pImport.getAttributes().get("version"));
        Assert.assertTrue("Incorrectly merged version", v.isFloorInclusive() && v.isCeilingInclusive() && v.getFloor().getMajor() == 2
            && v.getCeiling().getMajor() == 3);
    }

    @Test public void testDisjointVersionRangesInCollection() throws IOException {
        
        BundleManifest manifestA = BundleManifestFactory.createBundleManifest(new StringReader(
            "bundle-symbolicname: A\nimport-package: p;version=\"[1,2]\""));
        TrackedPackageImports tpiA = this.trackedPackageImportsFactory.create(manifestA);

        BundleManifest manifestB = BundleManifestFactory.createBundleManifest(new StringReader(
            "bundle-symbolicname: B\nimport-package: p;version=\"[3,4]\""));
        TrackedPackageImports tpiB = this.trackedPackageImportsFactory.create(manifestB);

        TrackedPackageImports tpiC = this.trackedPackageImportsFactory.createCollector();

        try {
            tpiC.merge(tpiB);
            tpiC.merge(tpiA);
            Assert.assertTrue("Exception should be thrown", false);
        } catch (ImportMergeException e) {
            System.out.println(e);
            Assert.assertEquals("Incorrect conflicting package name", "p", e.getConflictingPackageName());
            Assert.assertEquals("Incorrect sources", "bundle B, bundle A", e.getSources());
        }
    }

    @Test public void testThreeWayClash() throws IOException {
        
        BundleManifest manifestA = BundleManifestFactory.createBundleManifest(new StringReader(
            "bundle-symbolicname: A\nimport-package: p;version=\"[1,2]\""));
        TrackedPackageImports tpiA = this.trackedPackageImportsFactory.create(manifestA);

        BundleManifest manifestB = BundleManifestFactory.createBundleManifest(new StringReader(
            "bundle-symbolicname: B\nimport-package: p;version=\"[2,3]\""));
        TrackedPackageImports tpiB = this.trackedPackageImportsFactory.create(manifestB);

        TrackedPackageImports tpiC = this.trackedPackageImportsFactory.createCollector();

        BundleManifest manifestD = BundleManifestFactory.createBundleManifest(new StringReader(
            "bundle-symbolicname: D\nimport-package: p;version=\"[3,3]\""));
        TrackedPackageImports tpiD = this.trackedPackageImportsFactory.create(manifestD);

        try {
            tpiC.merge(tpiB);
            tpiC.merge(tpiA);
            tpiD.merge(tpiC);
            Assert.assertTrue("Exception should be thrown", false);
        } catch (ImportMergeException e) {
            System.out.println(e);
            Assert.assertEquals("Incorrect conflicting package name", "p", e.getConflictingPackageName());
            /*
             * Note that in a N-way clash of version ranges, there must be a pair of version ranges that clash, but the
             * current implementation simply reports all N.
             */
            Assert.assertEquals("Incorrect sources", "bundle D, bundle B, bundle A", e.getSources());
        }
    }

    @Test public void testThreeWayNestedClash() throws IOException {
        
        BundleManifest manifestA = BundleManifestFactory.createBundleManifest(new StringReader(
            "bundle-symbolicname: A\nimport-package: p;version=\"[1,2]\""));
        TrackedPackageImports tpiA = this.trackedPackageImportsFactory.create(manifestA);

        BundleManifest manifestB = BundleManifestFactory.createBundleManifest(new StringReader(
            "bundle-symbolicname: B\nimport-package: p;version=\"[2,3]\""));
        TrackedPackageImports tpiB = this.trackedPackageImportsFactory.create(manifestB);

        BundleManifest manifestD = BundleManifestFactory.createBundleManifest(new StringReader(
            "bundle-symbolicname: D\nimport-package: p;version=\"[3,3]\""));
        TrackedPackageImports tpiD = this.trackedPackageImportsFactory.create(manifestD);

        TrackedPackageImports tpiContainer = this.trackedPackageImportsFactory.createContainer("container");

        try {
            tpiContainer.merge(tpiA);
            tpiContainer.merge(tpiB);
            tpiD.merge(tpiContainer);
            Assert.assertTrue("Exception should be thrown", false);
        } catch (ImportMergeException e) {
            System.out.println(e);
            Assert.assertEquals("Incorrect conflicting package name", "p", e.getConflictingPackageName());
            /*
             * Note that in a N-way clash of version ranges, there must be a pair of version ranges that clash, but the
             * current implementation simply reports all N.
             */
            Assert.assertEquals("Incorrect sources", "bundle D, container(bundle A, bundle B)", e.getSources());
        }
    }

    @Test public void testFourWayNestedClashInACollector() throws IOException {
        
        BundleManifest manifestA = BundleManifestFactory.createBundleManifest(new StringReader(
            "bundle-symbolicname: A\nimport-package: p;version=\"[1,2]\""));
        TrackedPackageImports tpiA = this.trackedPackageImportsFactory.create(manifestA);

        BundleManifest manifestB = BundleManifestFactory.createBundleManifest(new StringReader(
            "bundle-symbolicname: B\nimport-package: p;version=\"[2,3]\""));
        TrackedPackageImports tpiB = this.trackedPackageImportsFactory.create(manifestB);

        BundleManifest manifestC = BundleManifestFactory.createBundleManifest(new StringReader(
            "bundle-symbolicname: C\nimport-package: p;version=\"[3,4]\""));
        TrackedPackageImports tpiC = this.trackedPackageImportsFactory.create(manifestC);

        BundleManifest manifestD = BundleManifestFactory.createBundleManifest(new StringReader(
            "bundle-symbolicname: D\nimport-package: p;version=\"[4,5]\""));
        TrackedPackageImports tpiD = this.trackedPackageImportsFactory.create(manifestD);

        TrackedPackageImports tpiContainerX = this.trackedPackageImportsFactory.createContainer("containerX");
        
        TrackedPackageImports tpiContainerY = this.trackedPackageImportsFactory.createContainer("containerY");
        
        TrackedPackageImports tpiCollector = this.trackedPackageImportsFactory.createCollector();

        try {
            tpiContainerX.merge(tpiA);
            tpiContainerX.merge(tpiB);
            tpiContainerY.merge(tpiC);
            tpiContainerY.merge(tpiD);
            tpiCollector.merge(tpiContainerX);
            tpiCollector.merge(tpiContainerY);
            Assert.assertTrue("Exception should be thrown", false);
        } catch (ImportMergeException e) {
            System.out.println(e);
            Assert.assertEquals("Incorrect conflicting package name", "p", e.getConflictingPackageName());
            /*
             * Note that in a N-way clash of version ranges, there must be a pair of version ranges that clash, but the
             * current implementation simply reports all N.
             */
            Assert.assertEquals("Incorrect sources", "containerX(bundle A, bundle B), containerY(bundle C, bundle D)", e.getSources());
        }
    }

    /**
     * Convert a given list of package imports with no duplicate package names to a map of package name to
     * {@link ImportedPackage}.
     * 
     * @param importedPackages a list of <code>PackageImport</code>
     * @return a map of package name to <code>PackageImport</code>
     */
    private static Map<String, ImportedPackage> convertImportedPackageListToMap(List<ImportedPackage> importedPackages) {
        Map<String, ImportedPackage> initialPackageImports = new HashMap<String, ImportedPackage>();
        for (ImportedPackage importedPackage : importedPackages) {            
            Assert.assertNull(initialPackageImports.put(importedPackage.getPackageName(), importedPackage));
        }
        return initialPackageImports;
    }
}
