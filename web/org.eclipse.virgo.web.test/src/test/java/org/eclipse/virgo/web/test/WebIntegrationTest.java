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

package org.eclipse.virgo.web.test;

import static java.util.Collections.singletonList;
import static org.apache.http.HttpStatus.SC_OK;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.*;

import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentIdentity;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Web personality integration test.
 */
public class WebIntegrationTest extends AbstractWebIntegrationTests {

    @Test
    public void testOsgiWebapp() throws Exception {
//        int i = 10;
//        while (i++ < 1000) {
//            System.out.println("Waiting...in breakpoint?!");
//            Thread.sleep(1000);
//            System.out.println("Waiting...in breakpoint?!");
//        }
        assertDeployAndUndeployBehavior("osgi-webapp", new File("src/test/test-apps/osgi-webapp-3.8.0.jar"), "anything.txt");
    }

    @Test
    @Ignore
    public void testStandardWarWithNoManifest() throws Exception {
        assertDeployAndUndeployBehavior("no_manifest", new File("src/test/apps-static/no_manifest.war"), "index.jsp");
    }

    @Test
    @Ignore
    public void testStandardWarWithNoWebXml() throws Exception {
        Map<String, List<String>> expectations = new HashMap<>();
        expectations.put("index.jsp", singletonList("Default web.xml"));
        assertDeployAndUndeployBehavior("default_web_xml", new File("src/test/apps-static/default_web_xml.war"), expectations);
    }

    @Test
    @Ignore
    public void testStandardWarWithLowercaseFileExtension() throws Exception {
        assertDeployAndUndeployBehavior("helloweb", new File("src/test/apps-static/helloweb.war"), "index.jsp");
    }

    @Test
    @Ignore
    public void testWebApplicationBundle() throws Exception {
        assertDeployAndUndeployBehavior("helloweb", new File("src/test/apps-static/helloweb.jar"), "index.jsp");
    }

    @Test
    @Ignore
    public void testStandardWarWithUppercaseFileExtension() throws Exception {
        assertDeployAndUndeployBehavior("HELLOWEB2", new File("src/test/apps-static/HELLOWEB2.WAR"), "index.jsp");
    }

    @Test
    @Ignore
    public void testWarWithExplicitPackageImports() throws Exception {
        assertDeployAndUndeployBehavior("explicit_package_imports", new File("src/test/apps-static/explicit_package_imports.war"), "jstl.jsp",
            "foo.hello", "foo.jsr250");
    }

    @Test
    @Ignore
    public void testWarWithExplicitBundleImports() throws Exception {
        assertDeployAndUndeployBehavior("explicit_bundle_imports", new File("src/test/apps-static/explicit_bundle_imports.war"), "jstl.jsp",
            "foo.hello", "foo.jsr250");
    }

    @Test
    @Ignore
    public void testStandardWarWithCustomContextPathManifestHeader() throws Exception {
        assertDeployAndUndeployBehavior("enigma_reptilian", new File("src/test/apps-static/custom_context_path.war"), "index.jsp");
    }

    @Test
    @Ignore
    public void testIndependence() throws Exception {
        // Test that undeploying one WAR does not undeploy another, known as the "rm -rf *" feature.
        DeploymentIdentity deploymentIdentity = assertDeployBehavior("helloweb", new File("src/test/apps-static/helloweb.war"));
        assertDeployAndUndeployBehavior("HELLOWEB2", new File("src/test/apps-static/HELLOWEB2.WAR"), "index.jsp");
        assertGetRequest("helloweb", "index.jsp", SC_OK, null);
        assertUndeployBehavior("helloweb", deploymentIdentity);
    }

    @Test
    @Ignore
    public void duplicateContextPaths() throws Exception {
        DeploymentIdentity deployed1 = this.appDeployer.deploy(new File("src/test/apps-static/duplicate-context-path-1.war").toURI());
        try {
            this.appDeployer.deploy(new File("src/test/apps-static/duplicate-context-path-2.war").toURI());
            fail("Deployment of war with duplicate context path did not fail");
        } catch (DeploymentException ignored) {
            
        }
        this.appDeployer.undeploy(deployed1);
    }
}
