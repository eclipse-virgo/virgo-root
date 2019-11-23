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
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests for web applications which attempt to load resources from the <code>ApplicationContext</code> (which
 * may be <em>Spring-DM powered</em>) and via the <code>ServletContext</code>.
 * 
 */
@Ignore
public class ResourceLoadingTests extends AbstractWebIntegrationTests {

    protected static String encode(String str) throws Exception {
        return URLEncoder.encode(str, "UTF-8");
    }

    protected void configureServletContextGetResourceAsStreamExpectations(Map<String, List<String>> expectations) {
        expectations.put("servletContextGetResourceAsStream?path=/WEB-INF/resource.xml",
            Arrays.asList("From ServletContext via path [/WEB-INF/resource.xml]"));
    }

    protected void configureAppCtxGetResourceGetFileExpectations(Map<String, List<String>> expectations) {
        expectations.put("appCtxGetResourceGetFile?path=/WEB-INF/resource.xml",
            Arrays.asList("From ApplicationContext/ResourceLoader's getResource().getFile() for [/WEB-INF/resource.xml]: file exists: true"));
    }

    protected void configureAppCtxGetResourceGetInputStreamExpectationsHelper(final Map<String, List<String>> expectations, final String modifiedPath) {
        expectations.put("appCtxGetResourceGetInputStream?path=" + modifiedPath,
            Arrays.asList("From ApplicationContext/ResourceLoader's getResource().getInputStream() for [" + modifiedPath + "]"));
    }

    protected List<String> getAppCtxGetResourceGetInputStreamTestPaths() {
        return Arrays.asList("WEB-INF/resource.xml");
    }

    protected void configureAppCtxGetResourceGetInputStreamExpectations(final Map<String, List<String>> expectations) {
        for (String path : getAppCtxGetResourceGetInputStreamTestPaths()) {
            configureAppCtxGetResourceGetInputStreamExpectationsHelper(expectations, path);
            configureAppCtxGetResourceGetInputStreamExpectationsHelper(expectations, "/" + path);

            // Note: the following are expected to fail, since WEB-INF should not be on the classpath in a WAR
            // configureAppCtxGetResourceGetInputStreamExpectationsHelper(expectations, "classpath:" + path);
            // configureAppCtxGetResourceGetInputStreamExpectationsHelper(expectations, "classpath:" + "/" + path);
        }
    }

    protected void configureClasspathStarExpectations(Map<String, List<String>> expectations) throws Exception {
        expectations.put("appCtxGetResources?path=" + encode("classpath*:META-INF/resource-1.xml"), Arrays.asList("<resource>1</resource>"));
        expectations.put("appCtxGetResources?path=" + encode("classpath*:resource-2.xml"), Arrays.asList("<resource>2</resource>"));
    }

    protected void configureSwfExpectations(final Map<String, List<String>> expectations) throws Exception {

        // http://localhost:48080/web_module_resource_loading/appCtxGetResourcesLikeSwfFlowDefinitionResourceFactory

        String baseUri = "appCtxGetResourcesLikeSwfFlowDefinitionResourceFactory";
        String baseUriWithFlowsPattern = baseUri + "?pattern=" + encode("/**/*-flow.xml");
        String basePath = "&basePath=" + encode("/WEB-INF/");

        // Without basePath:
        expectations.put(baseUri + "?pattern=/WEB-INF/rewards/newReward/newReward-flow.xml", Arrays.asList("Flow ID: newReward-flow"));
        expectations.put(baseUriWithFlowsPattern, Arrays.asList("Flow ID: newReward-flow", "Flow ID: oldReward-flow"));

        // With basePath:
        expectations.put(baseUri + "?pattern=/rewards/newReward/newReward-flow.xml" + basePath, Arrays.asList("Flow ID: rewards/newReward"));
        expectations.put(baseUriWithFlowsPattern + basePath, Arrays.asList("Flow ID: rewards/newReward", "Flow ID: rewards/oldReward"));
    }

    protected String getWebAppContextPath() {
        return "war_resource_loading";
    }

    protected File getWebAppFile() {
        return new File("src/test/apps/war_resource_loading.war");
    }
    
    @Test
    public void testApplicationContextAndServletContextResourceLoadingInWebApp() throws Exception {
        
        this.appDeployer.deploy(new File("src/test/apps-static/resource-bundle").toURI());

        Map<String, List<String>> expectations = new HashMap<String, List<String>>();

        configureServletContextGetResourceAsStreamExpectations(expectations);
        configureAppCtxGetResourceGetFileExpectations(expectations);
        configureAppCtxGetResourceGetInputStreamExpectations(expectations);
        configureClasspathStarExpectations(expectations);
        configureSwfExpectations(expectations);

        assertDeployAndUndeployBehavior(getWebAppContextPath(), getWebAppFile(), expectations);
    }

}
