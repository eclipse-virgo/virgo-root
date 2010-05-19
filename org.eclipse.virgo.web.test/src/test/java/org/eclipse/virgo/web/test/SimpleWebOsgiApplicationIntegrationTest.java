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

import org.junit.Test;

/**
 * Web personality inside OSGi application integration test.
 * 
 */

public class SimpleWebOsgiApplicationIntegrationTest extends AbstractWebIntegrationTests {

    @Test
    public void basicTest() throws Exception {
        assertDeployAndUndeployBehavior("helloweb", new File("src/test/apps-static/app1.par"), "index.jsp");
    }

    @Test
    public void doubleWarTest() throws Exception {
        assertDeployAndUndeployBehavior(null, new File("src/test/apps-static/app2.par"), "helloweb/index.jsp", "bundleclasspath/index.html",
            "bundleclasspath/jstl.jsp", "bundleclasspath/foo.hello", "bundleclasspath/foo.jsr250");
    }
}
