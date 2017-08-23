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

package org.eclipse.virgo.kernel.userregion.internal.quasi;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.equinox.region.Region;
import org.eclipse.equinox.region.RegionDigraph.FilteredRegion;
import org.eclipse.equinox.region.RegionFilter;
import org.eclipse.osgi.service.resolver.BundleDelta;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.BundleSpecification;
import org.eclipse.osgi.service.resolver.ExportPackageDescription;
import org.eclipse.osgi.service.resolver.HostSpecification;
import org.eclipse.osgi.service.resolver.ImportPackageSpecification;
import org.eclipse.osgi.service.resolver.ResolverError;
import org.eclipse.osgi.service.resolver.State;
import org.eclipse.osgi.service.resolver.StateDelta;
import org.eclipse.osgi.service.resolver.StateObjectFactory;
import org.eclipse.osgi.service.resolver.VersionConstraint;
import org.eclipse.osgi.service.resolver.VersionRange;
import org.eclipse.virgo.kernel.artifact.bundle.BundleBridge;
import org.eclipse.virgo.kernel.osgi.framework.UnableToSatisfyBundleDependenciesException;
import org.eclipse.virgo.kernel.osgi.framework.UnableToSatisfyDependenciesException;
import org.eclipse.virgo.kernel.userregion.internal.quasi.ResolutionFailureDetective.ResolverErrorsHolder;
import org.eclipse.virgo.medic.dump.DumpGenerator;
import org.eclipse.virgo.repository.ArtifactDescriptor;
import org.eclipse.virgo.repository.Attribute;
import org.eclipse.virgo.repository.Query;
import org.eclipse.virgo.repository.Repository;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Calculates the dependencies of a given set of {@link BundleDescription BundleDescriptions}.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe.
 * 
 */
public final class DependencyCalculator {

    // The following literal must match ResolutionDumpContributor.RESOLUTION_STATE_KEY from kernel core.
    private static final String RESOLUTION_STATE_KEY = "resolution.state";

    private static final String REGION_LOCATION_DELIMITER = "@";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ResolutionFailureDetective detective;

    private final AtomicLong nextBundleId = new AtomicLong(System.currentTimeMillis());

    private final Repository repository;

    private final Object monitor = new Object();

    private final StateObjectFactory stateObjectFactory;

    private final DumpGenerator dumpGenerator;

    private Region coregion;

    public DependencyCalculator(StateObjectFactory stateObjectFactory, ResolutionFailureDetective detective, Repository repository,
        BundleContext bundleContext) {
        this.repository = repository;
        this.detective = detective;
        this.stateObjectFactory = stateObjectFactory;
        this.dumpGenerator = bundleContext.getService(bundleContext.getServiceReference(DumpGenerator.class));
    }

