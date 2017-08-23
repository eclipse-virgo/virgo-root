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

import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.virgo.medic.eventlog.EventLoggerFactory;
import org.eclipse.virgo.medic.eventlog.impl.LocaleResolver;
import org.eclipse.virgo.medic.eventlog.impl.MessageResolver;
import org.eclipse.virgo.medic.eventlog.impl.PropertyResourceBundleResolver;
import org.eclipse.virgo.medic.eventlog.impl.StandardMessageResolver;
import org.osgi.framework.Bundle;


public class LogBackEventLoggerFactory implements EventLoggerFactory {

    private final Bundle secondaryBundle;

    private final PropertyResourceBundleResolver resourceBundleResolver;

    private final LocaleResolver localeResolver;

    public LogBackEventLoggerFactory(PropertyResourceBundleResolver resourceBundleResolver, LocaleResolver localeResolver, Bundle secondaryBundle) {
        this.secondaryBundle = secondaryBundle;
        this.resourceBundleResolver = resourceBundleResolver;
        this.localeResolver = localeResolver;
    }

    public EventLogger createEventLogger(Bundle primaryBundle) {
        MessageResolver messageResolver = new StandardMessageResolver(this.localeResolver, resourceBundleResolver, primaryBundle,
            this.secondaryBundle);
        return new LogBackEventLogger(messageResolver);
    }
}
