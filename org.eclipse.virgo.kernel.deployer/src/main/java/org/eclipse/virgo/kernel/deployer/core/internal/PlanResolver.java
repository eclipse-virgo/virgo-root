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

import java.util.List;

import org.eclipse.virgo.kernel.artifact.ArtifactSpecification;
import org.eclipse.virgo.kernel.artifact.plan.PlanDescriptor.Provisioning;
import org.eclipse.virgo.kernel.deployer.core.DeploymentException;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifactGraphInclosure;
import org.eclipse.virgo.kernel.install.artifact.PlanInstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.internal.AbstractInstallArtifact;
import org.eclipse.virgo.kernel.install.environment.InstallEnvironment;
import org.eclipse.virgo.kernel.install.pipeline.stage.transform.Transformer;
import org.eclipse.virgo.util.common.GraphNode;
import org.eclipse.virgo.util.common.GraphNode.ExceptionThrowingDirectedAcyclicGraphVisitor;
import org.osgi.framework.Version;

/**
 * {@link PlanResolver} adds the immediate child nodes to a plan node.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is thread safe.
 * 
 */
public class PlanResolver implements Transformer {

    private static final String SCOPE_SEPARATOR = "-";

    private final InstallArtifactGraphInclosure installArtifactGraphInclosure;

    public PlanResolver(InstallArtifactGraphInclosure installArtifactGraphInclosure) {
        this.installArtifactGraphInclosure = installArtifactGraphInclosure;
    }

    /**
     * {@InheritDoc}
     */
    public void transform(GraphNode<InstallArtifact> installGraph, final InstallEnvironment installEnvironment) throws DeploymentException {
        installGraph.visit(new ExceptionThrowingDirectedAcyclicGraphVisitor<InstallArtifact, DeploymentException>() {

            public boolean visit(GraphNode<InstallArtifact> graph) throws DeploymentException {
                PlanResolver.this.operate(graph.getValue());
                return true;
            }
        });
    }

    private void operate(InstallArtifact installArtifact) throws DeploymentException {
        if (installArtifact instanceof PlanInstallArtifact) {
            PlanInstallArtifact planInstallArtifact = (PlanInstallArtifact) installArtifact;
            if (planInstallArtifact.getGraph().getChildren().isEmpty()) {
                String scopeName = getArtifactScopeName(planInstallArtifact);
                GraphNode<InstallArtifact> graph = planInstallArtifact.getGraph();
                List<ArtifactSpecification> artifactSpecifications = planInstallArtifact.getArtifactSpecifications();
                for (ArtifactSpecification artifactSpecification : artifactSpecifications) {
                    GraphNode<InstallArtifact> childInstallArtifactGraph = createInstallArtifactGraph(artifactSpecification, scopeName,
                        planInstallArtifact.getProvisioning());
                    graph.addChild(childInstallArtifactGraph);

                    // Put child into the INSTALLING state as Transformers (like this) are after the "begin install"
                    // pipeline stage.
                    InstallArtifact childInstallArtifact = childInstallArtifactGraph.getValue();
                    ((AbstractInstallArtifact) childInstallArtifact).beginInstall();
                }
            }
        }
    }

    /**
     * Returns the scope name of the given {@link InstallArtifact} or <code>null</code> if the given InstallArtifact
     * does not belong to a scope.
     * 
     * @param installArtifact the <code>InstallArtiface</code> whose scope name is required
     * @return the scope name or <code>null</code> if the given InstallArtifact does not belong to a scope
     */
    private String getArtifactScopeName(InstallArtifact installArtifact) {
        if (installArtifact instanceof PlanInstallArtifact) {
            PlanInstallArtifact planInstallArtifact = (PlanInstallArtifact) installArtifact;
            boolean scoped = planInstallArtifact.isScoped();
            if (scoped) {
                return planInstallArtifact.getName() + SCOPE_SEPARATOR + versionToShortString(planInstallArtifact.getVersion());
            }
        }
        return installArtifact.getScopeName();
    }

    private static String versionToShortString(Version version) {
        String result = version.toString();
        while (result.endsWith(".0")) {
            result = result.substring(0, result.length() - 2);
        }
        return result;
    }

    private GraphNode<InstallArtifact> createInstallArtifactGraph(ArtifactSpecification artifactSpecification, String scopeName,
        Provisioning parentProvisioning)
        throws DeploymentException {
        return this.installArtifactGraphInclosure.createInstallGraph(artifactSpecification, scopeName, parentProvisioning);
    }
}
