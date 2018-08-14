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
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifactLifecycleListener;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifactLifecycleListenerSupport;

import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.util.io.PathReference;

/**
 * Test refreshing single bundles.
 * <p />
 * 
 */
public class BundleRefreshTests extends AbstractDeployerIntegrationTest {

    @Test
    public void testBundleRefresh() throws DeploymentException, InterruptedException {
        UninstallTrackingInstallArtifactLifecycleListener listener = new UninstallTrackingInstallArtifactLifecycleListener();
        ServiceRegistration<InstallArtifactLifecycleListener> listenerRegistration = this.context.registerService(InstallArtifactLifecycleListener.class, listener, null);
        
        new PathReference("./build/classes/test/bundle-refresh").createDirectory();

        PathReference exporterSource = new PathReference("./src/test/resources/bundle-refresh/RefreshExporter.jar");
        PathReference exporter = new PathReference("./build/classes/test/bundle-refresh/RefreshExporter.jar");
        exporter.delete();
        exporterSource.copy(exporter);

        this.deployer.deploy(exporter.toURI());

        PathReference importer = new PathReference("./src/test/resources/bundle-refresh/RefreshImporter.jar");
        this.deployer.deploy(importer.toURI());

        // Check that the test bundle's application contexts are created.
        String TEST_IMPORTER_BUNDLE_SYMBOLIC_NAME = "RefreshImporter";
        assertNotNull(ApplicationContextUtils.getApplicationContext(this.context, TEST_IMPORTER_BUNDLE_SYMBOLIC_NAME));

        checkV1Classes();

        PathReference exporterv2Source = new PathReference("./src/test/resources/bundle-refresh/RefreshExporterv2.jar");
        exporter.delete();
        exporterv2Source.copy(exporter);

        this.deployer.refresh(exporter.toURI(), "RefreshExporter");

        Thread.sleep(1000); // wait for appCtx to be properly closed before waiting for it to be created.

        ApplicationContextUtils.awaitApplicationContext(this.context, TEST_IMPORTER_BUNDLE_SYMBOLIC_NAME, 10);

        checkV2Classes();
        
        assertEquals(0, listener.getUninstalledArtifacts().size());

        PathReference exporterv3Source = new PathReference("./src/test/resources/bundle-refresh/RefreshExporterv3.jar");
        exporter.delete();
        exporterv3Source.copy(exporter);

        this.deployer.refresh(exporter.toURI(), "RefreshExporter");
        Thread.sleep(1000); // wait for appCtx to be properly closed before waiting for it to be created.

        ApplicationContextUtils.awaitApplicationContext(this.context, TEST_IMPORTER_BUNDLE_SYMBOLIC_NAME, 10);

        checkV3Classes();
        
        assertEquals(0, listener.getUninstalledArtifacts().size());        
        
        listenerRegistration.unregister();
    }

    private void checkV1Classes() {
        LoadableClasses loadableClasses = (LoadableClasses) getApplicationBundleContext().getService(
            getApplicationBundleContext().getServiceReference("org.eclipse.virgo.kernel.deployer.test.LoadableClasses"));
        Set<String> loadableClassNames = loadableClasses.getLoadableClasses();
        Assert.assertTrue(loadableClassNames.contains("refresh.exporter.b1.B11"));
        Assert.assertFalse(loadableClassNames.contains("refresh.exporter.b1.B12"));
        Assert.assertFalse(loadableClassNames.contains("refresh.exporter.b2.B21"));
    }

    private void checkV2Classes() {
        LoadableClasses loadableClasses = (LoadableClasses) getApplicationBundleContext().getService(
            getApplicationBundleContext().getServiceReference("org.eclipse.virgo.kernel.deployer.test.LoadableClasses"));
        Set<String> loadableClassNames = loadableClasses.getLoadableClasses();
        Assert.assertTrue(loadableClassNames.contains("refresh.exporter.b1.B11"));
        Assert.assertTrue(loadableClassNames.contains("refresh.exporter.b1.B12"));
        Assert.assertFalse(loadableClassNames.contains("refresh.exporter.b2.B21"));
    }

    private void checkV3Classes() {
        LoadableClasses loadableClasses = (LoadableClasses) getApplicationBundleContext().getService(
            getApplicationBundleContext().getServiceReference("org.eclipse.virgo.kernel.deployer.test.LoadableClasses"));
        Set<String> loadableClassNames = loadableClasses.getLoadableClasses();
        Assert.assertTrue(loadableClassNames.contains("refresh.exporter.b1.B11"));
        Assert.assertTrue(loadableClassNames.contains("refresh.exporter.b1.B12"));
        Assert.assertTrue(loadableClassNames.contains("refresh.exporter.b2.B21"));
    }

    private BundleContext getApplicationBundleContext() {
        Bundle[] bundles = this.context.getBundles();
        for (Bundle bundle : bundles) {
            String symbolicName = bundle.getSymbolicName();
            if (symbolicName.contains("RefreshImporter")) {
                System.out.println(bundle.getSymbolicName());
                return bundle.getBundleContext();
            }
        }
        fail("Cannot find bundle context");
        return null;
    }
    
    private final class UninstallTrackingInstallArtifactLifecycleListener extends InstallArtifactLifecycleListenerSupport {
        
        private final List<InstallArtifact> uninstalledArtifacts = new ArrayList<>();
        
        private final Object monitor = new Object();

        @Override
        public void onUninstalled(InstallArtifact installArtifact) {
            synchronized (this.monitor) {
                this.uninstalledArtifacts.add(installArtifact);
            }
        }
        
        private List<InstallArtifact> getUninstalledArtifacts() {
            synchronized (this.monitor) {
                return new ArrayList<>(this.uninstalledArtifacts);
            }
        }
    }
}
