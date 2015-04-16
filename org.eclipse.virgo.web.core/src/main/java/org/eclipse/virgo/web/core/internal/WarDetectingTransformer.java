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

package org.eclipse.virgo.web.core.internal;

import java.io.IOException;
import java.util.Locale;

import org.eclipse.virgo.kernel.install.artifact.BundleInstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.install.environment.InstallEnvironment;
import org.eclipse.virgo.kernel.install.pipeline.stage.transform.Transformer;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.util.common.GraphNode;
import org.eclipse.virgo.util.common.GraphNode.ExceptionThrowingDirectedAcyclicGraphVisitor;
import org.eclipse.virgo.util.osgi.manifest.BundleManifest;

/**
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread-safe.
 * 
 */
final class WarDetectingTransformer implements Transformer {

    private static final String WAR_EXTENSION = ".war";
    
    private static final String WAR_HEADER = "org-eclipse-virgo-web-war-detected";

    /**
     * {@inheritDoc}
     */
    public void transform(GraphNode<InstallArtifact> installGraph, InstallEnvironment installEnvironment) throws DeploymentException {
        installGraph.visit(new ExceptionThrowingDirectedAcyclicGraphVisitor<InstallArtifact, DeploymentException>() {

            public boolean visit(GraphNode<InstallArtifact> node) throws DeploymentException {
                detectWar(node.getValue());
                return true;
            }
        });
    }

    private void detectWar(InstallArtifact installArtifact) throws DeploymentException {
        if (installArtifact instanceof BundleInstallArtifact) {
            if (hasWarSuffix(installArtifact)) {
                BundleInstallArtifact bundleInstallArtifact = (BundleInstallArtifact) installArtifact;
                try {
                    BundleManifest bundleManifest = bundleInstallArtifact.getBundleManifest();
                    if (!WebContainerUtils.isWebApplicationBundle(bundleManifest)) {
                        bundleManifest.setHeader(WAR_HEADER, "true");
                    }
                } catch (IOException ignored) {
                }
            }
        }
    }

    private boolean hasWarSuffix(InstallArtifact installArtifact) {
        return installArtifact.getArtifactFS().getFile().getName().toLowerCase(Locale.ENGLISH).endsWith(WAR_EXTENSION);
    }

}