    /**
     * Calculates the dependencies of the supplied set of {@link BundleDescription bundles}.
     * <p/>
     * 
     * Callers must supply a {@link State} against which dependency satisfaction is executed. The supplied State is
     * destructively modified during constraint satisfaction so it <strong>must</strong> not be the system state.
     * <p/>
     * 
     * In a successful invocation, any new bundles that need to be installed are returned, and the supplied
     * <code>State</code> is transformed to reflect the newly resolved state of the supplied bundles. Callers can query
     * the <code>State</code> to find the fully wiring graph of the supplied bundles after successful constraint
     * satisfaction.
     * <p/>
     * 
     * If diagnostics are forced, then an {@link UnableToSatisfyDependenciesException} is thrown if the constraints
     * cannot be satisfied. If diagnostics are not forced, then either an
     * <code>UnableToSatisfyDependenciesException</code> is thrown or the new bundles that need to be installed are
     * returned depending on whether cloning some bundles may improve the chances of satisfying the constraints.
     * 
     * @param state the <code>State</code> to satisfy against.
     * @param coregion the coregion containing the side-state bundles
     * @param bundles the bundles to calculate dependencies for
     * @param disabledProvisioningBundles a subset of bundles which should not have their dependencies provisioned
     * @return an array of descriptions of bundles that need to be added to the state to satisfy constraints.
     * @throws BundleException
     * @throws UnableToSatisfyDependenciesException
     */
    public BundleDescription[] calculateDependencies(State state, Region coregion, BundleDescription[] bundles,
        BundleDescription[] disabledProvisioningBundles) throws BundleException, UnableToSatisfyDependenciesException {
        this.logger.info("Calculating missing dependencies of bundle(s) '{}'", (Object[]) bundles);
        synchronized (this.monitor) {
            this.coregion = coregion;
            try {
                doSatisfyConstraints(bundles, state, disabledProvisioningBundles);

                StateDelta delta = state.resolve(bundles);

                for (BundleDescription description : bundles) {
                    if (!description.isResolved()) {
                        generateDump(state);

                        ResolverErrorsHolder reh = new ResolverErrorsHolder();
                        String failure = this.detective.generateFailureDescription(state, description, reh);

                        ResolverError[] resolverErrors = reh.getResolverErrors();
                        if (resolverErrors != null) {
                            for (ResolverError resolverError : resolverErrors) {
                                if (resolverError.getType() == ResolverError.IMPORT_PACKAGE_USES_CONFLICT) {
                                    VersionConstraint unsatisfiedConstraint = resolverError.getUnsatisfiedConstraint();
                                    if (unsatisfiedConstraint instanceof ImportPackageSpecification) {
                                        ImportPackageSpecification importPackageSpecification = (ImportPackageSpecification) unsatisfiedConstraint;
                                        this.logger.debug("Uses conflict: package '{}' version '{}' bundle '{}' version '{}'", new Object[] {
                                            importPackageSpecification.getName(), importPackageSpecification.getVersionRange(),
                                            importPackageSpecification.getBundleSymbolicName(), importPackageSpecification.getBundleVersionRange() });
                                    }
                                }
                            }
                        }

                        throw new UnableToSatisfyBundleDependenciesException(description.getSymbolicName(), description.getVersion(), failure, state,
                            reh.getResolverErrors());
                    }
                }

                BundleDelta[] deltas = delta.getChanges(BundleDelta.ADDED, false);
                Set<BundleDescription> newBundles = new HashSet<BundleDescription>();

                for (BundleDelta bundleDelta : deltas) {
                    newBundles.add(bundleDelta.getBundle());
                }

                Set<BundleDescription> dependenciesSet = getNewTransitiveDependencies(new HashSet<BundleDescription>(Arrays.asList(bundles)),
                    newBundles);

                List<BundleDescription> dependencies = new ArrayList<BundleDescription>(dependenciesSet);
                this.logger.info("The dependencies of '{}' are '{}'", Arrays.toString(bundles), dependencies);

                Collections.sort(dependencies, new BundleDescriptionComparator());

                BundleDescription[] dependencyDescriptions = dependencies.toArray(new BundleDescription[dependencies.size()]);
                return dependencyDescriptions;
            } finally {
                this.coregion = null;
            }
        }
    }

    private Set<BundleDescription> getNewTransitiveDependencies(Set<BundleDescription> dependingBundles, Collection<BundleDescription> newBundles) {
        Set<BundleDescription> transitiveDependencies = new HashSet<BundleDescription>();

        while (!dependingBundles.isEmpty()) {
            Set<BundleDescription> newDependencies = new HashSet<BundleDescription>();
            for (BundleDescription bundle : dependingBundles) {
                newDependencies.addAll(getNewImmediateDependencies(bundle, newBundles));
            }

            // Next time round the loop, check new dependencies that we haven't already processed
            newDependencies.removeAll(transitiveDependencies);
            dependingBundles = newDependencies;

            transitiveDependencies.addAll(newDependencies);
        }
        return transitiveDependencies;
    }

    private Set<BundleDescription> getNewImmediateDependencies(BundleDescription bundle, Collection<BundleDescription> newBundles) {
        Set<BundleDescription> immediateDependencies = new HashSet<BundleDescription>();
        immediateDependencies.addAll(Arrays.asList(bundle.getFragments()));
        immediateDependencies.addAll(Arrays.asList(bundle.getResolvedRequires()));
        immediateDependencies.addAll(getPackageProviders(bundle));

        HostSpecification hostSpecification = bundle.getHost();
        if (hostSpecification != null) {
            for (BundleDescription host : hostSpecification.getHosts()) {
                immediateDependencies.add(host);
            }
        }
        immediateDependencies.retainAll(newBundles);
        return immediateDependencies;
    }

    private Set<BundleDescription> getPackageProviders(BundleDescription bundleDescription) {
        Set<BundleDescription> packageProviders = new HashSet<BundleDescription>();
        ExportPackageDescription[] resolvedImports = bundleDescription.getResolvedImports();
        for (ExportPackageDescription resolvedImport : resolvedImports) {
            packageProviders.add(resolvedImport.getExporter());
        }
        return packageProviders;
    }

    private void doSatisfyConstraints(BundleDescription description, State state, BundleDescription[] disabledProvisioningDescriptions)
        throws BundleException {
        doSatisfyConstraints(new BundleDescription[] { description }, state, disabledProvisioningDescriptions);
    }

