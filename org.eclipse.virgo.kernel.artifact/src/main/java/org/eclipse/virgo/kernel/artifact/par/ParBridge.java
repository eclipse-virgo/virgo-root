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

package org.eclipse.virgo.kernel.artifact.par;

import java.io.File;
import java.io.IOException;

import org.osgi.framework.Version;

import org.eclipse.virgo.kernel.artifact.internal.BundleManifestUtils;
import org.eclipse.virgo.repository.ArtifactBridge;
import org.eclipse.virgo.repository.ArtifactDescriptor;
import org.eclipse.virgo.repository.ArtifactGenerationException;
import org.eclipse.virgo.repository.HashGenerator;
import org.eclipse.virgo.repository.builder.ArtifactDescriptorBuilder;
import org.eclipse.virgo.repository.builder.AttributeBuilder;
import org.eclipse.virgo.util.common.StringUtils;
import org.eclipse.virgo.util.osgi.manifest.BundleManifest;

/**
 * An <code>ArtifactBridge</code> for PAR files.
 * 
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread-safe.
 * 
 */
public final class ParBridge implements ArtifactBridge {

    private static final String SYMBOLIC_NAME_REGEX = "[-_0-9a-zA-Z]+(\\.[-_0-9a-zA-Z]+)*";

    private static final String HEADER_APPLICATION_SYMBOLIC_NAME = "Application-SymbolicName";

    private static final String HEADER_APPLICATION_NAME = "Application-Name";

    private static final String HEADER_APPLICATION_DESCRIPTION = "Application-Description";

    private static final String HEADER_APPLICATION_VERSION = "Application-Version";

    public static final String BRIDGE_TYPE = "par";

    private final HashGenerator hashGenerator;

    public ParBridge(HashGenerator hashGenerator) {
        this.hashGenerator = hashGenerator;
    }

    public ArtifactDescriptor generateArtifactDescriptor(File artifactFile) throws ArtifactGenerationException {

        BundleManifest manifest;

        try {
            manifest = BundleManifestUtils.readBundleManifest(artifactFile, ".par");
        } catch (IOException ioe) {
            throw new ArtifactGenerationException("Failed to read manifest from " + artifactFile, ioe);
        }

        if (manifest != null) {
            return createDescriptorFromManifest(manifest, artifactFile);
        } else {
            return null;
        }
    }

    private ArtifactDescriptor createDescriptorFromManifest(BundleManifest manifest, File artifactFile) throws ArtifactGenerationException {

        String symbolicName = getApplicationSymbolicName(manifest);

        if (symbolicName == null) {
            return null;
        }

        Version version = getApplicationVersion(manifest);

        ArtifactDescriptorBuilder builder = new ArtifactDescriptorBuilder();
        builder.setType(BRIDGE_TYPE).setName(symbolicName).setVersion(version).setUri(artifactFile.toURI());

        applyAttributeIfPresent(HEADER_APPLICATION_NAME, manifest, builder);
        applyAttributeIfPresent(HEADER_APPLICATION_DESCRIPTION, manifest, builder);
        
        this.hashGenerator.generateHash(builder, artifactFile);

        return builder.build();
    }

    private void applyAttributeIfPresent(String headerName, BundleManifest manifest, ArtifactDescriptorBuilder builder) {
        String headerValue = manifest.getHeader(headerName);
        if (headerValue != null) {
            AttributeBuilder attributeBuilder = new AttributeBuilder();
            builder.addAttribute(attributeBuilder.setName(headerName).setValue(headerValue).build());
        }
    }

    private Version getApplicationVersion(BundleManifest manifest) throws ArtifactGenerationException {
        String versionString = manifest.getHeader(HEADER_APPLICATION_VERSION);
        Version version;

        if (!StringUtils.hasText(versionString)) {
            version = Version.emptyVersion;
        } else {
            try {
                version = new Version(versionString);
            } catch (IllegalArgumentException iae) {
                throw new ArtifactGenerationException("Version '" + versionString + "' is ill-formed", iae);
            }
        }
        return version;
    }

    private String getApplicationSymbolicName(BundleManifest manifest) throws ArtifactGenerationException {
        String symbolicName = manifest.getHeader(HEADER_APPLICATION_SYMBOLIC_NAME);

        if (!StringUtils.hasText(symbolicName)) {
            return null;
        }
        if (!symbolicName.matches(SYMBOLIC_NAME_REGEX)) {
            throw new ArtifactGenerationException(HEADER_APPLICATION_SYMBOLIC_NAME + " '" + symbolicName + "' contains illegal characters");
        }
        return symbolicName;
    }
}
