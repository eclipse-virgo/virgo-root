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

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.virgo.kernel.artifact.bundle.BundleBridge;
import org.eclipse.virgo.kernel.artifact.library.LibraryBridge;
import org.eclipse.virgo.kernel.osgi.framework.UnableToSatisfyBundleDependenciesException;
import org.eclipse.virgo.kernel.osgi.framework.UnableToSatisfyDependenciesException;
import org.eclipse.virgo.kernel.userregion.internal.equinox.StubHashGenerator;
import org.eclipse.virgo.medic.test.eventlog.LoggedEvent;
import org.eclipse.virgo.medic.test.eventlog.MockEventLogger;
import org.eclipse.virgo.repository.ArtifactDescriptor;
import org.eclipse.virgo.repository.ArtifactGenerationException;
import org.eclipse.virgo.repository.Attribute;
import org.eclipse.virgo.repository.Query;
import org.eclipse.virgo.repository.Repository;
import org.eclipse.virgo.repository.RepositoryAwareArtifactDescriptor;
import org.eclipse.virgo.util.osgi.manifest.BundleManifest;
import org.eclipse.virgo.util.osgi.manifest.BundleManifestFactory;
import org.eclipse.virgo.util.osgi.manifest.ImportedBundle;
import org.eclipse.virgo.util.osgi.manifest.ImportedLibrary;
import org.eclipse.virgo.util.osgi.manifest.ImportedPackage;
import org.eclipse.virgo.util.osgi.manifest.Resolution;
import org.eclipse.virgo.util.osgi.manifest.VersionRange;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Version;

/**
 */
public class ImportExpansionHandlerTests {

    private StubRepository repository = new StubRepository();

    private static Set<String> packagesExportedBySystemBundle = new HashSet<>();

    static {
        packagesExportedBySystemBundle.add("javax.crypto.spec");
        packagesExportedBySystemBundle.add("javax.imageio");
        packagesExportedBySystemBundle.add("javax.imageio.event");
    }

    @Before
    public void populateRepository() throws ArtifactGenerationException {
        BundleBridge bundleBridge = new BundleBridge(new StubHashGenerator());
        LibraryBridge libraryBridge = new LibraryBridge(new StubHashGenerator());

        this.repository.addArtifactDescriptor(bundleBridge.generateArtifactDescriptor(new File(System.getProperty("user.home")
            + "/.gradle/caches/modules-2/files-2.1/org.eclipse.virgo.mirrored/org.springframework.core/5.0.8.RELEASE"
            + "/415c7d22dcab46985f27bbe1ce6de968e073497c/org.springframework.core-5.0.8.RELEASE.jar")));
        this.repository.addArtifactDescriptor(bundleBridge.generateArtifactDescriptor(new File(System.getProperty("user.home")
            + "/.gradle/caches/modules-2/files-2.1/org.eclipse.virgo.mirrored/org.springframework.beans/5.0.8.RELEASE"
            + "/bf5fd324c11eb63777f810250cb8c2ea292f9279/org.springframework.beans-5.0.8.RELEASE.jar")));
        this.repository.addArtifactDescriptor(bundleBridge.generateArtifactDescriptor(new File("src/test/resources/silht/bundles/fragmentOne")));
        this.repository.addArtifactDescriptor(bundleBridge.generateArtifactDescriptor(new File("src/test/resources/silht/bundles/fragmentTwo")));
        this.repository.addArtifactDescriptor(bundleBridge.generateArtifactDescriptor(new File("src/test/resources/silht/bundles/fragmentThree")));
        this.repository.addArtifactDescriptor(bundleBridge.generateArtifactDescriptor(new File("src/test/resources/silht/bundles/noexports")));
        this.repository.addArtifactDescriptor(bundleBridge.generateArtifactDescriptor(new File("src/test/resources/silht/bundles/fragmentwithnoexports")));
        this.repository.addArtifactDescriptor(bundleBridge.generateArtifactDescriptor(new File("src/test/resources/silht/bundles/host")));
        this.repository.addArtifactDescriptor(bundleBridge.generateArtifactDescriptor(new File("src/test/resources/silht/bundles/overlapper")));
        this.repository.addArtifactDescriptor(bundleBridge.generateArtifactDescriptor(new File("src/test/resources/silht/bundles/multi-version-export")));
        this.repository.addArtifactDescriptor(libraryBridge.generateArtifactDescriptor(new File("src/test/resources/silht/libraries/spring.libd")));
        this.repository.addArtifactDescriptor(libraryBridge.generateArtifactDescriptor(new File("src/test/resources/silht/libraries/com.foo.libd")));
        this.repository.addArtifactDescriptor(libraryBridge.generateArtifactDescriptor(new File("src/test/resources/silht/libraries/missing.optional.bundle.libd")));
    }

