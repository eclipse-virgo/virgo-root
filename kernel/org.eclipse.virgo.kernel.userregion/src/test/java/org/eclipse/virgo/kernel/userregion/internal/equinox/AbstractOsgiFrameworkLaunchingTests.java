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

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.eclipse.equinox.internal.region.StandardRegionDigraph;
import org.eclipse.equinox.region.Region;
import org.eclipse.equinox.region.RegionDigraph;
import org.eclipse.osgi.launch.Equinox;
import org.eclipse.osgi.service.resolver.PlatformAdmin;
import org.eclipse.virgo.kernel.artifact.bundle.BundleBridge;
import org.eclipse.virgo.kernel.artifact.library.LibraryBridge;
import org.eclipse.virgo.kernel.equinox.extensions.EquinoxLauncherConfiguration;
import org.eclipse.virgo.kernel.equinox.extensions.ExtendedEquinoxLauncher;
import org.eclipse.virgo.kernel.equinox.extensions.hooks.PluggableClassLoadingHook;
import org.eclipse.virgo.kernel.osgi.framework.ImportExpander;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiFramework;
import org.eclipse.virgo.kernel.services.repository.internal.RepositoryFactoryBean;
import org.eclipse.virgo.kernel.services.work.WorkArea;
import org.eclipse.virgo.kernel.userregion.internal.DumpExtractor;
import org.eclipse.virgo.kernel.userregion.internal.dump.StandardDumpExtractor;
import org.eclipse.virgo.kernel.userregion.internal.importexpansion.ImportExpansionHandler;
import org.eclipse.virgo.kernel.userregion.internal.quasi.StandardQuasiFrameworkFactory;
import org.eclipse.virgo.kernel.userregion.internal.quasi.StandardResolutionFailureDetective;
import org.eclipse.virgo.medic.dump.DumpGenerator;
import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.virgo.medic.test.eventlog.MockEventLogger;
import org.eclipse.virgo.repository.ArtifactBridge;
import org.eclipse.virgo.repository.Repository;
import org.eclipse.virgo.repository.RepositoryFactory;
import org.eclipse.virgo.repository.internal.RepositoryBundleActivator;
import org.eclipse.virgo.util.io.FileSystemUtils;
import org.eclipse.virgo.util.io.PathReference;
import org.junit.After;
import org.junit.Before;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.packageadmin.ExportedPackage;
import org.osgi.service.packageadmin.PackageAdmin;

@SuppressWarnings("deprecation")
public abstract class AbstractOsgiFrameworkLaunchingTests {

    protected EquinoxOsgiFramework framework;

    PlatformAdmin platformAdmin;

    Repository repository;

    private RepositoryBundleActivator repositoryBundleActivator;

    private BundleContext bundleContext;

    private ServiceRegistration<Repository> repositoryRegistration;

    private ServiceRegistration<EventLogger> eventLoggerRegistration;

    private ServiceRegistration<DumpGenerator> dumpGeneratorRegistration;

    private ServiceRegistration<RegionDigraph> regionDigraphRegistration;

    private Equinox equinox;

    QuasiFramework quasiFramework;

