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

import org.eclipse.virgo.kernel.osgi.framework.OsgiFramework;
import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.virgo.nano.deployer.api.core.ApplicationDeployer;
import org.junit.Test;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationAdmin;

import static org.junit.Assert.assertNotNull;

public class WebIntegrationSetupTest extends AbstractTomcatServerIntegrationTest {

    @Test
    public void kernelContextShouldNotBeNull() {
        assertNotNull(this.kernelContext);
    }

    @Test
    public void testApplicationDeployerService() {
        ServiceReference<ApplicationDeployer> appDeployerServiceReference = kernelContext.getServiceReference(ApplicationDeployer.class);
        assertNotNull("ApplicationDeployer service reference not found", appDeployerServiceReference);
        ApplicationDeployer appDeployer = kernelContext.getService(appDeployerServiceReference);
        assertNotNull("ApplicationDeployer service not found", appDeployer);
    }

    // required by web.core

    // not exported in region where the bundle runs - not yet installed?!
    // assertNotNull("'org.eclipse.gemini.web.core.WebContainer' service was not present", serviceReference);
    // assertNotNull("'org.eclipse.gemini.web.core.WebBundleManifestTransformer' service was not present", serviceReference);

    @Test
    public void testOsgiFrameworkService() {
        ServiceReference<OsgiFramework> serviceReference = context.getServiceReference(
                OsgiFramework.class);
        assertNotNull("'org.eclipse.virgo.kernel.osgi.framework.OsgiFramework' service was not present", serviceReference);
    }

    // covered by kernel test
    // assertNotNull("'org.eclipse.virgo.medic.eventlog.EventLogger' service was not present", serviceReference);

    @Test
    public void testConfigurationAdminService() {
        ServiceReference<ConfigurationAdmin> serviceReference = context.getServiceReference(
                ConfigurationAdmin.class);
        assertNotNull("'org.osgi.service.cm.ConfigurationAdmin' service was not present", serviceReference);
    }
}
