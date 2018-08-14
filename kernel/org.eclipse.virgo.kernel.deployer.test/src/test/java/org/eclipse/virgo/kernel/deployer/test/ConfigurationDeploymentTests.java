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

import static org.eclipse.virgo.kernel.deployer.test.util.ConfigurationTestUtils.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.Dictionary;

import org.junit.*;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.Version;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationEvent;
import org.osgi.service.cm.ConfigurationListener;

import org.eclipse.virgo.nano.deployer.api.core.ApplicationDeployer;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentIdentity;
import org.eclipse.virgo.util.io.FileCopyUtils;

/**
 * Test deploying a configuration properties file.
 * 
 */
public class ConfigurationDeploymentTests extends AbstractDeployerIntegrationTest implements ConfigurationListener {

    private ServiceReference<ApplicationDeployer> appDeployerServiceReference;

    private ApplicationDeployer appDeployer;

    private ServiceReference<ConfigurationAdmin> configAdminServiceReference;

    private ConfigurationAdmin configAdmin;

    private volatile int cmUpdates;

    private volatile int cmDeletes;

    private ServiceRegistration<ConfigurationListener> configurationListenerServiceRegistration;

    @Before
    public void setUp() {
        this.appDeployerServiceReference = this.context.getServiceReference(ApplicationDeployer.class);
        this.appDeployer = this.context.getService(this.appDeployerServiceReference);
        this.configAdminServiceReference = this.context.getServiceReference(ConfigurationAdmin.class);
        this.configAdmin = this.context.getService(this.configAdminServiceReference);
        this.cmUpdates = 0;
        this.cmDeletes = 0;
        this.configurationListenerServiceRegistration = context.registerService(ConfigurationListener.class, this, null);
    }

    @After
    public void tearDown() {
        if (this.appDeployerServiceReference != null) {
            this.context.ungetService(this.appDeployerServiceReference);
        }
        if (this.configAdminServiceReference != null) {
            this.context.ungetService(this.configAdminServiceReference);
        }
        if (this.configurationListenerServiceRegistration != null) {
            this.configurationListenerServiceRegistration.unregister();
        }
    }

    @Test
    public void testDeployConfig() throws Exception {
        File file = new File("src/test/resources/configuration.deployment/t.properties");

        DeploymentIdentity deploymentIdentity = this.appDeployer.deploy(file.toURI());

        assertDeploymentIdentityEquals(deploymentIdentity, "t.properties", "configuration", "t", "0");
        Assert.assertTrue(isInDeploymentIdentities(appDeployer, deploymentIdentity));
        checkConfigAvailable();
        Assert.assertEquals(1, this.cmUpdates);
        Assert.assertEquals(0, this.cmDeletes);

        this.appDeployer.undeploy(deploymentIdentity);
        Assert.assertFalse(isInDeploymentIdentities(appDeployer, deploymentIdentity));
        checkConfigUnavailable();
        Assert.assertEquals(1, this.cmUpdates);
        Assert.assertEquals(1, this.cmDeletes);

        // Check that the configuration can be deployed again after being undeployed
        this.appDeployer.deploy(file.toURI());
        Assert.assertTrue(isInDeploymentIdentities(appDeployer, deploymentIdentity));
        checkConfigAvailable();
        Assert.assertEquals(2, this.cmUpdates);
        Assert.assertEquals(1, this.cmDeletes);

        // And that a deploy while deployed works as well
        this.appDeployer.deploy(file.toURI());
        Assert.assertTrue(isInDeploymentIdentities(appDeployer, deploymentIdentity));
        checkConfigAvailable();
        Assert.assertEquals(3, this.cmUpdates);
        Assert.assertEquals(1, this.cmDeletes);

        this.appDeployer.undeploy(deploymentIdentity);
        Assert.assertFalse(isInDeploymentIdentities(appDeployer, deploymentIdentity));
        checkConfigUnavailable();
        Assert.assertEquals(3, this.cmUpdates);
        Assert.assertEquals(2, this.cmDeletes);

    }