    @Test
    public void basicImportBundle() throws UnableToSatisfyDependenciesException {
        List<Object> mocks = new ArrayList<>();

        ImportedBundle bundleImport = createAndStoreMock(ImportedBundle.class, mocks);
        expect(bundleImport.getBundleSymbolicName()).andReturn("org.springframework.core").atLeastOnce();
        expect(bundleImport.getVersion()).andReturn(new VersionRange("[5,6)")).atLeastOnce();
        expect(bundleImport.isApplicationImportScope()).andReturn(false).atLeastOnce();
        expect(bundleImport.getResolution()).andReturn(Resolution.MANDATORY).atLeastOnce();

        ImportExpansionHandler handler = new ImportExpansionHandler(repository, packagesExportedBySystemBundle, new MockEventLogger());

        replayMocks(mocks);

        BundleManifest bundleManifest = BundleManifestFactory.createBundleManifest();

        handler.expandImports(new ArrayList<>(), singletonList(bundleImport), bundleManifest);

        verifyMocks(mocks);

        assertTrue(bundleManifest.getImportPackage().getImportedPackages().size() > 19);

        List<ImportedPackage> packageImports = bundleManifest.getImportPackage().getImportedPackages();
        for (ImportedPackage packageImport : packageImports) {
            Map<String, String> attributes = packageImport.getAttributes();
            assertEquals("org.springframework.core", attributes.get("bundle-symbolic-name"));
            assertEquals(new VersionRange("[5.0.8.RELEASE,5.0.8.RELEASE]"), new VersionRange(attributes.get("bundle-version")));
        }
    }

    @Test
    public void basicImportLibrary() throws UnableToSatisfyDependenciesException {

        List<Object> mocks = new ArrayList<>();

        ImportedLibrary libraryImport = createAndStoreMock(ImportedLibrary.class, mocks);

        expect(libraryImport.getLibrarySymbolicName()).andReturn("org.springframework").atLeastOnce();
        expect(libraryImport.getVersion()).andReturn(new VersionRange("[5,6)")).atLeastOnce();
        expect(libraryImport.getResolution()).andReturn(Resolution.MANDATORY).anyTimes();

        ImportExpansionHandler handler = new ImportExpansionHandler(repository, packagesExportedBySystemBundle, new MockEventLogger());

        replayMocks(mocks);

        BundleManifest bundleManifest = BundleManifestFactory.createBundleManifest();

        handler.expandImports(singletonList(libraryImport), asList(new ImportedBundle[0]), bundleManifest);

        verifyMocks(mocks);

        assertTrue(bundleManifest.getImportPackage().getImportedPackages().size() > 33);

        List<ImportedPackage> packageImports = bundleManifest.getImportPackage().getImportedPackages();
        for (ImportedPackage packageImport : packageImports) {
            Map<String, String> attributes = packageImport.getAttributes();
            if (packageImport.getPackageName().startsWith("org.springframework.beans")) {
                assertEquals("org.springframework.beans", attributes.get("bundle-symbolic-name"));
            } else {
                assertEquals("org.springframework.core", attributes.get("bundle-symbolic-name"));
            }
            assertEquals(new VersionRange("[5.0.8.RELEASE,5.0.8.RELEASE]"), new VersionRange(attributes.get("bundle-version")));
        }

    }

    @Test
    public void basicImportFragmentBundle() throws UnableToSatisfyDependenciesException {
        List<Object> mocks = new ArrayList<>();

        ImportedBundle bundleImport = createAndStoreMock(ImportedBundle.class, mocks);
        expect(bundleImport.getBundleSymbolicName()).andReturn("com.foo.fragment.one").atLeastOnce();
        expect(bundleImport.getVersion()).andReturn(new VersionRange("[1,1]")).atLeastOnce();
        expect(bundleImport.isApplicationImportScope()).andReturn(false).atLeastOnce();
        expect(bundleImport.getResolution()).andReturn(Resolution.MANDATORY).anyTimes();

        ImportExpansionHandler handler = new ImportExpansionHandler(repository, packagesExportedBySystemBundle, new MockEventLogger());

        replayMocks(mocks);

        BundleManifest bundleManifest = BundleManifestFactory.createBundleManifest();

        handler.expandImports(asList(new ImportedLibrary[0]), singletonList(bundleImport), bundleManifest);

        verifyMocks(mocks);

        assertEquals(1, bundleManifest.getImportPackage().getImportedPackages().size());

        ImportedPackage packageImport = bundleManifest.getImportPackage().getImportedPackages().get(0);
        Map<String, String> attributes = packageImport.getAttributes();
        assertEquals("com.foo.host", attributes.get("bundle-symbolic-name"));
        assertEquals(new VersionRange("[1.0, 2.0)"), new VersionRange(attributes.get("bundle-version")));
        assertEquals("com.foo.host", packageImport.getPackageName());
    }

