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

package org.eclipse.virgo.medic.eventlog.impl.logback;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Locale;

import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.virgo.medic.eventlog.Level;
import org.eclipse.virgo.medic.eventlog.impl.MessageResolver;
import org.eclipse.virgo.medic.eventlog.impl.logback.LogBackEventLogger;
import org.junit.Test;

import ch.qos.logback.classic.spi.LoggingEvent;


public class LogBackEventLoggerTests {
    
    @Test
    public void defaultAndLocalizedOutput() {
        MessageResolver resolver = createMock(MessageResolver.class);
        
        EventLogger eventLogger = new LogBackEventLogger(resolver);
        
        expect(resolver.resolveLogEventMessage("UT0001E")).andReturn("the message {} {}");
        expect(resolver.resolveLogEventMessage("UT0002W")).andReturn("the message {} {}");
        expect(resolver.resolveLogEventMessage("UT0003I")).andReturn("the message {} {}");
        expect(resolver.resolveLogEventMessage("UT0001E", Locale.ENGLISH)).andReturn("the english message {} {}");
        expect(resolver.resolveLogEventMessage("UT0002W", Locale.ENGLISH)).andReturn("the english message {} {}");
        expect(resolver.resolveLogEventMessage("UT0003I", Locale.ENGLISH)).andReturn("the english message {} {}");
        replay(resolver);
        
        eventLogger.log("UT0001E", Level.ERROR, true, new Integer(63));
        eventLogger.log("UT0002W", Level.WARNING, true, new Integer(63));
        eventLogger.log("UT0003I", Level.INFO, true, new Integer(63));
        
        verify(resolver);
        
        List<LoggingEvent> localizedEvents = LocalizedOutputAppender.getAndResetLoggingEvents();
        assertEquals(3, localizedEvents.size());
        
        LoggingEvent loggingEvent = localizedEvents.get(0);
        assertEquals(ch.qos.logback.classic.Level.ERROR, loggingEvent.getLevel());
        assertEquals("the message true 63", loggingEvent.getMessage());
        assertEquals("UT0001E", loggingEvent.getMDCPropertyMap().get("medic.eventCode"));
        
        loggingEvent = localizedEvents.get(1);
        assertEquals(ch.qos.logback.classic.Level.WARN, loggingEvent.getLevel());
        assertEquals("the message true 63", loggingEvent.getMessage());
        assertEquals("UT0002W", loggingEvent.getMDCPropertyMap().get("medic.eventCode"));
        
        loggingEvent = localizedEvents.get(2);
        assertEquals(ch.qos.logback.classic.Level.INFO, loggingEvent.getLevel());
        assertEquals("the message true 63", loggingEvent.getMessage());
        assertEquals("UT0003I", loggingEvent.getMDCPropertyMap().get("medic.eventCode"));
        
        List<LoggingEvent> defaultEvents = DefaultOutputAppender.getAndResetLoggingEvents();
        assertEquals(3, defaultEvents.size());
        
        loggingEvent = defaultEvents.get(0);
        assertEquals(ch.qos.logback.classic.Level.ERROR, loggingEvent.getLevel());
        assertEquals("the english message true 63", loggingEvent.getMessage());
        //        assertEquals("UT0001E", loggingEvent.getMDCPropertyMap().get("medic.eventCode"));
        
        loggingEvent = defaultEvents.get(1);
        assertEquals(ch.qos.logback.classic.Level.WARN, loggingEvent.getLevel());
        assertEquals("the english message true 63", loggingEvent.getMessage());
        //        assertEquals("UT0002W", loggingEvent.getMDCPropertyMap().get("medic.eventCode"));
        
        loggingEvent = defaultEvents.get(2);
        assertEquals(ch.qos.logback.classic.Level.INFO, loggingEvent.getLevel());
        assertEquals("the english message true 63", loggingEvent.getMessage());
        //        assertEquals("UT0003I", loggingEvent.getMDCPropertyMap().get("medic.eventCode"));
    }
    
    @Test
    public void handlingOfMissingMessages() {
        MessageResolver resolver = createMock(MessageResolver.class);
        
        EventLogger eventLogger = new LogBackEventLogger(resolver);
        
        expect(resolver.resolveLogEventMessage("UT0001")).andReturn(null);
        expect(resolver.resolveLogEventMessage("UT0001", Locale.ENGLISH)).andReturn(null);
        
        replay(resolver);
        
        eventLogger.log("UT0001", Level.ERROR, "apple", "orange", 345);
        
        verify(resolver);
        
        List<LoggingEvent> defaultEvents = DefaultOutputAppender.getAndResetLoggingEvents();
        assertEquals(1, defaultEvents.size());
        LoggingEvent loggingEvent = defaultEvents.get(0);
        assertEquals(ch.qos.logback.classic.Level.WARN, loggingEvent.getLevel());
        assertEquals("A message with the key 'UT0001' was not found. The inserts for the message were '[apple, orange, 345]'",
                     loggingEvent.getMessage());
        //        assertEquals("ME0001W", loggingEvent.getMDCPropertyMap().get("medic.eventCode"));
        
        List<LoggingEvent> localizedEvents = LocalizedOutputAppender.getAndResetLoggingEvents();
        assertEquals(1, localizedEvents.size());
        loggingEvent = localizedEvents.get(0);
        assertEquals(ch.qos.logback.classic.Level.WARN, loggingEvent.getLevel());
        assertEquals("A message with the key 'UT0001' was not found. The inserts for the message were '[apple, orange, 345]'",
                     loggingEvent.getMessage());
        assertEquals("ME0001W", loggingEvent.getMDCPropertyMap().get("medic.eventCode"));
    }
}