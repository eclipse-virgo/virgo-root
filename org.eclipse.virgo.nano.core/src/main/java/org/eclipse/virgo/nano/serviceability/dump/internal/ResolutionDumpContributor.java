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

package org.eclipse.virgo.nano.serviceability.dump.internal;

import java.io.File;

import org.eclipse.osgi.service.resolver.PlatformAdmin;
import org.eclipse.osgi.service.resolver.State;
import org.osgi.framework.BundleContext;

import org.eclipse.virgo.medic.dump.Dump;
import org.eclipse.virgo.medic.dump.DumpContributionFailedException;
import org.eclipse.virgo.medic.dump.DumpContributor;

/**
 * An implementation of {@link DumpContributor} that generates a resolution state dump.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread-safe
 * 
 */
public class ResolutionDumpContributor implements DumpContributor {

    // The following literal must match DependencyCalculator.RESOLUTION_STATE_KEY in the kernel userregion bundle.
    public final static String RESOLUTION_STATE_KEY = "resolution.state";

    private final ResolutionStateDumper resolutionStateDumper;

    public ResolutionDumpContributor(BundleContext bundleContext) {
        PlatformAdmin platformAdmin = bundleContext.getService(bundleContext.getServiceReference(PlatformAdmin.class));
            //OsgiFrameworkUtils.getService(bundleContext, PlatformAdmin.class).getService();
        this.resolutionStateDumper = new ResolutionStateDumper(new StandardSystemStateAccessor(platformAdmin), new StandardStateWriter(platformAdmin.getFactory()));
    }

    /**
     * {@inheritDoc}
     */
    public void contribute(Dump dump) throws DumpContributionFailedException {
        File outputFile = dump.createFile("osgi.zip");
        if (dump.getContext().containsKey(RESOLUTION_STATE_KEY)) {
            resolutionStateDumper.dump(outputFile, (State) dump.getContext().get(RESOLUTION_STATE_KEY));
        } else {
            resolutionStateDumper.dump(outputFile);
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getName() {
        return "resolution";
    }

}