    @Test
    public void basicImportFragmentBundleSpecifyingExactBundleVersionRange() throws UnableToSatisfyDependenciesException {
        List<Object> mocks = new ArrayList<>();

        ImportedBundle bundleImport = createAndStoreMock(ImportedBundle.class, mocks);
        expect(bundleImport.getBundleSymbolicName()).andReturn("com.foo.fragment.two").atLeastOnce();
        expect(bundleImport.getVersion()).andReturn(new VersionRange("[3,3]")).atLeastOnce();
        expect(bundleImport.isApplicationImportScope()).andReturn(false).atLeastOnce();
        expect(bundleImport.getResolution()).andReturn(Resolution.MANDATORY).anyTimes();

        ImportExpansionHandler handler = new ImportExpansionHandler(repository, packagesExportedBySystemBundle, new MockEventLogger());

        replayMocks(mocks);

        BundleManifest bundleManifest = BundleManifestFactory.createBundleManifest();

        handler.expandImports(asList(new ImportedLibrary[0]), singletonList(bundleImport), bundleManifest);

        verifyMocks(mocks);
    }

    @Test
    public void basicImportFragmentBundleWithNoFragmentHostBundleVersion() throws UnableToSatisfyDependenciesException {
        List<Object> mocks = new ArrayList<>();

        ImportedBundle bundleImport = createAndStoreMock(ImportedBundle.class, mocks);
        expect(bundleImport.getBundleSymbolicName()).andReturn("com.foo.fragment.three").atLeastOnce();
        expect(bundleImport.getVersion()).andReturn(new VersionRange("[0,3]")).atLeastOnce();
        expect(bundleImport.isApplicationImportScope()).andReturn(false).atLeastOnce();
        expect(bundleImport.getResolution()).andReturn(Resolution.MANDATORY).anyTimes();

        ImportExpansionHandler handler = new ImportExpansionHandler(repository, packagesExportedBySystemBundle, new MockEventLogger());

        replayMocks(mocks);

        BundleManifest bundleManifest = BundleManifestFactory.createBundleManifest();

        handler.expandImports(asList(new ImportedLibrary[0]), singletonList(bundleImport), bundleManifest);

        verifyMocks(mocks);

        assertEquals(1, bundleManifest.getImportPackage().getImportedPackages().size());

        ImportedPackage packageImport = bundleManifest.getImportPackage().getImportedPackages().get(0);
        Map<String, String> attributes = packageImport.getAttributes();
        assertEquals("com.foo.host", attributes.get("bundle-symbolic-name"));
        assertEquals(new VersionRange("0"), new VersionRange(attributes.get("bundle-version")));
        assertEquals("com.foo.fragment.three", packageImport.getPackageName());
        assertEquals(VersionRange.createExactRange(new Version("1")), packageImport.getVersion());
    }

    @Test(expected = UnableToSatisfyBundleDependenciesException.class)
    public void importLibraryReferringToNonExistentBundle() throws UnableToSatisfyDependenciesException, IOException {

        List<Object> mocks = new ArrayList<>();

        ImportedLibrary libraryImport = createAndStoreMock(ImportedLibrary.class, mocks);

        expect(libraryImport.getLibrarySymbolicName()).andReturn("bad.bundle").atLeastOnce();
        expect(libraryImport.getVersion()).andReturn(new VersionRange("[9,9]")).atLeastOnce();
        expect(libraryImport.getResolution()).andReturn(Resolution.MANDATORY).anyTimes();

        ImportExpansionHandler handler = new ImportExpansionHandler(repository, packagesExportedBySystemBundle, new MockEventLogger());

        replayMocks(mocks);

        BundleManifest bundleManifest = BundleManifestFactory.createBundleManifest(new StringReader(
            "Manifest-Version: 1.0\nBundle-SymbolicName: test.bundle"));

        handler.expandImports(singletonList(libraryImport), asList(new ImportedBundle[0]), bundleManifest);
    }

    @Test
    public void optionalImportBundle() throws UnableToSatisfyDependenciesException {
        List<Object> mocks = new ArrayList<>();

        ImportedBundle bundleImport = createAndStoreMock(ImportedBundle.class, mocks);
        expect(bundleImport.getBundleSymbolicName()).andReturn("org.springframework.dosnt.exist").atLeastOnce();
        expect(bundleImport.getVersion()).andReturn(new VersionRange("[6.5,7.0)")).atLeastOnce();
        expect(bundleImport.isApplicationImportScope()).andReturn(false).anyTimes();
        expect(bundleImport.getResolution()).andReturn(Resolution.OPTIONAL).anyTimes();

        ImportExpansionHandler handler = new ImportExpansionHandler(repository, packagesExportedBySystemBundle, new MockEventLogger());

        replayMocks(mocks);

        BundleManifest bundleManifest = BundleManifestFactory.createBundleManifest();

        handler.expandImports(asList(new ImportedLibrary[0]), singletonList(bundleImport), bundleManifest);

        verifyMocks(mocks);

        assertEquals("" + bundleManifest.getImportPackage().getImportedPackages().size(), 0,
            bundleManifest.getImportPackage().getImportedPackages().size());
    }

