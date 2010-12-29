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

import org.eclipse.virgo.kernel.osgi.region.IndeterminateRegionException;
import org.eclipse.virgo.kernel.osgi.region.Region;
import org.eclipse.virgo.kernel.osgi.region.RegionMembership;
import org.eclipse.virgo.kernel.serviceability.Assert;
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

    private volatile RegionMembership regionMembership;

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
        if (regionMembership == null) {
            Bundle agentBundle = FrameworkUtil.getBundle(getClass());
            BundleContext bundleContext = agentBundle.getBundleContext();
            ServiceReference<RegionMembership> regionMembershipServiceReference = bundleContext.getServiceReference(RegionMembership.class);
            if (regionMembershipServiceReference != null) {
                this.regionMembership = bundleContext.getService(regionMembershipServiceReference);
                try {
                    this.agentRegion = this.regionMembership.getRegion(agentBundle);
                } catch (IndeterminateRegionException e) {
                    Assert.isTrue(false, "Indeterminate agent region");
                }
            }
        }
        try {
            return this.regionMembership != null ? this.regionMembership.getRegion(bundle).equals(this.agentRegion) : true;
        } catch (IndeterminateRegionException e) {
            return true;
        }
    }

}
