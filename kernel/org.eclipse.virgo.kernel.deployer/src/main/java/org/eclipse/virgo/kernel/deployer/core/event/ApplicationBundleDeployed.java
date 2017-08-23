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
 * {@link ApplicationBundleDeployed} is an event which is broadcast when an application bundle has been successfully deployed.
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * This class is immutable and therefore thread safe.
 *
 */
public class ApplicationBundleDeployed extends ApplicationBundleDeploymentEvent {

    /**
     * Construct a {@link ApplicationBundleDeployed} event with the given application symbolic name and version.
     * 
     * @param applicationSymbolicName of bundle
     * @param applicationVersion of bundle
     * @param bundle object deployed
     */
    public ApplicationBundleDeployed(String applicationSymbolicName, Version applicationVersion, Bundle bundle) {
        super(applicationSymbolicName, applicationVersion, bundle);
    }

}
