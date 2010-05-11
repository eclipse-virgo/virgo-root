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
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.springframework.util.AntPathMatcher;

import org.eclipse.virgo.kernel.osgi.quasi.QuasiBundle;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiExportPackage;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiFramework;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiFrameworkFactory;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiImportPackage;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiResolutionFailure;
import org.eclipse.virgo.kernel.shell.state.QuasiLiveBundle;
import org.eclipse.virgo.kernel.shell.state.QuasiLiveService;
import org.eclipse.virgo.kernel.shell.state.QuasiPackage;
import org.eclipse.virgo.kernel.shell.state.StateService;

/**
 * 
 */
final public class StandardStateService implements StateService {

    private final QuasiFrameworkFactory quasiFrameworkFactory;

    private final BundleContext bundleContext;

    public StandardStateService(QuasiFrameworkFactory quasiFrameworkFactory, BundleContext bundleContext) {
        this.quasiFrameworkFactory = quasiFrameworkFactory;
        this.bundleContext = bundleContext;
    }

    /**
     * {@inheritDoc}
     */
    public List<QuasiBundle> getAllBundles(File source) {
        return this.getQuasiFramework(source).getBundles();
    }

    /**
     * {@inheritDoc}
     */
    public QuasiBundle getBundle(File source, long bundleId) {
        return this.getQuasiFramework(source).getBundle(bundleId);
    }

    /**
     * {@inheritDoc}
     */
    public List<QuasiLiveService> getAllServices(File source) {
        SortedMap<Long, QuasiLiveService> services = getServicesSortedMap(this.getQuasiFramework(source));

        List<QuasiLiveService> quasiLiveServices = new ArrayList<QuasiLiveService>();
        for (Entry<Long, QuasiLiveService> serviceEntry : services.entrySet()) {
            quasiLiveServices.add(serviceEntry.getValue());
        }
        return quasiLiveServices;
    }

    private SortedMap<Long, QuasiLiveService> getServicesSortedMap(QuasiFramework quasiFramework) {
        SortedMap<Long, QuasiLiveService> services = new TreeMap<Long, QuasiLiveService>();
        List<QuasiBundle> allBundles = quasiFramework.getBundles();
        for (QuasiBundle bundle : allBundles) {
            if (bundle instanceof QuasiLiveBundle) {
                QuasiLiveBundle liveBundle = (QuasiLiveBundle) bundle;
                for (QuasiLiveService service : liveBundle.getExportedServices()) {
                    services.put(service.getServiceId(), service);
                }
            }
        }
        return services;
    }

    /**
     * {@inheritDoc}
     */
    public QuasiLiveService getService(File source, long serviceId) {
        SortedMap<Long, QuasiLiveService> services = getServicesSortedMap(this.getQuasiFramework(source));
        if (services.containsKey(serviceId)) {
            return services.get(serviceId);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public List<QuasiResolutionFailure> getResolverReport(File source, long bundleId) {
        QuasiFramework framework = this.getQuasiFramework(source);
        return framework.diagnose(bundleId);
    }

    /**
     * {@inheritDoc}
     */
    public QuasiBundle installBundle(File source, String location) {
        if (source != null) {
            throw new IllegalStateException("Unable to install a bundle in to a non-live state.");
        }
        Bundle installBundle = null;
        try {
            installBundle = this.bundleContext.installBundle(location);
        } catch (BundleException e) {
            throw new IllegalStateException(String.format("Unable to install the bundle '%s'.", e.getMessage()), e);
        }
        if (installBundle == null) {
            return null;
        }
        QuasiFramework framework = this.getQuasiFramework(null);
        if (framework == null) {
            return null;
        }
        return framework.getBundle(installBundle.getBundleId());
    }

    /**
     * {@inheritDoc}
     */
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
            return this.quasiFrameworkFactory.create(source);
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
