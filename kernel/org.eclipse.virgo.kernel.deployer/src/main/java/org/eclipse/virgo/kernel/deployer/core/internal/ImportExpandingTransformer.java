/*******************************************************************************
 * Copyright (c) 2008, 2011 VMware Inc. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *   EclipseSource - Bug 358442 Change InstallArtifact graph from a tree to a DAG
 *******************************************************************************/

package org.eclipse.virgo.kernel.deployer.core.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.kernel.install.artifact.BundleInstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.PlanInstallArtifact;
import org.eclipse.virgo.kernel.install.environment.InstallEnvironment;
import org.eclipse.virgo.kernel.install.pipeline.stage.transform.Transformer;
import org.eclipse.virgo.kernel.install.pipeline.stage.transform.internal.BundleInstallArtifactGatheringGraphVisitor;
import org.eclipse.virgo.kernel.osgi.framework.ImportExpander;
import org.eclipse.virgo.kernel.osgi.framework.ImportMergeException;
import org.eclipse.virgo.kernel.osgi.framework.UnableToSatisfyDependenciesException;
import org.eclipse.virgo.util.common.GraphNode;
import org.eclipse.virgo.util.common.GraphNode.ExceptionThrowingDirectedAcyclicGraphVisitor;
import org.eclipse.virgo.util.osgi.manifest.BundleManifest;

/**
 * {@link ImportExpandingTransformer} is a {@link Transformer} that expands Import-Library and Import-Bundle into package imports.
 * Expansion of imports in bundles that are part of a scope is performed against all of the bundles at the same time.
 * Expansion of bundles that are not part of a scope is performed on a bundle-by-bundle basis.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread-safe.
 * 
 */
final class ImportExpandingTransformer implements Transformer {

    private final ImportExpander importExpander;

    ImportExpandingTransformer(ImportExpander importExpander) {
        this.importExpander = importExpander;
    }

    /**
     * {@inheritDoc}
     */
    public void transform(GraphNode<InstallArtifact> installGraph, final InstallEnvironment installEnvironment) throws DeploymentException {
        installGraph.visit(new ImportExpandingGraphVisitor(installEnvironment));
    }

    private final class ImportExpandingGraphVisitor implements ExceptionThrowingDirectedAcyclicGraphVisitor<InstallArtifact, DeploymentException> {

        private final InstallEnvironment installEnvironment;

        ImportExpandingGraphVisitor(InstallEnvironment installEnvironment) {
            this.installEnvironment = installEnvironment;
        }

        /**
         * {@inheritDoc}
         */
        public boolean visit(GraphNode<InstallArtifact> graph) throws DeploymentException {
            if (graph.getValue() instanceof PlanInstallArtifact) {
                PlanInstallArtifact planInstallArtifact = (PlanInstallArtifact) graph.getValue();
                if (planInstallArtifact.isScoped()) {
                    expandImportsOfBundlesInScopedPlan(graph, this.installEnvironment);
                    return false;
                }
            } else if (graph.getValue() instanceof BundleInstallArtifact) {
                expandImports(Collections.singleton((BundleInstallArtifact) graph.getValue()), this.installEnvironment);
            }
            return true;
        }
    }

    void expandImportsOfBundlesInScopedPlan(GraphNode<InstallArtifact> planGraph, InstallEnvironment installEnvironment) throws DeploymentException {
        BundleInstallArtifactGatheringGraphVisitor visitor = new BundleInstallArtifactGatheringGraphVisitor();
        planGraph.visit(visitor);
        expandImports(visitor.getChildBundles(), installEnvironment);
    }

    void expandImports(Set<BundleInstallArtifact> bundleInstallArtifacts, InstallEnvironment installEnvironment) throws DeploymentException {

        List<BundleManifest> bundleManifestList = new ArrayList<BundleManifest>(bundleInstallArtifacts.size());

        for (BundleInstallArtifact bundleInstallArtifact : bundleInstallArtifacts) {
            try {
                BundleManifest bundleManifest = bundleInstallArtifact.getBundleManifest();
                bundleManifestList.add(bundleManifest);
            } catch (IOException e) {
                installEnvironment.getInstallLog().log(this, "I/O error getting bundle manifest for  %s", bundleInstallArtifact.toString());
                throw new DeploymentException("I/O error getting bundle manifest for " + bundleInstallArtifact, e);
            }
        }

        try {
            this.importExpander.expandImports(bundleManifestList);
            installEnvironment.getInstallLog().log(this, "Expanded imports of %s", bundleInstallArtifacts.toString());
        } catch (ImportMergeException e) {
            installEnvironment.getInstallLog().log(this, "Error in %s merging expanded imports for package %s from %s",
                bundleInstallArtifacts.toString(), e.getConflictingPackageName(), e.getSources());
            throw new DeploymentException("Error merging expanded imports for " + bundleInstallArtifacts, e);
        } catch (UnableToSatisfyDependenciesException e) {
            installEnvironment.getInstallLog().log(this, "Unsatisfied dependencies in %s: %s", bundleInstallArtifacts.toString(),
                e.getFailureDescription());
            throw new DeploymentException(e.getMessage(), e);
        }
    }
}
