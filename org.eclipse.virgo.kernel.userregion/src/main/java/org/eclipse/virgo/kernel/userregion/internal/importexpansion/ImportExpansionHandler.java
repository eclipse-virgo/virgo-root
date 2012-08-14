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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.eclipse.osgi.framework.internal.core.BundleRepository;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.virgo.kernel.osgi.framework.ImportExpander;
import org.eclipse.virgo.kernel.osgi.framework.ImportMergeException;
import org.eclipse.virgo.kernel.osgi.framework.UnableToSatisfyBundleDependenciesException;
import org.eclipse.virgo.kernel.osgi.framework.UnableToSatisfyDependenciesException;

import org.eclipse.virgo.kernel.artifact.bundle.BundleBridge;
import org.eclipse.virgo.kernel.artifact.library.LibraryDefinition;
import org.eclipse.virgo.nano.serviceability.Assert;
import org.eclipse.virgo.kernel.userregion.internal.UserRegionLogEvents;
import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.virgo.repository.ArtifactDescriptor;
import org.eclipse.virgo.repository.Attribute;
import org.eclipse.virgo.repository.Repository;
import org.eclipse.virgo.util.math.OrderedPair;
import org.eclipse.virgo.util.osgi.manifest.VersionRange;
import org.eclipse.virgo.util.osgi.manifest.BundleManifest;
import org.eclipse.virgo.util.osgi.manifest.BundleManifestFactory;
import org.eclipse.virgo.util.osgi.manifest.BundleSymbolicName;
import org.eclipse.virgo.util.osgi.manifest.ExportedPackage;
import org.eclipse.virgo.util.osgi.manifest.ImportedBundle;
import org.eclipse.virgo.util.osgi.manifest.ImportedLibrary;
import org.eclipse.virgo.util.osgi.manifest.ImportedPackage;
import org.eclipse.virgo.util.osgi.manifest.Resolution;

/**
 * A helper class for handling the expansion of <code>Import-Library</code> and <code>Import-Bundle</code> headers in a
 * bundle manifest into <code>Import-Package</code> header entries.
 * <p/>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * This class is <strong>thread-safe</strong>.
 * 
 */
public final class ImportExpansionHandler implements ImportExpander {

    private static final String IMPORT_SCOPE_APPLICATION = "application";

    private static final String IMPORT_SCOPE_DIRECTIVE = "import-scope";

    private static final String MISSING_BUNDLE_SYMBOLIC_NAME = "<bundle symbolic name not present>";

    private static final String INSTRUMENTED_SUFFIX = ".instrumented";

    private static final String SYNTHETIC_CONTEXT_SUFFIX = "-synthetic.context";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Repository repository;

    private final TrackedPackageImportsFactory trackedPackageImportsFactory = new StandardTrackedPackageImportsFactory();

    private final BundleContext bundleContext;

    private final Set<String> packagesExportedBySystemBundle;

    private final EventLogger eventLogger;

    public ImportExpansionHandler(Repository repository, Set<String> packagesExportedBySystemBundle, EventLogger eventLogger) {
        this(repository, null, packagesExportedBySystemBundle, eventLogger);
    }

    public ImportExpansionHandler(Repository repository, BundleContext bundleContext, Set<String> packagesExportedBySystemBundle,
        EventLogger eventLogger) {
        this.repository = repository;
        this.bundleContext = bundleContext;
        this.packagesExportedBySystemBundle = packagesExportedBySystemBundle;
        this.eventLogger = eventLogger;
    }

