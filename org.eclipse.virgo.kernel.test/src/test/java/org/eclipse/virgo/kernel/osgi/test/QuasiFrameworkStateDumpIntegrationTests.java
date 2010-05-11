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

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Version;

import org.eclipse.virgo.kernel.osgi.framework.OsgiFrameworkUtils;
import org.eclipse.virgo.kernel.osgi.framework.OsgiServiceHolder;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiBundle;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiFramework;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiFrameworkFactory;
import org.eclipse.virgo.kernel.test.AbstractKernelIntegrationTest;
import org.eclipse.virgo.util.io.PathReference;
import org.eclipse.virgo.util.io.ZipUtils;
import org.eclipse.virgo.util.osgi.manifest.BundleManifest;
import org.eclipse.virgo.util.osgi.manifest.BundleManifestFactory;

/**
 */
public class QuasiFrameworkStateDumpIntegrationTests extends AbstractKernelIntegrationTest {

    private static final String IMPORTER_BSN = "importer";

    private static final String IMPORTER_JAR_PATH = "src/test/resources/quasi/simpleimporter.jar";

    private static final String QUASI_TEST_PACKAGE = "quasi.test";

    private static final Version BUNDLE_VERSION = new Version("2.3");

    private QuasiFrameworkFactory quasiFrameworkFactory;

    private QuasiFramework quasiFramework;

    private File dumpDir;

    @Before
    public void setUp() {
        BundleContext bundleContext = this.framework.getBundleContext();
        OsgiServiceHolder<QuasiFrameworkFactory> holder = OsgiFrameworkUtils.getService(bundleContext, QuasiFrameworkFactory.class);
        this.quasiFrameworkFactory = holder.getService();
        Assert.assertNotNull(this.quasiFrameworkFactory);
        this.quasiFramework = this.quasiFrameworkFactory.create();
        Assert.assertNotNull(this.quasiFramework);
        this.dumpDir = new File("target/serviceability/dump/");
    }

    @Test
    public void testStateDump() throws Exception {

        Set<File> oldFileSet = getDumpFiles();

        installImporterBundle();
        this.quasiFramework.resolve();

        Set<File> newFileSet = getDumpFiles();
        newFileSet.removeAll(oldFileSet);
        Assert.assertEquals(1, newFileSet.size());

        File dump = newFileSet.iterator().next();

        QuasiFramework stateDump = this.quasiFrameworkFactory.create(getStateDump(dump));
        List<QuasiBundle> bundles = stateDump.getBundles();
        boolean found = false;
        for (QuasiBundle quasiBundle : bundles) {
            if (IMPORTER_BSN.equals(quasiBundle.getSymbolicName())) {
                found = true;
            }
        }
        Assert.assertTrue(found);

    }

	private File getStateDump(File dump) throws ZipException, IOException {
        File stateDump;
        Assert.assertFalse(dump == null);
        File[] stateDumpZipFiles = dump.listFiles(new FilenameFilter() {

            public boolean accept(File dir, String name) {
                return name.equals("osgi.zip");
            }
        });
        Assert.assertEquals(1, stateDumpZipFiles.length);
        File stateDumpZipFile = stateDumpZipFiles[0];

        stateDump = unzip(stateDumpZipFile);
        return stateDump;
    }
    
    private File unzip(File stateDumpZipFile) throws IOException {
        PathReference zipFile = new PathReference(stateDumpZipFile);
        String stateDumpFileName = getTmpDir();
        PathReference dest = new PathReference(stateDumpFileName);
        ZipUtils.unzipTo(zipFile, dest);
        return dest.newChild("state").toFile();
    }

    private static String getTmpDir() {
        return System.getProperty("java.io.tmpdir") + File.separator + QuasiFrameworkStateDumpIntegrationTests.class.getSimpleName()
            + System.currentTimeMillis();
    }

    private Set<File> getDumpFiles() {
        Set<File> oldFileSet = null;
        {
            File[] oldFiles = this.dumpDir.listFiles();
            oldFileSet = new HashSet<File>();
            for (File oldFile : oldFiles) {
                oldFileSet.add(oldFile);
            }
        }
        return oldFileSet;
    }

    private QuasiBundle installImporterBundle() throws BundleException, URISyntaxException {
        QuasiBundle importerQuasiBundle;
        BundleManifest importerBundleManifest = getBundleManifest(IMPORTER_BSN, BUNDLE_VERSION);
        importerBundleManifest.getImportPackage().addImportedPackage(QUASI_TEST_PACKAGE);

        importerQuasiBundle = this.quasiFramework.install(new URI(IMPORTER_JAR_PATH), importerBundleManifest);
        Assert.assertEquals(IMPORTER_BSN, importerQuasiBundle.getSymbolicName());
        Assert.assertEquals(BUNDLE_VERSION, importerQuasiBundle.getVersion());
        Assert.assertFalse(importerQuasiBundle.isResolved());
        Assert.assertNull(importerQuasiBundle.getBundle());
        return importerQuasiBundle;
    }

    private BundleManifest getBundleManifest(String symbolicName, Version bundleVersion) {
        BundleManifest bundleManifest;
        bundleManifest = BundleManifestFactory.createBundleManifest();
        bundleManifest.setBundleManifestVersion(2);
        bundleManifest.getBundleSymbolicName().setSymbolicName(symbolicName);
        bundleManifest.setBundleVersion(bundleVersion);
        return bundleManifest;
    }

}
