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

package org.eclipse.virgo.kernel.artifact.plan;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import org.eclipse.virgo.kernel.artifact.ArtifactSpecification;
import org.eclipse.virgo.repository.ArtifactBridge;
import org.eclipse.virgo.repository.ArtifactDescriptor;
import org.eclipse.virgo.repository.ArtifactGenerationException;
import org.eclipse.virgo.repository.HashGenerator;
import org.eclipse.virgo.repository.builder.ArtifactDescriptorBuilder;
import org.eclipse.virgo.repository.builder.AttributeBuilder;
import org.eclipse.virgo.util.io.IOUtils;
import org.eclipse.virgo.util.osgi.manifest.VersionRange;

/**
 * An {@link ArtifactBridge} that reads and parses a .plan file.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe
 * 
 */
public class PlanBridge implements ArtifactBridge {

    public static final String BRIDGE_TYPE = "plan";

    private final PlanReader reader = new PlanReader();

    private static final String SCOPED = "scoped";

    private static final String ATOMIC = "atomic";
    
    private static final String PROVISIONING = "provisioning";

    private static final String ARTIFACT = "artifact";

    private static final String TYPE = "type";

    private static final String NAME = "name";

    private static final String VERSION = "version";

    private final HashGenerator hashGenerator;

    public PlanBridge(HashGenerator hashGenerator) {
        this.hashGenerator = hashGenerator;
    }

    /**
     * {@inheritDoc}
     */
    public ArtifactDescriptor generateArtifactDescriptor(File artifactFile) throws ArtifactGenerationException {
        if (!artifactFile.getPath().endsWith(".plan")) {
            return null;
        }

        PlanDescriptor plan;
        FileInputStream in = null;
        try {
            in = new FileInputStream(artifactFile);
            plan = reader.read(in);
        } catch (Exception e) {
            throw new ArtifactGenerationException("Failed to read plan descriptor", BRIDGE_TYPE, e);
        } finally {
            IOUtils.closeQuietly(in);
        }

        return parsePlan(plan, artifactFile);
    }

    private ArtifactDescriptor parsePlan(PlanDescriptor plan, File artifactFile) {
        ArtifactDescriptorBuilder builder = new ArtifactDescriptorBuilder();
        builder.setUri(artifactFile.toURI());
        builder.setType(BRIDGE_TYPE);
        builder.setName(plan.getName());
        builder.setVersion(plan.getVersion());

        builder.addAttribute(new AttributeBuilder().setName(SCOPED).setValue(Boolean.toString(plan.getScoped())).build());
        builder.addAttribute(new AttributeBuilder().setName(ATOMIC).setValue(Boolean.toString(plan.getAtomic())).build());
        builder.addAttribute(new AttributeBuilder().setName(PROVISIONING).setValue(plan.getProvisioning().toString()).build());

        parseArtifacts(plan.getArtifactSpecifications(), builder);
        
        this.hashGenerator.generateHash(builder, artifactFile);

        return builder.build();
    }

    private void parseArtifacts(List<ArtifactSpecification> artifacts, ArtifactDescriptorBuilder builder) {
        for (ArtifactSpecification artifact : artifacts) {
            AttributeBuilder attributeBuilder = new AttributeBuilder();
            attributeBuilder.setName(ARTIFACT);
            attributeBuilder.setValue("");
            attributeBuilder.putProperties(TYPE, artifact.getType());
            attributeBuilder.putProperties(NAME, artifact.getName());
            VersionRange versionRange = artifact.getVersionRange();
            attributeBuilder.putProperties(VERSION, versionRange == null ? "" : versionRange.toParseString());
            builder.addAttribute(attributeBuilder.build());
        }
    }
}
