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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.ExportPackageDescription;
import org.eclipse.osgi.service.resolver.ImportPackageSpecification;
import org.eclipse.osgi.service.resolver.ResolverError;
import org.eclipse.osgi.service.resolver.State;
import org.eclipse.osgi.service.resolver.VersionConstraint;
import org.osgi.framework.Constants;

import org.eclipse.virgo.util.math.Sets;

/**
 * Utility class for analysing uses failures in a given bundle.
 * 
 * <strong>Concurrent Semantics</strong><br/>
 * thread-safe
 * 
 */
public final class UsesAnalyser {

    public AnalysedUsesConflict[] getUsesConflicts(State state, ResolverError usesError) {
        VersionConstraint constraint = usesError.getUnsatisfiedConstraint();

        List<AnalysedUsesConflict> analysedUsesConflicts = new ArrayList<AnalysedUsesConflict>();

        if (constraint instanceof ImportPackageSpecification) {
            ImportPackageSpecification rootImport = (ImportPackageSpecification) constraint;

            /*
             * Compute the exports visible from the failed bundle except via rootImport. This is the exports matching
             * imports (except rootImport) of the failed bundle and the uses constraint closure of those exports. The
             * uses constraint closure of an export is those packages which are visible through transitive uses starting
             * with the export.
             */
            PackageSources visiblePackages = generateExportPackagesVisibleInFailedBundle(state, rootImport);

            /*
             * For each resolved export that satisfies rootImport, compute the uses constraint closure of the export and
             * add any that conflict with visiblePackages to the resultant set of uses conflicts.
             */
            for (ExportPackageDescription exportPackage : getResolvedCandidateExports(state, rootImport)) {
                PackageSources usedPackages = generateExportPackagesUsedViaExportPackage(state, exportPackage);
                analysedUsesConflicts.addAll(findConflictingExports(usedPackages, visiblePackages));
            }

            if (analysedUsesConflicts.isEmpty()) {
                // Be more aggressive by exploring unresolved exports that satisfy rootImport.
                for (ExportPackageDescription exportPackage : getUnresolvedCandidateExports(state, rootImport)) {
                    PackageSources usedPackages = generateExportPackagesUsedViaExportPackage(state, exportPackage);
                    analysedUsesConflicts.addAll(findConflictingExports(usedPackages, visiblePackages));
                }
            }
        }
        return analysedUsesConflicts.toArray(new AnalysedUsesConflict[analysedUsesConflicts.size()]);
    }

    public ResolverError[] getUsesResolverErrors(State state, BundleDescription bundle) {
        ResolverError[] errors = state.getResolverErrors(bundle);
        if (errors != null) {
            List<ResolverError> usesErrors = new ArrayList<ResolverError>(errors.length);
            for (ResolverError re : errors) {
                if (re.getType() == ResolverError.IMPORT_PACKAGE_USES_CONFLICT) {
                    usesErrors.add(re);
                }
            }
            return usesErrors.toArray(new ResolverError[usesErrors.size()]);
        }
        return null;
    }

    private List<AnalysedUsesConflict> findConflictingExports(PackageSources usedPackages, PackageSources directPackages) {
        List<AnalysedUsesConflict> usesConflicts = new ArrayList<AnalysedUsesConflict>();
        Set<String> packagesInCommon = Sets.<String> intersection(usedPackages.keySet(), directPackages.keySet());
        for (String packageName : packagesInCommon) {
            Set<SourcedPackage> allUsed = usedPackages.get(packageName);
            Set<SourcedPackage> allWired = directPackages.get(packageName);

            for (SourcedPackage sourcedPackage : allUsed) {
                UsedBySourcedPackage usedSourced = (UsedBySourcedPackage) sourcedPackage;
                if (!exportDescriptionOccursIn(usedSourced.getSource(), allWired)) {
                    for (SourcedPackage sp : allWired) {
                        usesConflicts.add(new AnalysedUsesConflict(usedSourced, sp));
                    }
                }
            }
        }
        return usesConflicts;
    }

