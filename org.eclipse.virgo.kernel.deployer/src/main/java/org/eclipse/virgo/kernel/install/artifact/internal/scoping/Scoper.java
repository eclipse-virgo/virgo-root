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

package org.eclipse.virgo.kernel.install.artifact.internal.scoping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osgi.framework.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.virgo.util.osgi.manifest.VersionRange;
import org.eclipse.virgo.util.osgi.manifest.BundleManifest;
import org.eclipse.virgo.util.osgi.manifest.BundleManifestFactory;
import org.eclipse.virgo.util.osgi.manifest.BundleSymbolicName;
import org.eclipse.virgo.util.osgi.manifest.DynamicImportPackage;
import org.eclipse.virgo.util.osgi.manifest.DynamicallyImportedPackage;
import org.eclipse.virgo.util.osgi.manifest.ExportedPackage;
import org.eclipse.virgo.util.osgi.manifest.FragmentHost;
import org.eclipse.virgo.util.osgi.manifest.ImportedBundle;
import org.eclipse.virgo.util.osgi.manifest.ImportedPackage;
import org.eclipse.virgo.util.osgi.manifest.RequiredBundle;

/**
 * {@link Scoper} is a utility class for scoping a set of bundles.
 * <p/>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is not thread safe.
 * 
 */
public class Scoper {

    private static final String BUNDLE_SYMBOLIC_NAME_ATTRIBUTE_NAME = "bundle-symbolic-name";

    public static final String SCOPE_SEPARATOR = "-";

    private static final int BUNDLE_MANIFEST_VERSION_FOR_OSGI_R4 = 2;

    private static final String SCOPING_ATTRIBUTE_NAME = "module_scope";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final List<BundleManifest> bundleManifests;

    private final String scopeName;

    private final String scopePrefix;

    // Map of unscoped package name to package version for all the packages exported by the bundles.
    private final Map<String, Version> exportedPackages = new HashMap<String, Version>();

    // Map of unscoped bundle symbolic name to bundle version for all the bundles.
    private final Map<String, Version> bundles = new HashMap<String, Version>();

    /**
     * @param bundleManifests the manifests of the bundles to be scoped
     * @param scopeName the name of the scope to be created
     */
    public Scoper(List<BundleManifest> bundleManifests, String scopeName) {
        this.bundleManifests = bundleManifests;
        this.scopeName = scopeName;
        this.scopePrefix = scopeName + "-";
    }

    /**
     * Scope the bundles by transforming their metadata. This involves adding a mandatory matching attribute to exports
     * and any corresponding imports and adding a prefix to each bundle's bundle symbolic name and any corresponding
     * require bundle statements and matching attributes. The value of the mandatory matching attribute and the prefix
     * are uniquely determined by the application name and version.
     * 
     * Scoping also checks for ambiguities which could lead to unexpected wirings and throws an exception if these
     * checks fail. Specifically, each package exported by the bundles must be exported only once and no two bundles may
     * have the same bundle symbolic name.
     * @throws UnsupportedBundleManifestVersionException 
     * @throws DuplicateExportException 
     * @throws DuplicateBundleSymbolicNameException 
     */
    public void scope() throws UnsupportedBundleManifestVersionException, DuplicateExportException, DuplicateBundleSymbolicNameException {
        scopeReferents();
        scopeReferences();
    }

    /**
     * Determine which packages are exported by the bundles and scope these exports and determine the bundles' symbolic
     * names and scope them.
     */
    private void scopeReferents() throws UnsupportedBundleManifestVersionException, DuplicateExportException, DuplicateBundleSymbolicNameException {
        for (BundleManifest bundleManifest : this.bundleManifests) {
            scopeBundleReferents(bundleManifest, true);
        }
    }

