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

package org.eclipse.virgo.kernel.deployer.test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentIdentity;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.framework.Version;

import static org.junit.Assert.assertTrue;

/**
 * These tests cover the use of URIs in plans.
 */
public class PlanUriIntegrationTests extends AbstractDeployerIntegrationTest {

    private static final String PLAN_TYPE = "plan";

    private static final String TEST_RESOURCES_DIRECTORY = "src/test/resources/plan-deployment/";

    private static final String GENERATED_PLAN_DIRECTORY = "build/PlanUriIntegrationTests/";

    private static final String PLAN_EXTENSION = ".plan";

    private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";

    private static final String NAMESPACES = "    xmlns=\"http://www.eclipse.org/virgo/schema/plan\" \n    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n    xsi:schemaLocation=\" http://www.eclipse.org/virgo/schema/plan http://www.eclipse.org/virgo/schema/plan/eclipse-virgo-plan.xsd\"";

    @BeforeClass
    public static void setUpClass() {
        File generatedPlanDirectory = new File(GENERATED_PLAN_DIRECTORY);
        generatedPlanDirectory.mkdirs();
    }

    @Test
    public void testUriInPlan() throws IOException, DeploymentException {
        TestArtifactInfo planInfo = createPlanFile("uriplan", Version.emptyVersion, false, false,
            new File(TEST_RESOURCES_DIRECTORY + "global.jar").toURI().toString());
        testPlan(planInfo);
    }

    @Test
    public void testBundleAndConfigUrisInPlan() throws IOException, DeploymentException {
        TestArtifactInfo planInfo = createPlanFile("uriplan", Version.emptyVersion, false, false, new File(TEST_RESOURCES_DIRECTORY
            + "com.foo.bar.properties").toURI().toString(), new File(TEST_RESOURCES_DIRECTORY + "global.jar").toURI().toString());
        testPlan(planInfo);
    }

    @Test(expected = DeploymentException.class)
    public void testNonFileUriInPlan() throws IOException, DeploymentException {
        TestArtifactInfo planInfo = createPlanFile("uriplan", Version.emptyVersion, false, false, "http://www.eclipse.org");
        testPlan(planInfo);
    }

    @Test(expected = DeploymentException.class)
    public void testRelativeUriInPlan() throws IOException, DeploymentException {
        TestArtifactInfo planInfo = createPlanFile("uriplan", Version.emptyVersion, false, false, "file:" + TEST_RESOURCES_DIRECTORY + "global.jar");
        testPlan(planInfo);
    }

    private void testPlan(TestArtifactInfo plan) throws DeploymentException {
        DeploymentIdentity parentId = deploy(plan);
        this.deployer.undeploy(parentId);
    }

    private DeploymentIdentity deploy(TestArtifactInfo i) throws DeploymentException {
        DeploymentIdentity identity = this.deployer.deploy(i.getFile().toURI());
        Assert.assertEquals(i.getType(), identity.getType());
        Assert.assertEquals(i.getName(), identity.getSymbolicName());
        Assert.assertEquals(i.getVersion(), new Version(identity.getVersion()));
        return identity;
    }

    private static TestArtifactInfo createPlanFile(String planName, Version planVersion, boolean scoped, boolean atomic, String... uris)
        throws IOException {
        StringBuilder planContent = new StringBuilder(1024);
        planContent.append(XML_HEADER);
        planContent.append("<plan name=\"").append(planName).append("\" version=\"")
                .append(planVersion).append("\" scoped=\"").append(Boolean.valueOf(scoped)).append("\" atomic=\"")
                .append(Boolean.valueOf(atomic)).append("\" \n").append(NAMESPACES).append(">\n");

        for (String uri : uris) {
            planContent.append("    <artifact uri=\"").append(uri).append("\"/>\n");
        }

        planContent.append("</plan>");

        TestArtifactInfo info = new TestArtifactInfo(PLAN_TYPE, planName, planVersion);

        String fileName = planName + "-" + planVersion + PLAN_EXTENSION;
        File planFile = new File(GENERATED_PLAN_DIRECTORY + fileName);
        try (Writer writer = new FileWriter(planFile)) {
            writer.write(planContent.toString());
        }

        info.setFile(planFile);

        return info;
    }

    private static class TestArtifactInfo {

        private final String type;

        private final String name;

        private final Version version;

        private File file;

        TestArtifactInfo(String type, String name, Version version) {
            this.type = type;
            this.name = name;
            this.version = version;
        }

        public String getType() {
            return type;
        }

        public String getName() {
            return name;
        }

        public Version getVersion() {
            return version;
        }

        public void setFile(File file) {
            this.file = file;
        }

        public File getFile() {
            if (this.file == null) {
                throw new IllegalStateException("TestArtifactInfo file not set");
            }
            return file;
        }

        @Override
        public String toString() {
            return name;
        }

    }

}
