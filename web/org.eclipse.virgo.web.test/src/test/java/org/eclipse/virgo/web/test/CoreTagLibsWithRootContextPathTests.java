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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

/**
 * Verifies that the use of <code>c:url</code> within a JSP in a web application deployed as the root context (i.e.,
 * "/") functions as expected.
 * 
 */
public class CoreTagLibsWithRootContextPathTests extends AbstractWebIntegrationTests {

    @Test
    public void testCoreTagLibsUrlSupportWithRootContextPath() throws Exception {
        Map<String, List<String>> expectations = new HashMap<String, List<String>>();
        expectations.put("", Arrays.asList("<link href=\"/css/style.css\""));
        assertDeployAndUndeployBehavior("/", new File("src/test/apps-static/core_taglibs_root_context_path.war"), expectations);
    }

}
