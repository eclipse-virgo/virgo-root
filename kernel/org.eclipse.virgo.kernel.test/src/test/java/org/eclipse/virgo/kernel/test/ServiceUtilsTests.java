/*******************************************************************************
 * Copyright (c) 2012 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.kernel.test;

import static org.eclipse.virgo.kernel.osgi.framework.ServiceUtils.getPotentiallyDelayedService;
import static org.eclipse.virgo.kernel.osgi.framework.ServiceUtils.getWaitLimitSeconds;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.eclipse.virgo.nano.deployer.api.core.ApplicationDeployer;
import org.junit.Test;

public class ServiceUtilsTests extends AbstractKernelIntegrationTest {

    @Test
    public void testServiceUtils() throws Exception {
        ApplicationDeployer service = getPotentiallyDelayedService(context, ApplicationDeployer.class);
        assertNotNull("Application deployer service was not present", service);
        assertEquals(181, getWaitLimitSeconds());
        
    }
}
