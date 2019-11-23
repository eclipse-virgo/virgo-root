/*******************************************************************************
 * Copyright (c) 2011 SAP AG
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Violeta Georgieva - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.web.test;

import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.virgo.nano.deployer.api.core.DeploymentIdentity;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentOptions;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class WebAppThreadContextClassloaderTests extends AbstractWebIntegrationTests {

    @Test
    public void retrieveResourceFromTCC() throws Exception {
        URI testBundle = new File("src/test/apps/war-load-resource-from-tcc.bundle.jar").toURI();
        DeploymentIdentity deploymentIdentity = this.appDeployer.deploy(testBundle, new DeploymentOptions(false, false, true));

        try {
            File testWeb = new File("src/test/apps/war-load-resource-from-tcc.web.war");
            Map<String, List<String>> expectations = new HashMap<String, List<String>>();
            expectations.put("", Arrays.asList("Size: 1"));
            assertDeployAndUndeployBehavior("war-load-resource-from-tcc.web", testWeb, expectations);
        } finally {
            this.appDeployer.undeploy(deploymentIdentity);
        }
    }
}