    @Test
    public void testHotDeployConfig() throws Exception {
        File source = new File("src/test/resources/configuration.deployment/t.properties");
        File target = new File("target/pickup/t.properties");

        if (target.exists()) {
            assertTrue(target.delete());
        }

        try {
            FileCopyUtils.copy(source, target);
            pollUntilInDeploymentIdentities(appDeployer, "configuration", "t", "0.0.0");

            checkConfigAvailable();

            target.delete();
            pollUntilNotInDeploymentIdentities(appDeployer, "configuration", "t", "0.0.0");
            checkConfigUnavailable();

            // Check that the configuration can be deployed again after being undeployed
            FileCopyUtils.copy(source, target);
            pollUntilInDeploymentIdentities(appDeployer, "configuration", "t", "0.0.0");
            checkConfigAvailable();

            // Trigger a redeploy of the file by the hot deployer and sleep till
            // the redeploy has taken effect.
            target.setLastModified(System.currentTimeMillis() + 1000);
            Thread.sleep(3000);
            pollUntilInDeploymentIdentities(appDeployer, "configuration", "t", "0.0.0");
            checkConfigAvailable();

            target.delete();
            pollUntilNotInDeploymentIdentities(appDeployer, "configuration", "t", "0.0.0");
            checkConfigUnavailable();
        } finally {
            target.delete();
        }
    }

    @Test
    public void testDeployEmptyConfig() throws Exception {
        File file = new File("src/test/resources/configuration.deployment/empty.properties");

        DeploymentIdentity deploymentIdentity = this.appDeployer.deploy(file.toURI());
        Assert.assertEquals("configuration", deploymentIdentity.getType());
        Assert.assertEquals("empty", deploymentIdentity.getSymbolicName());
        Assert.assertEquals(Version.emptyVersion, new Version(deploymentIdentity.getVersion()));

        Assert.assertTrue(isInDeploymentIdentities(appDeployer, deploymentIdentity));
        Configuration configuration = this.configAdmin.getConfiguration("empty", null);
        Dictionary<String, Object> dictionary = configuration.getProperties();
        Assert.assertEquals(1, dictionary.size());
        Assert.assertEquals("empty", dictionary.get("service.pid"));

        this.appDeployer.undeploy(deploymentIdentity);
        Assert.assertFalse(isInDeploymentIdentities(appDeployer, deploymentIdentity));
        configuration = this.configAdmin.getConfiguration("empty", null);
        Assert.assertNull(configuration.getProperties());
    }

    private void checkConfigAvailable() throws IOException, InvalidSyntaxException, InterruptedException {
        // Allow asynchronous delivery of configuration events to complete
        Thread.sleep(100);

        long start = System.currentTimeMillis();

        while (!isInConfigurationAdmin(configAdmin, "t")) {
            long delta = System.currentTimeMillis() - start;
            if (delta > 60000) {
                fail("Configuration was not available in ConfigAdmin within 60 seconds");
            }
            Thread.sleep(100);
        }

        Configuration configuration = this.configAdmin.getConfiguration("t", null);
        Dictionary<String, Object> dictionary = configuration.getProperties();
        Assert.assertEquals("t", dictionary.get("service.pid"));
        Assert.assertEquals("b", dictionary.get("a"));
    }

    private void checkConfigUnavailable() throws IOException, InvalidSyntaxException, InterruptedException {
        // Allow asynchronous delivery of configuration events to complete
        Thread.sleep(100);

        assertFalse(isInConfigurationAdmin(configAdmin, "t"));
    }

    @Override
    public void configurationEvent(ConfigurationEvent event) {
        switch (event.getType()) {
            case ConfigurationEvent.CM_UPDATED:
                this.cmUpdates++;
                break;
            case ConfigurationEvent.CM_DELETED:
                this.cmDeletes++;
                break;
            default:
                Assert.fail();
                break;
        }

    }

}
