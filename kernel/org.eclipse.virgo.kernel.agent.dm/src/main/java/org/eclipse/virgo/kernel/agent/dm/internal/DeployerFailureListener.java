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

import org.eclipse.equinox.region.Region;
import org.eclipse.equinox.region.RegionDigraph;
import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

/**
 * A {@link EventHandler} implementation that listens for and handles Blueprint container failure events.
 * <p />
 * 
 * Note that the events that are handled by this listener depend on the properties that are supplied when it is
 * registered as a service.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread-safe.
 * 
 */
final class DeployerFailureListener implements EventHandler {

    private static final String FAILURE_TOPIC = "org/osgi/service/blueprint/container/FAILURE";

    private final EventLogger eventLogger;

    private volatile RegionDigraph regionDigraph;

    private volatile Region agentRegion;

    public DeployerFailureListener(EventLogger eventLogger) {
        this.eventLogger = eventLogger;
    }

    /**
     * {@inheritDoc}
     */
    public void handleEvent(Event event) {
        if (FAILURE_TOPIC.equals(event.getTopic())) {
            Throwable failure = (Throwable) event.getProperty("exception");
            Bundle bundle = (Bundle) event.getProperty("bundle");
            if (inThisRegion(bundle)) {
                this.eventLogger.log(AgentLogEvents.BUNDLE_CONTEXT_FAILED, failure, bundle.getSymbolicName(), bundle.getVersion().toString());
            }
        }
    }

    private boolean inThisRegion(Bundle bundle) {
        if (regionDigraph == null) {
            Bundle agentBundle = FrameworkUtil.getBundle(getClass());
            BundleContext bundleContext = agentBundle.getBundleContext();
            ServiceReference<RegionDigraph> regionMembershipServiceReference = bundleContext.getServiceReference(RegionDigraph.class);
            if (regionMembershipServiceReference != null) {
                this.regionDigraph = bundleContext.getService(regionMembershipServiceReference);
                this.agentRegion = getRegion(agentBundle);
            }
        }
        return this.regionDigraph != null ? getRegion(bundle).equals(this.agentRegion) : true;
    }

    private Region getRegion(Bundle bundle) {
        for (Region region : this.regionDigraph) {
            if (region.contains(bundle)) {
                return region;
            }
        }
        return null;
    }

}