    private void doSatisfyConstraints(BundleDescription[] descriptions, State state, BundleDescription[] disabledProvisioningDescriptions)
        throws BundleException {

        VersionConstraint[] unsatisfiedConstraints = findUnsatisfiedConstraints(descriptions, state);

        List<BundleDescription> constraintsSatisfiers = new ArrayList<BundleDescription>();

        for (VersionConstraint versionConstraint : unsatisfiedConstraints) {
            BundleDescription unsatisfiedBundle = versionConstraint.getBundle();
            boolean found = false;
            for (BundleDescription description : descriptions) {
                if (description == unsatisfiedBundle) {
                    found = true;
                }
            }
            if (found) {
                if (provision(unsatisfiedBundle, disabledProvisioningDescriptions)) {
                    if (versionConstraint instanceof ImportPackageSpecification) {
                        satisfyImportPackage((ImportPackageSpecification) versionConstraint, state, constraintsSatisfiers);
                    } else if (versionConstraint instanceof BundleSpecification) {
                        satisfyRequireBundle(versionConstraint, state, constraintsSatisfiers);
                    } else if (versionConstraint instanceof HostSpecification) {
                        satisfyFragmentHost(versionConstraint, state, constraintsSatisfiers);
                    }
                }
            }
        }

        for (BundleDescription description : descriptions) {
            if (provision(description, disabledProvisioningDescriptions)) {
                satisfyFragments(description, state, constraintsSatisfiers);
            }
        }

        Collections.sort(constraintsSatisfiers, new BundleDescriptionComparator());

        for (BundleDescription constraintSatisfier : constraintsSatisfiers) {
            if (!isBundlePresentInState(constraintSatisfier.getName(), constraintSatisfier.getVersion(), state)) {
                state.addBundle(constraintSatisfier);
                this.coregion.addBundle(constraintSatisfier.getBundleId());
                doSatisfyConstraints(constraintSatisfier, state, disabledProvisioningDescriptions);
            }
        }
    }

    private boolean provision(BundleDescription bundleDescription, BundleDescription[] disabledProvisioningDescriptions) {
        boolean provision = true;
        for (BundleDescription disabledProvisioningDescription : disabledProvisioningDescriptions) {
            if (disabledProvisioningDescription == bundleDescription) {
                provision = false;
            }
        }
        return provision;
    }

    private void satisfyFragments(BundleDescription description, State state, List<BundleDescription> constraintSatisfiers) throws BundleException {
        Set<? extends ArtifactDescriptor> fragmentArtefacts = this.repository.createQuery("type", BundleBridge.BRIDGE_TYPE).addFilter(
            "Fragment-Host", description.getSymbolicName()).run();
        for (ArtifactDescriptor fragmentArtefact : fragmentArtefacts) {
            addBundle(fragmentArtefact, state, constraintSatisfiers);
        }
    }

    private void satisfyFragmentHost(VersionConstraint constraint, State state, List<BundleDescription> constraintSatisfiers) throws BundleException {
        Set<? extends ArtifactDescriptor> hostArtefacts = this.repository.createQuery("type", BundleBridge.BRIDGE_TYPE).addFilter(
            "Bundle-SymbolicName", constraint.getName()).run();
        for (ArtifactDescriptor hostArtefact : hostArtefacts) {
            addBundle(hostArtefact, state, constraintSatisfiers);
        }
    }

    private void satisfyRequireBundle(VersionConstraint constraint, State state, List<BundleDescription> constraintSatisfiers) throws BundleException {
        Set<? extends ArtifactDescriptor> requiredBundleArtefacts = this.repository.createQuery("type", BundleBridge.BRIDGE_TYPE).addFilter(
            "Bundle-SymbolicName", constraint.getName()).run();
        for (ArtifactDescriptor requiredBundleArtefact : requiredBundleArtefacts) {
            addBundle(requiredBundleArtefact, state, constraintSatisfiers);
        }
    }

