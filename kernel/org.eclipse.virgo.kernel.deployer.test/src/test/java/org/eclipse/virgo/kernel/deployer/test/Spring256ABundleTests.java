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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import org.osgi.framework.ServiceReference;
import org.springframework.context.ApplicationContext;

import org.eclipse.virgo.nano.deployer.api.core.ApplicationDeployer;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentIdentity;
import org.eclipse.virgo.test.framework.ConfigLocation;
import org.eclipse.virgo.util.io.PathReference;

@Ignore
@ConfigLocation("META-INF/spring256.test.config.properties")
public class Spring256ABundleTests extends AbstractDeployerIntegrationTest {

    private static final String BUNDLE_SYMBOLIC_NAME = "spring.256A.sample";

    private static final String BEAN_NAME = "sampleBean";

    private static final String expectedUserName = "admin";

    private static final String SPRING_256A_BUNDLE_SYMBOLIC_NAME = "spring.256A.sample";

    private static final String SPRING_256A_BUNDLE_VERSION = TEST_APPS_VERSION;

    private ApplicationDeployer appDeployer;

    private ServiceReference<ApplicationDeployer> appDeployerServiceReference;

    @Before
    public void setUp() throws Exception {
        PathReference pr = new PathReference("./target/work/org.eclipse.virgo.kernel");
        pr.delete(true);

        this.appDeployerServiceReference = this.context.getServiceReference(ApplicationDeployer.class);
        this.appDeployer = this.context.getService(this.appDeployerServiceReference);
    }

    @After
    public void tearDown() throws Exception {
        if (this.appDeployerServiceReference != null) {
            this.context.ungetService(this.appDeployerServiceReference);
        }
    }

    @Test
    public void testDeploymentOfBundleHavingSpring256A() throws Exception {
        File file = new File("src/test/resources/spring.256A.sample.jar");
        DeploymentIdentity deploymentId = this.appDeployer.deploy(file.toURI());

        assertDeploymentIdentityEquals(deploymentId, "spring.256A.sample.war", "bundle", SPRING_256A_BUNDLE_SYMBOLIC_NAME, SPRING_256A_BUNDLE_VERSION);

        ApplicationContext appCtx = ApplicationContextUtils.getApplicationContext(this.context, BUNDLE_SYMBOLIC_NAME);

        assertNotNull(appCtx);
        assertTrue(appCtx.containsBean(BEAN_NAME));

        Object beanName = appCtx.getBean(BEAN_NAME);
        assertEquals("UserName Value Getting from Bean is " + BUNDLE_SYMBOLIC_NAME, expectedUserName,
            beanName.getClass().getMethod("getUsername").invoke(beanName));

        this.appDeployer.undeploy(deploymentId.getType(), deploymentId.getSymbolicName(), deploymentId.getVersion());

        // Check that the spring 256A bundle's application context is destroyed.
        assertNull(ApplicationContextUtils.getApplicationContext(this.context, BUNDLE_SYMBOLIC_NAME));
    }

}
