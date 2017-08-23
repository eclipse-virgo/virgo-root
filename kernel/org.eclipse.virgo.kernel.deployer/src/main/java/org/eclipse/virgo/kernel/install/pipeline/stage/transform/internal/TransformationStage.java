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

import java.util.List;

import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.install.environment.InstallEnvironment;
import org.eclipse.virgo.kernel.install.pipeline.stage.AbstractPipelineStage;
import org.eclipse.virgo.kernel.install.pipeline.stage.transform.Transformer;
import org.eclipse.virgo.kernel.osgi.framework.OsgiFrameworkUtils;
import org.eclipse.virgo.kernel.osgi.framework.OsgiServiceHolder;
import org.eclipse.virgo.kernel.osgi.framework.UnableToSatisfyBundleDependenciesException;
import org.eclipse.virgo.util.common.GraphNode;
import org.osgi.framework.BundleContext;

/**
 * A pipeline stage that drives {@link Transformer Transformers}. Transformers are retrieved from the OSGi service
 * registry and are driven in the order defined by their service ranking and service id.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread-safe.
 * 
 */
public final class TransformationStage extends AbstractPipelineStage {

    private final BundleContext bundleContext;

    public TransformationStage(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    protected void doProcessGraph(GraphNode<InstallArtifact> installGraph, InstallEnvironment installEnvironment) throws DeploymentException,
        UnableToSatisfyBundleDependenciesException {
        List<OsgiServiceHolder<Transformer>> services = OsgiFrameworkUtils.getServices(this.bundleContext, Transformer.class);
        for (OsgiServiceHolder<Transformer> transformerHolder : services) {
            transformerHolder.getService().transform(installGraph, installEnvironment);
            this.bundleContext.ungetService(transformerHolder.getServiceReference());
        }
    }
}