    @Test
    public void optionalImportLibrary() throws UnableToSatisfyDependenciesException {

        List<Object> mocks = new ArrayList<>();

        ImportedLibrary libraryImport = createAndStoreMock(ImportedLibrary.class, mocks);

        expect(libraryImport.getLibrarySymbolicName()).andReturn("org.springframework.dosnt.exist").atLeastOnce();
        expect(libraryImport.getVersion()).andReturn(new VersionRange("[6.5,7.0)")).atLeastOnce();
        expect(libraryImport.getResolution()).andReturn(Resolution.OPTIONAL).atLeastOnce();

        ImportExpansionHandler handler = new ImportExpansionHandler(repository, packagesExportedBySystemBundle, new MockEventLogger());

        replayMocks(mocks);

        BundleManifest bundleManifest = BundleManifestFactory.createBundleManifest();

        handler.expandImports(singletonList(libraryImport), asList(new ImportedBundle[0]), bundleManifest);

        verifyMocks(mocks);

        assertEquals(0, bundleManifest.getImportPackage().getImportedPackages().size());
    }

    @Test(expected = UnableToSatisfyDependenciesException.class)
    public void optionalImportLibraryException() throws UnableToSatisfyDependenciesException {

        List<Object> mocks = new ArrayList<>();

        ImportedLibrary libraryImport = createAndStoreMock(ImportedLibrary.class, mocks);

        expect(libraryImport.getLibrarySymbolicName()).andReturn("org.springframework.dosnt.exist").atLeastOnce();
        expect(libraryImport.getVersion()).andReturn(new VersionRange("[6.5,7.0)")).atLeastOnce();
        expect(libraryImport.getResolution()).andReturn(Resolution.MANDATORY).atLeastOnce();

        ImportExpansionHandler handler = new ImportExpansionHandler(repository, packagesExportedBySystemBundle, new MockEventLogger());

        replayMocks(mocks);

        BundleManifest bundleManifest = BundleManifestFactory.createBundleManifest();

        handler.expandImports(singletonList(libraryImport), asList(new ImportedBundle[0]), bundleManifest);
    }

    /**
     * Test the expansion of the following import:
     *
     * Import-Library: com.foo;bundle-version="[1.0,2.0)"
     */
    @Test
    public void importLibraryWithFragment() throws UnableToSatisfyDependenciesException {
        List<Object> mocks = new ArrayList<>();

        ImportedLibrary libraryImport = createAndStoreMock(ImportedLibrary.class, mocks);
        expect(libraryImport.getLibrarySymbolicName()).andReturn("com.foo").atLeastOnce();
        expect(libraryImport.getVersion()).andReturn(new VersionRange("[1.0,2.0)")).atLeastOnce();
        expect(libraryImport.getResolution()).andReturn(Resolution.MANDATORY).anyTimes();

        ImportExpansionHandler handler = new ImportExpansionHandler(repository, packagesExportedBySystemBundle, new MockEventLogger());

        replayMocks(mocks);

        BundleManifest bundleManifest = BundleManifestFactory.createBundleManifest();
        handler.expandImports(singletonList(libraryImport), asList(new ImportedBundle[0]), bundleManifest);

        verifyMocks(mocks);

        assertImported(bundleManifest, asList("com.foo.host", "com.foo.host.a", "com.foo.host.b", "com.foo.fragment.two"),
            asList("1.5.0", "1.0.0", "1.0.0", "1.0.0"));
    }

    @Test(expected = UnableToSatisfyDependenciesException.class)
    public void incompatibleBundleVersions() throws UnableToSatisfyDependenciesException {
        List<Object> mocks = new ArrayList<>();

        ImportedLibrary libraryImport1 = createAndStoreMock(ImportedLibrary.class, mocks);
        expect(libraryImport1.getLibrarySymbolicName()).andReturn("org.springframework").atLeastOnce();
        expect(libraryImport1.getVersion()).andReturn(new VersionRange("[2.5,3.0)")).atLeastOnce();
        expect(libraryImport1.getResolution()).andReturn(Resolution.MANDATORY).atLeastOnce();

        ImportedLibrary libraryImport2 = createAndStoreMock(ImportedLibrary.class, mocks);
        expect(libraryImport2.getLibrarySymbolicName()).andReturn("org.springframework").atLeastOnce();
        expect(libraryImport2.getVersion()).andReturn(new VersionRange("[2.0,2.5)")).atLeastOnce();
        expect(libraryImport2.getResolution()).andReturn(Resolution.MANDATORY).atLeastOnce();

        ImportExpansionHandler handler = new ImportExpansionHandler(repository, packagesExportedBySystemBundle, new MockEventLogger());

        replayMocks(mocks);

        BundleManifest bundleManifest = BundleManifestFactory.createBundleManifest();
        handler.expandImports(asList(libraryImport1, libraryImport2), asList(new ImportedBundle[0]),
            bundleManifest);
    }

