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

public class JSTLCapabilityTests extends AbstractWebIntegrationTests {

	final Map<String, String> params = new HashMap<String, String>();
	final CountDownLatch latch = new CountDownLatch(1);
	final Map<String, List<String>> expectations = new HashMap<String, List<String>>();

	@Test
	public void testCout() throws Exception {
		latch.await(5, TimeUnit.SECONDS);
		DeploymentIdentity wardeploymentIdentity = assertDeployBehavior(
				"jstltest", new File("src/test/apps/jstl.tests.war"));

		assertGetRequest("jstltest", "jstltest1.jsp", SC_OK);
		params.put("text1", "abc");
		assertPostRequest("jstltest", "jstltest1.jsp", params, false, SC_OK,
				SC_OK, "NAME:abc");
		params.put("text2", "xyz");
		assertPostRequest("jstltest", "jstltest1.jsp", params, false, SC_OK,
				SC_OK, "PLACE:xyz");

		this.appDeployer.undeploy(wardeploymentIdentity);
	}

	@Test
	public void testJspUseBean() throws Exception {
		latch.await(5, TimeUnit.SECONDS);
		DeploymentIdentity wardeploymentIdentity = assertDeployBehavior(
				"jstltest", new File("src/test/apps/jstl.tests.war"));

		assertGetRequest("jstltest", "jstltest2.jsp", SC_OK);
		params.put("name", "abc");
		assertPostRequest("jstltest", "jstltest2.jsp", params, false, SC_OK,
				SC_OK, "Name: abc");
		params.put("place", "xyz");
		assertPostRequest("jstltest", "jstltest2.jsp", params, false, SC_OK,
				SC_OK, "Place: xyz");
		params.put("game", "lmn");
		assertPostRequest("jstltest", "jstltest2.jsp", params, false, SC_OK,
				SC_OK, "Game: lmn");

		this.appDeployer.undeploy(wardeploymentIdentity);
	}

	@Test
	public void testCif() throws Exception {
		latch.await(5, TimeUnit.SECONDS);
		DeploymentIdentity wardeploymentIdentity = assertDeployBehavior(
				"jstltest", new File("src/test/apps/jstl.tests.war"));

		assertGetRequest("jstltest", "jstltest3.jsp", SC_OK);
		params.put("combo1", "bar");
		assertPostRequest("jstltest", "jstltest3.jsp", params, false, SC_OK,
				SC_OK, "How Are You?....BAR!");
		params.put("combo1", "foo");
		assertPostRequest("jstltest", "jstltest3.jsp", params, false, SC_OK,
				SC_OK, "Good Morning...FOO!");

		this.appDeployer.undeploy(wardeploymentIdentity);
	}

	@Test
	public void testCChoose() throws Exception {
		latch.await(5, TimeUnit.SECONDS);
		DeploymentIdentity wardeploymentIdentity = assertDeployBehavior(
				"jstltest", new File("src/test/apps/jstl.tests.war"));

		assertGetRequest("jstltest", "jstltest4.jsp", SC_OK);
		params.put("combo1", "2");
		assertPostRequest("jstltest", "jstltest4.jsp", params, false, SC_OK,
				SC_OK, "Monday");
		params.put("combo1", "5");
		assertPostRequest("jstltest", "jstltest4.jsp", params, false, SC_OK,
				SC_OK, "Thursday");
		params.put("combo1", "6");
		assertPostRequest("jstltest", "jstltest4.jsp", params, false, SC_OK,
				SC_OK, "select between 1 & 5");
		params.put("combo1", "7");
		assertPostRequest("jstltest", "jstltest4.jsp", params, false, SC_OK,
				SC_OK, "select between 1 & 5");

		this.appDeployer.undeploy(wardeploymentIdentity);
	}

	@Test
	public void testCForEach() throws Exception {
		expectations.put("jstltest5.jsp", Arrays.asList("0", "1","true", "false" ,"red",
				"1","2", "false", "false", "green", "2","3", "false", "false", "blue",
				"3", "4" ,"false", "false", "orange", "4", "5", "false", "true" ,"black"));
		latch.await(5, TimeUnit.SECONDS);
		DeploymentIdentity wardeploymentIdentity = assertDeployBehavior(
				"jstltest", new File("src/test/apps/jstl.tests.war"));
		for (String resource : expectations.keySet()) {
			List<String> expectedContents = expectations.get(resource);
			assertGetRequest("jstltest",resource, SC_OK, expectedContents);
		}
		this.appDeployer.undeploy(wardeploymentIdentity);
	}
	
	@Test
	public void testForTokens() throws Exception{
		expectations.put("jstltest6.jsp", Arrays.asList("SAM","DELHI","MCA","24","90"));
		latch.await(5, TimeUnit.SECONDS);
		DeploymentIdentity wardeploymentIdentity = assertDeployBehavior(
				"jstltest", new File("src/test/apps/jstl.tests.war"));
		for (String resource : expectations.keySet()) {
			List<String> expectedContents = expectations.get(resource);
			assertGetRequest("jstltest", resource, SC_OK, expectedContents);
		}
		this.appDeployer.undeploy(wardeploymentIdentity);
	}
	
	@Test
	public void testCImport() throws Exception{
		expectations.put("jstltest7.jsp", Arrays.asList("WELCOME","to our web-site!"));
		latch.await(5, TimeUnit.SECONDS);
		DeploymentIdentity wardeploymentIdentity = assertDeployBehavior(
				"jstltest", new File("src/test/apps/jstl.tests.war"));
		for (String resource : expectations.keySet()) {
			List<String> expectedContents = expectations.get(resource);
			assertGetRequest("jstltest", resource, SC_OK, expectedContents);
		}
		this.appDeployer.undeploy(wardeploymentIdentity);
	}
	
	@Test
	public void testCRedirect() throws Exception{
		expectations.put("jstltest9.jsp", Arrays.asList("NAME:SAM"));
		latch.await(5, TimeUnit.SECONDS);
		DeploymentIdentity wardeploymentIdentity = assertDeployBehavior(
				"jstltest", new File("src/test/apps/jstl.tests.war"));
		for (String resource : expectations.keySet()) {
			List<String> expectedContents = expectations.get(resource);
			assertGetRequest("jstltest", resource, SC_OK, expectedContents);
		}
		this.appDeployer.undeploy(wardeploymentIdentity);
	}
	
	@Test
	public void testJspForward() throws Exception{
		expectations.put("jstltest10.jsp", Arrays.asList("NAME:","PLACE:"));
		latch.await(5, TimeUnit.SECONDS);
		DeploymentIdentity wardeploymentIdentity = assertDeployBehavior(
				"jstltest", new File("src/test/apps/jstl.tests.war"));
		for (String resource : expectations.keySet()) {
			List<String> expectedContents = expectations.get(resource);
			assertGetRequest("jstltest", resource, SC_OK, expectedContents);
		}
		this.appDeployer.undeploy(wardeploymentIdentity);
	}
	
	@Test
	public void testJspInclude() throws Exception{
		expectations.put("jstltest11.jsp", Arrays.asList("Before forwarded to another jsp","Name:","Place:","Game:","After forwarded to another jsp"));
		latch.await(5, TimeUnit.SECONDS);
		DeploymentIdentity wardeploymentIdentity = assertDeployBehavior(
				"jstltest", new File("src/test/apps/jstl.tests.war"));
		for (String resource : expectations.keySet()) {
			List<String> expectedContents = expectations.get(resource);
			assertGetRequest("jstltest", resource, SC_OK, expectedContents);
		}
		this.appDeployer.undeploy(wardeploymentIdentity);
	}
}