    /**
     * Scope the referents of the given bundle.
     * 
     * @param bundleManifest
     * @throws UnsupportedBundleManifestVersionException
     * @throws DuplicateExportException
     * @throws DuplicateBundleSymbolicNameException
     */
    private void scopeBundleReferents(BundleManifest bundleManifest, boolean allowDuplicates) throws UnsupportedBundleManifestVersionException,
        DuplicateExportException, DuplicateBundleSymbolicNameException {
        logger.debug("Bundle manifest before scoping:\n{}", bundleManifest);

        // OSGi R4 features are essential for scoping.
        if (bundleManifest.getBundleManifestVersion() < BUNDLE_MANIFEST_VERSION_FOR_OSGI_R4) {

            throw new UnsupportedBundleManifestVersionException();
        }

        for (ExportedPackage exportedPackage : bundleManifest.getExportPackage().getExportedPackages()) {
            scopeExportedPackage(exportedPackage, allowDuplicates);
        }
        setModuleScope(bundleManifest);
        
        try {            
            String symbolicName = bundleManifest.getBundleSymbolicName().getSymbolicName();
            if (symbolicName.startsWith(this.scopeName)) {
                symbolicName = symbolicName.substring(this.scopeName.length() + 1);
            }
            this.bundles.put(symbolicName, bundleManifest.getBundleVersion());
        } catch (StringIndexOutOfBoundsException e) {
            throw e;
        }
    }

    private void setModuleScope(BundleManifest bundleManifest) {
        bundleManifest.setModuleScope(this.scopeName);
    }

    /**
     * Scope the given package export.
     * 
     * @param exportedPackage the package export to be scoped
     * @param allowDuplicates true if and only if duplicated exports should be allowed
     */
    private void scopeExportedPackage(ExportedPackage exportedPackage, boolean allowDuplicates) throws DuplicateExportException,
        UnsupportedBundleManifestVersionException {
        exportedPackage.getAttributes().put(SCOPING_ATTRIBUTE_NAME, this.scopeName);
        exportedPackage.getMandatory().add(SCOPING_ATTRIBUTE_NAME);

        Version packageVersion = exportedPackage.getVersion();

        if (this.exportedPackages.containsKey(exportedPackage.getPackageName())) {
            if (!allowDuplicates) {
                diagnoseDuplicateExport(exportedPackage.getPackageName());
            }
        } else {
            this.exportedPackages.put(exportedPackage.getPackageName(), packageVersion);
        }
    }

    /**
     * @param packageName
     */
    private void diagnoseDuplicateExport(String packageName) throws DuplicateExportException, UnsupportedBundleManifestVersionException {
        StringBuffer exporters = new StringBuffer();
        boolean first = true;
        for (BundleManifest bundleManifest : this.bundleManifests) {
            // OSGi R4 features are essential for scoping.
            if (bundleManifest.getBundleManifestVersion() < BUNDLE_MANIFEST_VERSION_FOR_OSGI_R4) {
                throw new UnsupportedBundleManifestVersionException();
            }
            for (ExportedPackage exportedPackage : bundleManifest.getExportPackage().getExportedPackages()) {
                if (packageName.equals(exportedPackage.getPackageName())) {
                    if (!first) {
                        exporters.append(", ");
                    }
                    first = false;
                    exporters.append(getUnscopedSymbolicName(bundleManifest));
                    exporters.append(" ");
                    exporters.append(bundleManifest.getBundleVersion());
                }
            }
        }

        throw new DuplicateExportException(packageName, exporters.toString());
    }

    /**
     * Get the bundle symbolic name of the given bundle manifest prior to scoping.
     * 
     * @param bundleManifest the bundle manifest
     * @return the unscoped bundle symbolic name
     */
    public static String getUnscopedSymbolicName(BundleManifest bundleManifest) {
        String symbolicName = null;
        BundleSymbolicName bundleSymbolicName = bundleManifest.getBundleSymbolicName();
        if (bundleSymbolicName != null) {
            symbolicName = bundleSymbolicName.getSymbolicName();
            String moduleScope = bundleManifest.getModuleScope();
            if (moduleScope != null) {
                String scopeName = moduleScope + SCOPE_SEPARATOR;
                if (symbolicName.startsWith(scopeName)) {
                    symbolicName = symbolicName.substring(scopeName.length());
                }
            }
        }
        return symbolicName;
    }

    public String getUnscopedSymbolicName(String bundleSymbolicName) {
        String symbolicName = null;
        if (bundleSymbolicName != null) {
            symbolicName = bundleSymbolicName;
            if (symbolicName.startsWith(this.scopePrefix)) {
                symbolicName = symbolicName.substring(this.scopePrefix.length());
            }
        }
        return symbolicName;
    }

