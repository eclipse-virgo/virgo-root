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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

import org.junit.Test;
import org.osgi.framework.Version;

import org.eclipse.virgo.kernel.artifact.ArtifactSpecification;
import org.eclipse.virgo.kernel.artifact.plan.PlanDescriptor;
import org.eclipse.virgo.kernel.artifact.plan.PlanDescriptor.Provisioning;
import org.eclipse.virgo.kernel.artifact.plan.PlanReader;
import org.eclipse.virgo.util.osgi.manifest.VersionRange;

public class PlanReaderTests {

    private final PlanReader reader = new PlanReader();

    @Test(expected = RuntimeException.class)
    public void testBadPlanFile() throws FileNotFoundException {
        reader.read(new FileInputStream("src/test/resources/plans/malformed.plan"));
    }

    @Test
    public void testSingleArtifactPlan() throws FileNotFoundException {
        PlanDescriptor plan = reader.read(new FileInputStream("src/test/resources/plans/single-artifact.plan"));
        assertEquals("single-artifact.plan", plan.getName());
        assertEquals(new Version(1, 0, 0), plan.getVersion());
        
        assertEquals(Provisioning.INHERIT, plan.getProvisioning());

        List<ArtifactSpecification> artifactSpecifications = plan.getArtifactSpecifications();
        assertEquals(1, artifactSpecifications.size());
        ArtifactSpecification artifactSpecification = artifactSpecifications.iterator().next();
        assertEquals("bundle", artifactSpecification.getType());
        assertEquals("org.springframework.context.support", artifactSpecification.getName());
        assertEquals(new VersionRange("[1.0.0, 2.0.0)"), artifactSpecification.getVersionRange());
        assertNotNull(artifactSpecification.getProperties());
        assertTrue(artifactSpecification.getProperties().isEmpty());
        assertNull(artifactSpecification.getUri());
    }

    @Test
    public void testSingleArtifactPlanWithAttributes() throws FileNotFoundException {
        PlanDescriptor plan = reader.read(new FileInputStream("src/test/resources/plans/attributes.plan"));
        assertEquals("single-artifact.plan", plan.getName());
        assertEquals(new Version(1, 0, 0), plan.getVersion());

        List<ArtifactSpecification> artifactSpecifications = plan.getArtifactSpecifications();
        assertEquals(1, artifactSpecifications.size());
        ArtifactSpecification artifactSpecification = artifactSpecifications.iterator().next();
        assertEquals("bundle", artifactSpecification.getType());
        assertEquals("org.springframework.context.support", artifactSpecification.getName());
        assertEquals(new VersionRange("[1.0.0, 2.0.0)"), artifactSpecification.getVersionRange());
    }

    @Test
    public void testSingleArtifactPlanWithProperties() throws FileNotFoundException {
        PlanDescriptor plan = reader.read(new FileInputStream("src/test/resources/plans/properties.plan"));
        assertEquals("properties.plan", plan.getName());
        assertEquals(new Version(1, 0, 0), plan.getVersion());

        List<ArtifactSpecification> artifactSpecifications = plan.getArtifactSpecifications();
        assertEquals(1, artifactSpecifications.size());
        ArtifactSpecification artifactSpecification = artifactSpecifications.iterator().next();
        assertEquals("bundle", artifactSpecification.getType());
        assertEquals("my.webapp", artifactSpecification.getName());
        assertEquals(new VersionRange("[1.0.0, 2.0.0)"), artifactSpecification.getVersionRange());
        assertEquals("/foo", artifactSpecification.getProperties().get("Web-ContextPath"));
    }

    @Test
    public void testMultiArtifactPlan() throws FileNotFoundException {
        PlanDescriptor plan = reader.read(new FileInputStream("src/test/resources/plans/multi-artifact.plan"));
        assertEquals("multi-artifact.plan", plan.getName());
        assertEquals(new Version(1, 0, 0), plan.getVersion());

        List<ArtifactSpecification> artifactSpecifications = plan.getArtifactSpecifications();
        assertEquals(3, artifactSpecifications.size());
        for (ArtifactSpecification artifactSpecification : artifactSpecifications) {
            String type = artifactSpecification.getType();
            if ("alpha".equals(type)) {
                assertEquals("org.springframework.core", artifactSpecification.getName());
                assertEquals(new VersionRange("0"), artifactSpecification.getVersionRange());
            } else if ("bravo".equals(type)) {
                assertEquals("org.springframework.context", artifactSpecification.getName());
                assertEquals(new VersionRange("1.0.0"), artifactSpecification.getVersionRange());
            } else if ("charlie".equals(type)) {
                assertEquals("org.springframework.context.support", artifactSpecification.getName());
                assertEquals(new VersionRange("[1.0.0, 2.0.0)"), artifactSpecification.getVersionRange());
            }
        }
    }
    
    @Test
    public void testProvisioningDefaultPlan() throws FileNotFoundException {
        PlanDescriptor plan = reader.read(new FileInputStream("src/test/resources/plans/provisioning-default.plan"));
        assertEquals(Provisioning.INHERIT, plan.getProvisioning());
    }
    
    @Test
    public void testProvisioningInheritPlan() throws FileNotFoundException {
        PlanDescriptor plan = reader.read(new FileInputStream("src/test/resources/plans/provisioning-inherit.plan"));
        assertEquals(Provisioning.INHERIT, plan.getProvisioning());
    }
    
    @Test
    public void testProvisioningAutoPlan() throws FileNotFoundException {
        PlanDescriptor plan = reader.read(new FileInputStream("src/test/resources/plans/provisioning-auto.plan"));
        assertEquals(Provisioning.AUTO, plan.getProvisioning());
    }
    
    @Test
    public void testProvisioningDisabledPlan() throws FileNotFoundException {
        PlanDescriptor plan = reader.read(new FileInputStream("src/test/resources/plans/provisioning-disabled.plan"));
        assertEquals(Provisioning.DISABLED, plan.getProvisioning());
    }
    
    @Test
    public void testSingleUriPlan() throws FileNotFoundException {
        PlanDescriptor plan = reader.read(new FileInputStream("src/test/resources/plans/single-uri.plan"));
        assertEquals("single-uri.plan", plan.getName());
        assertEquals(new Version(1, 0, 0), plan.getVersion());
        
        assertEquals(Provisioning.INHERIT, plan.getProvisioning());

        List<ArtifactSpecification> artifactSpecifications = plan.getArtifactSpecifications();
        assertEquals(1, artifactSpecifications.size());
        ArtifactSpecification artifactSpecification = artifactSpecifications.iterator().next();
        assertNull(artifactSpecification.getType());
        assertNull(artifactSpecification.getName());
        assertNull(artifactSpecification.getVersionRange());
        assertNotNull(artifactSpecification.getProperties());
        assertTrue(artifactSpecification.getProperties().isEmpty());
        assertEquals("file:/a/b.c", artifactSpecification.getUri().toString());
    }

    @Test(expected=RuntimeException.class)
    public void testInvalidUriWithTypePlan() throws FileNotFoundException {
        reader.read(new FileInputStream("src/test/resources/plans/invalid-uri-type.plan"));
    }
    
    @Test(expected=RuntimeException.class)
    public void testInvalidUriWithNamePlan() throws FileNotFoundException {
        reader.read(new FileInputStream("src/test/resources/plans/invalid-uri-name.plan"));
    }
    
    @Test(expected=RuntimeException.class)
    public void testInvalidUriWithVersionRangePlan() throws FileNotFoundException {
        reader.read(new FileInputStream("src/test/resources/plans/invalid-uri-versionrange.plan"));
    }

}
