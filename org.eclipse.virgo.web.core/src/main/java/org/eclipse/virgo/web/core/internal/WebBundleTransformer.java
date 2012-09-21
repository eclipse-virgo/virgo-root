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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Locale;

import org.eclipse.gemini.web.core.InstallationOptions;
import org.eclipse.virgo.kernel.install.artifact.BundleInstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.install.environment.InstallEnvironment;
import org.eclipse.virgo.kernel.install.pipeline.stage.transform.Transformer;
import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.util.common.GraphNode;
import org.eclipse.virgo.util.common.GraphNode.ExceptionThrowingDirectedAcyclicGraphVisitor;
import org.eclipse.virgo.util.osgi.manifest.BundleManifest;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread-safe.
 * 
 */
final class WebBundleTransformer implements Transformer {
    
    private static final String WAR_HEADER = "org-eclipse-virgo-web-war-detected";
    
    private static final String WAR_EXTENSION = ".war";

    private static final String HEADER_DEFAULT_WAB_HEADERS = "org-eclipse-gemini-web-DefaultWABHeaders";

    private static final String WEB_CONFIGURATION_PID = "org.eclipse.virgo.web";

    private static final String PROPERTY_WAB_HEADERS = "WABHeaders";

    private static final String PROPERTY_VALUE_WAB_HEADERS_STRICT = "strict";

    private static final String PROPERTY_VALUE_WAB_HEADERS_DEFAULTED = "defaulted";
    
    private static final String DEFAULT_CONTEXT_PATH = "/";
    
    private static final String ROOT_WAR_NAME = "ROOT";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final WebDeploymentEnvironment environment;

    private final boolean strictWABHeaders;

    static final String WEB_BUNDLE_MODULE_TYPE = "web-bundle";

    static final String MANIFEST_HEADER_MODULE_TYPE = "Module-Type";

    private static final String MANIFEST_HEADER_WEB_CONTEXT_PATH = "Web-ContextPath";

    WebBundleTransformer(WebDeploymentEnvironment environment) {
        this.environment = environment;
        this.strictWABHeaders = getStrictWABHeadersConfiguration(environment.getConfigAdmin(), this.logger, environment.getEventLogger());
    }

    private static boolean getStrictWABHeadersConfiguration(ConfigurationAdmin configAdmin, Logger logger, EventLogger eventLogger) {
        boolean strictWABHeaders = true;

        try {
            Configuration config = configAdmin.getConfiguration(WEB_CONFIGURATION_PID, null);
            if (config != null) {
                Dictionary<String, Object> properties = config.getProperties();
                if (properties != null) {
                    String wabHeadersPropertyValue = null;
                    if (properties.get(PROPERTY_WAB_HEADERS) != null) {
                       wabHeadersPropertyValue = properties.get(PROPERTY_WAB_HEADERS).toString();
                    }
                    if (wabHeadersPropertyValue != null) {
                        if (PROPERTY_VALUE_WAB_HEADERS_DEFAULTED.equals(wabHeadersPropertyValue)) {
                            strictWABHeaders = false;
                            eventLogger.log(WebLogEvents.DEFAULTING_WAB_HEADERS);
                        } else if (!PROPERTY_VALUE_WAB_HEADERS_STRICT.equals(wabHeadersPropertyValue)) {
                            logger.error("Property '%s' in configuration '%s' has invalid value '%s'", new String[] { PROPERTY_WAB_HEADERS,
                                WEB_CONFIGURATION_PID, wabHeadersPropertyValue });
                        }
                    }
                }
            }
        } catch (IOException _) {
            // ignore
        }

        return strictWABHeaders;
    }

    /**
     * {@inheritDoc}
     */
    public void transform(GraphNode<InstallArtifact> installGraph, InstallEnvironment installEnvironment) throws DeploymentException {
        installGraph.visit(new ExceptionThrowingDirectedAcyclicGraphVisitor<InstallArtifact, DeploymentException>() {

            public boolean visit(GraphNode<InstallArtifact> node) throws DeploymentException {
                InstallArtifact installArtifact = node.getValue();
                if (checkWebBundle(installArtifact)) {
                    applyWebContainerTransformations((BundleInstallArtifact) installArtifact);
                }

                return true;
            }
        });
    }

    private boolean checkWebBundle(InstallArtifact installArtifact) throws DeploymentException {
        if (installArtifact instanceof BundleInstallArtifact) {
            BundleInstallArtifact bundleInstallArtifact = (BundleInstallArtifact) installArtifact;
            if (hasWebContextPath(bundleInstallArtifact)) {
                return true;
            } else if (isWar(bundleInstallArtifact)) {
                setDefaultWebContextPath(bundleInstallArtifact);
                return true;
            } else if (!this.strictWABHeaders && hasWarSuffix(installArtifact)) {
                setDefaultWebContextPath(bundleInstallArtifact);
                return true;
            }
        }
        return false;
    }

