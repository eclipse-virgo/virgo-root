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

package org.eclipse.virgo.util.osgi;

import org.junit.Test;
import org.osgi.framework.ServiceRegistration;

import static org.easymock.EasyMock.*;


public class ServiceRegistrationTrackerTests {

    @Test
    public void testTrackAndUnregister() {
        ServiceRegistration<?> registration = createServiceRegistration();

        replay(registration);

        ServiceRegistrationTracker tracker = new ServiceRegistrationTracker();
        tracker.track(registration);
        tracker.unregister(registration);

        verify(registration);
    }

    @Test
    public void testTrackAndUnregisterAll() {
        ServiceRegistration<?> registration1 = createServiceRegistration();
        ServiceRegistration<?> registration2 = createServiceRegistration();
        replay(registration1, registration2);

        ServiceRegistrationTracker tracker = new ServiceRegistrationTracker();
        tracker.track(registration1);
        tracker.track(registration2);
        tracker.unregisterAll();

        verify(registration1, registration2);
    }

    private ServiceRegistration<?> createServiceRegistration() {
        ServiceRegistration<?> registration = createMock(ServiceRegistration.class);
        registration.unregister();
        expectLastCall();
        return registration;
    }

}
