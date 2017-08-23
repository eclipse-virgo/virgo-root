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
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.eclipse.virgo.nano.deployer.api.core.DeploymentIdentity;
import org.junit.Test;

public class ListenerCapabilityTests extends AbstractWebIntegrationTests {

	final Map<String, List<String>> expectations = new HashMap<String, List<String>>();
	final CountDownLatch latch = new CountDownLatch(1);
	private final MBeanServer server = ManagementFactory.getPlatformMBeanServer();


	@Test
	public void servletRequestListener() throws Exception {
		expectations.put("requestlistenertest", Arrays.asList(
				"Request Listener Test Servlet", "Product Name: virgo"));

		latch.await(5, TimeUnit.SECONDS);
		DeploymentIdentity wardeploymentIdentity = assertDeployBehavior(
				"listenertest", new File(
						"src/test/apps/listeners.tests.war"), expectations);

		for (String resource : expectations.keySet()) {
			List<String> expectedContents = expectations.get(resource);
			assertGetRequest("listenertest", resource, SC_OK, expectedContents);
		}

		this.appDeployer.undeploy(wardeploymentIdentity);
		server.unregisterMBean(getObjectName());
	}

	
	@Test
	public void servletContextListener() throws Exception {
		expectations.put("contextlistenertest", Arrays.asList(
				"Context Listener Test Servlet", "Username: admin",
				"Password: springsource"));

		latch.await(5, TimeUnit.SECONDS);
		DeploymentIdentity wardeploymentIdentity = assertDeployBehavior(
				"listenertest", new File(
						"src/test/apps/listeners.tests.war"), expectations);

		for (String resource : expectations.keySet()) {
			List<String> expectedContents = expectations.get(resource);
			assertGetRequest("listenertest", resource, SC_OK, expectedContents);
		}

		this.appDeployer.undeploy(wardeploymentIdentity);
		server.unregisterMBean(getObjectName());
	}


	@Test
	public void sessionListener() throws Exception {
		expectations.put("session.jsp", Arrays.asList("Session Id:"));
		latch.await(5, TimeUnit.SECONDS);
		DeploymentIdentity wardeploymentIdentity = assertDeployBehavior(
				"listenertest", new File(
						"src/test/apps/listeners.tests.war"), expectations);
		assertGetRequest("listenertest", "session.jsp", SC_OK);
		assertEquals(1,getSessionCount());
		expectations.put("session.jsp", Arrays.asList(getSessionId()));

		for (String resource : expectations.keySet()) {
			List<String> expectedContents = expectations.get(resource);
			assertGetRequest("listenertest", resource, SC_OK, expectedContents);
		}
		 server.invoke(getObjectName(), "invalidate", null, null);
	     server.invoke(getObjectName(), "awaitNextDecrement", null, null);
	     assertEquals(0, getSessionCount());

		this.appDeployer.undeploy(wardeploymentIdentity);
		server.unregisterMBean(getObjectName());
	}
	
	
	@Test
	public void sessionAttributeListener() throws Exception{
		expectations.put("sessionlistenertest", Arrays.asList(
				"foo1: bar1", "foo2: bar2"));

		latch.await(5, TimeUnit.SECONDS);
		DeploymentIdentity wardeploymentIdentity = assertDeployBehavior(
				"listenertest", new File(
						"src/test/apps/listeners.tests.war"), expectations);
	
       for (String resource : expectations.keySet()) {
			List<String> expectedContents = expectations.get(resource);
			assertGetRequest("listenertest", resource, SC_OK, expectedContents);
			assertEquals(3,getAddedSessionAttribute().size());
			assertEquals("virgo",getAddedSessionAttribute().get("product"));
			assertEquals("bar1",getAddedSessionAttribute().get("foo1"));
			assertEquals("bar2",getAddedSessionAttribute().get("foo2"));
			
			assertEquals(1,getRemovedSessionAttribute().size());
			assertEquals("bar2",getRemovedSessionAttribute().get("foo2"));
			
			assertEquals(2,getReplacedSessionAttribute().size());
			assertEquals("virgo",getReplacedSessionAttribute().get("product"));
			assertEquals("bar1",getReplacedSessionAttribute().get("foo1"));
			}

		this.appDeployer.undeploy(wardeploymentIdentity);
		server.unregisterMBean(getObjectName());
	}

	private int getSessionCount() throws Exception {
		ObjectName oname = getObjectName();
		int count = (Integer) server.getAttribute(oname, "SessionCount");
		return count;
	}
	
	public String getSessionId() throws Exception{
		ObjectName oname = getObjectName();
		String id = server.getAttribute(oname, "SessionId").toString();
		return id;
	}
	
	@SuppressWarnings("unchecked")
	public Map<String,Object> getAddedSessionAttribute() throws Exception{
		ObjectName oname = getObjectName();
		Map<String,Object> attList = (Map<String, Object>) server.getAttribute(oname, "AddedSessionAttribute");
		return attList;
	}
	
	@SuppressWarnings("unchecked")
	public Map<String,Object> getRemovedSessionAttribute() throws Exception{
		ObjectName oname = getObjectName();
		Map<String,Object> attList = (Map<String, Object>) server.getAttribute(oname, "RemovedSessionAttribute");
		return attList;
	}
	
	@SuppressWarnings("unchecked")
	public Map<String,Object> getReplacedSessionAttribute() throws Exception{
		ObjectName oname = getObjectName();
		Map<String,Object> attList = (Map<String, Object>) server.getAttribute(oname, "ReplacedSessionAttribute");
		return attList;
	}

	private ObjectName getObjectName() throws MalformedObjectNameException {
		return ObjectName.getInstance("test:name=HttpSessionListener");
	}
}
