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

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Properties;

import org.eclipse.virgo.repository.ArtifactBridge;
import org.eclipse.virgo.repository.ArtifactDescriptor;
import org.eclipse.virgo.repository.ArtifactGenerationException;
import org.eclipse.virgo.repository.HashGenerator;
import org.eclipse.virgo.repository.builder.ArtifactDescriptorBuilder;
import org.eclipse.virgo.repository.builder.AttributeBuilder;
import org.eclipse.virgo.util.common.StringUtils;
import org.eclipse.virgo.util.io.IOUtils;
import org.osgi.framework.Constants;
import org.osgi.framework.Version;
import org.osgi.service.cm.ConfigurationAdmin;

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

    private final ConfigurationAdmin configAdmin;

    public PropertiesBridge(HashGenerator hashGenerator, ConfigurationAdmin configAdmin) {
        this.hashGenerator = hashGenerator;
        this.configAdmin = configAdmin;
    }

    public ArtifactDescriptor generateArtifactDescriptor(File artifactFile) throws ArtifactGenerationException {
        if (artifactFile.getName().endsWith(PROPERTIES_SUFFIX)) {
            Reader reader = null;
            try {
                reader = new InputStreamReader(new FileInputStream(artifactFile), UTF_8);
                Properties properties = new Properties();
                properties.load(reader);
                return createArtifactDescriptor(artifactFile, properties);
            } catch (IOException e) {
                throw new ArtifactGenerationException("Failed processing properties file", ARTIFACT_TYPE, e);
            } finally {
                IOUtils.closeQuietly(reader);
            }
        }
        return null;
    }

    private ArtifactDescriptor createArtifactDescriptor(File propertiesFile, Properties properties) throws IOException {

        String name = properties.getProperty(ConfigurationAdmin.SERVICE_FACTORYPID);
        if (StringUtils.hasText(name)) {
            // this is a factory configuration - need to generate actual PID for a new configuration
            return buildForManagedServiceFactoryConfiguration(propertiesFile, name, properties);
        }

        name = properties.getProperty(Constants.SERVICE_PID);
        if (!StringUtils.hasText(name)) {
            String fileName = propertiesFile.getName();
            name = fileName.substring(0, fileName.length() - PROPERTIES_SUFFIX.length());
        }

        return buildAtrifactDescriptor(propertiesFile, name).build();
    }

    /**
     * @param propertiesFile
     * @param name
     * @param properties
     * @return
     * @throws IOException
     */
    private ArtifactDescriptor buildForManagedServiceFactoryConfiguration(File propertiesFile, String factoryPid, Properties properties)
        throws IOException {

        // generated service.pid - will use as a name for artifactId
        String pid = configAdmin.createFactoryConfiguration(factoryPid, null).getPid();

        ArtifactDescriptorBuilder builder = buildAtrifactDescriptor(propertiesFile, pid);
        builder.addAttribute(new AttributeBuilder().setName(ConfigurationAdmin.SERVICE_FACTORYPID).setValue(factoryPid).build());

        return builder.build();
    }

    private ArtifactDescriptorBuilder buildAtrifactDescriptor(File propertiesFile, String name) {
        ArtifactDescriptorBuilder artifactDescriptorBuilder = new ArtifactDescriptorBuilder();

        artifactDescriptorBuilder //
        .setUri(propertiesFile.toURI()) //
        .setType(ARTIFACT_TYPE) //
        .setName(name) //
        .setVersion(Version.emptyVersion);

        this.hashGenerator.generateHash(artifactDescriptorBuilder, propertiesFile);

        return artifactDescriptorBuilder;
    }
}
