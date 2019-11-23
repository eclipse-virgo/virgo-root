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

import org.eclipse.virgo.test.framework.dmkernel.DmKernelTestRunner;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import javax.management.*;
import java.lang.management.ManagementFactory;

@RunWith(DmKernelTestRunner.class)
public abstract class AbstractTomcatServerIntegrationTest extends AbstractKernelIntegrationTest {

    private static final String CURRENT_VERSION = "3.8.0";

    private static final long PLAN_DEPLOY_TIMEOUT = 5*60*1000; // 1 minutes

    @BeforeClass
    public static void awaitKernelStartup() throws Exception {
        AbstractKernelIntegrationTest.awaitKernelStartup();
    }

    @Before
    public void setup() throws Exception {
        super.setup();

        // Add asserts like assertThat(state, is("ACTIVE")) 3 times - see above, too!
        awaitGeminiBlueprintPlanStartup();
        // Add asserts like assertThat(state, is("ACTIVE")) 3 times - see above, too!
        awaitTomcatPlanStartup();
        // Add asserts like assertThat(state, is("ACTIVE")) 3 times - see above, too!
        awaitShellPlanStartup();
    }

    private void awaitGeminiBlueprintPlanStartup() throws MalformedObjectNameException, MBeanException, AttributeNotFoundException, ReflectionException, InterruptedException {
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        ObjectName objectName = new ObjectName("org.eclipse.virgo.kernel:type=ArtifactModel,artifact-type=plan,name=org.eclipse.virgo.kernel.userregion.blueprint,version=" + CURRENT_VERSION + ",region=global");

        Object state = null;
        long startTime = System.currentTimeMillis();

        while (!"ACTIVE".equals(state)) {
            try {
                state = mBeanServer.getAttribute(objectName, "State");
                System.out.println("Waiting for plan 'org.eclipse.virgo.kernel.userregion.blueprint'...");
                Thread.sleep(100);
            } catch (InstanceNotFoundException ignored) {
            }
            if (System.currentTimeMillis() - startTime > PLAN_DEPLOY_TIMEOUT) {
                throw new RuntimeException("Plan 'org.eclipse.virgo.kernel.userregion.blueprint' did not start within " + (PLAN_DEPLOY_TIMEOUT / 1000) + " seconds.");
            }
        }
    }

    private void awaitTomcatPlanStartup() throws MalformedObjectNameException, MBeanException, AttributeNotFoundException, ReflectionException, InterruptedException {
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        ObjectName objectName = new ObjectName("org.eclipse.virgo.kernel:type=ArtifactModel,artifact-type=plan,name=org.eclipse.virgo.web.tomcat,version=" + CURRENT_VERSION + ",region=global");

        Object state = null;
        long startTime = System.currentTimeMillis();

        while (!"ACTIVE".equals(state)) {
            try {
                state = mBeanServer.getAttribute(objectName, "State");
                System.out.println("Waiting for 'web.plan'...");
                Thread.sleep(100);
            } catch (InstanceNotFoundException ignored) {
            }
            if (System.currentTimeMillis() - startTime > PLAN_DEPLOY_TIMEOUT) {
                throw new RuntimeException("Web plan did not start within " + (PLAN_DEPLOY_TIMEOUT / 1000) + " seconds.");
            }
        }
    }

    private void awaitShellPlanStartup() throws MalformedObjectNameException, MBeanException, AttributeNotFoundException, ReflectionException, InterruptedException {
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        ObjectName objectName = new ObjectName("org.eclipse.virgo.kernel:type=ArtifactModel,artifact-type=plan,name=org.eclipse.virgo.shell,version=" + CURRENT_VERSION + ",region=global");

        Object state = null;
        long startTime = System.currentTimeMillis();

        while (!"ACTIVE".equals(state)) {
            try {
                state = mBeanServer.getAttribute(objectName, "State");
                System.out.println("Waiting for 'shell.plan'...");
                Thread.sleep(100);
            } catch (InstanceNotFoundException ignored) {
            }
            if (System.currentTimeMillis() - startTime > PLAN_DEPLOY_TIMEOUT) {
                throw new RuntimeException("Web plan did not start within " + (PLAN_DEPLOY_TIMEOUT / 1000) + " seconds.");
            }
        }
    }
}
