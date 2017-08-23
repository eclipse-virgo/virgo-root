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

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.virgo.nano.deployer.api.core.DeploymentIdentity;
import org.junit.Test;

public class FilterCapabilityTests extends AbstractWebIntegrationTests {

	final Map<String, List<String>> expectations = new HashMap<String, List<String>>();
	final CountDownLatch latch = new CountDownLatch(1);
	
	@Test
	public void requestFilterWithMultipleUrlPatternsSpecifiedInTheSameFilterMapping() throws Exception{
		expectations.put("testFilterOne", Arrays
				.asList("fooTest Servlet"));
		expectations.put("testFilterTwo", Arrays
				.asList("fooTest Servlet"));
		
		latch.await(5, TimeUnit.SECONDS);
		DeploymentIdentity wardeploymentIdentity = assertDeployBehavior(
				"webtest", new File("src/test/apps/webfeature.tests.war"),
				expectations);

		for (String resource : expectations.keySet()) {
			List<String> expectedContents = expectations.get(resource);
			assertGetRequest("webtest", resource, SC_OK, expectedContents);
		}

		this.appDeployer.undeploy(wardeploymentIdentity);
	}

	@Test
	public void forwardFilter() throws Exception {
		expectations.put("/rd?test=forward-filter-servlet", Arrays
				.asList("barTest Servlet"));
		
		latch.await(5, TimeUnit.SECONDS);
		DeploymentIdentity wardeploymentIdentity = assertDeployBehavior(
				"webtest", new File("src/test/apps/webfeature.tests.war"),
				expectations);

		for (String resource : expectations.keySet()) {
			List<String> expectedContents = expectations.get(resource);
			assertGetRequest("webtest", resource, SC_OK, expectedContents);
		}

		this.appDeployer.undeploy(wardeploymentIdentity);
	}
	
	@Test
	public void includeFilter() throws Exception {
		expectations.put("/rd?test=include-filter-servlet", Arrays
				.asList("bazTest Servlet"));
		
		latch.await(5, TimeUnit.SECONDS);
		DeploymentIdentity wardeploymentIdentity = assertDeployBehavior(
				"webtest", new File("src/test/apps/webfeature.tests.war"),
				expectations);

		for (String resource : expectations.keySet()) {
			List<String> expectedContents = expectations.get(resource);
			assertGetRequest("webtest", resource, SC_OK, expectedContents);
		}

		this.appDeployer.undeploy(wardeploymentIdentity);
	}
}
