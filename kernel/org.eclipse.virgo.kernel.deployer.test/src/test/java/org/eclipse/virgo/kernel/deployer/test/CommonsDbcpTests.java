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

package org.eclipse.virgo.kernel.deployer.test;

import java.io.File;

import org.hsqldb.Server;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import org.eclipse.virgo.util.io.FileSystemUtils;

public class CommonsDbcpTests extends AbstractDeployerIntegrationTest {

    private static Server server;

    @BeforeClass
    public static void setupDatabase() {
        FileSystemUtils.deleteRecursively(new File("build/db"));
        server = new Server();
        server.setDatabasePath(0, "build/db/commons-dbcp-test-db");
        server.setDatabaseName(0, "");
        server.setSilent(true);
        server.start();
    }

    @AfterClass
    public static void stopDatabase() {
    	if (server != null) {
    		server.stop();
    	}
    }

    @Test
    public void testCommonsDbcpClassLoading() throws Exception {
        this.deployer.deploy(new File(System.getProperty("user.home") + "/virgo-build-cache/ivy-cache/repository/org.eclipse.virgo.mirrored/org.apache.commons.dbcp/1.4.0.v201204271417/org.apache.commons.dbcp-1.4.0.v201204271417.jar").toURI());
        this.deployer.deploy(new File("src/test/resources/com.springsource.platform.test.commons-dbcp.jar").toURI());
        ApplicationContextUtils.assertApplicationContextContainsExpectedBeanDefinitions(ApplicationContextUtils.getApplicationContext(this.context, "com.springsource.server.test.commons-dbcp"), "dataSourceTest");
    }    
}