    /**
     * {@inheritDoc}
     * 
     * @throws ImportMergeException
     */
    public ImportPromotionVector expandImports(List<BundleManifest> bundleManifests) throws UnableToSatisfyDependenciesException,
        ImportMergeException {
        StandardImportPromotionVector importPromotionVector = new StandardImportPromotionVector(this.trackedPackageImportsFactory);
        TrackedPackageImports packageImportsToBePromoted = this.trackedPackageImportsFactory.createCollector();

        for (BundleManifest bundleManifest : bundleManifests) {
            TrackedPackageImports bundlePackageImportsToBePromoted = this.trackedPackageImportsFactory.createCollector();

            detectPromotedPackageImports(bundleManifest, bundlePackageImportsToBePromoted);

            expandImportsIfNecessary(bundleManifest, bundlePackageImportsToBePromoted, bundleManifests);
            if (!bundlePackageImportsToBePromoted.isEmpty()) {
                packageImportsToBePromoted.merge(bundlePackageImportsToBePromoted);
                BundleSymbolicName bundleSymbolicNameHeader = bundleManifest.getBundleSymbolicName();
                Assert.notNull(bundleSymbolicNameHeader, "Bundle-SymbolicName must be present for import promotion tracking");
                importPromotionVector.put(bundleSymbolicNameHeader.getSymbolicName(), bundlePackageImportsToBePromoted);
            }
        }

        mergePromotedImports(packageImportsToBePromoted, bundleManifests);
        return importPromotionVector;
    }

    /**
     * Detect package imports with application import scope and add these to the package imports to be promoted.
     */
    private void detectPromotedPackageImports(BundleManifest bundleManifest, TrackedPackageImports bundlePackageImportsToBePromoted) {
        List<ImportedPackage> importedPackages = bundleManifest.getImportPackage().getImportedPackages();
        List<ImportedPackage> importedPackagesToPromote = new ArrayList<ImportedPackage>();
        for (ImportedPackage importedPackage : importedPackages) {
            if (IMPORT_SCOPE_APPLICATION.equals(importedPackage.getDirectives().get(IMPORT_SCOPE_DIRECTIVE))) {
                importedPackagesToPromote.add(importedPackage);
            }
        }
        TrackedPackageImports trackedPackageImportsToPromote = this.trackedPackageImportsFactory.create(importedPackagesToPromote,
            "Import-Package in '" + bundleManifest.getBundleSymbolicName().getSymbolicName() + "' version '" + bundleManifest.getBundleVersion()
                + "'");
        bundlePackageImportsToBePromoted.merge(trackedPackageImportsToPromote);
    }

    private void mergePromotedImports(TrackedPackageImports importsToBemerged, List<BundleManifest> bundleManifests) throws ImportMergeException {
        for (BundleManifest bundleManifest : bundleManifests) {
            mergePromotedImports(importsToBemerged, bundleManifest);
        }
    }

    private void mergePromotedImports(TrackedPackageImports importsToBemerged, BundleManifest bundleManifest) throws ImportMergeException {
        BundleSymbolicName bundleSymbolicNameHeader = bundleManifest.getBundleSymbolicName();
        if (bundleSymbolicNameHeader.getSymbolicName() == null || !bundleSymbolicNameHeader.getSymbolicName().endsWith(SYNTHETIC_CONTEXT_SUFFIX)) {
            mergeImports(importsToBemerged, bundleManifest);
        }
    }

    /**
     * @param importsToBemerged
     * @param bundleManifest
     */
    private void mergeImports(TrackedPackageImports importsToBemerged, BundleManifest bundleManifest) throws ImportMergeException {
        TrackedPackageImports bundleTrackedPackageImports = this.trackedPackageImportsFactory.create(bundleManifest);
        bundleTrackedPackageImports.merge(importsToBemerged);
        setMergedImports(bundleManifest, bundleTrackedPackageImports);
    }

    private boolean expandImportsIfNecessary(BundleManifest manifest, TrackedPackageImports packageImportsToBePromoted,
        List<BundleManifest> bundleManifests) throws UnableToSatisfyDependenciesException {
        boolean expanded = false;

        List<ImportedBundle> directlyImportedBundles = manifest.getImportBundle().getImportedBundles();
        List<ImportedLibrary> importedLibraries = manifest.getImportLibrary().getImportedLibraries();

        if (directlyImportedBundles.size() > 0 || importedLibraries.size() > 0) {
            this.logger.info("Import-Library and/or Import-Bundle header found. Original manifest: \n{}", manifest);
            expandImports(importedLibraries, directlyImportedBundles, manifest, packageImportsToBePromoted, bundleManifests);
            this.logger.info("Updated manifest: \n{}", manifest);
            expanded = true;
        }
        return expanded;
    }

