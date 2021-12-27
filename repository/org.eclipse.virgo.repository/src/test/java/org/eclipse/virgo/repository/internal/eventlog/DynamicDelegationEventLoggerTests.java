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

package org.eclipse.virgo.repository.internal.eventlog;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.osgi.framework.ServiceRegistration;

import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.virgo.medic.eventlog.Level;
import org.eclipse.virgo.medic.eventlog.LogEvent;
import org.eclipse.virgo.medic.test.eventlog.MockEventLogger;
import org.eclipse.virgo.test.stubs.framework.StubBundleContext;
import org.eclipse.virgo.test.stubs.support.ObjectClassFilter;

public class DynamicDelegationEventLoggerTests {
    
	@Test
    public void dynamicDelegation() {
        StubBundleContext bundleContext = new StubBundleContext();
        bundleContext.addFilter(new ObjectClassFilter(EventLogger.class));
        
        DynamicDelegationEventLogger delegatingEventLogger = new DynamicDelegationEventLogger(bundleContext);
        delegatingEventLogger.start();
        
        MockEventLogger eventLogger1 = new MockEventLogger();
		ServiceRegistration<EventLogger> registration1 = bundleContext
				.registerService(EventLogger.class, eventLogger1,
						null);
        
        MockEventLogger eventLogger2 = new MockEventLogger();
        ServiceRegistration<EventLogger> registration2 = bundleContext.registerService(EventLogger.class, eventLogger2, null);
        
        delegatingEventLogger.log("code1a", Level.INFO);
        delegatingEventLogger.log("code1b", Level.INFO, new Exception());
        delegatingEventLogger.log(new StubLogEvent("code1c", Level.INFO));
        delegatingEventLogger.log(new StubLogEvent("code1d", Level.INFO), new Exception());
        
        assertTrue(eventLogger1.isLogged("code1a", "code1b", "code1c", "code1d"));
        assertTrue(eventLogger2.isLogged("code1a", "code1b", "code1c", "code1d"));
        
        registration1.unregister();
        
        eventLogger1.reinitialise();
        eventLogger2.reinitialise();
        
        delegatingEventLogger.log("code2", Level.INFO);
        assertFalse(eventLogger1.isLogged("code2"));
        assertTrue(eventLogger2.isLogged("code2"));
        
        registration2.unregister();
        
        eventLogger1.reinitialise();
        eventLogger2.reinitialise();
        
        delegatingEventLogger.log("code3", Level.INFO);
        assertFalse(eventLogger1.isLogged("code3"));
        assertFalse(eventLogger2.isLogged("code3"));
       
        delegatingEventLogger.stop();
    }
    
    private static final class StubLogEvent implements LogEvent {
        
        private final String eventCode;
        
        private final Level level;
                        
        private StubLogEvent(String eventCode, Level level) {
            this.eventCode = eventCode;
            this.level = level;
        }

        public String getEventCode() {
            return eventCode;
        }
        
        public Level getLevel() {
            return level;
        }
    }
}
