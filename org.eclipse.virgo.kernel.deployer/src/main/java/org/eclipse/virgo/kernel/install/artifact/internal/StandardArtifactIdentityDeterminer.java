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

import java.io.File;
import java.util.Set;

import org.osgi.framework.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.virgo.kernel.install.artifact.ArtifactIdentity;
import org.eclipse.virgo.kernel.install.artifact.ArtifactIdentityDeterminer;
import org.eclipse.virgo.repository.ArtifactBridge;
import org.eclipse.virgo.repository.ArtifactDescriptor;
import org.eclipse.virgo.repository.ArtifactGenerationException;

/**
 * {@link StandardArtifactIdentityDeterminer} is a {@link ArtifactIdentityDeterminer} that can determine basic kernel artifact
 * identities.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is thread safe.
 * 
 */
final class StandardArtifactIdentityDeterminer implements ArtifactIdentityDeterminer {

    private static final Logger LOGGER = LoggerFactory.getLogger(StandardArtifactIdentityDeterminer.class);

    private static final String JAR_EXTENSION = ".jar";

    private static final String PLAN_EXTENSION = ".plan";

    private static final String PROPERTIES_EXTENSION = ".properties";

    private static final String PAR_EXTENSION = ".par";

    private final Set<ArtifactBridge> bridges;

    public StandardArtifactIdentityDeterminer(Set<ArtifactBridge> bridges) {
        this.bridges = bridges;
    }

    private ArtifactIdentity determineIdentityFromFile(File file, String scopeName) {
        String type = null;
        String name = null;
        
        String filename = file.getName();
        
        name = trimExtension(file);
        
        if (filename.endsWith(JAR_EXTENSION)) {
            type = BUNDLE_TYPE;
        } else if (filename.endsWith(PLAN_EXTENSION)) {
            type = PLAN_TYPE;
        } else if (filename.endsWith(PROPERTIES_EXTENSION)) {
            type = CONFIGURATION_TYPE;
        } else if (filename.endsWith(PAR_EXTENSION)) {
            type = PAR_TYPE;
        }

        
        if (type != null && name != null) {
            return new ArtifactIdentity(type, name, Version.emptyVersion, scopeName);
        } else {
            return null;
        }
    }
    
    private String trimExtension(File file) {
        String filename = file.getName();
        
        int lastIndexOf = filename.lastIndexOf('.');
        
        if (lastIndexOf > 0) {
            return filename.substring(0, lastIndexOf);
        } else {
            return filename;
        }
    }
    
    public ArtifactIdentity determineIdentity(File file, String scopeName) {
        ArtifactDescriptor artifactDescriptor = null;
        for (ArtifactBridge artifactBridge : this.bridges) {
            try {
                artifactDescriptor = artifactBridge.generateArtifactDescriptor(file);
            } catch (ArtifactGenerationException e) {
                LOGGER.warn(String.format("Error occurred while determining the type of an Artifact '%s' with the bridge '%s'.", file,
                    artifactBridge.getClass().getSimpleName()), e);
            }
            if (artifactDescriptor != null) {
                break;
            }
        }
        if (artifactDescriptor == null) {
            return this.determineIdentityFromFile(file, scopeName);
        } else {
            String type = artifactDescriptor.getType();
            String name = artifactDescriptor.getName();
            Version version = artifactDescriptor.getVersion();
            
            name = name == null ? trimExtension(file) : name;
            version = version == null ? Version.emptyVersion : version;
            
            return new ArtifactIdentity(type, name, version, scopeName);
        }
    }
}
