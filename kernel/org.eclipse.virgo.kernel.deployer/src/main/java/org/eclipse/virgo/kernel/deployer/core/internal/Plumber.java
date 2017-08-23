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

package org.eclipse.virgo.kernel.deployer.core.internal;

import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.internal.AbstractInstallArtifact;
import org.eclipse.virgo.kernel.install.environment.InstallEnvironment;
import org.eclipse.virgo.kernel.install.pipeline.Pipeline;
import org.eclipse.virgo.kernel.install.pipeline.PipelineFactory;
import org.eclipse.virgo.kernel.install.pipeline.stage.resolve.internal.CommitStage;
import org.eclipse.virgo.kernel.install.pipeline.stage.resolve.internal.QuasiInstallStage;
import org.eclipse.virgo.kernel.install.pipeline.stage.resolve.internal.QuasiResolveStage;
import org.eclipse.virgo.kernel.install.pipeline.stage.resolve.internal.ResolveStage;
import org.eclipse.virgo.kernel.install.pipeline.stage.transform.internal.TransformationStage;
import org.eclipse.virgo.kernel.install.pipeline.stage.visit.Visitor;
import org.eclipse.virgo.kernel.install.pipeline.stage.visit.internal.VisitationStage;
import org.eclipse.virgo.nano.serviceability.NonNull;
import org.osgi.framework.BundleContext;
import org.osgi.service.packageadmin.PackageAdmin;

import org.eclipse.virgo.kernel.osgi.quasi.QuasiFrameworkFactory;

/**
 * {@link Plumber} plumbs together pipeline stages for use in the {@link PipelinedApplicationDeployer}.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is thread safe.
 * 
 */
@SuppressWarnings("deprecation")
final class Plumber {

    private final PackageAdmin packageAdmin;

    private final BundleContext bundleContext;

    private final PipelineFactory pipelineFactory;

    private final Pipeline pipeline;

    private final Pipeline refreshSubpipeline;

    private final QuasiFrameworkFactory quasiFrameworkFactory;

    public Plumber(@NonNull PackageAdmin packageAdmin, @NonNull BundleContext bundleContext, @NonNull PipelineFactory pipelineFactory,
        @NonNull QuasiFrameworkFactory quasiFrameworkFactory) {
        this.packageAdmin = packageAdmin;
        this.bundleContext = bundleContext;
        this.pipelineFactory = pipelineFactory;
        this.quasiFrameworkFactory = quasiFrameworkFactory;
        this.refreshSubpipeline = this.pipelineFactory.create();
        this.pipeline = this.pipelineFactory.create();
        initialisePipelines();
    }

    public Pipeline getMainPipeline() {
        return this.pipeline;
    }

    public Pipeline getRefreshSubpipeline() {
        return this.refreshSubpipeline;
    }

    private void initialisePipelines() {
        // new ManifestUpgrader(), new ImportExpander(this.bundleInstaller), new
        // PlanResolver(this.installArtifactTreeInclosure));

        TransformationStage transformationStage = new TransformationStage(this.bundleContext);

        plumbRefreshPipeline(transformationStage);

        plumbMainPipeline(transformationStage);
    }

    /**
     * Build the main pipeline from normalization, install, and resolve stages using the given normalization and
     * transformation stages.
     */
    private void plumbMainPipeline(TransformationStage transformationStage) {
        plumbMainPipelineInstallStages(transformationStage);
        plumbMainPipelineResolveStages();
    }

    private void plumbMainPipelineInstallStages(TransformationStage transformationStage) {
        VisitationStage beginInstallStage = new VisitationStage(new Visitor() {

            public void operate(InstallArtifact installArtifact, InstallEnvironment installEnvironment) throws DeploymentException {
                ((AbstractInstallArtifact) installArtifact).beginInstall();

            }
        });

        VisitationStage endInstallStage = new VisitationStage(new Visitor() {

            public void operate(InstallArtifact installArtifact, InstallEnvironment installEnvironment) throws DeploymentException {
                ((AbstractInstallArtifact) installArtifact).endInstall();

            }
        }, false);

        VisitationStage failInstallStage = new VisitationStage(new Visitor() {

            public void operate(InstallArtifact installArtifact, InstallEnvironment installEnvironment) throws DeploymentException {
                ((AbstractInstallArtifact) installArtifact).failInstall();

            }
        }, false);

        Pipeline installStages = this.pipelineFactory.createCompensatingPipeline(failInstallStage);

        installStages.appendStage(transformationStage).appendStage(new QuasiInstallStage()).appendStage(new QuasiResolveStage()).appendStage(
            new CommitStage());

        this.pipeline.appendStage(beginInstallStage).appendStage(installStages).appendStage(endInstallStage);
    }

    private void plumbMainPipelineResolveStages() {
        VisitationStage beginResolveStage = new VisitationStage(new Visitor() {

            public void operate(InstallArtifact installArtifact, InstallEnvironment installEnvironment) throws DeploymentException {
                ((AbstractInstallArtifact) installArtifact).beginResolve();

            }
        });

        VisitationStage endResolveStage = new VisitationStage(new Visitor() {

            public void operate(InstallArtifact installArtifact, InstallEnvironment installEnvironment) throws DeploymentException {
                ((AbstractInstallArtifact) installArtifact).endResolve();

            }
        });

        VisitationStage failResolveStage = new VisitationStage(new Visitor() {

            public void operate(InstallArtifact installArtifact, InstallEnvironment installEnvironment) throws DeploymentException {
                ((AbstractInstallArtifact) installArtifact).failResolve();

            }
        });

        Pipeline resolveStages = this.pipelineFactory.createCompensatingPipeline(failResolveStage);

        resolveStages.appendStage(new ResolveStage(this.packageAdmin, this.quasiFrameworkFactory));

        this.pipeline.appendStage(beginResolveStage).appendStage(resolveStages).appendStage(endResolveStage);
    }

    private void plumbRefreshPipeline(TransformationStage transformationStage) {
        this.refreshSubpipeline.appendStage(transformationStage);
    }

}
