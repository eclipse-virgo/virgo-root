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

package org.eclipse.virgo.kernel.artifact.properties;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import org.osgi.framework.Version;

import org.eclipse.virgo.repository.ArtifactBridge;
import org.eclipse.virgo.repository.ArtifactDescriptor;
import org.eclipse.virgo.repository.ArtifactGenerationException;
import org.eclipse.virgo.repository.HashGenerator;
import org.eclipse.virgo.repository.builder.ArtifactDescriptorBuilder;
import org.eclipse.virgo.util.io.IOUtils;

/**
 * An {@link ArtifactBridge} that creates {@link ArtifactDescriptor ArtifactDescriptors} for .properties files.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread-safe
 * 
 */
public final class PropertiesBridge implements ArtifactBridge {

    static final String PROPERTIES_SUFFIX = ".properties";

    static final String ARTIFACT_TYPE = "configuration";

    private final HashGenerator hashGenerator;

    public PropertiesBridge(HashGenerator hashGenerator) {
        this.hashGenerator = hashGenerator;
    }

    public ArtifactDescriptor generateArtifactDescriptor(File artifactFile) throws ArtifactGenerationException {
        if (artifactFile.getName().endsWith(PROPERTIES_SUFFIX)) {
            FileReader reader = null;
            try {
                reader = new FileReader(artifactFile);
                new Properties().load(reader);
                return createArtifactDescriptor(artifactFile);
            } catch (IOException e) {
                throw new ArtifactGenerationException("Failed to read properties file", ARTIFACT_TYPE, e);
            } finally {
                IOUtils.closeQuietly(reader);
            }
        }
        return null;
    }

    private ArtifactDescriptor createArtifactDescriptor(File propertiesFile) {
        String fileName = propertiesFile.getName();
        String name = fileName.substring(0, fileName.length() - PROPERTIES_SUFFIX.length());

        ArtifactDescriptorBuilder artifactDescriptorBuilder = new ArtifactDescriptorBuilder();

        artifactDescriptorBuilder //
        .setUri(propertiesFile.toURI()) //
        .setType(ARTIFACT_TYPE) //
        .setName(name) //
        .setVersion(Version.emptyVersion);
        
        this.hashGenerator.generateHash(artifactDescriptorBuilder, propertiesFile);

        return artifactDescriptorBuilder.build();
    }
}
