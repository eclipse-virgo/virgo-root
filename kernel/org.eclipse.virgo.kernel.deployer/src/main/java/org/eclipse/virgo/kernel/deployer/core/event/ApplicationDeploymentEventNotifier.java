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

import java.util.Set;

import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ApplicationDeploymentEventNotifier} is used by the deployer to notify all {@link DeploymentListener
 * ApplicationDeploymentListeners} of an application deployment event.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is thread safe.
 * 
 */
class ApplicationDeploymentEventNotifier implements DeploymentListener {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Set<DeploymentListener> listeners;

    /**
     * Construct an {@link ApplicationDeploymentEventNotifier} for the given set of listeners. The given set will be
     * modified externally to this class as listeners come and go.
     * 
     * @param listeners the listeners to be notified
     */
    ApplicationDeploymentEventNotifier(Set<DeploymentListener> listeners) {
        this.listeners = listeners;
    }

    /**
     * {@inheritDoc}
     */
    public void onEvent(ApplicationDeploymentEvent event) {
        Bundle bundle = null;
        if (event instanceof ApplicationBundleDeploymentEvent) {
            bundle = ((ApplicationBundleDeploymentEvent) event).getBundle();
        }

        if (bundle == null) {
            logger.info("Delivering '{}' for application '{}' version '{}' to application deployment listeners", new Object[] {
                event.getClass().getName(), event.getApplicationSymbolicName(), event.getApplicationVersion() });
        } else {
            logger.info("Delivering '{}' for bundle '{}' of application '{}' version '{}' to application deployment listeners", new Object[] {
                event.getClass().getName(), bundle.getSymbolicName(), event.getApplicationSymbolicName(), event.getApplicationVersion() });
        }

        for (DeploymentListener listener : listeners) {
            try {
                listener.onEvent(event);
            } catch (RuntimeException e) {
                // Trace and ignore the exception.
                logger.error("Application deployment listener '{}' threw exception", e, listener);
            }
        }

        if (bundle == null) {
            logger.info("Delivered '{}' for application '%s' version '{}' to application deployment listeners", new Object[] {
                event.getClass().getName(), event.getApplicationSymbolicName(), event.getApplicationVersion() });
        } else {
            logger.info("Delivered '{}' for bundle '{}' of application '{}' version '{}' to application deployment listeners", new Object[] {
                event.getClass().getName(), bundle.getSymbolicName(), event.getApplicationSymbolicName(), event.getApplicationVersion() });
        }
    }

}
