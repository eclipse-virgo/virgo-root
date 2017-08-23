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

import java.util.ArrayList;
import java.util.List;

import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.AppenderBase;

public class DefaultOutputAppender extends AppenderBase<LoggingEvent> {

    private static List<LoggingEvent> loggingEvents = new ArrayList<LoggingEvent>();

    @Override
    protected void append(LoggingEvent eventObject) {
        loggingEvents.add(eventObject);
    }

    static List<LoggingEvent> getAndResetLoggingEvents() {
        List<LoggingEvent> loggingEvents = DefaultOutputAppender.loggingEvents;
        DefaultOutputAppender.loggingEvents = new ArrayList<LoggingEvent>();
        return loggingEvents;
    }
}
