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

package org.eclipse.virgo.medic.dump.impl;

import static org.easymock.EasyMock.createMock;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.List;

import org.junit.Test;
import org.osgi.framework.ServiceRegistration;

import org.eclipse.virgo.medic.dump.DumpContributor;
import org.eclipse.virgo.medic.dump.impl.DumpContributorTracker;
import org.eclipse.virgo.test.stubs.framework.StubBundleContext;

public class DumpContributorTrackerTests {

    @SuppressWarnings("unchecked")
	@Test
    public void serviceAdditionAndRemoval() {
        StubBundleContext bundleContext = new StubBundleContext();
        DumpContributor service = createMock(DumpContributor.class);
        ServiceRegistration<DumpContributor> serviceRegistration = (ServiceRegistration<DumpContributor>)bundleContext.registerService(DumpContributor.class.getName(), service, null);

        DumpContributorTracker tracker = new DumpContributorTracker(bundleContext);

        tracker.addingService(serviceRegistration.getReference());
        List<DumpContributor> contributors = tracker.getDumpContributors();
        assertEquals(1, contributors.size());
        assertSame(service, contributors.get(0));

        tracker.removedService(serviceRegistration.getReference(), service);
        contributors = tracker.getDumpContributors();
        assertEquals(0, contributors.size());
    }
}
