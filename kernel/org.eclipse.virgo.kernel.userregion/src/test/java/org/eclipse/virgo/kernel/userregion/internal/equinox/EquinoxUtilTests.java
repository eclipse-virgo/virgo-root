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

import java.io.File;
import java.io.IOException;
import java.io.Reader;

import org.eclipse.virgo.kernel.osgi.quasi.QuasiBundle;
import org.eclipse.virgo.util.io.IOUtils;
import org.eclipse.virgo.util.osgi.manifest.BundleManifestFactory;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

/**
 */
public class EquinoxUtilTests extends AbstractOsgiFrameworkLaunchingTests {

    @Override
    protected String getRepositoryConfigDirectory() {
        return new File("src/test/resources/config/BundleInstallationTests").getAbsolutePath();
    }

    @Test
    public void testInstallBundleWithRequireBundle() throws Exception {
        installBundle(new File(System.getProperty("user.home")
            + "/.gradle/caches/modules-2/files-2.1/org.eclipse.virgo.mirrored/org.eclipse.osgi.services/3.5.0.v20150519-2006/f8b39d71416901549bf27cbff5d709d25a347632/org.eclipse.osgi.services-3.5.0.v20150519-2006.jar"));
        installBundle(new File(System.getProperty("user.home")
            +"/.gradle/caches/modules-2/files-2.1/org.eclipse.virgo.mirrored/org.eclipse.equinox.util/1.0.500.v20130404-1337/ffedd440831050fce73a848a14104028759ff9fb/org.eclipse.equinox.util-1.0.500.v20130404-1337.jar"));
        Bundle dsBundle = installBundle(new File(System.getProperty("user.home")
            + "/.gradle/caches/modules-2/files-2.1/org.eclipse.virgo.mirrored/org.eclipse.equinox.ds/1.4.200.v20131126-2331/8306727a3fe6b9d6c2aec60f229c04edcf8268f0/org.eclipse.equinox.ds-1.4.200.v20131126-2331.jar"));
        dsBundle.start();

        assertNotNull(dsBundle);
        assertEquals(Bundle.ACTIVE, dsBundle.getState());
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
