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

package org.eclipse.virgo.kernel.install.artifact.internal;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.jar.JarFile;

import org.eclipse.virgo.kernel.artifact.fs.ArtifactFS;
import org.eclipse.virgo.kernel.artifact.fs.ArtifactFSEntry;
import org.eclipse.virgo.kernel.install.artifact.ScopeServiceRepository;
import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.util.io.IOUtils;
import org.eclipse.virgo.util.osgi.manifest.BundleManifest;
import org.eclipse.virgo.util.osgi.manifest.BundleManifestFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.xml.XmlValidationModeDetector;

/**
 * Generates the service model in the {@link StandardScopeServiceRepository} for a bundle in a given scope.
 * <p/>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Not thread safe.
 * 
 */
final class ServiceScoper {

    private static final String SPRING_CONFIG_DIR = "META-INF/spring/";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final XmlValidationModeDetector xmlValidationModeDetector = new XmlValidationModeDetector();

    private final String scopeName;

    private final ScopeServiceRepository repository;

    private final EventLogger eventLogger;

    /**
     * Creates a new <code>ServiceScoper</code> for the supplied scope name.
     * 
     * @param scopeName supplied
     * @param scopeServiceRepository the {@link StandardScopeServiceRepository}.
     * @param eventLogger logger for events
     */
    public ServiceScoper(String scopeName, ScopeServiceRepository scopeServiceRepository, EventLogger eventLogger) {
        this.scopeName = scopeName;
        this.repository = scopeServiceRepository;
        this.eventLogger = eventLogger;
    }

    /**
     * Scopes the application's services.
     * 
     * @param modules set of stored artifacts to search for configurations
     * @throws DeploymentException if configuration files or manifests are not well-formed
     */
    public void scope(Set<ArtifactFS> modules) throws DeploymentException {
        Map<ArtifactFS, List<ArtifactFSEntry>> configFiles = new HashMap<ArtifactFS, List<ArtifactFSEntry>>();
        for (ArtifactFS moduleData : modules) {
            configFiles.put(moduleData, findConfigFiles(moduleData));
        }
        doScope(configFiles);
    }

    private List<ArtifactFSEntry> findConfigFiles(ArtifactFS bundleData) throws DeploymentException {
        List<ArtifactFSEntry> configFiles = new ArrayList<ArtifactFSEntry>();

        ArtifactFSEntry entry = bundleData.getEntry(SPRING_CONFIG_DIR);
        if (entry.exists()) {
            try {
                configFiles.addAll(findConfigFiles(bundleData, entry));
            } catch (IOException e) {
                throw new DeploymentException("Unable to read Spring config files.", e);
            }
        }

        return configFiles;
    }

    private List<ArtifactFSEntry> findConfigFiles(ArtifactFS bundleData, ArtifactFSEntry entry) throws IOException {
        ArtifactFSEntry[] children = entry.getChildren();
        List<ArtifactFSEntry> configFiles = new ArrayList<ArtifactFSEntry>();
        for (ArtifactFSEntry e : children) {
            if (e.isDirectory()) {
                configFiles.addAll(findConfigFiles(bundleData, e));
            } else if (e.getPath().endsWith(".xml")) {
                try {
                    InputStream is = e.getInputStream();
                    int validationMode;
                    try {
                        validationMode = xmlValidationModeDetector.detectValidationMode(is);
                    } finally {
                        IOUtils.closeQuietly(is);
                    }
                    if (validationMode != XmlValidationModeDetector.VALIDATION_DTD) {
                        configFiles.add(e);
                    } else {
                        logger.debug("Skipping entry '{}' as it uses a DTD.", e);
                    }
                } catch (IOException ioe) {
                    logger.debug("Unexpected error detecting validation mode of entry '{}'", ioe, e);
                    configFiles.add(e);
                }
            }
        }
        return configFiles;
    }

    /**
     * Re-scope the service exports and service references of the given {@link ArtifactFS}.
     * 
     * @param bundleData the {@link ArtifactFS} to be re-scoped.
     * @throws DeploymentException
     */
    public void rescope(ArtifactFS bundleData) throws DeploymentException {
        Map<ArtifactFS, List<ArtifactFSEntry>> configFiles = new HashMap<ArtifactFS, List<ArtifactFSEntry>>();
        configFiles.put(bundleData, findConfigFiles(bundleData));
        doScope(configFiles);
    }

    /**
     * Updates the {@link StandardScopeServiceRepository} with the service information from the given config files.
     * 
     * @param scopeName the name of the scope.
     * @param configFiles the config files to scope.
     * @throws DeploymentException
     */
    private void doScope(Map<ArtifactFS, List<ArtifactFSEntry>> configFiles) throws DeploymentException {
        SpringConfigServiceModelScanner scanner = new SpringConfigServiceModelScanner(this.scopeName, this.repository, this.eventLogger);
        Map<ArtifactFS, BundleManifest> manifests = loadBundleManifests(configFiles.keySet());
        for (Entry<ArtifactFS, List<ArtifactFSEntry>> entry : configFiles.entrySet()) {
            BundleManifest bundleManifest = manifests.get(entry.getKey());
            for (ArtifactFSEntry configFile : entry.getValue()) {
                InputStream is = configFile.getInputStream();
                try {
                    scanner.scanConfigFile(bundleManifest.getBundleSymbolicName().getSymbolicName(), bundleManifest.getBundleVersion(),
                        configFile.getPath(), is);
                } finally {
                    IOUtils.closeQuietly(is);
                }
            }
        }
    }

    private Map<ArtifactFS, BundleManifest> loadBundleManifests(Collection<ArtifactFS> modules) throws DeploymentException {
        Map<ArtifactFS, BundleManifest> result = new HashMap<ArtifactFS, BundleManifest>();
        for (ArtifactFS module : modules) {
            if (!result.containsKey(module)) {
                BundleManifest manifest = loadManifest(module);
                result.put(module, manifest);
            }
        }
        return result;
    }

    private BundleManifest loadManifest(ArtifactFS compositeArtifactFS) throws DeploymentException {
        ArtifactFSEntry entry = compositeArtifactFS.getEntry(JarFile.MANIFEST_NAME);
        Reader reader = null;
        try {
            reader = new InputStreamReader(entry.getInputStream(), UTF_8);
            return BundleManifestFactory.createBundleManifest(reader);
        } catch (IOException ex) {
            throw new DeploymentException("Error reading MANIFEST.MF from '" + compositeArtifactFS + "'", ex);
        } finally {
            IOUtils.closeQuietly(reader);
        }
    }
}