    @Test(expected = UnableToSatisfyDependenciesException.class)
    public void incompatibleIntersection() throws UnableToSatisfyDependenciesException {
        List<Object> mocks = new ArrayList<>();

        ImportedLibrary libraryImport1 = createAndStoreMock(ImportedLibrary.class, mocks);
        expect(libraryImport1.getLibrarySymbolicName()).andReturn("com.intersect.one").atLeastOnce();
        expect(libraryImport1.getVersion()).andReturn(new VersionRange("[1.0,2.0)")).atLeastOnce();
        expect(libraryImport1.getResolution()).andReturn(Resolution.MANDATORY).atLeastOnce();

        ImportedLibrary libraryImport2 = createAndStoreMock(ImportedLibrary.class, mocks);
        expect(libraryImport2.getLibrarySymbolicName()).andReturn("com.intersect.two").atLeastOnce();
        expect(libraryImport2.getVersion()).andReturn(new VersionRange("[1.0,2.0)")).atLeastOnce();
        expect(libraryImport2.getResolution()).andReturn(Resolution.MANDATORY).atLeastOnce();

        ImportExpansionHandler handler = new ImportExpansionHandler(repository, packagesExportedBySystemBundle, new MockEventLogger());

        replayMocks(mocks);

        BundleManifest bundleManifest = BundleManifestFactory.createBundleManifest();
        handler.expandImports(asList(libraryImport1, libraryImport2), asList(new ImportedBundle[0]),
            bundleManifest);
    }

    @Test(expected = UnableToSatisfyBundleDependenciesException.class)
    public void disjointImportedPackageAndImportedLibraryVersionRanges() throws UnableToSatisfyDependenciesException, IOException {
        List<Object> mocks = new ArrayList<>();

        ImportedLibrary libraryImport = createAndStoreMock(ImportedLibrary.class, mocks);
        expect(libraryImport.getLibrarySymbolicName()).andReturn("org.springframework").atLeastOnce();
        expect(libraryImport.getVersion()).andReturn(new VersionRange("[2.5,3.0)")).atLeastOnce();
        expect(libraryImport.getResolution()).andReturn(Resolution.MANDATORY).atLeastOnce();

        ImportExpansionHandler handler = new ImportExpansionHandler(repository, packagesExportedBySystemBundle, new MockEventLogger());

        replayMocks(mocks);

        BundleManifest bundleManifest = BundleManifestFactory.createBundleManifest(new StringReader(
            "Bundle-SymbolicName: B\nImport-Package: org.springframework.core;version=\"[1,2]\""));
        handler.expandImports(singletonList(libraryImport), asList(new ImportedBundle[0]), bundleManifest);
    }

    @Test(expected = UnableToSatisfyDependenciesException.class)
    // TODO review - this test had a missing @Test annotation
    public void disjointImportedPackageAndImportedBundleVersionRanges() throws UnableToSatisfyDependenciesException, IOException {
        List<Object> mocks = new ArrayList<>();

        ImportedBundle bundleImport = createAndStoreMock(ImportedBundle.class, mocks);
        expect(bundleImport.getBundleSymbolicName()).andReturn("org.springframework.bundle.spring.core").atLeastOnce();
        expect(bundleImport.getVersion()).andReturn(new VersionRange("[2.5,3.0)")).atLeastOnce();
        expect(bundleImport.getResolution()).andReturn(Resolution.MANDATORY).atLeastOnce();
        expect(bundleImport.isApplicationImportScope()).andReturn(false);

        ImportExpansionHandler handler = new ImportExpansionHandler(repository, packagesExportedBySystemBundle, new MockEventLogger());

        replayMocks(mocks);

        BundleManifest bundleManifest = BundleManifestFactory.createBundleManifest(new StringReader(
            "Bundle-SymbolicName: B\nImport-Package: org.springframework.core;version=\"[1,2]\""));
        handler.expandImports(asList(new ImportedLibrary[0]), singletonList(bundleImport), bundleManifest);
    }

    @Test
    public void packageImportAndImportedBundleVersionRangeIntersection() throws UnableToSatisfyDependenciesException, IOException {
        List<Object> mocks = new ArrayList<>();

        ImportedBundle bundleImport = createAndStoreMock(ImportedBundle.class, mocks);
        expect(bundleImport.getBundleSymbolicName()).andReturn("org.springframework.core").atLeastOnce();
        expect(bundleImport.getVersion()).andReturn(new VersionRange("[5,6)")).atLeastOnce();
        expect(bundleImport.getResolution()).andReturn(Resolution.MANDATORY).atLeastOnce();
        expect(bundleImport.isApplicationImportScope()).andReturn(false);

        ImportExpansionHandler handler = new ImportExpansionHandler(repository, packagesExportedBySystemBundle, new MockEventLogger());

        replayMocks(mocks);

        BundleManifest bundleManifest = BundleManifestFactory.createBundleManifest(new StringReader(
            "Manifest-Version: 1.0, Bundle-SymbolicName: B\nImport-Package: org.springframework.core;version=\"[4.5,4.6)\""));
        handler.expandImports(asList(new ImportedLibrary[0]), singletonList(bundleImport), bundleManifest);

        verifyMocks(mocks);

        assertTrue(bundleManifest.getImportPackage().getImportedPackages().size() > 19);
    }

