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

package org.eclipse.virgo.nano.core;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;


/**
 * Provides a mechanism to start {@link Bundle bundles}, and wait until the <code>Bundle</code> is started, including
 * any asynchronous creation of an application context.
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations <strong>must</strong> be thread safe.
 * 
 */
public interface BundleStarter {

    /**
     * Starts the supplied {@link Bundle}, driving the supplied signal upon successful or unsuccessful completion
     * of start processing. If the <code>Bundle</code> constructs an application context, then the
     * signal will not be driven until application context construction has completed. 
     * 
     * @param bundle the <code>Bundle</code> to start.
     * @param signal the <code>Signal</code> to drive.
     * @throws BundleException if Bundle {@link Bundle#start()} fails
     */
    void start(Bundle bundle, AbortableSignal signal) throws BundleException;

    /**
     * Starts the supplied {@link Bundle}, driving the supplied signal upon successful or unsuccessful completion
     * of start processing. If the <code>Bundle</code> constructs an application context, then the
     * signal will not be driven until application context construction has completed.  The supplied <code>options</code>
     * are passed to the <code>bundle</code>'s {@link Bundle#start() start} method.
     * 
     * @param bundle the <code>Bundle</code> to start.
     * @param options the options to be passed to the bundle's {@link Bundle#start() start} method.
     * @param signal the <code>Signal</code> to drive.
     * @throws BundleException
     */
    void start(Bundle bundle, int options, AbortableSignal signal) throws BundleException;
    
    /**
     * Apply tracking to the given <code>Bundle</code> using the given <code>Signal</code> but don't actually start it.
     * 
     * @param bundle the <code>Bundle</code> to track.
     * @param signal the <code>Signal</code> to be notified.
     */
    void trackStart(Bundle bundle, AbortableSignal signal);
}
