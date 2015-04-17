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

import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static javax.servlet.http.HttpServletResponse.SC_OK;

import java.io.File;

import org.eclipse.virgo.util.io.PathReference;
import org.junit.Before;
import org.junit.Test;

/**
 * War in a directory.
 * 
 */

public class WarDirectoryDeploymentTests extends AbstractWebIntegrationTests {    

    @Before
    public void clean() {
        PathReference pickup = new PathReference("build/pickup");
        pickup.delete(true);
        pickup.createDirectory();
    }

    @Test
    public void warDirectoryTest() throws Exception {
        assertDeployAndUndeployBehavior("hellowebdir", new File("src/test/apps-static/hellowebdir.war").toURI(), "index.jsp");
    }

    @Test
    public void warDirectoryHotDeployTest() throws Exception {
        PathReference pr = new PathReference("src/test/apps-static/hellowebdir.war");
        PathReference deployed = hotDeploy(pr, "hellowebdir", "0.0.0");

        assertGetRequest("hellowebdir", "index.jsp", SC_OK);

        hotUnDeploy(deployed);

        assertGetRequest("hellowebdir", "index.jsp", SC_NOT_FOUND);
    }
}