    UnableToSatisfyBundleDependenciesException createExceptionForMissingLibrary(String name, VersionRange versionRange, BundleManifest bundleManifest) {
        String description = String.format("A library with the name '%s' and a version within the range '%s' could not be found", name, versionRange);
        BundleSymbolicName bundleSymbolicName = bundleManifest.getBundleSymbolicName();
        return new UnableToSatisfyBundleDependenciesException(bundleSymbolicName != null ? bundleSymbolicName.getSymbolicName()
            : MISSING_BUNDLE_SYMBOLIC_NAME, bundleManifest.getBundleVersion(), description);
    }

    @SuppressWarnings("unchecked")
    void expandImports(List<ImportedLibrary> libraryImports, List<ImportedBundle> directlyImportedBundles, BundleManifest bundleManifest)
        throws UnableToSatisfyDependenciesException {
        expandImports(libraryImports, directlyImportedBundles, bundleManifest, this.trackedPackageImportsFactory.createCollector(),
            Collections.EMPTY_LIST);
    }

    void expandImports(List<ImportedLibrary> libraryImports, List<ImportedBundle> directlyImportedBundles, BundleManifest bundleManifest,
        List<BundleManifest> bundleManifests) throws UnableToSatisfyDependenciesException {
        expandImports(libraryImports, directlyImportedBundles, bundleManifest, this.trackedPackageImportsFactory.createCollector(), bundleManifests);
    }

    private void expandImports(List<ImportedLibrary> libraryImports, List<ImportedBundle> directlyImportedBundles, BundleManifest bundleManifest,
        TrackedPackageImports packageImportsToBePromoted, List<BundleManifest> bundleManifests) throws UnableToSatisfyDependenciesException {
        Assert.notNull(libraryImports, "Library imports must be non-null");
        Assert.notNull(directlyImportedBundles, "Direct bundle imports must be non-null");

        mergeImports(
            getAdditionalPackageImports(libraryImports, directlyImportedBundles, bundleManifest, packageImportsToBePromoted, bundleManifests),
            bundleManifest);

        bundleManifest.getImportBundle().getImportedBundles().clear();
        bundleManifest.getImportLibrary().getImportedLibraries().clear();
    }

    /**
     * The bundle with the given bundle manifest imports the given list of libraries and the given list of bundles.
     * Dereference these imports and return a collection of the corresponding {@link TrackedPackageImports}. Any
     * promoted imports are added to the given <code>TrackedPackageImports</code> of imports to be promoted.
     * 
     * @param importedLibraries
     * @param directlyImportedBundles
     * @param bundleManifest
     * @param bundleRepository
     * @param packageImportsToBePromoted
     * @return
     * @throws UnableToSatisfyDependenciesException
     * @throws UnableToSatisfyBundleDependenciesException
     */
    private TrackedPackageImports getAdditionalPackageImports(List<ImportedLibrary> importedLibraries, List<ImportedBundle> directlyImportedBundles,
        BundleManifest bundleManifest, TrackedPackageImports packageImportsToBePromoted, List<BundleManifest> additionalManifests)
        throws UnableToSatisfyDependenciesException, UnableToSatisfyBundleDependenciesException {
        TrackedPackageImports additionalPackageImports = this.trackedPackageImportsFactory.createCollector();
        TrackedPackageImports libraryPackageImports = getLibraryPackageImports(importedLibraries, packageImportsToBePromoted, bundleManifest,
            additionalManifests);
        additionalPackageImports.merge(libraryPackageImports);
        for (ImportedBundle directlyImportedBundle : directlyImportedBundles) {
            additionalPackageImports.merge(getBundlePackageImports(directlyImportedBundle, packageImportsToBePromoted, bundleManifest,
                additionalManifests));
        }
        return additionalPackageImports;
    }

