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

package org.eclipse.virgo.kernel.shell.state.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.equinox.region.RegionDigraph;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiBundle;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiExportPackage;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiFramework;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiFrameworkFactory;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiImportPackage;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiResolutionFailure;
import org.eclipse.virgo.kernel.shell.state.QuasiLiveService;
import org.eclipse.virgo.kernel.shell.state.QuasiPackage;
import org.eclipse.virgo.kernel.shell.state.StateService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.springframework.util.AntPathMatcher;

/**
 * {@link StandardStateService} is the default implementation of {@link StateService}.
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 * Thread safe.
 */
final public class StandardStateService implements StateService {

    private final QuasiFrameworkFactory quasiFrameworkFactory;

    private final BundleContext bundleContext;

    private final RegionDigraph regionDigraph;

    public StandardStateService(QuasiFrameworkFactory quasiFrameworkFactory, BundleContext bundleContext, RegionDigraph regionDigraph) {
        this.quasiFrameworkFactory = quasiFrameworkFactory;
        this.bundleContext = bundleContext;
        this.regionDigraph = regionDigraph;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<QuasiBundle> getAllBundles(File source) {
        return this.getQuasiFramework(source).getBundles();
    }

    /**
     * {@inheritDoc}
     */
    public String getBundleRegionName(long bundleId) {
        return this.regionDigraph.getRegion(bundleId).getName();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public QuasiBundle getBundle(File source, long bundleId) {
        return this.getQuasiFramework(source).getBundle(bundleId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<QuasiLiveService> getAllServices(File source) {
        List<QuasiLiveService> quasiLiveServices = new ArrayList<QuasiLiveService>();
        if (source == null) {
            SortedMap<Long, QuasiLiveService> services = getServicesSortedMap(this.getQuasiFramework(source));
            for (Entry<Long, QuasiLiveService> serviceEntry : services.entrySet()) {
                quasiLiveServices.add(serviceEntry.getValue());
            }
        }
        return quasiLiveServices;
    }

    /**
     * Pre-condition: this must only be used on the live framework. 
     */
    private SortedMap<Long, QuasiLiveService> getServicesSortedMap(QuasiFramework quasiFramework) {
        SortedMap<Long, QuasiLiveService> services = new TreeMap<Long, QuasiLiveService>();
        ServiceReference<?>[] allServiceReferences = null;
        try {
            /*
             * Use bundleContext (which belongs to the user region) to find services so this is subject to the region
             * digraph filtering.
             */
            allServiceReferences = this.bundleContext.getAllServiceReferences(null, null);
        } catch (InvalidSyntaxException e) {
            // Will not happen
        }
        for (ServiceReference<?> serviceReference : allServiceReferences) {
            QuasiLiveService service = new StandardQuasiLiveService(quasiFramework, serviceReference);
            services.put(service.getServiceId(), service);
        }
        return services;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QuasiLiveService getService(File source, long serviceId) {
        if (source == null) {
            SortedMap<Long, QuasiLiveService> services = getServicesSortedMap(this.getQuasiFramework(source));
            return services.get(serviceId);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<QuasiResolutionFailure> getResolverReport(File source, long bundleId) {
        QuasiFramework framework = this.getQuasiFramework(source);
        framework.resolve();
        return framework.diagnose(bundleId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QuasiPackage getPackages(File source, String packageName) {
        QuasiFramework framework = this.getQuasiFramework(source);
        if (packageName != null) {
            List<QuasiImportPackage> importers = new ArrayList<QuasiImportPackage>();
            List<QuasiExportPackage> exporters = new ArrayList<QuasiExportPackage>();
            List<QuasiBundle> bundles = framework.getBundles();
            for (QuasiBundle qBundle : bundles) {
                QuasiImportPackage importPackage = processImporters(qBundle, packageName);
                if (importPackage != null) {
                    importers.add(importPackage);
                }
                QuasiExportPackage exportPackage = processExporters(qBundle, packageName);
                if (exportPackage != null) {
                    exporters.add(exportPackage);
                }
            }
            return new StandardQuasiPackage(exporters, importers, packageName);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<QuasiBundle> search(File source, String term) {
        QuasiFramework framework = this.getQuasiFramework(source);
        List<QuasiBundle> matchingBundles = new ArrayList<QuasiBundle>();
        if (term != null) {
            AntPathMatcher matcher = new AntPathMatcher();
            matcher.setPathSeparator(".");
            List<QuasiBundle> bundles = framework.getBundles();
            for (QuasiBundle bundle : bundles) {
                if (matcher.match(term, bundle.getSymbolicName())) {
                    matchingBundles.add(bundle);
                }

            }
        }
        return matchingBundles;
    }

    private QuasiFramework getQuasiFramework(File source) {
        if (source == null) {
            return new StandardQuasiLiveFramework(this.quasiFrameworkFactory.create(), this.bundleContext);
        } else {
            try {
                return this.quasiFrameworkFactory.create(source);
            } catch (Exception e) {
                throw new RuntimeException("Error creating quasi-framework", e);
            }
        }
    }

    private QuasiImportPackage processImporters(QuasiBundle qBundle, String packageName) {
        for (QuasiImportPackage qImportPackage : qBundle.getImportPackages()) {
            if (qImportPackage.getPackageName().equals(packageName)) {
                return qImportPackage;
            }
        }
        return null;
    }

    private QuasiExportPackage processExporters(QuasiBundle qBundle, String packageName) {
        for (QuasiExportPackage qExportPackage : qBundle.getExportPackages()) {
            if (qExportPackage.getPackageName().equals(packageName)) {
                return qExportPackage;
            }
        }
        return null;
    }

}