    @Test
    public void packageImportAndImportedLibraryVersionRangeIntersection() throws UnableToSatisfyDependenciesException, IOException {
        List<Object> mocks = new ArrayList<>();

        ImportedLibrary libraryImport = createAndStoreMock(ImportedLibrary.class, mocks);
        expect(libraryImport.getLibrarySymbolicName()).andReturn("org.springframework").atLeastOnce();
        expect(libraryImport.getVersion()).andReturn(new VersionRange("[5,6)")).atLeastOnce();

        ImportExpansionHandler handler = new ImportExpansionHandler(repository, packagesExportedBySystemBundle, new MockEventLogger());

        replayMocks(mocks);

        BundleManifest bundleManifest = BundleManifestFactory.createBundleManifest(new StringReader(
            "Manifest-Version: 1.0\nBundle-SymbolicName: B\nImport-Package: org.springframework.core;version=\"[4.5,4.6)\""));
        handler.expandImports(singletonList(libraryImport), asList(new ImportedBundle[0]), bundleManifest);

        verifyMocks(mocks);

        assertTrue(bundleManifest.getImportPackage().getImportedPackages().size() > 33);
    }

    @Test(expected = UnableToSatisfyDependenciesException.class)
    public void disjointImportedBundleVersionRangeIntersection() throws UnableToSatisfyDependenciesException {
        List<Object> mocks = new ArrayList<>();

        ImportedBundle bundleImport1 = createAndStoreMock(ImportedBundle.class, mocks);
        expect(bundleImport1.getBundleSymbolicName()).andReturn("org.springframework.core").atLeastOnce();
        expect(bundleImport1.getVersion()).andReturn(new VersionRange("[2.5,3.0)")).atLeastOnce();
        expect(bundleImport1.isApplicationImportScope()).andReturn(false).atLeastOnce();
        expect(bundleImport1.getResolution()).andReturn(Resolution.MANDATORY).atLeastOnce();

        ImportedBundle bundleImport2 = createAndStoreMock(ImportedBundle.class, mocks);
        expect(bundleImport2.getBundleSymbolicName()).andReturn("org.springframework.core").atLeastOnce();
        expect(bundleImport2.getVersion()).andReturn(new VersionRange("[2.0,2.5)")).atLeastOnce();
        expect(bundleImport2.isApplicationImportScope()).andReturn(false).atLeastOnce();
        expect(bundleImport2.getResolution()).andReturn(Resolution.MANDATORY).atLeastOnce();

        ImportExpansionHandler handler = new ImportExpansionHandler(repository, packagesExportedBySystemBundle, new MockEventLogger());

        replayMocks(mocks);

        BundleManifest bundleManifest = BundleManifestFactory.createBundleManifest();

        handler.expandImports(asList(new ImportedLibrary[0]), asList(bundleImport1, bundleImport2),
            bundleManifest);
    }

    @Test
    public void overlappingBundleAndImportedLibrarys() throws UnableToSatisfyDependenciesException {
        List<Object> mocks = new ArrayList<>();

        ImportedBundle bundleImport = createAndStoreMock(ImportedBundle.class, mocks);
        expect(bundleImport.getBundleSymbolicName()).andReturn("org.springframework.core").atLeastOnce();
        expect(bundleImport.getVersion()).andReturn(new VersionRange("[5.0.8,6)")).atLeastOnce();
        expect(bundleImport.isApplicationImportScope()).andReturn(false).atLeastOnce();
        expect(bundleImport.getResolution()).andReturn(Resolution.MANDATORY).atLeastOnce();

        ImportedLibrary libraryImport = createAndStoreMock(ImportedLibrary.class, mocks);
        expect(libraryImport.getLibrarySymbolicName()).andReturn("org.springframework").atLeastOnce();
        expect(libraryImport.getVersion()).andReturn(new VersionRange("[5.0.8,6)")).atLeastOnce();
        expect(libraryImport.getResolution()).andReturn(Resolution.MANDATORY).atLeastOnce();

        ImportExpansionHandler handler = new ImportExpansionHandler(repository, packagesExportedBySystemBundle, new MockEventLogger());

        replayMocks(mocks);

        BundleManifest bundleManifest = BundleManifestFactory.createBundleManifest();

        handler.expandImports(singletonList(libraryImport), singletonList(bundleImport),
            bundleManifest);
    }

    @Test
    public void importBundleWithNoExports() throws UnableToSatisfyDependenciesException {
        List<Object> mocks = new ArrayList<>();

        ImportedBundle bundleImport = createAndStoreMock(ImportedBundle.class, mocks);
        expect(bundleImport.getBundleSymbolicName()).andReturn("silht.bundles.noexports").atLeastOnce();
        expect(bundleImport.getVersion()).andReturn(new VersionRange("[1.0,1.0]")).atLeastOnce();
        expect(bundleImport.isApplicationImportScope()).andReturn(false).atLeastOnce();
        expect(bundleImport.getResolution()).andReturn(Resolution.MANDATORY).atLeastOnce();

        ImportExpansionHandler handler = new ImportExpansionHandler(repository, packagesExportedBySystemBundle, new MockEventLogger());

        replayMocks(mocks);

        BundleManifest bundleManifest = BundleManifestFactory.createBundleManifest();
        handler.expandImports(asList(new ImportedLibrary[0]), singletonList(bundleImport), bundleManifest);
    }

