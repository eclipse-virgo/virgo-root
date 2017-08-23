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

package org.eclipse.virgo.kernel.deployer.core.internal.event;

import java.net.URI;

import org.eclipse.virgo.nano.deployer.api.core.DeploymentOptions;



/**
 * A <code>DeploymentListener</code> is notified of deployment events.
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * Thread-safe.
 *
 */
public interface DeploymentListener {
    
    void refreshed(URI sourceLocation);
    
    void deployed(URI sourceLocation, DeploymentOptions deploymentOptions);
    
    void undeployed(URI sourceLocation);
}
