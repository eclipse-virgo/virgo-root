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
import java.net.URL;

import org.eclipse.virgo.nano.deployer.api.core.DeployerLogEvents;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.kernel.install.artifact.BundleInstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.install.environment.InstallEnvironment;
import org.eclipse.virgo.kernel.install.pipeline.stage.transform.Transformer;
import org.eclipse.virgo.util.common.GraphNode;
import org.eclipse.virgo.util.common.GraphNode.ExceptionThrowingDirectedAcyclicGraphVisitor;
import org.eclipse.virgo.util.osgi.manifest.BundleManifest;

/**
 * {@link ManifestUpgrader} upgrades OSGi R3 manifests to be R4.1 compliant as this the minimum required to enable
 * scoping.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is thread safe.
 * 
 */
public class ManifestUpgrader implements Transformer {

    private static final int BUNDLE_MANIFEST_VERSION_FOR_OSGI_R4 = 2;

    /**
     * {@inheritDoc}
     */
    public void transform(GraphNode<InstallArtifact> installGraph, final InstallEnvironment installEnvironment) throws DeploymentException {
        installGraph.visit(new ExceptionThrowingDirectedAcyclicGraphVisitor<InstallArtifact, DeploymentException>() {

            public boolean visit(GraphNode<InstallArtifact> graph) throws DeploymentException {
                operate(graph.getValue(), installEnvironment);
                return true;
            }
        });
    }

    void operate(InstallArtifact installArtifact, InstallEnvironment installEnvironment) throws DeploymentException {
        if (installArtifact instanceof BundleInstallArtifact) {
            BundleManifest bundleManifest = getBundleManifest(installArtifact, installEnvironment);
            upgradeManifestIfNecessary(bundleManifest, installArtifact, installEnvironment);
            validateManifest(bundleManifest, installArtifact, installEnvironment);
            removeBundleUpdateLocation(installArtifact, installEnvironment, bundleManifest);
        }
    }

    private BundleManifest getBundleManifest(InstallArtifact installArtifact, InstallEnvironment installEnvironment) throws DeploymentException {
        try {
            return ((BundleInstallArtifact) installArtifact).getBundleManifest();
        } catch (IOException e) {
            installEnvironment.getInstallLog().log(DeployerLogEvents.BUNDLE_MANIFEST_NOT_FOUND, e, installArtifact.getName(), installArtifact.getVersion());
            throw new DeploymentException("I/O error accessing bundle manifest for " + installArtifact, e);
        }
    }

    /**
     * Upgrade the bundle manifest version to OSGi R4 if it is older. Do not issue a log message to avoid flooding the
     * log.
     * 
     * This upgrade is necessary to support application scoping.
     * 
     * @param bundleManifest The manifest to upgrade
     * @throws DeploymentException
     */
    private void upgradeManifestIfNecessary(BundleManifest bundleManifest, InstallArtifact installArtifact, InstallEnvironment installEnvironment) {
        if (bundleManifest.getBundleManifestVersion() < BUNDLE_MANIFEST_VERSION_FOR_OSGI_R4) {
            installEnvironment.getInstallLog().log(this, "OSGi R3 or earlier manifest detected:\n'%s'\nfor %s - upgrading manifest to OSGi R4.",
                bundleManifest.toString(), installArtifact.toString());

            bundleManifest.setBundleManifestVersion(BUNDLE_MANIFEST_VERSION_FOR_OSGI_R4);

            if (bundleManifest.getBundleSymbolicName().getSymbolicName() == null) {
                bundleManifest.getBundleSymbolicName().setSymbolicName(installArtifact.getName());
            }
        }
    }

    private void validateManifest(BundleManifest bundleManifest, InstallArtifact installArtifact, InstallEnvironment installEnvironment)
        throws DeploymentException {
        if (bundleManifest.getBundleSymbolicName().getSymbolicName() == null) {
            installEnvironment.getInstallLog().log(DeployerLogEvents.MISSING_BUNDLE_SYMBOLIC_NAME, installArtifact.getName(),
                installArtifact.getVersion(), bundleManifest);
            throw new DeploymentException("OSGi R4 manifest with no bundle symbolic name detected:\n" + bundleManifest + "'\nfor module '" + this
                + "'.");
        }
    }

    /**
     * Remove any bundle update location header to avoid it interfering with refresh.
     */
    private void removeBundleUpdateLocation(InstallArtifact installArtifact, InstallEnvironment installEnvironment, BundleManifest bundleManifest) {
        URL bundleUpdateLocation = bundleManifest.getBundleUpdateLocation();

        if (bundleUpdateLocation != null) {
            installEnvironment.getInstallLog().log(DeployerLogEvents.DISCARDING_BUNDLE_UPDATE_LOCATION, bundleUpdateLocation,
                installArtifact.getName(), installArtifact.getVersion().toString());
            bundleManifest.setBundleUpdateLocation(null);
        }
    }

}