    @Test
    public void importBundleWithFragmentWithNoExports() throws UnableToSatisfyDependenciesException {
        List<Object> mocks = new ArrayList<>();

        ImportedBundle hostImportedBundle = createAndStoreMock(ImportedBundle.class, mocks);
        expect(hostImportedBundle.getBundleSymbolicName()).andReturn("silht.bundles.noexports").atLeastOnce();
        expect(hostImportedBundle.getVersion()).andReturn(new VersionRange("[1.0,1.0]")).atLeastOnce();
        expect(hostImportedBundle.isApplicationImportScope()).andReturn(false).atLeastOnce();
        expect(hostImportedBundle.getResolution()).andReturn(Resolution.MANDATORY).atLeastOnce();

        ImportedBundle fragmentImportedBundle = createAndStoreMock(ImportedBundle.class, mocks);
        expect(fragmentImportedBundle.getBundleSymbolicName()).andReturn("silht.bundles.fragmentwithnoexports").atLeastOnce();
        expect(fragmentImportedBundle.getVersion()).andReturn(new VersionRange("[1.0,1.0]")).atLeastOnce();
        expect(fragmentImportedBundle.isApplicationImportScope()).andReturn(false).atLeastOnce();
        expect(fragmentImportedBundle.getResolution()).andReturn(Resolution.MANDATORY).atLeastOnce();

        ImportExpansionHandler handler = new ImportExpansionHandler(repository, packagesExportedBySystemBundle, new MockEventLogger());

        replayMocks(mocks);

        BundleManifest bundleManifest = BundleManifestFactory.createBundleManifest();
        handler.expandImports(asList(new ImportedLibrary[0]),
            asList(hostImportedBundle, fragmentImportedBundle), bundleManifest);
        assertEquals(0, bundleManifest.getImportPackage().getImportedPackages().size());
    }

    @Test
    public void importBundleBetweenManifests() throws Exception {
        List<BundleManifest> manifests = new ArrayList<>();

        BundleManifest manifest = BundleManifestFactory.createBundleManifest(new StringReader("Manifest-Version: 1.0\nImport-Bundle: com.foo"));
        manifests.add(manifest);
        manifests.add(BundleManifestFactory.createBundleManifest(new StringReader(
            "Manifest-Version: 1.0\nExport-Package: com.foo;version=1.0\nBundle-SymbolicName: com.foo\n")));

        ImportExpansionHandler handler = new ImportExpansionHandler(this.repository, packagesExportedBySystemBundle, new MockEventLogger());
        handler.expandImports(manifests);
        assertImported(manifest, singletonList("com.foo"), singletonList("1.0.0"));
    }

    @Test
    public void importBundleExportingPackagesExportedBySystemBundle() throws Exception {
        List<BundleManifest> manifests = new ArrayList<>();
        BundleManifest manifest = BundleManifestFactory.createBundleManifest(new StringReader("Manifest-Version: 1.0\nImport-Bundle: overlapper"));
        manifests.add(manifest);

        MockEventLogger eventLogger = new MockEventLogger();
        ImportExpansionHandler handler = new ImportExpansionHandler(this.repository, packagesExportedBySystemBundle, eventLogger);
        handler.expandImports(manifests);
        assertImported(manifest, asList("javax.crypto.spec", "javax.imageio", "javax.imageio.event", "overlapper.pkg"),
            asList("0.0.0", "0.0.0", "0.0.0", "0.0.0"));

        Assert.assertTrue("No events were logged.", eventLogger.getCalled());
        Assert.assertTrue("The correct event was not logged.", eventLogger.containsLogged("UR0003W"));
        List<LoggedEvent> ur3Events = eventLogger.getEventsWithCodes("UR0003W");
        Assert.assertEquals(1, ur3Events.size());
        LoggedEvent ur3Event = ur3Events.get(0);
        Object[] inserts = ur3Event.getInserts();
        Assert.assertTrue("Wrong number of inserts.", inserts.length >= 3);
        Object overlap = inserts[2];
        Assert.assertTrue("Insert at index 2 is not a String", overlap instanceof String);
        String overlapString = (String)overlap;
        String[] splitOverlap = overlapString.substring(1, overlapString.length()-1).split(", ");
        Set<String> overlapSet = new HashSet<>();
        Collections.addAll(overlapSet, splitOverlap);
        Assert.assertEquals("Unexpected overlap with system bundle exports", packagesExportedBySystemBundle, overlapSet);
    }

    @Test
    public void importLibraryThatImportsMissingOptionalBundle() throws Exception {
        List<Object> mocks = new ArrayList<>();

        ImportedLibrary libraryImport = createAndStoreMock(ImportedLibrary.class, mocks);

        expect(libraryImport.getLibrarySymbolicName()).andReturn("missing.optional.bundle").atLeastOnce();
        expect(libraryImport.getVersion()).andReturn(new VersionRange("[1.0,1.0]")).atLeastOnce();
        expect(libraryImport.getResolution()).andReturn(Resolution.MANDATORY).anyTimes();

        ImportExpansionHandler handler = new ImportExpansionHandler(repository, packagesExportedBySystemBundle, new MockEventLogger());

        replayMocks(mocks);

        BundleManifest bundleManifest = BundleManifestFactory.createBundleManifest();

        handler.expandImports(singletonList(libraryImport), asList(new ImportedBundle[0]), bundleManifest);

        verifyMocks(mocks);

        assertEquals(0, bundleManifest.getImportPackage().getImportedPackages().size());
    }

