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

import org.osgi.framework.Version;

/**
 * {@link ApplicationDeploymentEvent} is the base class for all events relating to deployment of applications and
 * modules of applications.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is immutable and therefore thread safe.
 * 
 */
public abstract class ApplicationDeploymentEvent {

    private final String applicationSymbolicName;

    private final Version applicationVersion;

    /**
     * Construct a new {@link ApplicationDeploymentEvent} with the given application name and version.
     * 
     * @param applicationSymbolicName the symbolic name of the application
     * @param applicationVersion the version of the application
     */
    protected ApplicationDeploymentEvent(String applicationSymbolicName, Version applicationVersion) {
        this.applicationSymbolicName = applicationSymbolicName;
        this.applicationVersion = applicationVersion;
    }

    /**
     * Get the symbolic name of this event's application.
     * 
     * @return the application symbolic name
     */
    public String getApplicationSymbolicName() {
        return applicationSymbolicName;
    }

    /**
     * Get the version of this event's application.
     * 
     * @return the application version
     */
    public Version getApplicationVersion() {
        return applicationVersion;
    }

    /**
     * {@inheritDoc}
     */
    @Override public String toString() {
        return this.getClass().getName() + " for application " + this.applicationSymbolicName + " version " + this.applicationVersion;
    }

}
