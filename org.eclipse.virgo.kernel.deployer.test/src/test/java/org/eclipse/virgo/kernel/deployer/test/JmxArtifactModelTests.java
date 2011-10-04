/*******************************************************************************
 * Copyright (c) 2011 VMware Inc.
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
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.eclipse.virgo.kernel.deployer.core.ApplicationDeployer;
import org.eclipse.virgo.kernel.deployer.core.DeploymentIdentity;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.ServiceReference;

/**
 * Test deployed artifacts show up correctly on JMX.
 * 
 */
public class JmxArtifactModelTests extends AbstractDeployerIntegrationTest {

    private static final String JMX_URL = "service:jmx:rmi:///jndi/rmi://localhost:9875/jmxrmi";

    private ServiceReference<ApplicationDeployer> appDeployerServiceReference;

    private ApplicationDeployer appDeployer;

    private MBeanServerConnection mBeanServerConnection;

    @Before
    public void setUp() throws Exception {
        this.appDeployerServiceReference = this.context.getServiceReference(ApplicationDeployer.class);
        this.appDeployer = (ApplicationDeployer) this.context.getService(this.appDeployerServiceReference);
        this.mBeanServerConnection = getMBeanServerConnection();
    }

    @After
    public void tearDown() throws Exception {
        if (this.appDeployerServiceReference != null) {
            this.context.ungetService(this.appDeployerServiceReference);
        }
    }

    @Test
    public void testDeployConfig() throws Exception {
        File file = new File("src/test/resources/configuration.deployment/t.properties");

        DeploymentIdentity deploymentIdentity = this.appDeployer.deploy(file.toURI());

        assertDeploymentIdentityEquals(deploymentIdentity, "t.properties", "configuration", "t", "0");
        
        assertArtifactState("configuration", "t", "0", "ACTIVE");

    }

    private void assertArtifactState(String type, String name, String version, String state) throws MalformedObjectNameException, IOException,
        Exception {
        assertArtifactExists(type, name, version);
        assertEquals(String.format("Artifact %s:%s:%s is not in state %s", type, name, version, state), state,
            this.mBeanServerConnection.getAttribute(getObjectName(type, name, version), "State"));
    }

    private void assertArtifactExists(String type, String name, String version) throws IOException, Exception, MalformedObjectNameException {
        assertTrue(String.format("Artifact %s:%s:%s does not exist", type, name, version),
            this.mBeanServerConnection.isRegistered(getObjectName(type, name, version)));
    }

    protected static ObjectName getObjectName(String type, String name, String version) throws MalformedObjectNameException {
        return new ObjectName(String.format("org.eclipse.virgo.kernel:type=Model,artifact-type=%s,name=%s,version=%s", type, name, version));
    }

    private static MBeanServerConnection getMBeanServerConnection() throws Exception {
        // String serverDir = null;
        // String[] creds = { "admin", "springsource" };
        // Map<String, String[]> env = new HashMap<String, String[]>();
        //
        // File testExpanded = new File("./../org.eclipse.virgo.server.svt/target/test-expanded/");
        // for (File mainDir : testExpanded.listFiles()) {
        // if (mainDir.isDirectory()) {
        // serverDir = new File(mainDir.toURI()).getCanonicalPath();
        // }
        // }
        // env.put(JMXConnector.CREDENTIALS, creds);
        // System.setProperty("javax.net.ssl.trustStore", serverDir + KEYSTORE);
        // System.setProperty("javax.net.ssl.trustStorePassword", KEYPASSWORD);
        JMXServiceURL url = new JMXServiceURL(JMX_URL);
        return JMXConnectorFactory.connect(url, null /* env */).getMBeanServerConnection();
    }

}
