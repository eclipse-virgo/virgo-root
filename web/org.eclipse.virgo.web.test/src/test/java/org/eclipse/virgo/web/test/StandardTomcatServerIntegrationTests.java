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

import org.eclipse.equinox.region.RegionDigraph;
import org.eclipse.virgo.kernel.services.work.WorkArea;
import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.virgo.medic.eventlog.EventLoggerFactory;
import org.eclipse.virgo.nano.deployer.api.core.ApplicationDeployer;
import org.eclipse.virgo.repository.Repository;
import org.junit.Test;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.PackageAdmin;

import static org.junit.Assert.assertNotNull;

public class StandardTomcatServerIntegrationTests extends AbstractKernelIntegrationTest {

    @Test
    public void testRunKernelBundle() {
        ServiceReference<ApplicationDeployer> serviceReference = context.getServiceReference(ApplicationDeployer.class);
        assertNotNull("Application deployer service was not present", serviceReference);
    }

    public void testRepositoryService() {
        ServiceReference<Repository> serviceReference = context.getServiceReference(Repository.class);
        assertNotNull("Repository service was not present", serviceReference);
    }

    @Test
    public void testDeprecatedPackageAdminService() {
        ServiceReference<PackageAdmin> serviceReference = context.getServiceReference(PackageAdmin.class);
        assertNotNull("PackageAdmin service was not present", serviceReference);
    }

    @Test
    public void testEventLoggerFactoryService() {
        ServiceReference<EventLoggerFactory> serviceReference = context.getServiceReference(EventLoggerFactory.class);
        assertNotNull("EventLoggerFactory service was not present", serviceReference);
    }

    @Test
    public void testEventLoggerService() {
        ServiceReference<EventLogger> serviceReference = context.getServiceReference(EventLogger.class);
        assertNotNull("EventLogger service was not present", serviceReference);
    }

    @Test
    public void testRegionDigraphService() {
        ServiceReference<RegionDigraph> serviceReference = context.getServiceReference(RegionDigraph.class);
        assertNotNull("RegionDigraph service was not present", serviceReference);
    }

    @Test
    public void testWorkAreaService() {
        ServiceReference<WorkArea> serviceReference = context.getServiceReference(WorkArea.class);
        assertNotNull("WorkArea service was not present", serviceReference);
    }
}