    private static final boolean exportDescriptionOccursIn(ExportPackageDescription source, Set<SourcedPackage> allWired) {
        for (SourcedPackage w : allWired) {
            if (w.getSource().equals(source))
                return true;
        }
        return false;
    }

    private PackageSources generateExportPackagesUsedViaExportPackage(State state, ExportPackageDescription exportPackage) {
        PackageSources usedPackages = constructEmptyPackageSources();
        Set<String> knownPackages = new HashSet<String>();

        addUsedImportedPackages(state, usedPackages, exportPackage, exportPackage, knownPackages);
        return usedPackages;
    }

    private PackageSources generateExportPackagesVisibleInFailedBundle(State state, ImportPackageSpecification rootImport) {
        PackageSources visiblePackages = getOtherImportedPackages(state, rootImport);

        visiblePackages.putAll(computeUsesClosure(state, visiblePackages));

        BundleDescription failedBundle = rootImport.getBundle();
        visiblePackages.putAll(getExportedPackages(failedBundle));

        return visiblePackages;
    }

    private PackageSources computeUsesClosure(State state, PackageSources packages) {
        // Compute all the exports visible through transitive uses from directPackages
        PackageSources additionalPackages = constructEmptyPackageSources();
        Set<Entry<String, Set<SourcedPackage>>> keys = packages.entrySet();

        Set<String> knownPackages = new HashSet<String>();

        for (Entry<String, Set<SourcedPackage>> key : keys) {
            for (SourcedPackage sp : key.getValue()) {
                ExportPackageDescription source = sp.getSource();
                addUsedImportedPackages(state, additionalPackages, source, source, knownPackages);
            }
        }
        return additionalPackages;
    }

    private ExportPackageDescription[] getResolvedCandidateExports(State state, ImportPackageSpecification rootImport) {
        List<ExportPackageDescription> exports = new ArrayList<ExportPackageDescription>();

        BundleDescription[] bundles = state.getResolvedBundles();
        if (bundles != null) {
            for (BundleDescription bundle : bundles) {
                for (ExportPackageDescription exportPackage : bundle.getExportPackages()) {
                    if (rootImport.isSatisfiedBy(exportPackage)) {
                        exports.add(exportPackage);
                    }
                }
            }
        }

        return exports.toArray(new ExportPackageDescription[exports.size()]);
    }

    private ExportPackageDescription[] getUnresolvedCandidateExports(State state, ImportPackageSpecification rootImport) {
        List<ExportPackageDescription> exports = new ArrayList<ExportPackageDescription>();

        BundleDescription[] resolvedBundles = state.getResolvedBundles();

        BundleDescription[] bundles = state.getBundles();
        if (bundles != null) {
            for (BundleDescription bundle : bundles) {
                if (notInArray(bundle, resolvedBundles)) {
                    for (ExportPackageDescription exportPackage : bundle.getExportPackages()) {
                        if (rootImport.isSatisfiedBy(exportPackage)) {
                            exports.add(exportPackage);
                        }
                    }
                }
            }
        }

        return exports.toArray(new ExportPackageDescription[exports.size()]);
    }

    private static final boolean notInArray(BundleDescription bundle, BundleDescription[] resolvedBundles) {
        if (resolvedBundles == null)
            return true;
        for (BundleDescription b : resolvedBundles) {
            if (b == bundle) {
                return false;
            }
        }
        return true;
    }

