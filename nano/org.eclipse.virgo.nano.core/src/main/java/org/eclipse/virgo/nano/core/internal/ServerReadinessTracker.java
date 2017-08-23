/*******************************************************************************
 * Copyright (c) 2012 SAP AG
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   SAP AG - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.nano.core.internal;

import org.eclipse.virgo.nano.diagnostics.KernelLogEvents;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.osgi.framework.BundleContext;

public class ServerReadinessTracker implements EventHandler {
    
    private static final String TOPIC_RECOVERY_COMPLETED = "org/eclipse/virgo/kernel/deployer/recovery/COMPLETED";
    private EventLogger eventLogger;
    private BundleContext bundleContext;
    
    /** 
     * {@inheritDoc}
     */
    public void handleEvent(Event event) {
        if (TOPIC_RECOVERY_COMPLETED.equals(event.getTopic())) {
            Thread readinessPrinter = new Thread(new ServerReadinessPrinter(this.bundleContext, this.eventLogger));
            readinessPrinter.setName("startup-readiness");
            readinessPrinter.start();
        }
    }
    
    private class ServerReadinessPrinter implements Runnable {

        private BundleContext bundleContext;
        private EventLogger eventLogger;
        
        ServerReadinessPrinter(BundleContext bundleContext, EventLogger logger) {
            this.bundleContext = bundleContext;
            this.eventLogger = logger;
        }

        @Override
        public void run() {
            String frameworkStartTimeString = this.bundleContext.getProperty("eclipse.startTime");
            if (frameworkStartTimeString != null) {
                Long frameworkStartTime = Long.valueOf(frameworkStartTimeString);
                long sinceStart = System.currentTimeMillis() - frameworkStartTime;
                this.eventLogger.log(KernelLogEvents.VIRGO_STARTED, String.valueOf(sinceStart/1000) + "." + String.valueOf(sinceStart%1000));
            } else {
                this.eventLogger.log(KernelLogEvents.VIRGO_STARTED_NOTIME);
            }
        }
    }
    
    public void activate(BundleContext context) {
        this.bundleContext = context;
    }
    
    public void deactivate(BundleContext context) {
        this.bundleContext = null;
    }
    
    public void bindEventLogger(EventLogger logger) {
        this.eventLogger = logger;
    }
    
    public void unbindEventLogger(EventLogger logger) {
        this.eventLogger = null;
    }
    
}
