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

import java.io.File;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.virgo.nano.deployer.api.core.DeploymentIdentity;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class FilterCapabilityTests extends AbstractWebIntegrationTests {

	private final Map<String, List<String>> expectations = new HashMap<>();
	private final CountDownLatch latch = new CountDownLatch(1);
	
	@Test
	public void requestFilterWithMultipleUrlPatternsSpecifiedInTheSameFilterMapping() throws Exception{
		expectations.put("testFilterOne", singletonList("fooTest Servlet"));
		expectations.put("testFilterTwo", singletonList("fooTest Servlet"));
		
		latch.await(5, TimeUnit.SECONDS);
		DeploymentIdentity warDeploymentIdentity = assertDeployBehavior(
				"webtest", new File("src/test/apps/webfeature.tests.war"),
				expectations);

		for (String resource : expectations.keySet()) {
			List<String> expectedContents = expectations.get(resource);
			assertGetRequest("webtest", resource, SC_OK, expectedContents);
		}

		this.appDeployer.undeploy(warDeploymentIdentity);
	}

	@Test
	public void forwardFilter() throws Exception {
		expectations.put("/rd?test=forward-filter-servlet", singletonList("barTest Servlet"));
		
		latch.await(5, TimeUnit.SECONDS);
		DeploymentIdentity warDeploymentIdentity = assertDeployBehavior(
				"webtest", new File("src/test/apps/webfeature.tests.war"),
				expectations);

		for (String resource : expectations.keySet()) {
			List<String> expectedContents = expectations.get(resource);
			assertGetRequest("webtest", resource, SC_OK, expectedContents);
		}

		this.appDeployer.undeploy(warDeploymentIdentity);
	}
	
	@Test
	public void includeFilter() throws Exception {
		expectations.put("/rd?test=include-filter-servlet", singletonList("bazTest Servlet"));
		
		latch.await(5, TimeUnit.SECONDS);
		DeploymentIdentity warDeploymentIdentity = assertDeployBehavior(
				"webtest", new File("src/test/apps/webfeature.tests.war"),
				expectations);

		for (String resource : expectations.keySet()) {
			List<String> expectedContents = expectations.get(resource);
			assertGetRequest("webtest", resource, SC_OK, expectedContents);
		}

		this.appDeployer.undeploy(warDeploymentIdentity);
	}
}
