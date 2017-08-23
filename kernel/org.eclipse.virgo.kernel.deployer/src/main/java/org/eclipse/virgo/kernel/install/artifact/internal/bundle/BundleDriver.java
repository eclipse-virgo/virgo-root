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

package org.eclipse.virgo.kernel.install.artifact.internal.bundle;

import java.io.File;

import org.osgi.framework.Bundle;


import org.eclipse.virgo.nano.core.AbortableSignal;
import org.eclipse.virgo.nano.core.Signal;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.kernel.install.artifact.ArtifactState;
import org.eclipse.virgo.util.osgi.manifest.BundleManifest;

/**
 * {@link BundleDriver} monitors the state of a bundle and updates an associated {@link ArtifactState}.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations of this class must be thread safe.
 * 
 */
public interface BundleDriver {

    /**
     * Set the bundle associated with this {@link BundleDriver}. If a bundle has already been set, do nothing.
     * 
     * @param bundle the {@link Bundle} to be associated
     */
    void setBundle(Bundle bundle);

    /**
     * Starts the bundle associated with this {@link BundleDriver} and drives the given {@link Signal} when the start, including any
     * asynchronous processing, completes either successfully or unsuccessfully.
     * <p/>
     * If the start does not involve asynchronous processing, drives the given <code>Signal</code> before returning.
     * <p/>
     * Note that the given <code>Signal</code> may be driven before this method returns, after the method has returned,
     * or possibly never if there is asynchronous processing which never completes. The caller must ensure that the
     * given <code>Signal</code> is ready to be driven <i>before</i> calling this method.
     * <p/>
     * If the caller is not interested in being signalled, it should pass a <code>null</code> <code>Signal</code>.
     * <p/>
     * Note: this method behaves as specified above when called multiple times on the same <code>InstallArtifact</code>
     * with any combination of the same or distinct or <code>null</code> <code>Signals</code>.
     * 
     * @param signal an <code>AbortableSignal</code> that is ready to be driven or <code>null</code> if no signalling is required
     * @throws DeploymentException 
     */
    void start(AbortableSignal signal) throws DeploymentException;
    
    /**
     * Updates the bundle associated with this {@link BundleDriver} to the given file or directory using the given {@link BundleManifest} which
     * could, for example, result from transforming the updated artifact.
     * 
     * @param manifest the <code>BundleManifest</code>
     * @param location the <code>File</code>
     * @return <code>true</code> if and only if the bundle was successfully updated
     * @throws DeploymentException if the update failed
     */
    boolean update(BundleManifest manifest, File location) throws DeploymentException;

    /**
     * Performs a refresh packages operation specifying the bundle associated with this {@link BundleDriver} and
     * waits, for a period of time, for the operation to complete before returning.
     * 
     * @throws DeploymentException if the refresh failed
     */
    void refreshBundle() throws DeploymentException;

    /**
     * Stops the bundle associated with this {@link BundleDriver}. If the bundle is already stopped, does nothing.
     * 
     * @throws DeploymentException if stopping the bundle fails
     */
    void stop() throws DeploymentException;

    /**
     * Uninstalls the bundle associated with this {@link BundleDriver}.
     * 
     * @throws DeploymentException if uninstalling the bundle fails
     */
    void uninstall() throws DeploymentException;

    /**
     * Push the thread context including any application trace name and thread context class loader. The caller is
     * responsible for calling <code>popThreadContext</code>.
     */
    void pushThreadContext();

    /**
     * Pop a previously pushed thread context.
     */
    void popThreadContext();

    void trackStart(AbortableSignal signal);

}
