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

import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentIdentity;
import org.junit.Test;

/**
 * Web personality integration test.
 * 
 */
public class WebIntegrationTest extends AbstractWebIntegrationTests {
    
    @Test
    public void testStandardWarWithNoManifest() throws Exception {
        assertDeployAndUndeployBehavior("no_manifest", new File("src/test/apps-static/no_manifest.war"), "index.jsp");
    }

    @Test
    public void testStandardWarWithNoWebXml() throws Exception {
        Map<String, List<String>> expectations = new HashMap<String, List<String>>();
        expectations.put("index.jsp", Arrays.asList("Default web.xml"));
        assertDeployAndUndeployBehavior("default_web_xml", new File("src/test/apps-static/default_web_xml.war"), expectations);
    }

    @Test
    public void testStandardWarWithLowercaseFileExtension() throws Exception {
        assertDeployAndUndeployBehavior("helloweb", new File("src/test/apps-static/helloweb.war"), "index.jsp");
    }

    @Test
    public void testWebApplicationBundle() throws Exception {
        assertDeployAndUndeployBehavior("helloweb", new File("src/test/apps-static/helloweb.jar"), "index.jsp");
    }

    @Test
    public void testStandardWarWithUppercaseFileExtension() throws Exception {
        assertDeployAndUndeployBehavior("HELLOWEB2", new File("src/test/apps-static/HELLOWEB2.WAR"), "index.jsp");
    }

    @Test
    public void testWarWithExplicitPackageImports() throws Exception {
        assertDeployAndUndeployBehavior("explicit_package_imports", new File("src/test/apps-static/explicit_package_imports.war"), "jstl.jsp",
            "foo.hello", "foo.jsr250");
    }

    @Test
    public void testWarWithExplicitBundleImports() throws Exception {
        assertDeployAndUndeployBehavior("explicit_bundle_imports", new File("src/test/apps-static/explicit_bundle_imports.war"), "jstl.jsp",
            "foo.hello", "foo.jsr250");
    }

    @Test
    public void testStandardWarWithCustomContextPathManifestHeader() throws Exception {
        assertDeployAndUndeployBehavior("enigma_reptilian", new File("src/test/apps-static/custom_context_path.war"), "index.jsp");
    }

    @Test
    public void testIndependence() throws Exception {
        // Test that undeploying one WAR does not undeploy another, known as the "rm -rf *" feature.
        DeploymentIdentity deploymentIdentity = assertDeployBehavior("helloweb", new File("src/test/apps-static/helloweb.war"));
        assertDeployAndUndeployBehavior("HELLOWEB2", new File("src/test/apps-static/HELLOWEB2.WAR"), "index.jsp");
        assertGetRequest("helloweb", "index.jsp", SC_OK, null);
        assertUndeployBehavior("helloweb", deploymentIdentity);
    }

    @Test
    public void duplicateContextPaths() throws Exception {
        DeploymentIdentity deployed1 = this.appDeployer.deploy(new File("src/test/apps-static/duplicate-context-path-1.war").toURI());
        try {
            this.appDeployer.deploy(new File("src/test/apps-static/duplicate-context-path-2.war").toURI());
            fail("Deployment of war with duplicate context path did not fail");
        } catch (DeploymentException de) {
            
        }
        this.appDeployer.undeploy(deployed1);
    }
}
