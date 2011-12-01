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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.osgi.framework.Version;


import org.eclipse.virgo.kernel.artifact.StubHashGenerator;
import org.eclipse.virgo.kernel.artifact.plan.PlanBridge;
import org.eclipse.virgo.repository.ArtifactDescriptor;
import org.eclipse.virgo.repository.ArtifactGenerationException;
import org.eclipse.virgo.repository.Attribute;

public class PlanBridgeTests {

    private final PlanBridge bridge = new PlanBridge(new StubHashGenerator());

    @Test(expected = ArtifactGenerationException.class)
    public void testBadPlanFile() throws ArtifactGenerationException {
        bridge.generateArtifactDescriptor(new File("src/test/resources/plans/malformed.plan"));
    }

    @Test()
    public void testNotAPlanFile() throws ArtifactGenerationException {
        assertNull(bridge.generateArtifactDescriptor(new File("src/test/resources/plans/not-a-plan.xml")));
    }

    @Test
    public void testSingleArtifactPlan() throws ArtifactGenerationException {
        org.eclipse.virgo.repository.ArtifactDescriptor artefact = bridge.generateArtifactDescriptor(new File(
            "src/test/resources/plans/single-artifact.plan"));
        assertEquals("plan", artefact.getType());
        assertEquals("single-artifact.plan", artefact.getName());
        assertEquals(new Version(1, 0, 0), artefact.getVersion());

        Set<Attribute> attributes = artefact.getAttribute("artifact");
        assertEquals(1, attributes.size());
        Map<String, Set<String>> properties = attributes.iterator().next().getProperties();
        assertEquals("bundle", getProperty("type", properties));
        assertEquals("org.springframework.context.support", getProperty("name", properties));
        assertEquals("[1.0.0, 2.0.0)", getProperty("version", properties));
    }

    @Test
    public void testMultiArtifactPlan() throws ArtifactGenerationException {
        ArtifactDescriptor artefact = bridge.generateArtifactDescriptor(new File("src/test/resources/plans/multi-artifact.plan"));
        assertEquals("plan", artefact.getType());
        assertEquals("multi-artifact.plan", artefact.getName());
        assertEquals(new Version(1, 0, 0), artefact.getVersion());

        Set<Attribute> attributes = artefact.getAttribute("artifact");
        assertEquals(3, attributes.size());
        for (Attribute attribute : attributes) {
            Map<String, Set<String>> properties = attribute.getProperties();
            String type = getProperty("type", properties);
            if ("alpha".equals(type)) {
                assertEquals("org.springframework.core", getProperty("name", properties));
                assertEquals("0.0.0", getProperty("version", properties));
            } else if ("bravo".equals(type)) {
                assertEquals("org.springframework.context", getProperty("name", properties));
                assertEquals("1.0.0", getProperty("version", properties));
            } else if ("charlie".equals(type)) {
                assertEquals("org.springframework.context.support", getProperty("name", properties));
                assertEquals("[1.0.0, 2.0.0)", getProperty("version", properties));
            }
        }
    }
    
    @Test
    public void testProvisioningDisabledPlan() throws ArtifactGenerationException {
        org.eclipse.virgo.repository.ArtifactDescriptor artefact = bridge.generateArtifactDescriptor(new File(
            "src/test/resources/plans/provisioning-disabled.plan"));
        assertEquals("plan", artefact.getType());
        assertEquals("provisioningdisabled.plan", artefact.getName());
        assertEquals(new Version(1, 0, 0), artefact.getVersion());

        Set<Attribute> attributes = artefact.getAttribute("provisioning");
        assertEquals(1, attributes.size());
        Attribute attribute = attributes.iterator().next();
        assertEquals("provisioning", attribute.getKey());
        assertEquals("disabled", attribute.getValue());
        assertEquals(0, attribute.getProperties().size());
    }

    private String getProperty(String key, Map<String, Set<String>> properties) {
        return properties.get(key).iterator().next();
    }
}