    private void satisfyImportPackage(ImportPackageSpecification constraint, State state, List<BundleDescription> constraintSatisfiers)
        throws BundleException {
        VersionRange packageVersionRange = constraint.getVersionRange();
        Query query = this.repository.createQuery("type", BundleBridge.BRIDGE_TYPE);
        boolean loosePackageVersionRange = false;
        if (packageVersionRange != null && packageVersionRange.getMaximum().equals(packageVersionRange.getMinimum())) {
            Map<String, Set<String>> properties = new HashMap<String, Set<String>>();
            properties.put("version", new HashSet<String>(Arrays.asList(packageVersionRange.getMaximum().toString())));
            query.addFilter("Export-Package", constraint.getName(), properties);
        } else {
            query.addFilter("Export-Package", constraint.getName());
            loosePackageVersionRange = packageVersionRange != null;
        }

        String bundleSymbolicName = constraint.getBundleSymbolicName();
        if (bundleSymbolicName != null) {
            query.addFilter("Bundle-SymbolicName", bundleSymbolicName);
        }

        VersionRange bundleVersionRange = constraint.getBundleVersionRange();
        boolean looseBundleVersionRange = false;
        if (bundleVersionRange != null && bundleVersionRange.getMaximum().equals(bundleVersionRange.getMinimum())) {
            query.addFilter("Bundle-Version", bundleVersionRange.getMaximum().toString());
        } else {
            looseBundleVersionRange = bundleVersionRange != null;
        }

        Set<? extends ArtifactDescriptor> packageExportingArtefacts = query.run();

        for (ArtifactDescriptor packageExportingArtefact : packageExportingArtefacts) {
            if ((!loosePackageVersionRange || packageVersionInRange(packageExportingArtefact, packageVersionRange, constraint.getName()))
                && (!looseBundleVersionRange || bundleVersionInRange(packageExportingArtefact, bundleVersionRange))) {
                addBundle(packageExportingArtefact, state, constraintSatisfiers);
            }
        }
    }

    private boolean packageVersionInRange(ArtifactDescriptor packageExportingArtefact, VersionRange packageVersionRange, String packageName) {
        for (Attribute attribute : packageExportingArtefact.getAttribute("Export-Package")) {
            if (attribute.getValue().equals(packageName)) {
                Set<String> versions = attribute.getProperties().get("version");
                Version version = new org.osgi.framework.Version(versions == null || versions.isEmpty() ? "0" : versions.iterator().next());
                if (packageVersionRange.isIncluded(version)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean bundleVersionInRange(ArtifactDescriptor packageExportingArtefact, VersionRange bundleVersionRange) {
        return bundleVersionRange.isIncluded(packageExportingArtefact.getVersion());
    }

    private void addBundle(ArtifactDescriptor artefact, State state, List<BundleDescription> constraintSatisfiers) throws BundleException {
        if (!isBundlePresentInState(artefact.getName(), artefact.getVersion(), state)) {
            BundleDescription description = createBundleDescription(artefact, state);
            constraintSatisfiers.add(description);
        }
    }

    private boolean isBundlePresentInState(String bundleSymbolicName, Version version, State state) {
        BundleDescription[] bundleDescriptions = state.getBundles(bundleSymbolicName);
        for (BundleDescription bundleDescription : bundleDescriptions) {
            if (bundleDescription.getVersion().equals(version)) {
                long bundleId = bundleDescription.getBundleId();
                if (bundleId == 0L || this.coregion.contains(bundleId)) {
                    return true;
                }
                // XXX Refactoring required here. This temporary code only traverses the coregion and user region.
                Set<FilteredRegion> edges = this.coregion.getEdges();
                Iterator<FilteredRegion> iterator = edges.iterator();
                // Bug 377392: cope with the unexpected case of a coregion with no edges.
                if (iterator.hasNext()) {
                    FilteredRegion edge = iterator.next();
                    Region userRegion = edge.getRegion();
                    RegionFilter filter = edge.getFilter();
                    if (filter.isAllowed(bundleDescription) && userRegion.contains(bundleId)) {
                        return true;
                    }
                }
            }

        }
        return false;
    }

    private BundleDescription createBundleDescription(ArtifactDescriptor artifact, State state) throws BundleException {
        Dictionary<String, String> manifest = BundleBridge.convertToDictionary(artifact);
        try {
            URI uri = artifact.getUri();
            String installLocation = "file".equals(uri.getScheme()) ? new File(uri).getAbsolutePath() : uri.toString();
            BundleDescription bundleDescription = this.stateObjectFactory.createBundleDescription(state, manifest, this.coregion.getName()
                + REGION_LOCATION_DELIMITER + installLocation, this.nextBundleId.getAndIncrement());
            this.coregion.addBundle(bundleDescription.getBundleId());
            return bundleDescription;
        } catch (RuntimeException e) {
            throw new BundleException("Unable to read bundle at '" + artifact.getUri() + "'", e);
        } catch (BundleException be) {
            throw new BundleException("Failed to create BundleDescriptor for artifact at '" + artifact.getUri() + "'", be);
        }
    }

    private VersionConstraint[] findUnsatisfiedConstraints(BundleDescription[] bundles, State state) {
        return state.getStateHelper().getUnsatisfiedLeaves(bundles);
    }

    private void generateDump(State state) {
        Map<String, Object> context = new HashMap<String, Object>();
        context.put(RESOLUTION_STATE_KEY, state);
        this.dumpGenerator.generateDump("resolutionFailure", context);
    }

    public long getNextBundleId() {
        return this.nextBundleId.getAndIncrement();
    }
}
