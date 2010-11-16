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

package org.eclipse.virgo.medic.eventlog.impl;

import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.virgo.medic.eventlog.EventLoggerFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;


public final class EventLoggerServiceFactory implements ServiceFactory<EventLogger> {

    private final EventLoggerFactory eventLoggerFactory;

    public EventLoggerServiceFactory(EventLoggerFactory factory) {
        this.eventLoggerFactory = factory;
    }

    public EventLogger getService(Bundle bundle, ServiceRegistration<EventLogger> registration) {
        return this.eventLoggerFactory.createEventLogger(bundle);
    }

    public void ungetService(Bundle bundle, ServiceRegistration<EventLogger> registration, EventLogger service) {
    }
}