    /**
     * Get a {@link TrackedPackageImports} instance representing the package imports that correspond to the given bundle
     * import. The imported bundle is looked up in the given {@link BundleRepository} and, if it is not found,
     * {@link UnableToSatisfiedBundleDependenciesException} is thrown. If the bundle import is to be promoted, then the
     * result is also merged into the given <code>TrackedPackageImports</code> instance representing the package imports
     * to be promoted.
     * 
     * @param importedBundle
     * @param bundleRepository
     * @param packageImportsToBePromoted
     * @param importingBundle
     * @return
     * @throws UnableToSatisfyBundleDependenciesException
     */
    private TrackedPackageImports getBundlePackageImports(ImportedBundle importedBundle, TrackedPackageImports packageImportsToBePromoted,
        BundleManifest importingBundle, List<BundleManifest> additionalManifests) throws UnableToSatisfyBundleDependenciesException {
        String bundleSymbolicName = importedBundle.getBundleSymbolicName();
        VersionRange importVersionRange = importedBundle.getVersion();
        boolean mandatory = importedBundle.getResolution().equals(Resolution.MANDATORY);
        if (bundleSymbolicName.equals(importingBundle.getBundleSymbolicName().getSymbolicName())
            && importVersionRange.includes(importingBundle.getBundleVersion())) {
            throw new UnableToSatisfyBundleDependenciesException(importingBundle.getBundleSymbolicName().getSymbolicName(),
                importingBundle.getBundleVersion(), "Import-Bundle must not import the importing bundle");
        }

        OrderedPair<BundleManifest, Boolean> bundleManifestHolder = findBundle(bundleSymbolicName, importVersionRange, additionalManifests);
        if (bundleManifestHolder != null && bundleManifestHolder.getFirst() != null) {
            return createTrackedPackageImportsFromImportedBundle(bundleManifestHolder, importedBundle.isApplicationImportScope(),
                packageImportsToBePromoted);
        } else if (mandatory) {
            throw new UnableToSatisfyBundleDependenciesException(
                importingBundle.getBundleSymbolicName() != null ? importingBundle.getBundleSymbolicName().getSymbolicName()
                    : MISSING_BUNDLE_SYMBOLIC_NAME, importingBundle.getBundleVersion(), "Import-Bundle with symbolic name '" + bundleSymbolicName
                    + "' in version range '" + importVersionRange + "' could not be satisfied");
        } else {
            return this.trackedPackageImportsFactory.createEmpty();
        }
    }

    /**
     * Return the {@link TrackedPackageImports} corresponding to importing the given {@link BundleManifest} and, if
     * appropriate, merge them into the given imports to be promoted.
     * 
     * @param bundleManifest
     * @param promoteExports
     * @param packageImportsToBePromoted
     * @return
     */
    private TrackedPackageImports createTrackedPackageImportsFromImportedBundle(OrderedPair<BundleManifest, Boolean> bundleManifest,
        boolean promoteExports, TrackedPackageImports packageImportsToBePromoted) {
        TrackedPackageImports bundlePackageImports = createPackageImportsFromPackageExports(bundleManifest, promoteExports);
        if (promoteExports) {
            packageImportsToBePromoted.merge(bundlePackageImports);
        }
        return bundlePackageImports;
    }

