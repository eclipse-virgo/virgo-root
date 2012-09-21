/*******************************************************************************
 * Copyright (c) 2012 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.web.core.internal;

import org.eclipse.virgo.util.osgi.manifest.BundleManifest;
import org.osgi.framework.Constants;

/**
 * {@link WebContainerUtils} provides common utilities for the web core bundle.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * Thread safe.
 */
final class WebContainerUtils {
    
    private static final String WAR_HEADER = "org-eclipse-virgo-web-war-detected";
    
    private static final String DEFAULTED_BSN = "org-eclipse-virgo-kernel-DefaultedBSN";

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
        return !isWar(manifest) && (specifiesBundleSymbolicName(manifest) || specifiesBundleVersion(manifest) || specifiesBundleManifestVersion(manifest)
            || specifiesImportPackage(manifest) || specifiesWebContextPath(manifest));
    }
    
    private static boolean isWar(BundleManifest manifest) {
        return manifest.getHeader(WAR_HEADER) != null;
    }

    private static boolean specifiesBundleSymbolicName(BundleManifest manifest) {
        if (manifest.getHeader(DEFAULTED_BSN) != null) {
            return false;
        }
        return manifest.getBundleSymbolicName().getSymbolicName() != null;
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
