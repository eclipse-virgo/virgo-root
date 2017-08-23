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
 * {@link ApplicationBundleStarted} is an event which is broadcast when an application bundle has been started.
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * This class is immutable and therefore thread safe.
 *
 */
public class ApplicationBundleStarted extends ApplicationBundleDeploymentEvent {

    /**
     * Construct a {@link ApplicationBundleStarted} event with the given application symbolic name and version.
     * 
     * @param applicationSymbolicName of bundle
     * @param applicationVersion of bundle
     * @param bundle object started
     */
    public ApplicationBundleStarted(String applicationSymbolicName, Version applicationVersion, Bundle bundle) {
        super(applicationSymbolicName, applicationVersion, bundle);
    }

}