    /**
     * Create a {@link TrackedPackageImports} instance corresponding to the given package exports from the given bundle
     * manifest.
     * <p />
     * Importing fragment bundles is not encouraged but sometimes it's the only way out when reusing poorly packaged
     * bundles. Packages exported by a fragment are imported with the host's bundle symbolic name, an exact range
     * matching only the exported package version, and a bundle version range corresponding to that of the fragment host
     * header. Since the generated imports do not include a bundle version matching attribute with a value of the form
     * [v,v], there is a risk that the package import will wire to the wrong version of the host. This risk seems very
     * small and not worth the extra complexity of matching up hosts and fragments (some of which would need searching
     * for in the repository) during import expansion.
     * 
     */
    private TrackedPackageImports createPackageImportsFromPackageExports(OrderedPair<BundleManifest, Boolean> bundleManifestHolder,
        boolean promoteExports) {
        BundleManifest bundleManifest = bundleManifestHolder.getFirst();
        List<ExportedPackage> exportedPackages = bundleManifest.getExportPackage().getExportedPackages();
        if (exportedPackages.isEmpty()) {
            return this.trackedPackageImportsFactory.createEmpty();
        }
        String bundleVersion = bundleManifest.getBundleVersion().toString();
        String bundleSymbolicName = bundleManifest.getBundleSymbolicName().getSymbolicName();

        List<ImportedPackage> packageImports;

        if (bundleManifest.getFragmentHost().getBundleSymbolicName() != null) {
            bundleSymbolicName = bundleManifest.getFragmentHost().getBundleSymbolicName();
            packageImports = BundleManifestProcessor.createImportedPackageForEachExportedPackageOfFragment(exportedPackages, bundleSymbolicName,
                bundleManifest.getFragmentHost().getBundleVersion());
        } else {
            packageImports = BundleManifestProcessor.createImportedPackageForEachExportedPackage(exportedPackages, bundleSymbolicName, bundleVersion);
        }

        if (promoteExports) {
            tagImportsAsPromoted(packageImports);
        }

        if (bundleManifestHolder.getSecond()) {
            diagnoseSystemBundleOverlap(packageImports, bundleSymbolicName, bundleVersion);
        }

        return this.trackedPackageImportsFactory.create(packageImports, "Import-Bundle '" + bundleManifest.getBundleSymbolicName().getSymbolicName()
            + "' version '" + bundleManifest.getBundleVersion() + "'");
    }

    private void tagImportsAsPromoted(List<ImportedPackage> packageImports) {
        for (ImportedPackage importedPackage : packageImports) {
            importedPackage.getDirectives().put(IMPORT_SCOPE_DIRECTIVE, IMPORT_SCOPE_APPLICATION);
        }
    }

    /**
     * Check whether the packages imported by importing a bundle are exported by the system bundle and, if they are,
     * issue a warning.
     * 
     * @param importedPackages the imported packages
     * @param bundleSymbolicNameString the symbolic name of the imported bundle
     * @param bundleVersion the version of the imported bundle
     */
    private void diagnoseSystemBundleOverlap(List<ImportedPackage> importedPackages, String bundleSymbolicNameString, String bundleVersion) {
        Set<String> overlap = new HashSet<String>();
        for (ImportedPackage importedPackage : importedPackages) {
            String packageName = importedPackage.getPackageName();
            if (this.packagesExportedBySystemBundle.contains(packageName)) {
                overlap.add(packageName);
            }
        }
        if (!overlap.isEmpty()) {
            StringBuilder imports = new StringBuilder();
            boolean first = true;
            for (ImportedPackage packageImport : importedPackages) {
                if (!first) {
                    imports.append(",");
                }
                first = false;
                imports.append(packageImport.getPackageName());
            }
            this.eventLogger.log(UserRegionLogEvents.SYSTEM_BUNDLE_OVERLAP, bundleSymbolicNameString, bundleVersion, overlap.toString(),
                imports.toString());
        }
    }