    public String getScopedSymbolicName(String bundleSymbolicName) {
        String symbolicName = getUnscopedSymbolicName(bundleSymbolicName);
        if (this.bundles.containsKey(symbolicName)) {
            return this.scopePrefix + symbolicName;
        }
        return bundleSymbolicName;
    }

    /**
     * Scope the corresponding imports and dynamic imports and scope the corresponding require-bundle and imports and
     * dynamic imports.
     */
    private void scopeReferences() {
        for (BundleManifest bundleManifest : this.bundleManifests) {
            scopeBundleReferences(bundleManifest);
        }
    }

    /**
     * Scope the references of the given bundle.
     * 
     * @param bundleManifest
     */
    private void scopeBundleReferences(BundleManifest bundleManifest) {
        for (ImportedPackage importedPackage : bundleManifest.getImportPackage().getImportedPackages()) {
            scopeImportedPackage(importedPackage);
        }
        scopeDynamicImports(bundleManifest);        
        scopeImportBundle(bundleManifest);
        scopeRequireBundle(bundleManifest);
        scopeFragmentHost(bundleManifest);

        logger.debug("Bundle manifest after scoping:\n{}", bundleManifest);
    }

	private void scopeDynamicImports(BundleManifest bundleManifest) {
		DynamicImportPackage unscopedList = BundleManifestFactory.createBundleManifest().getDynamicImportPackage();        
        List<DynamicallyImportedPackage> dynamicallyImportedPackages = bundleManifest.getDynamicImportPackage().getDynamicallyImportedPackages();
        for (DynamicallyImportedPackage dynamicallyImportedPackage : dynamicallyImportedPackages) {
            scopeDynamicallyImportedPackage(dynamicallyImportedPackage);            
            addUnscopedDynamicallyImportedPackage(unscopedList, dynamicallyImportedPackage);
        }		
		dynamicallyImportedPackages.addAll(unscopedList.getDynamicallyImportedPackages());
	}
	
	private void addUnscopedDynamicallyImportedPackage(DynamicImportPackage unscopedList, DynamicallyImportedPackage dynamicallyImportedPackage) {
		unscopedList.addDynamicallyImportedPackage(dynamicallyImportedPackage.getPackageName());
        List<DynamicallyImportedPackage> unscopedDynamicallyImportedPackages = unscopedList.getDynamicallyImportedPackages();
        DynamicallyImportedPackage unscopedDIP = unscopedDynamicallyImportedPackages.get(unscopedDynamicallyImportedPackages.size()-1);
        Map<String, String> attributes = unscopedDIP.getAttributes();
        attributes.putAll(dynamicallyImportedPackage.getAttributes());
        attributes.remove(SCOPING_ATTRIBUTE_NAME);          
	}

    /**
     * Scope the given package import.
     * 
     * @param importedPackage the package import to be scoped
     */
    private void scopeImportedPackage(ImportedPackage importedPackage) {
        Version exportedVersion = this.exportedPackages.get(importedPackage.getPackageName());
        if (exportedVersion != null) {
            VersionRange importVersionRange = importedPackage.getVersion();
            if (importVersionRange.includes(exportedVersion)) {
                importedPackage.getAttributes().put(SCOPING_ATTRIBUTE_NAME, this.scopeName);
                if (importedPackage.getAttributes().containsKey(BUNDLE_SYMBOLIC_NAME_ATTRIBUTE_NAME)) {
                    String symbolicName = importedPackage.getAttributes().get(BUNDLE_SYMBOLIC_NAME_ATTRIBUTE_NAME);
                    if (this.bundles.containsKey(symbolicName)) {
                        importedPackage.getAttributes().put(BUNDLE_SYMBOLIC_NAME_ATTRIBUTE_NAME, this.scopePrefix + symbolicName);
                    }
                }
            } else {
                logger.warn(
                    "Import of package '{}' was not scoped to scope '{}' as its version range did not include the version of the scoped export of the package",
                    importedPackage.getPackageName(), this.scopeName);
            }
        }
    }

