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

package org.eclipse.virgo.kernel.test;

import static org.junit.Assert.assertNotNull;

import org.eclipse.virgo.nano.deployer.api.core.ApplicationDeployer;
import org.junit.Test;
import org.osgi.framework.ServiceReference;


/**
 */
public class StandardKernelIntegrationTests extends AbstractKernelIntegrationTest {

    @Test
    public void testRunKernelBundle() throws Exception {
        ServiceReference<ApplicationDeployer> serviceReference = context.getServiceReference(ApplicationDeployer.class);
        assertNotNull("Application deployer service was not present", serviceReference);
    }
}
