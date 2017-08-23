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

package org.eclipse.virgo.kernel.install.artifact;

import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;

/**
 * An <code>InstallArtifactLifecycleListener</code> is notified of {@link InstallArtifact} lifecycle events.
 * <p />
 * An <code>InstallArtifactLifecycleListener</code> implementation should be stateless and is made known to the deployer
 * by publishing it as an OSGi service.
 * <p />
 * An <code>InstallArtifactLifecycleListener</code> is not notified of events it has missed. For example, if an
 * <code>InstallArtifactLifecycleListener</code> is published while an <code>InstallArtifact</code> is being started, it
 * may miss the <code>onStarting</code> notification and any <code>onStarted</code> notification.
 * <p />
 * If an <code>InstallArtifactLifecycleListener</code> has missed some events, it is still notified of other events. For
 * example, if an <code>InstallArtifactLifecycleListener</code> is published while an <code>InstallArtifact</code> is
 * being started, it may be notified of an <code>onStarted</code> event for the <code>InstallArtifact</code> even if it
 * has missed the <code>onStarting</code> event for the <code>InstallArtifact</code>. Similarly, an
 * <code>InstallArtifactLifecycleListener</code> may be notified of an <code>onStopping</code> event for an
 * <code>InstallArtifact</code> event if it missed the <code>onStarting</code> event and any <code>onStarted</code>
 * event for the <code>InstallArtifact</code>.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations <strong>must</strong> be thread-safe. Notifications of events for a given
 * <code>InstallArtifact</code> may be delivered on the same thread or on distinct threads.
 * 
 */
public interface InstallArtifactLifecycleListener {

    /**
     * Notification that the given {@link InstallArtifact} is installing.
     * <p/>
     * 
     * Throwing a {@link DeploymentException} will result in no further listeners being notified that the
     * <code>installArtifact</code> is being installed. Install failure will then be notified and the installation
     * aborted.
     * 
     * @param installArtifact the <code>InstallArtifact</code> that is installing
     * @throws DeploymentException if the listener failed to handle the event
     */
    void onInstalling(InstallArtifact installArtifact) throws DeploymentException;

    /**
     * Notification that the given {@link InstallArtifact} failed to install.
     * 
     * @param installArtifact the <code>InstallArtifact</code> that failed to install
     * @throws DeploymentException if the listener failed to handle the event
     */
    void onInstallFailed(InstallArtifact installArtifact) throws DeploymentException;

    /**
     * Notification that the given {@link InstallArtifact} installed successfully.
     * 
     * @param installArtifact the <code>InstallArtifact</code> that installed successfully
     * @throws DeploymentException if the listener failed to handle the event
     */
    void onInstalled(InstallArtifact installArtifact) throws DeploymentException;

    /**
     * Notification that the given {@link InstallArtifact} is resolving.
     * 
     * @param installArtifact the <code>InstallArtifact</code> that is resolving
     * @throws DeploymentException if the listener failed to handle the event
     */
    void onResolving(InstallArtifact installArtifact) throws DeploymentException;

    /**
     * Notification that the given {@link InstallArtifact} failed to resolve.
     * 
     * @param installArtifact the <code>InstallArtifact</code> that failed to resolve
     * @throws DeploymentException if the listener failed to handle the event
     */
    void onResolveFailed(InstallArtifact installArtifact) throws DeploymentException;

    /**
     * Notification that the given {@link InstallArtifact} resolved successfully.
     * 
     * @param installArtifact the <code>InstallArtifact</code> that resolved successfully
     * @throws DeploymentException if the listener failed to handle the event
     */
    void onResolved(InstallArtifact installArtifact) throws DeploymentException;

    /**
     * Notification that the given {@link InstallArtifact} is starting.
     * 
     * @param installArtifact the <code>InstallArtifact</code> that is starting
     * @throws DeploymentException if the listener failed to handle the event
     */
    void onStarting(InstallArtifact installArtifact) throws DeploymentException;

    /**
     * Notification that the given {@link InstallArtifact} failed to start.
     * 
     * @param installArtifact the <code>InstallArtifact</code> that failed to start
     * @param cause the exception indicating the cause of the failure or <code>null</code> if there was no exception
     * @throws DeploymentException if the listener failed to handle the event
     */
    void onStartFailed(InstallArtifact installArtifact, Throwable cause) throws DeploymentException;

    /**
     * Notification that the given {@link InstallArtifact} aborted while starting.
     * 
     * @param installArtifact the <code>InstallArtifact</code> that aborted
     * @throws DeploymentException if the listener failed to handle the event
     */
    void onStartAborted(InstallArtifact installArtifact) throws DeploymentException;

    /**
     * Notification that the given {@link InstallArtifact} has started.
     * 
     * <p/>
     * 
     * Throwing a {@link DeploymentException} will result in no further listeners being notified that the
     * <code>installArtifact</code> has started. The <code>installArtifact</code> will then be stopped.
     * 
     * @param installArtifact the <code>InstallArtifact</code> that has started
     * @throws DeploymentException if the listener failed to handle the event
     */
    void onStarted(InstallArtifact installArtifact) throws DeploymentException;

    /**
     * Notification that the given {@link InstallArtifact} is stopping.
     * 
     * @param installArtifact the <code>InstallArtifact</code> that is stopping
     */
    void onStopping(InstallArtifact installArtifact);

    /**
     * Notification that the given {@link InstallArtifact} failed to stop.
     * 
     * @param installArtifact the <code>InstallArtifact</code> that failed to stop
     * @param cause the exception indicating the cause of the failure or <code>null</code> if there was no exception
     * @throws DeploymentException if the listener failed to handle the event
     */
    void onStopFailed(InstallArtifact installArtifact, Throwable cause) throws DeploymentException;

    /**
     * Notification that the given {@link InstallArtifact} has stopped.
     * 
     * @param installArtifact the <code>InstallArtifact</code> that has stopped
     */
    void onStopped(InstallArtifact installArtifact);

    /**
     * Notification that the given {@link InstallArtifact} has become unresolved.
     * 
     * @param installArtifact the <code>InstallArtifact</code> that has become unresolved
     * @throws DeploymentException if the listener failed to handle the event
     */
    void onUnresolved(InstallArtifact installArtifact) throws DeploymentException;

    /**
     * Notification that the given {@link InstallArtifact} is uninstalling.
     * 
     * @param installArtifact the <code>InstallArtifact</code> that is unintalling
     * @throws DeploymentException if the listener failed to handle the event
     */
    void onUninstalling(InstallArtifact installArtifact) throws DeploymentException;

    /**
     * Notification that the given {@link InstallArtifact} failed to uninstall.
     * 
     * @param installArtifact the <code>InstallArtifact</code> that failed to uninstall
     * @param cause the exception indicating the cause of the failure or <code>null</code> if there was no exception
     * @throws DeploymentException if the listener failed to handle the event
     */
    void onUninstallFailed(InstallArtifact installArtifact, Throwable cause) throws DeploymentException;

    /**
     * Notification that the given {@link InstallArtifact} has uninstalled.
     * 
     * @param installArtifact the <code>InstallArtifact</code> that has uninstalled
     * @throws DeploymentException if the listener failed to handle the event
     */
    void onUninstalled(InstallArtifact installArtifact) throws DeploymentException;
}