    @Before
    public void setUp() throws Exception {

        final File workDir = new File("build/work");

        if (workDir.exists()) {
            assertTrue(FileSystemUtils.deleteRecursively(new File("build/work")));
        }

        // Uncomment this line to enable Equinox debugging
        // FrameworkProperties.setProperty("osgi.debug", "src/test/resources/debug.options");

        // Uncomment thils line to enable Equinox console
        // FrameworkProperties.setProperty("osgi.console", "2401");
        EquinoxLauncherConfiguration launcherConfiguration = new EquinoxLauncherConfiguration();
        launcherConfiguration.setClean(true);
        URI targetURI = new File("./build").toURI();
        launcherConfiguration.setConfigPath(targetURI);
        launcherConfiguration.setInstallPath(targetURI);

        equinox = ExtendedEquinoxLauncher.launch(launcherConfiguration);

        this.bundleContext = equinox.getBundleContext();

        DumpGenerator dumpGenerator = new DumpGenerator() {

            public void generateDump(String cause, Throwable... throwables) {
            }

            public void generateDump(String cause, Map<String, Object> context, Throwable... throwables) {
            }

        };

        ThreadLocal<Region> threadLocal = new ThreadLocal<>();
        RegionDigraph regionDigraph = new StandardRegionDigraph(this.bundleContext, threadLocal);

        Region userRegion = regionDigraph.createRegion("org.eclipse.virgo.region.user");
        userRegion.addBundle(this.bundleContext.getBundle());

        final EventLogger mockEventLogger = new MockEventLogger();

        eventLoggerRegistration = bundleContext.registerService(EventLogger.class, mockEventLogger, null);
        dumpGeneratorRegistration = bundleContext.registerService(DumpGenerator.class, dumpGenerator, null);
        regionDigraphRegistration = bundleContext.registerService(RegionDigraph.class, regionDigraph, null);

        this.repositoryBundleActivator = new RepositoryBundleActivator();
        this.repositoryBundleActivator.start(bundleContext);

        ServiceReference<RepositoryFactory> repositoryFactoryServiceReference = bundleContext.getServiceReference(RepositoryFactory.class);
        RepositoryFactory repositoryFactory = bundleContext.getService(repositoryFactoryServiceReference);

        Properties repositoryProperties = new Properties();

        try (InputStream properties = new FileInputStream(new File(getRepositoryConfigDirectory(), "repository.properties"))) {
            repositoryProperties.load(properties);
        }

        Set<ArtifactBridge> artifactBridges = new HashSet<>();
        artifactBridges.add(new BundleBridge(new StubHashGenerator()));
        artifactBridges.add(new LibraryBridge(new StubHashGenerator()));

        RepositoryFactoryBean bean = new RepositoryFactoryBean(repositoryProperties, mockEventLogger, repositoryFactory, new File("build/work"),
            artifactBridges, null);
        repository = bean.getObject();

        repositoryRegistration = bundleContext.registerService(Repository.class, repository, null);

        ServiceReference<PlatformAdmin> platformAdminServiceReference = bundleContext.getServiceReference(PlatformAdmin.class);
        this.platformAdmin = bundleContext.getService(platformAdminServiceReference);

        ServiceReference<PackageAdmin> packageAdminServiceReference = bundleContext.getServiceReference(PackageAdmin.class);
        PackageAdmin packageAdmin = bundleContext.getService(packageAdminServiceReference);

        ImportExpander importExpander = createImportExpander(packageAdmin);
        TransformedManifestProvidingBundleFileWrapper bundleFileWrapper = new TransformedManifestProvidingBundleFileWrapper(importExpander);
        this.framework = new EquinoxOsgiFramework(equinox.getBundleContext(), packageAdmin, bundleFileWrapper);

        PluggableClassLoadingHook.getInstance().setClassLoaderCreator(new KernelClassLoaderCreator());
        StandardResolutionFailureDetective detective = new StandardResolutionFailureDetective(platformAdmin);

        WorkArea workArea = new WorkArea() {

            @Override
            public Bundle getOwner() {
                return bundleContext.getBundle();
            }

            @Override
            public PathReference getWorkDirectory() {
                return new PathReference(new File("build/work"));
            }
        };
        DumpExtractor dumpExtractor = new StandardDumpExtractor(workArea);
        this.quasiFramework = new StandardQuasiFrameworkFactory(bundleContext, detective, repository, bundleFileWrapper, regionDigraph, dumpExtractor).create();
    }

    private ImportExpander createImportExpander(PackageAdmin packageAdmin) {
        Set<String> packagesExportedBySystemBundle = new HashSet<>(30);
        ExportedPackage[] exportedPackages = packageAdmin.getExportedPackages(bundleContext.getBundle(0));

        for (ExportedPackage exportedPackage : exportedPackages) {
            packagesExportedBySystemBundle.add(exportedPackage.getName());
        }

        return new ImportExpansionHandler(repository, bundleContext, packagesExportedBySystemBundle, new MockEventLogger());
    }

    @After
    public void stop() throws Exception {

        if (this.repositoryRegistration != null) {
            this.repositoryRegistration.unregister();
            this.repositoryRegistration = null;
        }

        if (this.dumpGeneratorRegistration != null) {
            this.dumpGeneratorRegistration.unregister();
            this.dumpGeneratorRegistration = null;
        }

        if (this.regionDigraphRegistration != null) {
            this.regionDigraphRegistration.unregister();
            this.regionDigraphRegistration = null;
        }

        if (this.eventLoggerRegistration != null) {
            this.eventLoggerRegistration.unregister();
            this.eventLoggerRegistration = null;
        }

        if (this.repositoryBundleActivator != null) {
            this.repositoryBundleActivator.stop(this.bundleContext);
            this.repositoryBundleActivator = null;
        }

        if (this.framework != null) {
            this.framework.stop();
            this.framework = null;
        }

        if (this.equinox != null) {
            this.equinox.stop();
            this.equinox.waitForStop(30000);
        }
    }

    protected abstract String getRepositoryConfigDirectory();
}
