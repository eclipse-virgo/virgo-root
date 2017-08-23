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
 * {@link ApplicationUndeployed} is an event which is broadcast when an application has been undeployed.
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * This class is immutable and therefore thread safe.
 *
 */
public class ApplicationUndeployed extends ApplicationDeploymentEvent {

    /**
     * Construct a {@link ApplicationUndeployed} event with the given application symbolic name and version.
     * 
     * @param applicationSymbolicName
     * @param applicationVersion
     */
    public ApplicationUndeployed(String applicationSymbolicName, Version applicationVersion) {
        super(applicationSymbolicName, applicationVersion);
    }

}
