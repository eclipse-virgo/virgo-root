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

package org.eclipse.virgo.kernel.install.pipeline.stage.resolve.internal;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.eclipse.virgo.kernel.artifact.plan.PlanDescriptor.Provisioning;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.kernel.install.artifact.BundleInstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.PlanInstallArtifact;
import org.eclipse.virgo.kernel.install.environment.InstallEnvironment;
import org.eclipse.virgo.kernel.install.environment.InstallLog;
import org.eclipse.virgo.kernel.install.pipeline.stage.PipelineStage;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiBundle;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiFramework;
import org.eclipse.virgo.util.common.GraphNode;
import org.eclipse.virgo.util.common.GraphNode.DirectedAcyclicGraphVisitor;
import org.eclipse.virgo.util.osgi.manifest.BundleManifest;
import org.osgi.framework.BundleException;

/**
 * {@link QuasiInstallStage} is a {@link PipelineStage} which installs the bundle artifacts of the install graph in the
 * side state.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is thread safe.
 * 
 */
public final class QuasiInstallStage implements PipelineStage {

    /**
     * {@inheritDoc}
     */
    public void process(GraphNode<InstallArtifact> installGraph, InstallEnvironment installEnvironment) throws DeploymentException {
        QuasiFramework quasiFramework = installEnvironment.getQuasiFramework();
        installGraph.visit(new InstallVisitor(quasiFramework, installEnvironment.getInstallLog()));
    }

    private static class InstallVisitor implements DirectedAcyclicGraphVisitor<InstallArtifact> {

        private final QuasiFramework quasiFramework;

        private final InstallLog installLog;

        public InstallVisitor(QuasiFramework quasiFramework, InstallLog installLog) {
            this.quasiFramework = quasiFramework;
            this.installLog = installLog;
        }

        public boolean visit(GraphNode<InstallArtifact> graph) {
            InstallArtifact installArtifact = graph.getValue();
            if (installArtifact instanceof BundleInstallArtifact) {
                Provisioning provisioning = getProvisioning(graph);
                BundleInstallArtifact bundleInstallArtifact = (BundleInstallArtifact) installArtifact;
                try {
                    BundleManifest bundleManifest = bundleInstallArtifact.getBundleManifest();
                    File location = bundleInstallArtifact.getArtifactFS().getFile();
                    QuasiBundle quasiBundle = this.quasiFramework.install(location.toURI(), bundleManifest);
                    quasiBundle.setProvisioning(provisioning);
                    bundleInstallArtifact.setQuasiBundle(quasiBundle);
                } catch (IOException e) {
                    this.installLog.log(bundleInstallArtifact, "failed to read bundle manifest", e.getMessage());
                    throw new RuntimeException("failed to read bundle manifest", e);
                } catch (BundleException e) {
                    this.installLog.log(bundleInstallArtifact, "failed to install bundle in side state", e.getMessage());
                    throw new RuntimeException("failed to install bundle in side state", e);
                }
            }
            return true;
        }

        /**
         * Returns the provisioning behaviour for the given install artifact node. If the artifact has no parents, then
         * this is AUTO. If the artifact has at least one parent, then its provisioning behaviour is AUTO unless all its
         * parents are plans with provisioning behaviour DISABLED, in which case its provisioning behaviour is DISABLED.
         * 
         * @param artifactGraphNode the {@link GraphNode} of the install artifact
         * @return the {@link Provisioning} of the given install artifact node
         */
        private Provisioning getProvisioning(GraphNode<InstallArtifact> artifactGraphNode) {
            Provisioning provisioning;
            List<GraphNode<InstallArtifact>> parents = artifactGraphNode.getParents();
            if (parents.isEmpty()) {
                provisioning = Provisioning.AUTO;
            } else {
                boolean allParentsDisabled = true;
                for (GraphNode<InstallArtifact> parent : parents) {
                    InstallArtifact parentInstallArtifact = parent.getValue();
                    if (parentInstallArtifact instanceof PlanInstallArtifact) {
                        if (((PlanInstallArtifact) parentInstallArtifact).getProvisioning() == Provisioning.AUTO) {
                            allParentsDisabled = false;
                        }
                    } else {
                        allParentsDisabled = false; // in case other kinds of parents are introduced
                    }
                }
                provisioning = allParentsDisabled ? Provisioning.DISABLED : Provisioning.AUTO;
            }
            return provisioning;
        }

    }

}
