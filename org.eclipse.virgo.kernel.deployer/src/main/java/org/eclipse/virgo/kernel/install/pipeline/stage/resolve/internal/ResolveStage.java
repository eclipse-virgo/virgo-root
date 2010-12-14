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

package org.eclipse.virgo.kernel.install.pipeline.stage.resolve.internal;

import java.util.ArrayList;
import java.util.List;

import org.osgi.framework.Bundle;
import org.osgi.service.packageadmin.PackageAdmin;

import org.eclipse.virgo.kernel.osgi.framework.UnableToSatisfyBundleDependenciesException;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiFramework;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiFrameworkFactory;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiResolutionFailure;

import org.eclipse.virgo.kernel.deployer.core.DeploymentException;
import org.eclipse.virgo.kernel.install.artifact.BundleInstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.install.environment.InstallEnvironment;
import org.eclipse.virgo.kernel.install.pipeline.stage.PipelineStage;
import org.eclipse.virgo.kernel.serviceability.NonNull;
import org.eclipse.virgo.util.common.Tree;
import org.eclipse.virgo.util.common.Tree.TreeVisitor;

/**
 * {@link ResolveStage} is a {@link PipelineStage} which resolves the bundles committed from an install tree in the OSGi
 * framework.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is thread safe.
 * 
 */
@SuppressWarnings("deprecation")
public final class ResolveStage implements PipelineStage {

    private final PackageAdmin packageAdmin;

    private final QuasiFrameworkFactory quasiFrameworkFactory;

    public ResolveStage(@NonNull PackageAdmin packageAdmin, @NonNull QuasiFrameworkFactory quasiFrameworkFactory) {
        this.packageAdmin = packageAdmin;
        this.quasiFrameworkFactory = quasiFrameworkFactory;
    }

    /**
     * {@inheritDoc}
     */
    public void process(Tree<InstallArtifact> installTree, InstallEnvironment installEnvironment) throws DeploymentException,
        UnableToSatisfyBundleDependenciesException {
        BundleFinderVisitor visitor = new BundleFinderVisitor();
        installTree.visit(visitor);
        Bundle[] bundles = visitor.getBundles();
        boolean resolved = this.packageAdmin.resolveBundles(bundles);
        if (!resolved) {
            diagnoseResolutionFailure(bundles);
        }
    }

    private static class BundleFinderVisitor implements TreeVisitor<InstallArtifact> {

        private final List<Bundle> bundles = new ArrayList<Bundle>();

        public boolean visit(Tree<InstallArtifact> tree) {
            InstallArtifact installArtifact = tree.getValue();
            if (installArtifact instanceof BundleInstallArtifact) {
                BundleInstallArtifact bundleInstallArtifact = (BundleInstallArtifact) installArtifact;
                Bundle bundle = bundleInstallArtifact.getBundle();
                this.bundles.add(bundle);
            }
            return true;
        }

        public Bundle[] getBundles() {
            return this.bundles.toArray(new Bundle[0]);
        }

    }

    private void diagnoseResolutionFailure(Bundle[] bundles) throws UnableToSatisfyBundleDependenciesException {
        QuasiFramework quasiFramework = this.quasiFrameworkFactory.create();
        for (Bundle bundle : bundles) {
            if (bundle.getState() == Bundle.INSTALLED) {
                List<QuasiResolutionFailure> resolutionFailures = quasiFramework.diagnose(bundle.getBundleId());
                if (!resolutionFailures.isEmpty()) {
                    QuasiResolutionFailure failure = resolutionFailures.get(0);
                    throw new UnableToSatisfyBundleDependenciesException(failure.getUnresolvedQuasiBundle().getSymbolicName(),
                        failure.getUnresolvedQuasiBundle().getVersion(), failure.getDescription());
                }
            }
        }
    }
}
