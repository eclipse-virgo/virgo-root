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

package org.eclipse.virgo.kernel.osgi.region.eventlog;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;

import org.eclipse.virgo.medic.eventlog.EventLoggerFactory;

/**
 * RegionAwareEventLoggerServiceFactory is a {@link ServiceFactory} for 
 * {@link EventLogger} instances in the user region. Medic's default ServiceFactory
 * cannot be used due to the nested framework service-sharing mechnism which
 * causes the wrong {@link Bundle} to be passed to {@link #getService(Bundle, ServiceRegistration)}
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * Thread-safe.
 *
 */
final class RegionAwareEventLoggerServiceFactory implements ServiceFactory {
    
    private final EventLoggerFactory eventLoggerFactory;
    
    /**
     * @param eventLoggerFactory
     */
    public RegionAwareEventLoggerServiceFactory(EventLoggerFactory eventLoggerFactory) {
        this.eventLoggerFactory = eventLoggerFactory;
    }

    /** 
     * {@inheritDoc}
     */
    public Object getService(Bundle bundle, ServiceRegistration registration) {
        return this.eventLoggerFactory.createEventLogger(bundle);
    }

    /** 
     * {@inheritDoc}
     */
    public void ungetService(Bundle bundle, ServiceRegistration registration, Object service) {
    }
}
