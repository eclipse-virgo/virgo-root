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

/**
 * {@link DeploymentListener} is used to listen for events relating to application deployment.
 * <p />
 * Application wide events extend {@link ApplicationDeploymentEvent} and are delivered synchronously by the thread which
 * initiated deployment.
 * <p />
 * Events for specific bundles of an application extend {@link ApplicationBundleDeploymentEvent} and may be delivered
 * during deployment or undeployment of the application or refresh of a bundle of the application.
 * <p />
 * Users of this interface should simply publish an object that implements this interface to the OSGi service registry.
 * <p />
 * 
 * @see ApplicationDeploymentEvent
 * @see ApplicationBundleDeploymentEvent
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations of this interface must be thread safe.
 * 
 */
public interface DeploymentListener {

    /**
     * Notify an {@link ApplicationDeploymentEvent}.
     * 
     * @param event the event being notified
     */
    void onEvent(ApplicationDeploymentEvent event);

}