    private void scopeDynamicallyImportedPackage(DynamicallyImportedPackage dynamicallyImportedPackage) {
        dynamicallyImportedPackage.getAttributes().put(SCOPING_ATTRIBUTE_NAME, this.scopeName);
        if (dynamicallyImportedPackage.getAttributes().containsKey(BUNDLE_SYMBOLIC_NAME_ATTRIBUTE_NAME)) {
            String symbolicName = dynamicallyImportedPackage.getAttributes().get(BUNDLE_SYMBOLIC_NAME_ATTRIBUTE_NAME);
            if (this.bundles.containsKey(symbolicName)) {
                dynamicallyImportedPackage.getAttributes().put(BUNDLE_SYMBOLIC_NAME_ATTRIBUTE_NAME, this.scopePrefix + symbolicName);
            }
        }
    }

    /**
     * Scope the given require bundle.
     * 
     * @param bundleManifest the bundle manifest to be scoped
     */
    private void scopeRequireBundle(BundleManifest bundleManifest) {
        List<RequiredBundle> requiredBundles = bundleManifest.getRequireBundle().getRequiredBundles();
        for (RequiredBundle requiredBundle : requiredBundles) {
            String requiredBundleSymbolicName = requiredBundle.getBundleSymbolicName();
            if (this.bundles.containsKey(requiredBundleSymbolicName)) {
                Version version = this.bundles.get(requiredBundleSymbolicName);
                VersionRange requiredVersionRange = requiredBundle.getBundleVersion();
                if (requiredVersionRange.includes(version)) {
                    requiredBundle.setBundleSymbolicName(this.scopePrefix + requiredBundleSymbolicName);
                }
            }
        }
    }
    
    /**
     * Scope the manifest's Import-Bundle header
     * 
     * @param bundleManifest the bundle manifest to be scoped
     */
    private void scopeImportBundle(BundleManifest bundleManifest) {
        List<ImportedBundle> importedBundles = bundleManifest.getImportBundle().getImportedBundles();
        for (ImportedBundle importedBundle : importedBundles) {
            String importedBundleSymbolicName = importedBundle.getBundleSymbolicName();
            if (this.bundles.containsKey(importedBundleSymbolicName)) {
                Version version = this.bundles.get(importedBundleSymbolicName);
                VersionRange versionRange = importedBundle.getVersion();
                if (versionRange.includes(version)) {
                    importedBundle.setBundleSymbolicName(this.scopePrefix + importedBundleSymbolicName);
                }
            }
        }
    }

    /**
     * Scope any reference to one of the scoped bundles by a fragment host header.
     * 
     * @param bundleManifest the bundle manifest to be scoped for fragment host
     */
    private void scopeFragmentHost(BundleManifest bundleManifest) {
        FragmentHost fragmentHost = bundleManifest.getFragmentHost();
        String bundleSymbolicName = fragmentHost.getBundleSymbolicName();
        if (bundleSymbolicName != null) {
            if (this.bundles.containsKey(bundleSymbolicName)) {
                fragmentHost.setBundleSymbolicName(this.scopePrefix + bundleSymbolicName);
            }
        }
    }

    public static class DuplicateExportException extends Exception {

        private static final long serialVersionUID = -6672058951941449763L;

        private final String packageName;

        private final String exporters;

        private DuplicateExportException(String packageName, String exporters) {
            this.packageName = packageName;
            this.exporters = exporters;
        }

        public String getPackageName() {
            return this.packageName;
        }

        public String getExporters() {
            return this.exporters;
        }
    }

    public static class UnsupportedBundleManifestVersionException extends Exception {

        private static final long serialVersionUID = -282020071571817876L;

        UnsupportedBundleManifestVersionException() {
            super("Cannot scope a bundle which does not specify a bundle manifest version of at least " + BUNDLE_MANIFEST_VERSION_FOR_OSGI_R4);
        }

        /**
         * Returns the lowest bundle manifest version for which scoping is supported
         * 
         * @return the lowest bundle manifest version for which scoping is supported
         */
        public int getLowestSupportedVersion() {
            return BUNDLE_MANIFEST_VERSION_FOR_OSGI_R4;
        }
    }

    public static class DuplicateBundleSymbolicNameException extends Exception {

        private static final long serialVersionUID = -4228236795055040322L;

        private final String bundleSymbolicName;

        public DuplicateBundleSymbolicNameException(String bundleSymbolicName) {
            this.bundleSymbolicName = bundleSymbolicName;
        }

        public String getBundleSymbolicName() {
            return this.bundleSymbolicName;
        }
    }
}
