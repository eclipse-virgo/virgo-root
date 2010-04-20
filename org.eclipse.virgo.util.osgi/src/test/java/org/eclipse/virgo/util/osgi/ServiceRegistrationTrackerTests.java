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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import org.eclipse.virgo.util.osgi.ServiceRegistrationTracker;
import org.junit.Test;
import org.osgi.framework.ServiceRegistration;


public class ServiceRegistrationTrackerTests {

    @Test
    public void testTrackAndUnregister() {
        
        ServiceRegistration registration = createMock(ServiceRegistration.class);
        registration.unregister();
        expectLastCall();
        
        replay(registration);
        
        ServiceRegistrationTracker tracker = new ServiceRegistrationTracker();
        tracker.track(registration);
        tracker.unregisterAll();
        
        verify(registration);
    }
}
