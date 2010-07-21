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

package org.eclipse.virgo.kernel.install.pipeline.stage.transform.internal;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;

import org.osgi.framework.Version;


import org.eclipse.virgo.kernel.artifact.fs.ArtifactFS;
import org.eclipse.virgo.kernel.artifact.fs.ArtifactFSEntry;
import org.eclipse.virgo.kernel.deployer.core.DeploymentException;
import org.eclipse.virgo.kernel.deployer.core.FatalDeploymentException;
import org.eclipse.virgo.kernel.deployer.core.internal.TreeUtils;
import org.eclipse.virgo.kernel.install.artifact.ArtifactIdentity;
import org.eclipse.virgo.kernel.install.artifact.ArtifactIdentityDeterminer;
import org.eclipse.virgo.kernel.install.artifact.ArtifactStorage;
import org.eclipse.virgo.kernel.install.artifact.BundleInstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifactTreeFactory;
import org.eclipse.virgo.kernel.install.artifact.PlanInstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.internal.ArtifactStorageFactory;
import org.eclipse.virgo.kernel.install.artifact.internal.scoping.ScopeNameFactory;
import org.eclipse.virgo.kernel.install.environment.InstallEnvironment;
import org.eclipse.virgo.kernel.install.pipeline.stage.transform.Transformer;
import org.eclipse.virgo.util.common.Tree;
import org.eclipse.virgo.util.io.IOUtils;
import org.eclipse.virgo.util.osgi.manifest.BundleManifest;
import org.eclipse.virgo.util.osgi.manifest.BundleManifestFactory;
import org.eclipse.virgo.util.osgi.manifest.ImportBundle;

/**
 * A {@link Transformer} implementation that examines the install tree and, for each scoped plan found within the tree,
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

    private final InstallArtifactTreeFactory installArtifactTreeFactory;

    private final ArtifactStorageFactory artifactStorageFactory;

    SyntheticContextBundleCreatingTransformer(InstallArtifactTreeFactory installArtifactTreeFactory, ArtifactStorageFactory artifactStorageFactory) {
        this.installArtifactTreeFactory = installArtifactTreeFactory;
        this.artifactStorageFactory = artifactStorageFactory;
    }

    /**
     * {@inheritDoc}
     */
    public void transform(Tree<InstallArtifact> installTree, InstallEnvironment installEnvironment) throws DeploymentException {
        installTree.visit(new ScopedPlanIdentifyingTreeVisitor(this));
    }

    /**
     * {@inheritDoc}
     * 
     */
    public void processScopedPlanInstallArtifact(Tree<InstallArtifact> plan) throws DeploymentException {
        if (!syntheticContextExists(plan)) {
            Set<BundleInstallArtifact> childBundles = getBundlesInScope(plan);

            PlanInstallArtifact planArtifact = (PlanInstallArtifact) plan.getValue();
            
            String scopeName = determineSyntheticContextScopeName(planArtifact);
            
            String name = scopeName + SYNTHETIC_CONTEXT_SUFFIX;
            Version version = planArtifact.getVersion();
            
            ArtifactIdentity identity = new ArtifactIdentity(ArtifactIdentityDeterminer.BUNDLE_TYPE, name, version, scopeName);
                        
            BundleManifest syntheticContextBundleManifest = createSyntheticContextBundleManifest(identity, childBundles);
            
            ArtifactStorage artifactStorage = this.artifactStorageFactory.createDirectoryStorage(identity, name + ".jar");
            writeSyntheticContextBundle(syntheticContextBundleManifest, artifactStorage.getArtifactFS());
            
            Tree<InstallArtifact> syntheticContextBundle = this.installArtifactTreeFactory.constructInstallArtifactTree(
                identity, artifactStorage, null, null);
            TreeUtils.addChild(plan, syntheticContextBundle);
        }
    }

    private boolean syntheticContextExists(Tree<InstallArtifact> plan) {
        PlanInstallArtifact planInstallArtifact = (PlanInstallArtifact) plan.getValue();
        String syntheticContextBundleSymbolicName = determineSyntheticContextScopeName(planInstallArtifact) + SYNTHETIC_CONTEXT_SUFFIX;
        List<Tree<InstallArtifact>> children = plan.getChildren();
        for (Tree<InstallArtifact> child : children) {
            if (syntheticContextBundleSymbolicName.equals(child.getValue().getName())) {
                return true;
            }
        }
        return false;
    }

    private Set<BundleInstallArtifact> getBundlesInScope(Tree<InstallArtifact> plan) {
        BundleInstallArtifactGatheringTreeVisitor visitor = new BundleInstallArtifactGatheringTreeVisitor();
        plan.visit(visitor);
        return visitor.getChildBundles();
    }

    private void writeSyntheticContextBundle(BundleManifest syntheticContextBundleManifest, ArtifactFS artifactFS) {                
        ArtifactFSEntry entry = artifactFS.getEntry(JarFile.MANIFEST_NAME);
        Writer manifestWriter = new OutputStreamWriter(entry.getOutputStream());
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