    private void addUsedImportedPackages(State state, PackageSources packages, ExportPackageDescription exportPackage,
        ExportPackageDescription topDependency, Set<String> knownPackages) {
        String[] packageNames = (String[]) exportPackage.getDirective(Constants.USES_DIRECTIVE);
        if (packageNames != null) {
            BundleDescription bundle = exportPackage.getExporter();

            ExportPackageDescription[] allExports = bundle.getExportPackages();
            ImportPackageSpecification[] allImports = bundle.getImportPackages();
            ExportPackageDescription[] allResolvedImports = bundle.getResolvedImports();

            for (String packageName : packageNames) {
                ExportPackageDescription localExport = findExportPackageDescriptionInArray(allExports, packageName);
                if (null != localExport) {
                    packages.addPackageSource(packageName, new UsedBySourcedPackage(topDependency, localExport));
                }

                if (!knownPackages.contains(packageName)) {
                    ExportPackageDescription localResolvedImport = findExportPackageDescriptionInArray(allResolvedImports, packageName);
                    if (null != localResolvedImport) {
                        knownPackages.add(packageName);
                        packages.addPackageSource(packageName, new UsedBySourcedPackage(topDependency, localResolvedImport));
                        addUsedImportedPackages(state, packages, localResolvedImport, topDependency, knownPackages);
                    } else {
                        ImportPackageSpecification anImport = findImportPackageSpecificationInArray(allImports, packageName);
                        if (anImport != null) {
                            ExportPackageDescription[] matchingExports = getCandidateExports(state, anImport);
                            if (matchingExports.length != 0) {
                                knownPackages.add(packageName);
                                for (ExportPackageDescription matchingExport : matchingExports) {
                                    packages.addPackageSource(packageName, new UsedBySourcedPackage(topDependency, matchingExport));
                                    addUsedImportedPackages(state, packages, matchingExport, topDependency, knownPackages);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static final ImportPackageSpecification findImportPackageSpecificationInArray(ImportPackageSpecification[] allImports, String packageName) {
        for (ImportPackageSpecification ips : allImports) {
            if (packageName.equals(ips.getName())) {
                return ips;
            }
        }
        return null;
    }

    private static final ExportPackageDescription findExportPackageDescriptionInArray(ExportPackageDescription[] allExports, String packageName) {
        for (ExportPackageDescription epd : allExports) {
            if (packageName.equals(epd.getName())) {
                return epd;
            }
        }
        return null;
    }

    private PackageSources getOtherImportedPackages(State state, ImportPackageSpecification rootImport) {
        BundleDescription bundle = rootImport.getBundle();

        PackageSources packages = constructEmptyPackageSources();

        ImportPackageSpecification[] importSpecifications = bundle.getImportPackages();
        for (ImportPackageSpecification importSpecification : importSpecifications) {
            if (rootImport != importSpecification) {
                if (!Constants.RESOLUTION_OPTIONAL.equals(importSpecification.getDirective(Constants.RESOLUTION_DIRECTIVE))) {
                    ExportPackageDescription[] exportPackages = getCandidateExports(state, importSpecification);
                    for (ExportPackageDescription exportPackage : exportPackages) {
                        packages.addPackageSource(exportPackage.getName(), new ImportedSourcedPackage(rootImport, exportPackage));
                    }
                }
            }
        }
        return packages;
    }

    private ExportPackageDescription[] getCandidateExports(State state, ImportPackageSpecification importSpecification) {
        ExportPackageDescription[] pkgs = getResolvedCandidateExports(state, importSpecification);
        if (pkgs.length == 0)
            pkgs = getUnresolvedCandidateExports(state, importSpecification);
        return pkgs;
    }

    private PackageSources getExportedPackages(BundleDescription bundle) {
        ExportPackageDescription[] packageArray = bundle.getExportPackages();
        PackageSources packages = constructEmptyPackageSources();
        if (packageArray != null)
            for (ExportPackageDescription exportPackage : packageArray) {
                packages.addPackageSource(exportPackage.getName(), new SourcedPackage(exportPackage));
            }
        return packages;
    }

    private final static String stringOf(ExportPackageDescription source) {
        BundleDescription bundle = source.getSupplier();
        StringBuilder sb = new StringBuilder("'");
        sb.append(source.getName()).append("_").append(source.getVersion()).append("' in bundle ").append(stringOf(bundle));
        return sb.toString();
    }

    private final static String stringOf(BundleDescription bundle) {
        StringBuilder sb = new StringBuilder("'");
        sb.append(bundle.getSymbolicName()).append("_").append(bundle.getVersion()).append("[").append(bundle.getBundleId()).append("]").append("'");
        return sb.toString();
    }

    public static interface UsesViolation {

        VersionConstraint getConstraint();

        PossibleMatch[] getPossibleMatches();
    }

    public static interface PossibleMatch {

        ExportPackageDescription getSupplier();

        boolean isDependentConstraintMismatch();

        DependentConstraintCollision[] getCollisions();
    }

    public static interface DependentConstraintCollision {

        ImportPackageSpecification getConsumerConstraint();

        ImportPackageSpecification getSupplierConstraint();

        CollisionReason getCollisionReason();

    }

    public static enum CollisionReason {
        DISJOINT_VERSION_RANGES, ATTRIBUTE_MISMATCH
    }

    private static class SourcedPackage {

        private final ExportPackageDescription source;

        public SourcedPackage(ExportPackageDescription source) {
            this.source = source;
        }

        public String toString() {
            return stringOf(this.source);
        }

        public ExportPackageDescription getSource() {
            return this.source;
        }
    }

    private static final class ImportedSourcedPackage extends SourcedPackage {

        private final ImportPackageSpecification rootImport;

        public ImportedSourcedPackage(ImportPackageSpecification rootImport, ExportPackageDescription source) {
            super(source);
            this.rootImport = rootImport;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder(super.toString());
            sb.append(" imported by bundle ").append(stringOf(this.rootImport.getBundle()));
            return sb.toString();
        }
    }

    private static final class UsedBySourcedPackage extends SourcedPackage {

        private final ExportPackageDescription usedBy;

        public UsedBySourcedPackage(ExportPackageDescription usedBy, ExportPackageDescription source) {
            super(source);
            this.usedBy = usedBy;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder(super.toString());
            sb.append(" used by ").append(stringOf(this.usedBy));
            return sb.toString();
        }

        public ExportPackageDescription getUsedBy() {
            return this.usedBy;
        }
    }

    private interface PackageSources extends Map<String, Set<SourcedPackage>> {

        public void addPackageSource(String packageName, SourcedPackage sourcedPackage);
    }

    private class PackageSourcesImpl extends HashMap<String, Set<SourcedPackage>> implements PackageSources {

        private static final long serialVersionUID = 1L;
        
        public final void addPackageSource(String packageName, SourcedPackage sourcedPackage) {
            Set<SourcedPackage> sourcedSet = get(packageName);
            if (sourcedSet == null) {
                sourcedSet = new HashSet<SourcedPackage>();
                put(packageName, sourcedSet);
            }
            sourcedSet.add(sourcedPackage);
        }
    }

    private PackageSources constructEmptyPackageSources() {
        return new PackageSourcesImpl();
    }

    public static final class AnalysedUsesConflict {

        private final UsedBySourcedPackage usedPackage;

        private final SourcedPackage resolvedPackage;

        private AnalysedUsesConflict(UsedBySourcedPackage used, SourcedPackage resolved) {
            this.usedPackage = used;
            this.resolvedPackage = resolved;
        }

        public String[] getConflictStatement() {
            return new String[] { "package        " + this.usedPackage.toString(), "conflicts with " + this.resolvedPackage.toString() };
        }

        public ExportPackageDescription getConflictingPackage() {
            return this.usedPackage == null ? null : this.usedPackage.getSource();
        }

        public ExportPackageDescription getUsesRootPackage() {
            return this.usedPackage == null ? null : this.usedPackage.getUsedBy();
        }

        public ExportPackageDescription getPackage() {
            return this.resolvedPackage == null ? null : this.resolvedPackage.getSource();
        }
    }
}
