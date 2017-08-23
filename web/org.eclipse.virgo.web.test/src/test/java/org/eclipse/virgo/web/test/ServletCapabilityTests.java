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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.virgo.nano.deployer.api.core.DeploymentIdentity;
import org.junit.Test;


public class ServletCapabilityTests extends AbstractWebIntegrationTests {

	final Map<String, List<String>> expectations = new HashMap<String, List<String>>();
	final List<String> expectedContent = new ArrayList<String>();
	final CountDownLatch latch = new CountDownLatch(1);

	@Test
	public void directJspRequest() throws Exception {
		expectedContent.add("index");
		latch.await(5, TimeUnit.SECONDS);
		assertDeployAndUndeployBehavior("webtest", new File(
				"src/test/apps/webfeature.tests.war"), "index.jsp");
	}

	@Test
	public void directServletRequest() throws Exception {
		expectations.put("test", Arrays.asList("Test Servlet"));
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
	public void requestDispatcherForwardServletRequest() throws Exception {
		expectations.put("forward/index.htm?foo=bar", Arrays.asList(
				"javax.servlet.forward.request_uri:/webtest/forward/index.htm",
				"javax.servlet.forward.context_path:/webtest",
				"javax.servlet.forward.servlet_path:/forward",
				"javax.servlet.forward.path_info:/index.htm",
				"javax.servlet.forward.query_string:foo=bar",
				"requestURI:/webtest/fa", "ContextPath:/webtest",
				"ServletPath:/fa", "PathInfo:null", "QueryString:foo=bar"));
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
	public void requestDispatcherMultipleForwardServletRequest()
			throws Exception {
		expectations.put("rd?test=forward-servlet", Arrays.asList(
				"javax.servlet.forward.request_uri:/webtest/rd",
				"javax.servlet.forward.context_path:/webtest",
				"javax.servlet.forward.servlet_path:/rd",
				"javax.servlet.forward.path_info:null",
				"javax.servlet.forward.query_string:test=forward-servlet",
				"requestURI:/webtest/fa", "ContextPath:/webtest",
				"ServletPath:/fa", "PathInfo:null",
				"QueryString:test=forward-servlet"));
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
	public void forwardWithPathTranslationPathMapping() throws Exception {
		expectations
				.put(
						"rd?test=forward-path-translation",
						Arrays
								.asList(
										"javax.servlet.forward.request_uri:/webtest/rd",
										"javax.servlet.forward.context_path:/webtest",
										"javax.servlet.forward.servlet_path:/rd",
										"javax.servlet.forward.path_info:null",
										"javax.servlet.forward.query_string:test=forward-path-translation",
										"requestURI:/webtest/fa/1",
										"ContextPath:/webtest",
										"ServletPath:/fa", "PathInfo:/1",
										"QueryString:test=forward-path-translation"));

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
	public void requestDispatcherIncludeServletRequest() throws Exception {
		expectations.put("rd?test=include-servlet", Arrays.asList(
				"Before forwarding the request to /ia in DispatcherServlet",
				"javax.servlet.include.request_uri:/webtest/ia",
				"javax.servlet.include.context_path:/webtest",
				"javax.servlet.include.servlet_path:/ia",
				"javax.servlet.include.path_info:null",
				"javax.servlet.include.query_string:null",
				"requestURI:/webtest/rd", "ContextPath:/webtest",
				"ServletPath:/rd", "PathInfo:null",
				"QueryString:test=include-servlet",
				"After forwarding the request to /ia in DispatcherServlet"));
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
	public void includeWithPathTranslationPathMapping() throws Exception {
		expectations.put("rd?test=include-path-translation", Arrays.asList(
				"javax.servlet.include.request_uri:/webtest/ia/1",
				"javax.servlet.include.context_path:/webtest",
				"javax.servlet.include.servlet_path:/ia",
				"javax.servlet.include.path_info:/1",
				"javax.servlet.include.query_string:null",
				"requestURI:/webtest/rd", "ContextPath:/webtest",
				"ServletPath:/rd", "PathInfo:null",
				"QueryString:test=include-path-translation"));
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
	public void requestDispatcherForwardJspRequest() throws Exception {
		expectations.put("rd?test=forward-jsp", Arrays.asList("index"));
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
	public void requestDispatcherIncludeJspRequest() throws Exception {
		expectations.put("rd?test=include-jsp", Arrays.asList("index"));
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
	public void requestPathTranslationPathMapping() throws Exception {
		expectations.put("rd?test=path-translation", Arrays.asList(
				"RequestUri=/webtest/path/1", "ContextPath=/webtest",
				"ServletPath=/path", "PathInfo=/1"));
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
	public void requestPathTranslationExact() throws Exception {
		expectations.put("rd?test=path-exact", Arrays.asList(
				"RequestUri=/webtest/exact", "ContextPath=/webtest",
				"ServletPath=/exact", "PathInfo=null"));
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
