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

package org.eclipse.virgo.kernel.install.pipeline.stage.transform.internal;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;

import org.eclipse.virgo.kernel.artifact.fs.ArtifactFS;
import org.eclipse.virgo.kernel.artifact.fs.ArtifactFSEntry;
import org.eclipse.virgo.kernel.install.artifact.ArtifactIdentity;
import org.eclipse.virgo.kernel.install.artifact.ArtifactIdentityDeterminer;
import org.eclipse.virgo.kernel.install.artifact.ArtifactStorage;
import org.eclipse.virgo.kernel.install.artifact.BundleInstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifactGraphFactory;
import org.eclipse.virgo.kernel.install.artifact.PlanInstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.internal.ArtifactStorageFactory;
import org.eclipse.virgo.kernel.install.artifact.internal.scoping.ScopeNameFactory;
import org.eclipse.virgo.kernel.install.environment.InstallEnvironment;
import org.eclipse.virgo.kernel.install.pipeline.stage.transform.Transformer;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.nano.deployer.api.core.FatalDeploymentException;
import org.eclipse.virgo.util.common.GraphNode;
import org.eclipse.virgo.util.io.IOUtils;
import org.eclipse.virgo.util.osgi.manifest.BundleManifest;
import org.eclipse.virgo.util.osgi.manifest.BundleManifestFactory;
import org.eclipse.virgo.util.osgi.manifest.ImportBundle;
import org.osgi.framework.Version;

/**
 * A {@link Transformer} implementation that examines the install graph and, for each scoped plan found within the graph,
 * adds a synthetic context bundle to the plan.
 * 
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread-safe.
 * 
 */
final class SyntheticContextBundleCreatingTransformer implements Transformer, ScopedPlanInstallArtifactProcessor {

    private static final int SYNTHETIC_BUNDLE_MANIFEST_VERSION = 2;

    private static final String SYNTHETIC_CONTEXT_SUFFIX = "-synthetic.context";

    private final InstallArtifactGraphFactory installArtifactGraphFactory;

    private final ArtifactStorageFactory artifactStorageFactory;

    SyntheticContextBundleCreatingTransformer(InstallArtifactGraphFactory installArtifactGraphFactory, ArtifactStorageFactory artifactStorageFactory) {
        this.installArtifactGraphFactory = installArtifactGraphFactory;
        this.artifactStorageFactory = artifactStorageFactory;
    }

    /**
     * {@inheritDoc}
     */
    public void transform(GraphNode<InstallArtifact> installGraph, InstallEnvironment installEnvironment) throws DeploymentException {
        installGraph.visit(new ScopedPlanIdentifyingDirectedAcyclicGraphVisitor(this));
    }

    /**
     * {@inheritDoc}
     * 
     */
    public void processScopedPlanInstallArtifact(GraphNode<InstallArtifact> graph) throws DeploymentException {
        if (!syntheticContextExists(graph)) {
            Set<BundleInstallArtifact> childBundles = getBundlesInScope(graph);

            PlanInstallArtifact planArtifact = (PlanInstallArtifact) graph.getValue();
            
            String scopeName = determineSyntheticContextScopeName(planArtifact);
            
            String name = scopeName + SYNTHETIC_CONTEXT_SUFFIX;
            Version version = planArtifact.getVersion();
            
            ArtifactIdentity identity = new ArtifactIdentity(ArtifactIdentityDeterminer.BUNDLE_TYPE, name, version, scopeName);
                        
            BundleManifest syntheticContextBundleManifest = createSyntheticContextBundleManifest(identity, childBundles);
            
            ArtifactStorage artifactStorage = this.artifactStorageFactory.createDirectoryStorage(identity, name + ".jar");
            writeSyntheticContextBundle(syntheticContextBundleManifest, artifactStorage.getArtifactFS());
            
            GraphNode<InstallArtifact> syntheticContextBundle = this.installArtifactGraphFactory.constructInstallArtifactGraph(
                identity, artifactStorage, null, null);
            graph.addChild(syntheticContextBundle);
        }
    }

    private boolean syntheticContextExists(GraphNode<InstallArtifact> plan) {
        PlanInstallArtifact planInstallArtifact = (PlanInstallArtifact) plan.getValue();
        String syntheticContextBundleSymbolicName = determineSyntheticContextScopeName(planInstallArtifact) + SYNTHETIC_CONTEXT_SUFFIX;
        List<GraphNode<InstallArtifact>> children = plan.getChildren();
        for (GraphNode<InstallArtifact> child : children) {
            if (syntheticContextBundleSymbolicName.equals(child.getValue().getName())) {
                return true;
            }
        }
        return false;
    }

    private Set<BundleInstallArtifact> getBundlesInScope(GraphNode<InstallArtifact> plan) {
        BundleInstallArtifactGatheringGraphVisitor visitor = new BundleInstallArtifactGatheringGraphVisitor();
        plan.visit(visitor);
        return visitor.getChildBundles();
    }

    private void writeSyntheticContextBundle(BundleManifest syntheticContextBundleManifest, ArtifactFS artifactFS) {                
        ArtifactFSEntry entry = artifactFS.getEntry(JarFile.MANIFEST_NAME);
        Writer manifestWriter = new OutputStreamWriter(entry.getOutputStream(), UTF_8);
        try {
            syntheticContextBundleManifest.write(manifestWriter);
        } catch (IOException ioe) {
            throw new FatalDeploymentException("Failed to write out synthetic context's manifest", ioe);
        } finally {
            IOUtils.closeQuietly(manifestWriter);
        }
    }

    private BundleManifest createSyntheticContextBundleManifest(ArtifactIdentity identity, Set<BundleInstallArtifact> childBundles) {
        BundleManifest bundleManifest = BundleManifestFactory.createBundleManifest();

        bundleManifest.setBundleVersion(identity.getVersion());
        bundleManifest.setBundleManifestVersion(SYNTHETIC_BUNDLE_MANIFEST_VERSION);

        bundleManifest.getBundleSymbolicName().setSymbolicName(identity.getName());

        bundleManifest.setModuleScope(identity.getScopeName());

        addImportForEachChildBundle(bundleManifest, childBundles);

        return bundleManifest;
    }

    private String determineSyntheticContextScopeName(PlanInstallArtifact plan) {
        return ScopeNameFactory.createScopeName(plan.getName(), plan.getVersion());        
    }

    private void addImportForEachChildBundle(BundleManifest bundleManifest, Set<BundleInstallArtifact> childBundles) {
        ImportBundle importBundle = bundleManifest.getImportBundle();

        for (BundleInstallArtifact bundle : childBundles) {
            String symbolicName = bundle.getName();
            importBundle.addImportedBundle(symbolicName);
        }
    }
}
