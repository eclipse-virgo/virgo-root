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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osgi.framework.Version;

import org.eclipse.virgo.util.common.StringUtils;
import org.eclipse.virgo.util.osgi.manifest.VersionRange;
import org.eclipse.virgo.util.osgi.manifest.BundleManifest;
import org.eclipse.virgo.util.osgi.manifest.BundleManifestFactory;
import org.eclipse.virgo.util.osgi.manifest.ExportedPackage;
import org.eclipse.virgo.util.osgi.manifest.ImportedPackage;

/**
 * Provides a number of helper methods for processing {@link BundleManifest BundleManifests}.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * Thread-safe
 * 
 */
class BundleManifestProcessor {

    private static final String BUNDLE_SYMBOLIC_NAME_ATTRIBUTE_NAME = "bundle-symbolic-name";

    private static final String BUNDLE_VERSION_ATTRIBUTE_NAME = "bundle-version";

    private static final String VERSION_ATTRIBUTE_NAME = "version";

    /**
     * Creates a {@link ImportedPackage} for each of the supplied {@link ExportedPackage ExportedPackages} and returns a
     * {@link Result} containing them. The result also contains any warnings that were generated during the processing
     * of the manifests.
     * 
     * @param packageExports the <code>ExportedPackages</code> to process
     * @return a Result containing the package imports and any warnings
     */
    static List<ImportedPackage> createImportedPackageForEachExportedPackage(List<ExportedPackage> packageExports, String bundleSymbolicName,
        String bundleVersion) {

        BundleManifest manifest = BundleManifestFactory.createBundleManifest();

        doCreateImportedPackageForEachExportedPackage(packageExports, bundleSymbolicName, bundleVersion, null, manifest);

        return manifest.getImportPackage().getImportedPackages();
    }

    /**
     * Creates a {@link ImportedPackage} for each of the supplied {@link ExportedPackage ExportedPackages} and returns a
     * {@link Result} containing them. The result also contains any warnings that were generated during the processing
     * of the manifests.
     * 
     * @param packageExports the <code>ExportedPackages</code> to process
     * @return a Result containing the package imports and any warnings
     */
    static List<ImportedPackage> createImportedPackageForEachExportedPackageOfFragment(List<ExportedPackage> packageExports,
        String bundleSymbolicName, VersionRange bundleVersionRange) {

        BundleManifest manifest = BundleManifestFactory.createBundleManifest();

        doCreateImportedPackageForEachExportedPackage(packageExports, bundleSymbolicName, null, bundleVersionRange, manifest);

        return manifest.getImportPackage().getImportedPackages();
    }

    /**
     * Creates a {@link ImportedPackage} for each package export found in the given {@link BundleManifest
     * BundleManifests} and returns a {@link Result} containing the package imports. The result also contains any
     * warnings that were generated during the processing of the manifests.
     * 
     * @param bundleManifests the bundle manifests to process
     * @return a Result containing the package imports and any warnings
     */
    static Result<ImportedPackage[]> createImportedPackageForEachExportedPackage(BundleManifest[] bundleManifests) {

        List<ImportedPackage> allImportedPackages = new ArrayList<ImportedPackage>();
        List<Warning> warnings = new ArrayList<Warning>();

        for (BundleManifest bundleManifest : bundleManifests) {
            List<ExportedPackage> packageExports = bundleManifest.getExportPackage().getExportedPackages();
            BundleManifest resultManifest = BundleManifestFactory.createBundleManifest();
            doCreateImportedPackageForEachExportedPackage(packageExports, null, null, null, resultManifest);

            List<ImportedPackage> packageImports = resultManifest.getImportPackage().getImportedPackages();

            for (ImportedPackage packageImport : packageImports) {
                String name = packageImport.getPackageName();
                if (!containsImport(allImportedPackages, name)) {
                    allImportedPackages.add(packageImport);
                } else {
                    warnings.add(new Warning(Code.DUPLICATE_PACKAGE_EXPORTS, name));
                }
            }
        }

        return new Result<ImportedPackage[]>(allImportedPackages.toArray(new ImportedPackage[allImportedPackages.size()]),
            warnings.toArray(new Warning[warnings.size()]));
    }

