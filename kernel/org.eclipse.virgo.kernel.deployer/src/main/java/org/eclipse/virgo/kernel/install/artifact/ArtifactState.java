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

import org.eclipse.virgo.kernel.install.artifact.InstallArtifact.State;
import org.eclipse.virgo.nano.serviceability.NonNull;


/**
 * {@link ArtifactState} encapsulates the state of an install artifact.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is thread safe.
 * 
 */
public final class ArtifactState {

    private final Object monitor = new Object();

    private State state;

    /**
     * Creates an {@link ArtifactState} in the INITIAL {@link State}.
     */
    public ArtifactState() {
        this.state = State.INITIAL;
    }

    /**
     * Gets the current {@link State}.
     * 
     * @return the current <code>State</code>
     */
    public State getState() {
        synchronized (this.monitor) {
            return this.state;
        }
    }

    /**
     * Sets the state to the given value.
     * 
     * @param newState
     * @return <code>true</code> if and only if the state changed
     */
    private boolean setState(@NonNull State newState) {
        synchronized (this.monitor) {
            boolean changed = !this.state.equals(newState);
            this.state = newState;
            return changed;
        }
    }

    /**
     * Sets the current state to INITIAL.
     * @return <code>true</code> if and only if the state changed
     */
    public boolean setInitial() {
        return setState(State.INITIAL);
    }

    /**
     * Sets the current state to INSTALLING.
     * @return <code>true</code> if and only if the state changed
     */
    public boolean setInstalling() {
        return setState(State.INSTALLING);
    }

    /**
     * Sets the current state to INSTALLED.
     * @return <code>true</code> if and only if the state changed
     */
    public boolean setInstalled() {
        return setState(State.INSTALLED);
    }

    /**
     * Sets the current state to RESOLVING.
     * @return <code>true</code> if and only if the state changed
     */
    public boolean setResolving() {
        return setState(State.RESOLVING);
    }

    /**
     * Sets the current state to RESOLVED.
     * @return <code>true</code> if and only if the state changed
     */
    public boolean setResolved() {
        return setState(State.RESOLVED);
    }

    /**
     * Sets the current state to STARTING.
     * @return <code>true</code> if and only if the state changed
     */
    public boolean setStarting() {
        return setState(State.STARTING);
    }

    /**
     * Sets the current state to ACTIVE.
     * @return <code>true</code> if and only if the state changed
     */
    public boolean setActive() {
        return setState(State.ACTIVE);
    }

    /**
     * Sets the current state to STOPPING.
     * @return <code>true</code> if and only if the state changed
     */
    public boolean setStopping() {
        return setState(State.STOPPING);
    }

    /**
     * Sets the current state to UNINSTALLING.
     * @return <code>true</code> if and only if the state changed
     */
    public boolean setUninstalling() {
        return setState(State.UNINSTALLING);
    }

    /**
     * Sets the current state to UNINSTALLED.
     * @return <code>true</code> if and only if the state changed
     */
    public boolean setUninstalled() {
        return setState(State.UNINSTALLED);
    }

}
