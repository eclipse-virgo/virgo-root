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

package org.eclipse.virgo.kernel.deployer.core.event;

import org.osgi.framework.Bundle;
import org.osgi.framework.Version;

/**
 * {@link ApplicationBundleDeploymentEvent} is the base class for all events relating to deployment of bundles of
 * applications.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is immutable and therefore thread safe.
 * 
 */
public abstract class ApplicationBundleDeploymentEvent extends ApplicationDeploymentEvent {

    private final Bundle bundle;

    /**
     * Construct a {@link ApplicationBundleDeploymentEvent} event with the given application symbolic name, application
     * version, and bundle.
     * 
     * @param applicationSymbolicName
     * @param applicationVersion
     */
    protected ApplicationBundleDeploymentEvent(String applicationSymbolicName, Version applicationVersion, Bundle bundle) {
        super(applicationSymbolicName, applicationVersion);
        this.bundle = bundle;
    }

    /**
     * Get this event's {@link Bundle}.
     * 
     * @return the bundle
     */
    public Bundle getBundle() {
        return this.bundle;
    }

    /**
     * {@inheritDoc}
     */
    @Override public String toString() {
        return this.getClass().getName() + " for bundle " + this.bundle.getSymbolicName() + " of application " + this.getApplicationSymbolicName()
            + " version " + this.getApplicationVersion();
    }

}
