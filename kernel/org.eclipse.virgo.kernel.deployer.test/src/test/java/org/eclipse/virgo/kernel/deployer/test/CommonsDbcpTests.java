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
import org.junit.Ignore;
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
        // TODO - rewrite test to use newer libraries
        this.deployer.deploy(new File(System.getProperty("user.home") + "/.gradle/caches/modules-2/files-2.1" +
                "/org.eclipse.virgo.mirrored/org.apache.commons.pool/1.6.0.v201204271246/d07ad53300e04f66b6245f54f06eeb255bd2d7b0/org.apache.commons.pool-1.6.0.v201204271246.jar").toURI());
        this.deployer.deploy(new File(System.getProperty("user.home") + "/.gradle/caches/modules-2/files-2.1" +
                "/org.eclipse.virgo.mirrored/org.apache.commons.dbcp/1.4.0.v201204271417/4378c1a6c057f1e1da2b8287351b288c2c13e6c0/org.apache.commons.dbcp-1.4.0.v201204271417.jar").toURI());
        this.deployer.deploy(new File("src/test/resources/com.springsource.platform.test.commons-dbcp.jar").toURI());
        ApplicationContextUtils.assertApplicationContextContainsExpectedBeanDefinitions(ApplicationContextUtils.getApplicationContext(this.context, "com.springsource.server.test.commons-dbcp"), "dataSourceTest");
    }    
}
