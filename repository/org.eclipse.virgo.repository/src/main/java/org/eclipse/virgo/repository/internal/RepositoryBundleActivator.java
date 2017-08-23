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

package org.eclipse.virgo.repository.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import org.eclipse.virgo.medic.dump.DumpContributor;
import org.eclipse.virgo.repository.HashGenerator;
import org.eclipse.virgo.repository.RepositoryFactory;
import org.eclipse.virgo.repository.codec.XMLRepositoryCodec;
import org.eclipse.virgo.repository.internal.eventlog.DynamicDelegationEventLogger;
import org.eclipse.virgo.util.osgi.ServiceRegistrationTracker;

/**
 * A {@link BundleActivator} for the repository. Publishes a {@link RepositoryFactory} implementation in the OSGi
 * service registry.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread-safe.
 * 
 */
public class RepositoryBundleActivator implements BundleActivator {

    private final ServiceRegistrationTracker tracker = new ServiceRegistrationTracker();

    private volatile DynamicDelegationEventLogger eventLogger = null;

    /**
     * {@inheritDoc}
     */
    public void start(BundleContext bundleContext) throws Exception {
        this.eventLogger = new DynamicDelegationEventLogger(bundleContext);
        this.eventLogger.start();

        RepositoryDumpContributor contributor = new RepositoryDumpContributor(new XMLRepositoryCodec());
        this.tracker.track(bundleContext.registerService(DumpContributor.class.getName(), contributor, null));

        RepositoryFactory repositoryFactory = new StandardRepositoryFactory(eventLogger, bundleContext, tracker, contributor);
        this.tracker.track(bundleContext.registerService(RepositoryFactory.class.getName(), repositoryFactory, null));

        HashGenerator hashGenerator = new ShaHashGenerator();
        this.tracker.track(bundleContext.registerService(HashGenerator.class.getName(), hashGenerator, null));
    }

    /**
     * {@inheritDoc}
     */
    public void stop(BundleContext bundleContext) throws Exception {
        this.tracker.unregisterAll();

        if (this.eventLogger != null) {
            this.eventLogger.stop();
        }
    }
}
