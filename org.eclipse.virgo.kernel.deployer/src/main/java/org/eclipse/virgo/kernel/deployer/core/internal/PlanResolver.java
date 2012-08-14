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

import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.virgo.kernel.artifact.ArtifactSpecification;
import org.eclipse.virgo.kernel.artifact.plan.PlanDescriptor.Provisioning;
import org.eclipse.virgo.nano.deployer.api.core.DeployerLogEvents;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.kernel.deployer.model.GCRoots;
import org.eclipse.virgo.kernel.install.artifact.ArtifactIdentity;
import org.eclipse.virgo.kernel.install.artifact.ArtifactIdentityDeterminer;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifactGraphInclosure;
import org.eclipse.virgo.kernel.install.artifact.PlanInstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.internal.AbstractInstallArtifact;
import org.eclipse.virgo.kernel.install.environment.InstallEnvironment;
import org.eclipse.virgo.kernel.install.pipeline.stage.transform.Transformer;
import org.eclipse.virgo.nano.serviceability.NonNull;
import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.virgo.repository.Repository;
import org.eclipse.virgo.repository.RepositoryAwareArtifactDescriptor;
import org.eclipse.virgo.util.common.GraphNode;
import org.eclipse.virgo.util.common.GraphNode.ExceptionThrowingDirectedAcyclicGraphVisitor;
import org.eclipse.virgo.util.osgi.manifest.VersionRange;
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

    private static final String PROVISIONING_PROPERTY_NAME = "org.eclipse.virgo.kernel.provisioning";

    private static final String SCOPE_SEPARATOR = "-";

    private final InstallArtifactGraphInclosure installArtifactGraphInclosure;

    private final GCRoots gcRoots;

    private final Repository repository;

    private final ArtifactIdentityDeterminer artifactIdentityDeterminer;

    private final EventLogger eventLogger;

    public PlanResolver(@NonNull InstallArtifactGraphInclosure installArtifactGraphInclosure, @NonNull GCRoots gcRoots,
        @NonNull Repository repository, @NonNull ArtifactIdentityDeterminer artifactIdentityDeterminer, @NonNull EventLogger eventLogger) {
        this.installArtifactGraphInclosure = installArtifactGraphInclosure;
        this.gcRoots = gcRoots;
        this.repository = repository;
        this.artifactIdentityDeterminer = artifactIdentityDeterminer;
        this.eventLogger = eventLogger;
    }

    /**
     * {@InheritDoc}
     */
    @Override
    public void transform(GraphNode<InstallArtifact> installGraph, final InstallEnvironment installEnvironment) throws DeploymentException {
        installGraph.visit(new ExceptionThrowingDirectedAcyclicGraphVisitor<InstallArtifact, DeploymentException>() {

            @Override
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
                try {
                    String scopeName = getArtifactScopeName(planInstallArtifact);
                    GraphNode<InstallArtifact> graph = planInstallArtifact.getGraph();
                    List<ArtifactSpecification> artifactSpecifications = planInstallArtifact.getArtifactSpecifications();
                    for (ArtifactSpecification artifactSpecification : artifactSpecifications) {
                        GraphNode<InstallArtifact> childInstallNode = obtainInstallArtifactGraph(artifactSpecification, scopeName,
                            planInstallArtifact.getProvisioning());

                        boolean newNode = childInstallNode.getParents().isEmpty()
                            && !(((AbstractInstallArtifact) childInstallNode.getValue()).getTopLevelDeployed());
                        graph.addChild(childInstallNode);
                        if (newNode) {
                            // Put child into the INSTALLING state as Transformers (like this) are after the
                            // "begin install"
                            // pipeline stage.
                            InstallArtifact childInstallArtifact = childInstallNode.getValue();
                            ((AbstractInstallArtifact) childInstallArtifact).beginInstall();
                        }
                    }
                } catch (DeploymentException de) {
                    throw new DeploymentException("Deployment of " + planInstallArtifact + " failed: " + de.getMessage(), de);
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

    private GraphNode<InstallArtifact> obtainInstallArtifactGraph(ArtifactSpecification artifactSpecification, String scopeName,
        Provisioning parentProvisioning) throws DeploymentException {
        GraphNode<InstallArtifact> sharedNode = null;
        ArtifactIdentity identity = null;
        File artifact = null;
        Map<String, String> properties = determineDeploymentProperties(artifactSpecification.getProperties(), parentProvisioning);
        String repositoryName = null;
        URI uri = artifactSpecification.getUri();
        if (uri == null) {
            RepositoryAwareArtifactDescriptor repositoryAwareArtifactDescriptor = lookup(artifactSpecification);
            if (repositoryAwareArtifactDescriptor == null) {
                String type = artifactSpecification.getType();
                String name = artifactSpecification.getName();
                VersionRange versionRange = artifactSpecification.getVersionRange();
                sharedNode = findSharedNode(type, name, versionRange, null);
                if (sharedNode == null) {
                    this.eventLogger.log(DeployerLogEvents.ARTIFACT_NOT_FOUND, type, name, versionRange, this.repository.getName());
                    throw new DeploymentException(type + " '" + name + "' in version range '" + versionRange + "' not found");
                }
            } else {
                URI artifactUri = repositoryAwareArtifactDescriptor.getUri();

                artifact = new File(artifactUri);
                identity = new ArtifactIdentity(repositoryAwareArtifactDescriptor.getType(), repositoryAwareArtifactDescriptor.getName(),
                    repositoryAwareArtifactDescriptor.getVersion(), scopeName);
                repositoryName = repositoryAwareArtifactDescriptor.getRepositoryName();
                sharedNode = findSharedNode(identity);
            }

        } else {
            try {
                artifact = new File(uri);
            } catch (IllegalArgumentException e) {
                throw new DeploymentException("Invalid artifact specification URI '" + uri.toString() + "'", e);
            }
            identity = determineIdentity(uri, scopeName);
            sharedNode = findSharedNode(identity);
        }
        return sharedNode == null ? this.installArtifactGraphInclosure.constructGraphNode(identity, artifact, properties, repositoryName)
            : sharedNode;
    }

    private Map<String, String> determineDeploymentProperties(Map<String, String> properties, Provisioning parentProvisioning) {
        Map<String, String> deploymentProperties = new HashMap<String, String>(properties);
        deploymentProperties.put(PROVISIONING_PROPERTY_NAME, parentProvisioning.toString());
        return deploymentProperties;
    }

    private RepositoryAwareArtifactDescriptor lookup(ArtifactSpecification specification) throws DeploymentException {
        String type = specification.getType();
        String name = specification.getName();
        VersionRange versionRange = specification.getVersionRange();

        return this.repository.get(type, name, versionRange);
    }

    private ArtifactIdentity determineIdentity(URI artifactUri, String scopeName) throws DeploymentException {
        try {
            File artifact = new File(artifactUri);
            if (!artifact.exists()) {
                throw new DeploymentException(artifact + " does not exist");
            }

            return determineIdentity(artifact, scopeName);
        } catch (Exception e) {
            throw new DeploymentException(e.getMessage() + ": uri='" + artifactUri + "'", e);
        }
    }

    private ArtifactIdentity determineIdentity(File file, String scopeName) throws DeploymentException {
        ArtifactIdentity artifactIdentity = this.artifactIdentityDeterminer.determineIdentity(file, scopeName);

        if (artifactIdentity == null) {
            this.eventLogger.log(DeployerLogEvents.INDETERMINATE_ARTIFACT_TYPE, file);
            throw new DeploymentException("Cannot determine the artifact identity of the file '" + file + "'");
        }

        return artifactIdentity;
    }

    private GraphNode<InstallArtifact> findSharedNode(ArtifactIdentity artifactIdentity) {
        return ExistingNodeLocator.findSharedNode(this.gcRoots, artifactIdentity);
    }

    public GraphNode<InstallArtifact> findSharedNode(String type, String name, VersionRange versionRange, String scopeName) {
        return ExistingNodeLocator.findSharedNode(this.gcRoots, type, name, versionRange, scopeName);
    }

}