    /**
     * Get a {@link TrackedPackageImports} instance representing the package imports that correspond to the given
     * library imports. Each imported library is looked up in the given {@link BundleRepository} and, if it is not
     * found, {@link UnableToSatisfiedBundleDependenciesException} is thrown. If any package imports are to be promoted,
     * then the result is also merged into the given <code>TrackedPackageImports</code> instance representing the
     * package imports to be promoted.
     * 
     * @param importedLibraries
     * @param bundleRepository
     * @param packageImportsToBePromoted
     * @param importingBundle
     * @return
     * @throws UnableToSatisfyBundleDependenciesException
     */
    private TrackedPackageImports getLibraryPackageImports(List<ImportedLibrary> importedLibraries, TrackedPackageImports packageImportsToBePromoted,
        BundleManifest importingBundle, List<BundleManifest> additionalManifests) throws UnableToSatisfyBundleDependenciesException {
        TrackedPackageImports allLibraryPackageImports = this.trackedPackageImportsFactory.createCollector();
        for (ImportedLibrary importedLibrary : importedLibraries) {
            VersionRange libraryVersionRange = importedLibrary.getVersion();
            String libraryName = importedLibrary.getLibrarySymbolicName();

            ArtifactDescriptor libraryArtefact = findArtifactDescriptorForLibrary(libraryName, libraryVersionRange);

            if (libraryArtefact != null) {

                if (!libraryName.endsWith(INSTRUMENTED_SUFFIX) && libraryArtefact.getName().endsWith(INSTRUMENTED_SUFFIX)) {
                    this.eventLogger.log(UserRegionLogEvents.ALTERNATE_INSTRUMENTED_LIBRARY_FOUND, importingBundle.getBundleSymbolicName(),
                        libraryName, libraryVersionRange.toString(), libraryArtefact.getName());
                }

                Version libraryVersion = libraryArtefact.getVersion();
                TrackedPackageImports libraryPackageImports = this.trackedPackageImportsFactory.createContainer("Import-Library '"
                    + importedLibrary.getLibrarySymbolicName() + "' version '" + libraryVersion + "'");

                Set<Attribute> importedBundles = libraryArtefact.getAttribute("Import-Bundle");

                for (Attribute importedBundle : importedBundles) {
                    String bundleSymbolicName = importedBundle.getValue();
                    Map<String, Set<String>> properties = importedBundle.getProperties();
                    Set<String> versionSet = properties.get("version");
                    VersionRange bundleVersionRange;
                    if (versionSet != null && !versionSet.isEmpty()) {
                        bundleVersionRange = new VersionRange(versionSet.iterator().next());
                    } else {
                        bundleVersionRange = VersionRange.NATURAL_NUMBER_RANGE;
                    }
                    OrderedPair<BundleManifest, Boolean> bundleManifest = findBundle(bundleSymbolicName, bundleVersionRange, additionalManifests);
                    if (bundleManifest.getFirst() != null) {
                        boolean applicationImportScope = false;
                        Set<String> importScopeSet = properties.get(IMPORT_SCOPE_DIRECTIVE);
                        if (importScopeSet != null && !importScopeSet.isEmpty()) {
                            applicationImportScope = IMPORT_SCOPE_APPLICATION.equals(importScopeSet.iterator().next());
                        }
                        libraryPackageImports.merge(createTrackedPackageImportsFromImportedBundle(bundleManifest, applicationImportScope,
                            packageImportsToBePromoted));
                    } else {
                        Resolution importedBundleResolution = Resolution.MANDATORY;
                        Set<String> resolutionSet = properties.get("resolution");
                        if (resolutionSet != null && !resolutionSet.isEmpty()) {
                            importedBundleResolution = Resolution.valueOf(resolutionSet.iterator().next().toUpperCase(Locale.ENGLISH));
                        }

                        if (importedBundleResolution.equals(Resolution.MANDATORY)) {
                            throw new UnableToSatisfyBundleDependenciesException(
                                importingBundle.getBundleSymbolicName() != null ? importingBundle.getBundleSymbolicName().getSymbolicName()
                                    : MISSING_BUNDLE_SYMBOLIC_NAME, importingBundle.getBundleVersion(), "Imported library '" + libraryName
                                    + "' version '" + libraryVersion + "' contains Import-Bundle for bundle '" + bundleSymbolicName
                                    + "' in version range '" + bundleVersionRange + "' which could not be satisfied");
                        }
                    }
                }
                allLibraryPackageImports.merge(libraryPackageImports);
            } else if (importedLibrary.getResolution().equals(Resolution.MANDATORY)) {
                throw createExceptionForMissingLibrary(libraryName, libraryVersionRange, importingBundle);
            }
        }
        return allLibraryPackageImports;
    }

    /**
     * Set the package imports of the given {@link BundleManifest} to the merged imports of the given
     * {@link TrackedPackageImports}.
     * 
     * @param bundleManifest the bundle manifest to be modified
     * @param bundleTrackedPackageImports the <code>TrackedPackageImports</code> containing the merged imports
     */
    private void setMergedImports(BundleManifest bundleManifest, TrackedPackageImports bundleTrackedPackageImports) {
        bundleManifest.getImportPackage().getImportedPackages().clear();
        bundleManifest.getImportPackage().getImportedPackages().addAll(bundleTrackedPackageImports.getMergedImports());
    }

