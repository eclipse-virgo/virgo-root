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

package org.eclipse.virgo.nano.serviceability.dump;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.virgo.medic.dump.DumpGenerator;

/**
 * Advises throws of {@link RuntimeException} and triggers {@link DumpGenerator#generateDump(String,Throwable...) a dump}.
 * <p/>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe.
 * 
 */
public final aspect DumpCoordinator {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    private volatile DumpGenerator dumpGenerator;

    private final Object monitor = new Object();

    pointcut serviceability() : within(org.eclipse.virgo.nano.serviceability..*);

    pointcut dumpCandidate() : (execution(* *(..)) || initialization(*.new(..))) && !serviceability();

    after() throwing(RuntimeException e) : dumpCandidate() {
        if (!FFDCExceptionState.seen(e)) {
            FFDCExceptionState.record(e);

            synchronized (this.monitor) {
                if (this.dumpGenerator != null) {
                    this.dumpGenerator.generateDump("error", e);
                } else {
                    logger.warn("No DumpGenerator available");
                }
            }
        }
    }
    
    // TODO Consider using a ServiceTracker
    public void setBundleContext(BundleContext bundleContext) {
        ServiceReference<DumpGenerator> serviceReference = bundleContext.getServiceReference(DumpGenerator.class);
        if (serviceReference != null) {
            DumpGenerator dumpGenerator = bundleContext.getService(serviceReference);
            if (dumpGenerator != null) {
                setDumpGenerator(dumpGenerator);
            } else {
                throw new IllegalStateException("DumpGenerator not available in the ServiceRegistry");
            }
        }
    }

    public void setDumpGenerator(DumpGenerator dumpGenerator) {
        synchronized (this.monitor) {
            this.dumpGenerator = dumpGenerator;
        }
    }
}