    private boolean isWar(BundleInstallArtifact installArtifact) {
        try {
            return installArtifact.getBundleManifest().getHeader(WAR_HEADER) != null;
        } catch (IOException e_) {
            return false;
        }
    }
    
    private boolean hasWarSuffix(InstallArtifact installArtifact) {
        return installArtifact.getArtifactFS().getFile().getName().toLowerCase(Locale.ENGLISH).endsWith(WAR_EXTENSION);
    }

    private boolean hasWebContextPath(BundleInstallArtifact installArtifact) throws DeploymentException {
        try {
            return ((BundleInstallArtifact) installArtifact).getBundleManifest().getHeader(MANIFEST_HEADER_WEB_CONTEXT_PATH) != null;
        } catch (IOException ioe) {
            throw new DeploymentException("Could not retrieve manifest for bundle install artifact " + installArtifact, ioe);
        }
    }

    private void setDefaultWebContextPath(BundleInstallArtifact bundleInstallArtifact) throws DeploymentException {
        String webContextPath = getBaseName(bundleInstallArtifact.getArtifactFS().getFile().getPath());
        try {
        	if (webContextPath.equals(ROOT_WAR_NAME)) {
        		bundleInstallArtifact.getBundleManifest().setHeader(MANIFEST_HEADER_WEB_CONTEXT_PATH, DEFAULT_CONTEXT_PATH);
        	} else {
        		bundleInstallArtifact.getBundleManifest().setHeader(MANIFEST_HEADER_WEB_CONTEXT_PATH, webContextPath);
        	}
        } catch (IOException ioe) {
            throw new DeploymentException("Could not retrieve manifest for bundle install artifact " + bundleInstallArtifact, ioe);
        }
    }

    private static String getBaseName(String path) {
        String base = path;
        base = unifySeparators(base);
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }

        base = stripQuery(base);
        base = stripSchemeAndDrive(base);
        base = stripLeadingPathElements(base);
        base = stripExtension(base);
        return base;
    }

    private static String unifySeparators(String base) {
        return base.replaceAll("\\\\", "/");
    }

    private static String stripExtension(String base) {
        int index;
        index = base.lastIndexOf(".");
        if (index > -1) {
            base = base.substring(0, index);
        }
        return base;
    }

    private static String stripLeadingPathElements(String base) {
        int index = base.lastIndexOf("/");
        if (index > -1) {
            base = base.substring(index + 1);
        }
        return base;
    }

    private static String stripQuery(String path) {
        String result = path;
        int index = result.lastIndexOf("?");
        if (index > -1) {
            result = result.substring(0, index);
        }
        return result;
    }

    private static String stripSchemeAndDrive(String path) {
        String result = path;
        int index = result.indexOf(":");
        while (index > -1 && index < result.length()) {
            result = result.substring(index + 1);
            index = result.indexOf(":");
        }
        return result;
    }

    private void applyWebContainerTransformations(BundleInstallArtifact bundleArtifact) throws DeploymentException {
        try {
            BundleManifest bundleManifest = bundleArtifact.getBundleManifest();
            if (bundleManifest.getModuleType() == null || WEB_BUNDLE_MODULE_TYPE.equalsIgnoreCase(bundleManifest.getModuleType())) {
                boolean webBundle = WebContainerUtils.isWebApplicationBundle(bundleManifest);
                boolean defaultWABHeaders = !webBundle || !this.strictWABHeaders;
                if (defaultWABHeaders) {
                    bundleManifest.setHeader(HEADER_DEFAULT_WAB_HEADERS, "true");
                }
                bundleManifest.setModuleType(WEB_BUNDLE_MODULE_TYPE);
                InstallationOptions installationOptions = new InstallationOptions(Collections.<String, String> emptyMap());
                installationOptions.setDefaultWABHeaders(defaultWABHeaders);
                this.environment.getManifestTransformer().transform(bundleManifest, getSourceUrl(bundleArtifact), installationOptions, webBundle);
            } else {
                logger.debug("Bundle '{}' version '{}' is not being transformed as it already has a Module-Type of '{}'", new Object[] {
                    bundleManifest.getBundleSymbolicName().getSymbolicName(), bundleManifest.getBundleVersion(), bundleManifest.getModuleType() });
            }
        } catch (IOException e) {
            throw new DeploymentException("Failed to apply web container transformations to bundle '" + bundleArtifact.getName() + "' version '"
                + bundleArtifact.getVersion() + "'", e);
        }
    }

    private static URL getSourceUrl(InstallArtifact installArtifact) throws DeploymentException {
        URI sourceUri = installArtifact.getArtifactFS().getFile().toURI();
        if (sourceUri != null) {
            try {
                return sourceUri.toURL();
            } catch (MalformedURLException murle) {
                throw new DeploymentException("Install artifact '" + installArtifact + "' has source URI that is not a valid URL", murle);
            }
        } else {
            throw new DeploymentException("Install artifact '" + installArtifact + "' has a null source URI");
        }
    }

}