    /**
     * Find the bundle with the given symbolic name and version range in the given bundle repository. If the given
     * scoper is non-null, scope the symbolic name before searching the repository. Return the {@link BundleDefinition}
     * of the bundle if it was found.
     * 
     * @param bundleSymbolicName
     * @param versionRange
     * @param bundleRepository
     * @param scoper
     * @return
     */
    private OrderedPair<BundleManifest, Boolean> findBundle(String bundleSymbolicName, VersionRange versionRange,
        List<BundleManifest> additionalManifests) {

        boolean diagnose = false;

        // prefer bundles from the supplied list
        BundleManifest bundleManifest = findMatchingManifest(bundleSymbolicName, versionRange, additionalManifests);

        if (bundleManifest == null && this.bundleContext != null) {
            Bundle[] installedBundles = this.bundleContext.getBundles();
            for (Bundle bundle : installedBundles) {
                if (bundleSymbolicName.equals(bundle.getSymbolicName()) && versionRange.includes(bundle.getVersion())) {
                    bundleManifest = getBundleManifest(bundle);
                    if (bundleManifest != null) {
                        diagnose = true;
                        break;
                    }
                }
            }
        }

        if (bundleManifest == null) {
            ArtifactDescriptor artefact = findArtifactDescriptorForBundle(bundleSymbolicName, versionRange);
            if (artefact != null) {
                diagnose = true;
                bundleManifest = BundleManifestFactory.createBundleManifest(BundleBridge.convertToDictionary(artefact));
            }
        }

        if (bundleManifest != null) {
            this.logger.info("Found definition for bundle with symbolic name '{}' and version range '{}': {}", new Object[] { bundleSymbolicName,
                versionRange, bundleManifest });
        } else {
            this.logger.info("Could not find definition for bundle with symbolic name '{}' and version range '{}'", bundleSymbolicName, versionRange);
        }

        return new OrderedPair<BundleManifest, Boolean>(bundleManifest, diagnose);
    }

    /**
     * Finds the manifest in the supplied list that matches the given bundle symbolic name and version range. If no
     * match is found, null is returned. If many matches are found, the one with the highest version range is selected.
     */
    private BundleManifest findMatchingManifest(String bundleSymbolicName, VersionRange versionRange, List<BundleManifest> additionalManifests) {
        Version selectedVersion = null;
        BundleManifest selectedManifest = null;

        for (final BundleManifest manifest : additionalManifests) {
            BundleSymbolicName bsn = manifest.getBundleSymbolicName();
            if (bsn != null && bundleSymbolicName.equals(bsn.getSymbolicName())) {
                Version version = manifest.getBundleVersion();
                if (versionRange.includes(version)) {
                    if (selectedVersion == null || version.compareTo(selectedVersion) > 0) {
                        selectedVersion = version;
                        selectedManifest = manifest;
                    }
                }
            }
        }
        return selectedManifest;
    }

    private ArtifactDescriptor findArtifactDescriptorForBundle(String bundleSymbolicName, VersionRange versionRange) {
        return this.repository.get(BundleBridge.BRIDGE_TYPE, bundleSymbolicName, versionRange);
    }

    private ArtifactDescriptor findArtifactDescriptorForLibrary(String librarySymbolicName, VersionRange versionRange) {
        return this.repository.get(LibraryDefinition.LIBRARY_TYPE, librarySymbolicName, versionRange);
    }

    /**
     * Get a {@link BundleDefinition} for the given {@link Bundle}. If a definition cannot be created, return
     * <code>null</code>.
     * 
     * @param bundle the bundle whose definition is required
     * @return the bundle definition or <code>null</code> if no definition can be created
     */
    private BundleManifest getBundleManifest(Bundle bundle) {
        return BundleManifestFactory.createBundleManifest(bundle.getHeaders());
    }

}
