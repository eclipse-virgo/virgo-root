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

package org.eclipse.virgo.test.stubs.framework.aspects;

import java.util.List;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;

import org.eclipse.virgo.test.stubs.framework.StubBundle;
import org.eclipse.virgo.test.stubs.framework.StubBundleContext;

/**
 * Sends {@link BundleEvent}s to {@link BundleListener}s.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe
 * 
 */
public final aspect BundleEvents {

    /**
     * Sends a {@link BundleEvent#INSTALLED} event to all of the {@link BundleListener}s registered with a
     * {@link Bundle}
     * 
     * @param context The {@link BundleContext} to get the {@link BundleListener}s from
     * @param bundle The {@link Bundle} to send the event against
     */
    after(StubBundleContext context, Bundle bundle) : 
            this(context) &&
            withincode(* org.eclipse.virgo.test.stubs.framework.StubBundleContext.installBundle(..)) &&
            target(bundle) &&
            call(* org.eclipse.virgo.test.stubs.framework.StubBundle.setState(int)) &&
            if(thisJoinPoint.getArgs()[0].equals(Bundle.INSTALLED)) {
        sendEvent(context.getBundleListeners(), new BundleEvent(BundleEvent.INSTALLED, bundle));
    }

    /**
     * Sends a {@link BundleEvent#STARTING} event to all of the {@link BundleListener}s registered with a {@link Bundle}
     * 
     * @param bundle The {@link Bundle} to send the event against
     */
    after(StubBundle bundle) : 
            this(bundle) &&
            withincode(* org.eclipse.virgo.test.stubs.framework.StubBundle.start(int)) &&
            call(* org.eclipse.virgo.test.stubs.framework.StubBundle.setState(int)) &&
            if(thisJoinPoint.getArgs()[0].equals(Bundle.STARTING)) {
        StubBundleContext bundleContext = (StubBundleContext) bundle.getBundleContext();
        sendEvent(bundleContext.getBundleListeners(), new BundleEvent(BundleEvent.STARTING, bundle));
    }

    private void sendEvent(List<BundleListener> listeners, BundleEvent event) {
        for (BundleListener listener : listeners) {
            try {
                listener.bundleChanged(event);
            } catch (Exception e) {
                // Swallow exceptions to allow all listeners to be called
            }
        }
    }

    /**
     * Sends a {@link BundleEvent#STARTED} event to all of the {@link BundleListener}s registered with a {@link Bundle}
     * 
     * @param bundle The {@link Bundle} to send the event against
     */
    after(StubBundle bundle) : 
            this(bundle) &&
            withincode(* org.eclipse.virgo.test.stubs.framework.StubBundle.start(int)) &&
            call(* org.eclipse.virgo.test.stubs.framework.StubBundle.setState(int)) &&
            if(thisJoinPoint.getArgs()[0].equals(Bundle.ACTIVE)) {
        StubBundleContext bundleContext = (StubBundleContext) bundle.getBundleContext();
        sendEvent(bundleContext.getBundleListeners(), new BundleEvent(BundleEvent.STARTED, bundle));
    }

    /**
     * Sends a {@link BundleEvent#STOPPING} event to all of the {@link BundleListener}s registered with a {@link Bundle}
     * 
     * @param bundle The {@link Bundle} to send the event against
     */
    after(StubBundle bundle) : 
            this(bundle) &&
            withincode(* org.eclipse.virgo.test.stubs.framework.StubBundle.stop(int)) &&
            call(* org.eclipse.virgo.test.stubs.framework.StubBundle.setState(int)) &&
            if(thisJoinPoint.getArgs()[0].equals(Bundle.STOPPING)) {
        StubBundleContext bundleContext = (StubBundleContext) bundle.getBundleContext();
        sendEvent(bundleContext.getBundleListeners(), new BundleEvent(BundleEvent.STOPPING, bundle));
    }

    /**
     * Sends a {@link BundleEvent#STOPPED} event to all of the {@link BundleListener}s registered with a {@link Bundle}
     * 
     * @param bundle The {@link Bundle} to send the event against
     */
    after(StubBundle bundle) : 
            this(bundle) &&
            withincode(* org.eclipse.virgo.test.stubs.framework.StubBundle.stop(int)) &&
            call(* org.eclipse.virgo.test.stubs.framework.StubBundle.setState(int)) &&
            if(thisJoinPoint.getArgs()[0].equals(Bundle.RESOLVED)) {
        StubBundleContext bundleContext = (StubBundleContext) bundle.getBundleContext();
        sendEvent(bundleContext.getBundleListeners(), new BundleEvent(BundleEvent.STOPPED, bundle));
    }

    /**
     * Sends a {@link BundleEvent#UPDATED} event to all of the {@link BundleListener}s registered with a {@link Bundle}
     * 
     * @param bundle The {@link Bundle} to send the event against
     */
    after(StubBundle bundle) : 
            this(bundle) &&
            withincode(* org.eclipse.virgo.test.stubs.framework.StubBundle.update(..)) &&
            call(* org.eclipse.virgo.test.stubs.framework.StubBundle.setState(int)) &&
            if(thisJoinPoint.getArgs()[0].equals(Bundle.INSTALLED)) {
        StubBundleContext bundleContext = (StubBundleContext) bundle.getBundleContext();
        sendEvent(bundleContext.getBundleListeners(), new BundleEvent(BundleEvent.UPDATED, bundle));
    }

    /**
     * Sends a {@link BundleEvent#UNINSTALLED} event to all of the {@link BundleListener}s registered with a
     * {@link Bundle}
     * 
     * @param bundle The {@link Bundle} to send the event against
     */
    after(StubBundle bundle) : 
            this(bundle) &&
            withincode(* org.eclipse.virgo.test.stubs.framework.StubBundle.uninstall()) &&
            call(* org.eclipse.virgo.test.stubs.framework.StubBundle.setState(int)) &&
            if(thisJoinPoint.getArgs()[0].equals(Bundle.UNINSTALLED)) {
        StubBundleContext bundleContext = (StubBundleContext) bundle.getBundleContext();
        sendEvent(bundleContext.getBundleListeners(), new BundleEvent(BundleEvent.UNINSTALLED, bundle));
    }
}
