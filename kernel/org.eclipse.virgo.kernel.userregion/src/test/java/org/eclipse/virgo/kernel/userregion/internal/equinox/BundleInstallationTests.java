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

package org.eclipse.virgo.kernel.userregion.internal.equinox;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;

import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiBundle;
import org.eclipse.virgo.util.io.IOUtils;
import org.eclipse.virgo.util.osgi.manifest.VersionRange;
import org.eclipse.virgo.util.osgi.manifest.BundleManifestFactory;

/**
 */
public class BundleInstallationTests extends AbstractOsgiFrameworkLaunchingTests {

    @Override
    protected String getRepositoryConfigDirectory() {
        return new File("src/test/resources/config/BundleInstallationTests").getAbsolutePath();
    }

    @Test(expected = BundleException.class)
    public void testFailedInstall() throws Exception {
        installBundle("fail.parent");
    }

    @Test
    public void testInstallFragHost() throws Exception {
        Bundle bundle = installBundle("frag.host");
        assertNotNull(bundle);
        assertEquals(Bundle.INSTALLED, bundle.getState());

        Bundle[] bundles = this.framework.getBundleContext().getBundles();
        boolean foundChild = false;
        for (Bundle b : bundles) {
            if ("frag.child".equals(b.getSymbolicName())) {
                foundChild = true;
                break;
            }
        }
        assertTrue(foundChild);
    }

    @Test
    public void testInstallFragChild() throws Exception {
        Bundle bundle = installBundle("frag.child");
        assertNotNull(bundle);
        assertEquals(Bundle.RESOLVED, bundle.getState());
    }

    @Test
    public void testInstallBundleWithNoDependencies() throws Exception {
        Bundle bundle = installBundle("org.eclipse.virgo.server.mock.bundle");
        assertNotNull(bundle);
        bundle.start();
        assertEquals(Bundle.ACTIVE, bundle.getState());
    }

    @Test
    public void testInstallBundleWithMultipleDependencies() throws Exception {
        Bundle bundle = installBundle("install.three");
        assertNotNull(bundle);
        bundle.start();
        assertEquals(Bundle.ACTIVE, bundle.getState());
    }

    @Test
    public void testInstallBundleWithTransitiveDependencies() throws Exception {
        Bundle bundle = installBundle("install.four");
        assertNotNull(bundle);
        assertEquals(Bundle.RESOLVED, bundle.getState());
    }

    @Test
    public void testInstallBundleWithRequireBundle() throws Exception {
        Bundle bundle = installBundle("install.five");
        assertNotNull(bundle);
        assertEquals(Bundle.RESOLVED, bundle.getState());
    }

    @Test
    public void testInstallBundleWithCircle() throws Exception {
        Bundle bundle = installBundle("install.six");
        assertNotNull(bundle);
        assertEquals(Bundle.RESOLVED, bundle.getState());
    }

    @Test
    public void platform170() throws Exception {
        Bundle b = installBundle(new File("./src/test/resources/platform170/simpleosgiservice-1.0.0.jar"));
        b.start();

        b = installBundle(new File("./src/test/resources/platform170/simpleosgiservice-2.0.0.jar"));
        b.start();

        b = installBundle(new File("./src/test/resources/platform170/simpleosgiapp-1.0.0.jar"));
        b.start();

        assertEquals(Bundle.ACTIVE, b.getState());
    }

    @Test
    public void testInstallWithOptionalImportNotSatisfied() throws Exception {
        Bundle b = installBundle("install.optional.ns");
        b.start();
        assertEquals(Bundle.ACTIVE, b.getState());
    }

    @Test
    public void testInstallWithOptionalImportWithNotSatisfiedDependencyInOptional() throws Exception {
        Bundle b = installBundle("install.optional.dep.bundle");
        b.start();
        assertEquals(Bundle.ACTIVE, b.getState());
    }

    @Test
    public void testMultipleOptionsChoosesOnlyOneOption() throws Exception {
        Bundle b = installBundle("install.multi.bundle");
        b.start();
        assertEquals(Bundle.ACTIVE, b.getState());
        Bundle[] bundles = b.getBundleContext().getBundles();
        for (Bundle bundle : bundles) {
            if ("install.multi.a".equals(bundle.getSymbolicName())) {
                fail("Bundle install.multi.a should not have been installed into the framework");
            }
        }
    }

    @Test
    public void testSatisfyAgainstBundleNotInRepo() throws Exception {
        installBundle(new File("./src/test/resources/bit/standalone"));
        Bundle bundle = installBundle("install.six");
        bundle.start();
        assertEquals(Bundle.ACTIVE, bundle.getState());
    }

    @Test(expected = BundleException.class)
    public void testFailedDueToMissingImport() throws Exception {
        installBundle("install.error.import");
    }

    @Test
    public void testUnresolvableFragmentIsIgnored() throws Exception {
        Bundle b = installBundle("fragments.unresolvable.host");
        b.start();
        assertEquals(Bundle.ACTIVE, b.getState());
    }

    @Test(expected = BundleException.class)
    public void testFailDueToUses() throws Exception {
        installBundle("install.uses.hibernate325");
        installBundle("install.uses.hibernate326");
        Bundle b = installBundle("install.uses.spring");
        b.start();

        assertEquals(Bundle.ACTIVE, b.getState());

        installBundle("install.uses.bundle");
    }

    private Bundle installBundle(String symbolicName) throws BundleException, IOException {
        URI bundleLocation = this.repository.get("bundle", symbolicName, VersionRange.NATURAL_NUMBER_RANGE).getUri();
        File bundleFile = new File(bundleLocation);
        return installBundle(bundleFile);
    }

    private Bundle installBundle(File bundleFile) throws BundleException, IOException {
        Reader manifest = null;
        try {
            if (bundleFile.isDirectory()) {
                manifest = ManifestUtils.manifestReaderFromExplodedDirectory(bundleFile);
            } else {
                manifest = ManifestUtils.manifestReaderFromJar(bundleFile);
            }

            QuasiBundle quasiBundle = this.quasiFramework.install(bundleFile.toURI(), BundleManifestFactory.createBundleManifest(manifest));
            this.quasiFramework.resolve();
            this.quasiFramework.commit();
            return quasiBundle.getBundle();
        } finally {
            IOUtils.closeQuietly(manifest);
        }
    }
}
