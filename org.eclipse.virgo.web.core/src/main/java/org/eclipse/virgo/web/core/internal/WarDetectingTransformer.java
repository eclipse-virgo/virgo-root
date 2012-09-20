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
import org.osgi.framework.Constants;

/**
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread-safe.
 * 
 */
final class WarDetectingTransformer implements Transformer {

    private static final String WAR_EXTENSION = ".war";
    
    private static final String DEFAULTED_BSN = "org-eclipse-virgo-kernel-DefaultedBSN";

    private static final String HEADER_DEFAULT_WAB_HEADERS = "org-eclipse-gemini-web-DefaultWABHeaders";

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
                    if (!isWebApplicationBundle(bundleManifest)) {
                        bundleManifest.setHeader(HEADER_DEFAULT_WAB_HEADERS, "true");
                    }
                } catch (IOException _) {
                    // ignore
                }
            }
        }
    }

    private boolean hasWarSuffix(InstallArtifact installArtifact) {
        return installArtifact.getArtifactFS().getFile().getName().toLowerCase(Locale.ENGLISH).endsWith(WAR_EXTENSION);
    }

    // Following methods temporarily copied from WebContainerUtils
    /**
     * Determines whether the given manifest represents a web application bundle. According to the R4.2 Enterprise
     * Specification, this is true if and only if the manifest contains any of the headers in Table 128.3:
     * Bundle-SymbolicName, Bundle-Version, Bundle-ManifestVersion, Import-Package, Web-ContextPath. Note: there is no
     * need to validate the manifest as if it is invalid it will cause an error later.
     * 
     * @param manifest the bundle manifest
     * @return <code>true</code> if and only if the given manifest represents a web application bundle
     */
    public static boolean isWebApplicationBundle(BundleManifest manifest) {
        return specifiesBundleSymbolicName(manifest) || specifiesBundleVersion(manifest) || specifiesBundleManifestVersion(manifest)
            || specifiesImportPackage(manifest) || specifiesWebContextPath(manifest);
    }

    private static boolean specifiesBundleSymbolicName(BundleManifest manifest) {
        if (manifest.getHeader(DEFAULTED_BSN) != null) {
            return false;
        }
        return  manifest.getBundleSymbolicName().getSymbolicName() != null;
    }

    private static boolean specifiesBundleVersion(BundleManifest manifest) {
        return manifest.getHeader(Constants.BUNDLE_VERSION) != null;
    }

    private static boolean specifiesBundleManifestVersion(BundleManifest manifest) {
        return manifest.getBundleManifestVersion() != 1;
    }

    private static boolean specifiesImportPackage(BundleManifest manifest) {
        return !manifest.getImportPackage().getImportedPackages().isEmpty();
    }

    private static boolean specifiesWebContextPath(BundleManifest manifest) {
        return manifest.getHeader("Web-ContextPath") != null;
    }
}
