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

package org.eclipse.virgo.kernel.agent.dm.internal;

import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.springframework.osgi.context.event.OsgiBundleApplicationContextListener;

import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.virgo.medic.eventlog.EventLoggerFactory;
import org.eclipse.virgo.util.osgi.ServiceRegistrationTracker;

/**
 * {@link BundleActivator} for the Kernel's Agent bundle
 * 
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread-safe.
 * 
 */
public final class AgentActivator implements BundleActivator {

    private final ServiceRegistrationTracker registrationTracker = new ServiceRegistrationTracker();

    /**
     * {@inheritDoc}
     */
    public void start(BundleContext context) {
        registerSpringDmToBlueprintEventAdapter(context);
    }

    private void registerSpringDmToBlueprintEventAdapter(BundleContext context) {
        registerBlueprintEventPostingOsgiBundleApplicationContextListener(context);
        registerDeployerFailureListener(context);
    }

    private void registerBlueprintEventPostingOsgiBundleApplicationContextListener(BundleContext context) {
        EventAdmin eventAdmin = getRequiredService(context, EventAdmin.class);
        BlueprintEventPostingOsgiBundleApplicationContextListener listener = new BlueprintEventPostingOsgiBundleApplicationContextListener(eventAdmin);
        this.registrationTracker.track(context.registerService(OsgiBundleApplicationContextListener.class.getName(), listener, null));
    }

    private void registerDeployerFailureListener(BundleContext context) {
        EventLoggerFactory eventLoggerFactory = getRequiredService(context, EventLoggerFactory.class);
        EventLogger eventLogger = eventLoggerFactory.createEventLogger(context.getBundle());
        DeployerFailureListener failureListener = new DeployerFailureListener(eventLogger);
        Dictionary<String, String[]> props = new Hashtable<String, String[]>();
        props.put(EventConstants.EVENT_TOPIC, new String[] { "org/osgi/service/blueprint/container/*" });
        this.registrationTracker.track(context.registerService(EventHandler.class.getName(), failureListener, props));
    }
    
    /**
     * {@inheritDoc}
     */
    public void stop(BundleContext context) throws Exception {
        this.registrationTracker.unregisterAll();
    }

    @SuppressWarnings("unchecked")
    private <T> T getRequiredService(BundleContext context, Class<T> clazz) {
        T result = null;
        ServiceReference ref = context.getServiceReference(clazz.getName());
        if (ref != null) {
            result = (T) context.getService(ref);
        }
        if (result == null) {
//TODO: is consuming service references reasonable if failures are retried many times? May need the following code.
//            if (ref != null) {
//                context.ungetService(ref); 
//            }
            throw new IllegalStateException("Unable to access required service of type '" + clazz.getName() + "' from bundle '"
                + context.getBundle().getSymbolicName() + "'");
        }
        return result;
    }
}
