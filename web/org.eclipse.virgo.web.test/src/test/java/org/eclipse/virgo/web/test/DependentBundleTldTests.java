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

import java.io.File;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.virgo.nano.deployer.api.core.DeploymentIdentity;
import org.junit.Ignore;
import org.junit.Test;

import static java.util.Collections.singletonList;
import static org.apache.http.HttpStatus.SC_OK;

@Ignore
public class DependentBundleTldTests extends AbstractWebIntegrationTests {

	private final CountDownLatch latch = new CountDownLatch(1);
	private final Map<String, List<String>> expectations = new HashMap<>();

	@Test
	public void testTldsInDependentBundle() throws Exception {
		expectations.put("index.jsp", singletonList("OODMO"));
		latch.await(5, TimeUnit.SECONDS);
		DeploymentIdentity jarDeploymentIdentity = assertDeployBehavior(
				"tldbundletest", new File("src/test/apps/tld.bundle.jar"));
		latch.await(5, TimeUnit.SECONDS);
		DeploymentIdentity warDeploymentIdentity = assertDeployBehavior(
				"tldbundletest", new File("src/test/apps/tld.web.bundle.war"));
		for (String resource : expectations.keySet()) {
			List<String> expectedContents = expectations.get(resource);
			assertGetRequest("tldbundletest", resource, SC_OK, expectedContents);
		}
		this.appDeployer.undeploy(jarDeploymentIdentity);
		this.appDeployer.undeploy(warDeploymentIdentity);
	}

}
