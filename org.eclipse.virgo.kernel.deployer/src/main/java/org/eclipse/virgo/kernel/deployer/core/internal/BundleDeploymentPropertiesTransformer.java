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
import java.util.Map;

import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.kernel.install.artifact.BundleInstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.install.environment.InstallEnvironment;
import org.eclipse.virgo.kernel.install.pipeline.stage.transform.Transformer;
import org.eclipse.virgo.util.common.GraphNode;
import org.eclipse.virgo.util.common.GraphNode.ExceptionThrowingDirectedAcyclicGraphVisitor;
import org.eclipse.virgo.util.osgi.manifest.BundleManifest;

/**
 * {@link Transformer} implementation that converts deployment properties on bundle artifacts into the corresponding
 * manifest headers.
 * <p/>
 * Any deployment property with a key prefixed by <code>header:</code> will be treated a as bundle header.
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread-safe.
 * 
 */
final class BundleDeploymentPropertiesTransformer implements Transformer {

    private static final String HEADER_PREFIX = "header:";

    public void transform(GraphNode<InstallArtifact> installGraph, InstallEnvironment installEnvironment) throws DeploymentException {
        installGraph.visit(new Visitor());
    }

    private void doTransformBundleArtifact(BundleInstallArtifact value, Map<String, String> deploymentProperties) throws IOException {
        BundleManifest manifest = value.getBundleManifest();
        
        for(Map.Entry<String, String> entries : deploymentProperties.entrySet()) {
            String headerName = convertToHeaderName(entries.getKey());
            if(headerName != null) {
                manifest.setHeader(headerName, entries.getValue());
            }
        }
    }

    /**
     * Attempts to convert the supplied property key into a header name. If the key is not a valid header name (it
     * doesn't have the header: prefix) then <code>null</code> is returned.
     */
    private String convertToHeaderName(String propertyKey) {
        if(propertyKey.startsWith(HEADER_PREFIX)) {
            return propertyKey.substring(HEADER_PREFIX.length());
        } else {
            return null;
        }
    }

    private final class Visitor implements ExceptionThrowingDirectedAcyclicGraphVisitor<InstallArtifact, DeploymentException> {

        public boolean visit(GraphNode<InstallArtifact> graph) throws DeploymentException {
            InstallArtifact value = graph.getValue();
            if (value instanceof BundleInstallArtifact) {
                Map<String, String> deploymentProperties = ((BundleInstallArtifact)value).getDeploymentProperties();
                if (deploymentProperties != null && !deploymentProperties.isEmpty()) {
                    try {
                        doTransformBundleArtifact((BundleInstallArtifact) value, deploymentProperties);
                    } catch (Exception e) {
                        throw new DeploymentException("Unable to apply deployment for artifact '" + value.getName() + "'", e);
                    }
                }
            }
            return true;
        }

    }

}