    private static void doCreateImportedPackageForEachExportedPackage(List<ExportedPackage> packageExports, String bundleSymbolicName,
        String version, VersionRange versionRange, BundleManifest bundleManifest) {
        for (ExportedPackage packageExport : packageExports) {
            String name = packageExport.getPackageName();
            List<ImportedPackage> packageImports = bundleManifest.getImportPackage().getImportedPackages();
            ImportedPackage packageImport = findImport(packageImports, name);
            if (packageImport == null) {
                packageImport = bundleManifest.getImportPackage().addImportedPackage(name);
            }

            List<String> mandatory = packageExport.getMandatory();
            Map<String, String> attributes = new HashMap<String, String>();
            if (mandatory != null) {
                for (String attrName : mandatory) {
                    if (packageExport.getAttributes().containsKey(attrName)) {
                        attributes.put(attrName, packageExport.getAttributes().get(attrName));
                    }
                }
            }

            if (bundleSymbolicName != null) {
                attributes.put(BUNDLE_SYMBOLIC_NAME_ATTRIBUTE_NAME, bundleSymbolicName);
            }

            if (StringUtils.hasText(version)) {
                VersionRange vr;
                try {
                    vr = VersionRange.createExactRange(new Version(version));
                } catch (IllegalArgumentException e) {
                    vr = new VersionRange(version);
                }
                attributes.put(BUNDLE_VERSION_ATTRIBUTE_NAME, vr.toParseString());
            }

            /*
             * If we are importing a fragment, use the supplied bundle version range and if this range is not exact, add
             * an exact package version.
             */
            if (versionRange != null) {
                // If the bundle version range is not all possible versions, add it as an attribute.
                if (!(versionRange.isFloorInclusive() && !versionRange.isCeilingInclusive() && versionRange.getFloor().equals(Version.emptyVersion))) {
                    attributes.put(BUNDLE_VERSION_ATTRIBUTE_NAME, versionRange.toParseString());
                }
                if (!versionRange.isExact()) {
                    VersionRange packageVersionRange = VersionRange.createExactRange(packageExport.getVersion());
                    attributes.put(VERSION_ATTRIBUTE_NAME, packageVersionRange.toParseString());
                }
            }

            packageImport.getAttributes().putAll(attributes);
        }
    }

    private static boolean containsImport(List<ImportedPackage> imports, String packageName) {
        for (ImportedPackage packageImport : imports) {
            if (packageImport.getPackageName().equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    private static ImportedPackage findImport(List<ImportedPackage> imports, String packageName) {
        for (ImportedPackage packageImport : imports) {
            if (packageImport.getPackageName().equals(packageName)) {
                return packageImport;
            }
        }
        return null;
    }

    /**
     * Encapsulates the output from bundle manifest processing processing along with any {@link Warning Warnings} that
     * were generated during that processing.
     * <p />
     * 
     * <strong>Concurrent Semantics</strong><br />
     * Thread-safe.
     * 
     * @param <T> output type
     */
    public static class Result<T> {

        private final T output;

        private final Warning[] warnings;

        private Result(T output, Warning[] warnings) {
            this.output = output;
            this.warnings = warnings;
        }

        /**
         * Returns the output of this result
         * 
         * @return the output
         */
        public T getOutput() {
            return this.output;
        }

        /**
         * Returns any warnings generated while producing the result. If no warnings were generated an empty array will
         * be returned.
         * 
         * @return any generated warnings
         */
        public Warning[] getWarnings() {
            return this.warnings.clone();
        }

        /**
         * Returns whether this result was successful. A result is deemed to be successful if it does not contain any
         * warnings
         * 
         * @return <code>true</code> if the result is successful
         */
        public boolean success() {
            return this.warnings.length == 0;
        }
    }

    /**
     * A Warning describes a problem that was encountered during BundleManifestHelper processing
     * <p />
     * 
     * <strong>Concurrent Semantics</strong><br />
     * Thread-safe.
     * 
     */
    public static class Warning {

        private final Code code;

        private final String reason;

        private Warning(Code code, String reason) {
            this.code = code;
            this.reason = reason;
        }

        /**
         * Returns the {@link Code} that identifies the nature of this warning
         * 
         * @return the warning's code
         */
        public Code getCode() {
            return this.code;
        }

        /**
         * Returns the reason for the generation of this warning
         * 
         * @return the warning's reason
         */
        public String getReason() {
            return this.reason;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(Object other) {
            if (other == null) {
                return false;
            }

            if (other == this) {
                return true;
            }

            if (!(other instanceof Warning)) {
                return false;
            }

            Warning that = (Warning) other;
            return this.getCode().equals(that.getCode()) && this.getReason().equals(that.getReason());
        }

        @Override
        public int hashCode() {
            return this.code.hashCode();
        }
    }

    /**
     * Defines a unique code for each of the warnings that may be generated as a result of bundle manifest processing.
     * <p />
     * <strong>Concurrent Semantics</strong><br />
     * Thread-safe.
     * 
     */
    public enum Code {
        /**
         * More than one of the input {@link BundleManifest BundleManifests} exported the same package.
         */
        DUPLICATE_PACKAGE_EXPORTS;
    }
}
