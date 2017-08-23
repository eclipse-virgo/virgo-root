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

package org.eclipse.virgo.web.core.internal;

import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.Version;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.gemini.web.core.WebContainer;
import org.eclipse.gemini.web.core.spi.ContextPathExistsException;

final class WebApplicationEventLogger implements EventHandler {
            
    private static final String EMPTY_CONTEXT_PATH = "";

    private static final String ROOT_CONTEXT_PATH = "/";

    private final EventLogger eventLogger;

    private static final Map<String, WebLogEvents> MAPPINGS;    

    static {
        Map<String, WebLogEvents> mappings = new HashMap<String, WebLogEvents>();
        mappings.put(WebContainer.EVENT_DEPLOYING, WebLogEvents.STARTING_WEB_BUNDLE);
        mappings.put(WebContainer.EVENT_DEPLOYED, WebLogEvents.STARTED_WEB_BUNDLE);
        mappings.put(WebContainer.EVENT_UNDEPLOYING, WebLogEvents.STOPPING_WEB_BUNDLE);
        mappings.put(WebContainer.EVENT_UNDEPLOYED, WebLogEvents.STOPPED_WEB_BUNDLE);
        MAPPINGS = mappings;
    }

    public WebApplicationEventLogger(EventLogger eventLogger) {
        this.eventLogger = eventLogger;
    }

    public void handleEvent(Event event) {
        String topic = event.getTopic();
        if (WebContainer.EVENT_FAILED.equals(topic)) {
            logFailure(event);
        } else {
            WebLogEvents logEvent = MAPPINGS.get(topic);
            if (logEvent != null) {
                this.eventLogger.log(logEvent, bundleName(event), bundleVersion(event), contextPathFor(event));
            }
        }
    }

    private void logFailure(Event event) {
        Exception ex = (Exception) event.getProperty(EventConstants.EXCEPTION);
        if (ex instanceof ContextPathExistsException) {
            this.eventLogger.log(WebLogEvents.WEB_BUNDLE_FAILED_CONTEXT_PATH_USED, bundleName(event), bundleVersion(event), contextPathFor(event));
        } else {
            this.eventLogger.log(WebLogEvents.WEB_BUNDLE_FAILED, bundleName(event), bundleVersion(event));
        }
    }

    private String contextPathFor(Event event) {
        String contextPath = (String) event.getProperty(WebContainer.EVENT_PROPERTY_CONTEXT_PATH);
        if(EMPTY_CONTEXT_PATH.equals(contextPath)) {
            return ROOT_CONTEXT_PATH;
        }
        return contextPath;
    }

    private String bundleName(Event event) {
        return (String) event.getProperty(EventConstants.BUNDLE_SYMBOLICNAME);
    }
    
    private Version bundleVersion(Event event) {
        return (Version) event.getProperty(WebContainer.EVENT_PROPERTY_BUNDLE_VERSION);
    }
}