    @Test
    public void importBundleThatExportsPackageAtMultipleVersions() throws UnableToSatisfyDependenciesException {
        List<Object> mocks = new ArrayList<>();

        ImportedBundle bundleImport = createAndStoreMock(ImportedBundle.class, mocks);
        expect(bundleImport.getBundleSymbolicName()).andReturn("multi.version.export").atLeastOnce();
        expect(bundleImport.getVersion()).andReturn(new VersionRange("[1.0,2.0)")).atLeastOnce();
        expect(bundleImport.isApplicationImportScope()).andReturn(false).atLeastOnce();
        expect(bundleImport.getResolution()).andReturn(Resolution.MANDATORY).atLeastOnce();

        ImportExpansionHandler handler = new ImportExpansionHandler(repository, packagesExportedBySystemBundle, new MockEventLogger());

        replayMocks(mocks);

        BundleManifest bundleManifest = BundleManifestFactory.createBundleManifest();

        handler.expandImports(asList(new ImportedLibrary[0]), singletonList(bundleImport), bundleManifest);

        verifyMocks(mocks);

        assertImported(bundleManifest, singletonList("a"), singletonList("1.0.0"));
    }

    private static <T> T createAndStoreMock(Class<T> classToMock, List<Object> mocks) {
        T mock = createMock(classToMock);
        mocks.add(mock);
        return mock;
    }

    private static void replayMocks(List<Object> mocks) {
        Object[] mocksArray = mocks.toArray(new Object[0]);
        replay(mocksArray);
    }

    private static void verifyMocks(List<Object> mocks) {
        Object[] mocksArray = mocks.toArray(new Object[0]);
        verify(mocksArray);
    }

    private static void assertImported(BundleManifest bundleManifest, List<String> packages, List<String> versions) {
        List<ImportedPackage> packageImports = bundleManifest.getImportPackage().getImportedPackages();
        List<String> expectedPackages = new ArrayList<>(packages);
        List<String> expectedVersions = new ArrayList<>(versions);
        for (ImportedPackage packageImport : packageImports) {

            String packageName = packageImport.getPackageName();
            int index = expectedPackages.indexOf(packageName);
            if (index > -1) {
                Version expected = new Version(expectedVersions.get(index));
                VersionRange actualRange = new VersionRange(packageImport.getAttributes().get("version"));

                if (actualRange.includes(expected)) {
                    expectedPackages.remove(packageName);
                    expectedVersions.remove(index);
                }
            }
        }

        if (expectedPackages.size() > 0) {
            fail("No import(s) were found for package(s) " + expectedPackages + " with version(s) " + expectedVersions + " in manifest: \n"
                + bundleManifest);
        }
    }

    private static final class StubRepository implements Repository {

        private final List<RepositoryAwareArtifactDescriptor> artifactDescriptors = new ArrayList<>();

        /**
         * {@inheritDoc}
         */
        public Query createQuery(String key, String value) {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public Query createQuery(String key, String value, Map<String, Set<String>> properties) {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public RepositoryAwareArtifactDescriptor get(String type, String name, VersionRange versionRange) {
            RepositoryAwareArtifactDescriptor bestMatch = null;

            for (RepositoryAwareArtifactDescriptor candidate : this.artifactDescriptors) {
                if (type.equals(candidate.getType()) && name.equals(candidate.getName()) && versionRange.includes(candidate.getVersion())) {
                    if (bestMatch == null || bestMatch.getVersion().compareTo(candidate.getVersion()) < 0) {
                        bestMatch = candidate;
                    }
                }
            }
            return bestMatch;
        }

        /**
         * {@inheritDoc}
         */
        public String getName() {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public void stop() {
            throw new UnsupportedOperationException();
        }

        private void addArtifactDescriptor(ArtifactDescriptor descriptor) {
            this.artifactDescriptors.add(new StubRepositoryAwareArtifactDescriptor(descriptor));
        }

        private static final class StubRepositoryAwareArtifactDescriptor implements RepositoryAwareArtifactDescriptor {

            private final ArtifactDescriptor delegate;

            private StubRepositoryAwareArtifactDescriptor(ArtifactDescriptor delegate) {
                this.delegate = delegate;
            }

            public Set<Attribute> getAttribute(String name) {
                return delegate.getAttribute(name);
            }

            public Set<Attribute> getAttributes() {
                return delegate.getAttributes();
            }

            public String getFilename() {
                return delegate.getFilename();
            }

            public String getName() {
                return delegate.getName();
            }

            public String getType() {
                return delegate.getType();
            }

            public java.net.URI getUri() {
                return delegate.getUri();
            }

            public Version getVersion() {
                return delegate.getVersion();
            }

            public String getRepositoryName() {
                return "Unit test repository";
            }
        }
    }
}
