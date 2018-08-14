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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.virgo.kernel.osgi.framework.OsgiFrameworkUtils;
import org.eclipse.virgo.kernel.osgi.framework.OsgiServiceHolder;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiBundle;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiFramework;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiFrameworkFactory;
import org.eclipse.virgo.kernel.test.AbstractKernelIntegrationTest;
import org.eclipse.virgo.util.io.FileSystemUtils;
import org.eclipse.virgo.util.osgi.manifest.BundleManifest;
import org.eclipse.virgo.util.osgi.manifest.BundleManifestFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Version;

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
        if(this.dumpDir.exists()){
        	FileSystemUtils.deleteRecursively(this.dumpDir);
        }
    	this.dumpDir.mkdirs();
    }

    @Test
    @Ignore("currently fails on CI server")
    // java.lang.AssertionError: expected:<1> but was:<24>
    public void testStateDump() throws Exception {

        Set<File> oldFileSet = getDumpFiles();

        installImporterBundle();
        this.quasiFramework.resolve();

        Set<File> newFileSet = getDumpFiles();
        newFileSet.removeAll(oldFileSet);
        Assert.assertEquals(1, newFileSet.size());

        File dump = newFileSet.iterator().next();

        QuasiFramework stateDump = this.quasiFrameworkFactory.create(dump);
        List<QuasiBundle> bundles = stateDump.getBundles();
        boolean found = false;
        for (QuasiBundle quasiBundle : bundles) {
            if (IMPORTER_BSN.equals(quasiBundle.getSymbolicName())) {
                found = true;
            }
        }
        Assert.assertTrue(found);

    }

    private Set<File> getDumpFiles() {
        Set<File> oldFileSet;
        {
            File[] oldFiles = this.dumpDir.listFiles();
            oldFileSet = new HashSet<>();
            Collections.addAll(oldFileSet, oldFiles);
        }
        return oldFileSet;
    }

    private void installImporterBundle() throws BundleException, URISyntaxException {
        QuasiBundle importerQuasiBundle;
        BundleManifest importerBundleManifest = getBundleManifest();
        importerBundleManifest.getImportPackage().addImportedPackage(QUASI_TEST_PACKAGE);

        importerQuasiBundle = this.quasiFramework.install(new URI(IMPORTER_JAR_PATH), importerBundleManifest);
        Assert.assertEquals(IMPORTER_BSN, importerQuasiBundle.getSymbolicName());
        Assert.assertEquals(BUNDLE_VERSION, importerQuasiBundle.getVersion());
        Assert.assertFalse(importerQuasiBundle.isResolved());
        Assert.assertNull(importerQuasiBundle.getBundle());
    }

    private BundleManifest getBundleManifest() {
        BundleManifest bundleManifest;
        bundleManifest = BundleManifestFactory.createBundleManifest();
        bundleManifest.setBundleManifestVersion(2);
        bundleManifest.getBundleSymbolicName().setSymbolicName(QuasiFrameworkStateDumpIntegrationTests.IMPORTER_BSN);
        bundleManifest.setBundleVersion(QuasiFrameworkStateDumpIntegrationTests.BUNDLE_VERSION);
        return bundleManifest;
    }

}
