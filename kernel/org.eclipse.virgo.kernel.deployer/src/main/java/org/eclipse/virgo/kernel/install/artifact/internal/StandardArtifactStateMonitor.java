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

package org.eclipse.virgo.kernel.install.artifact.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.kernel.install.artifact.ArtifactState;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifactLifecycleListener;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact.State;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import org.eclipse.virgo.kernel.osgi.framework.OsgiFrameworkUtils;
import org.eclipse.virgo.kernel.osgi.framework.OsgiServiceHolder;

/**
 * {@link StandardArtifactStateMonitor} logs {@link InstallArtifact} state changes and notifies
 * {@link InstallArtifactLifecycleListener InstallArtifactLifecycleListeners}.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is thread safe.
 * 
 */
public class StandardArtifactStateMonitor implements ArtifactStateMonitor {

    private static final ArrayList<OsgiServiceHolder<InstallArtifactLifecycleListener>> EMPTY_LISTENER_HOLDER_LIST = new ArrayList<OsgiServiceHolder<InstallArtifactLifecycleListener>>();

    private final BundleContext bundleContext;

    private final Bundle bundle;

    private final ArtifactState artifactState;

    private StandardArtifactStateMonitor(BundleContext bundleContext, ArtifactState artifactState) {
        this.bundleContext = bundleContext;
        this.bundle = this.bundleContext.getBundle();
        this.artifactState = artifactState;
    }

    public StandardArtifactStateMonitor(BundleContext bundleContext) {
        this(bundleContext, new ArtifactState());
    }

    /**
     * {@inheritDoc}
     */
    public State getState() {
        return this.artifactState.getState();
    }

    /**
     * {@inheritDoc}
     */
    public void setState(State state) {
        switch (state) {
            case ACTIVE:
                this.artifactState.setActive();
                break;
            case INITIAL:
                this.artifactState.setInitial();
                break;
            case INSTALLED:
                this.artifactState.setInstalled();
                break;
            case INSTALLING:
                this.artifactState.setInstalling();
                break;
            case RESOLVED:
                this.artifactState.setResolved();
                break;
            case RESOLVING:
                this.artifactState.setResolving();
                break;
            case STARTING:
                this.artifactState.setStarting();
                break;
            case STOPPING:
                this.artifactState.setStopping();
                break;
            case UNINSTALLED:
                this.artifactState.setUninstalled();
                break;
            case UNINSTALLING:
                this.artifactState.setUninstalling();
                break;
        }
    }

    /**
     * {@inheritDoc}
     */
    public void onInstalling(InstallArtifact installArtifact) throws DeploymentException {
        State state = this.artifactState.getState();
        if (state.equals(State.INITIAL)) {
            if (this.artifactState.setInstalling()) {
                List<OsgiServiceHolder<InstallArtifactLifecycleListener>> listenerHolders = getListenerHolders();
                try {
                    for (InstallArtifactLifecycleListener listener : getListeners(listenerHolders)) {
                        listener.onInstalling(installArtifact);
                    }
                } finally {
                    ungetListeners(listenerHolders);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void onInstallFailed(InstallArtifact installArtifact) throws DeploymentException {
        State state = this.artifactState.getState();
        if (state.equals(State.INSTALLING)) {
            if (this.artifactState.setInitial()) {
                List<OsgiServiceHolder<InstallArtifactLifecycleListener>> listenerHolders = getListenerHolders();
                try {
                    for (InstallArtifactLifecycleListener listener : getListeners(listenerHolders)) {
                        listener.onInstallFailed(installArtifact);
                    }
                } finally {
                    ungetListeners(listenerHolders);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void onInstalled(InstallArtifact installArtifact) throws DeploymentException {
        State state = this.artifactState.getState();
        if (state.equals(State.INITIAL) || state.equals(State.INSTALLING)) {
            if (this.artifactState.setInstalled()) {
                List<OsgiServiceHolder<InstallArtifactLifecycleListener>> listenerHolders = getListenerHolders();
                try {
                    for (InstallArtifactLifecycleListener listener : getListeners(listenerHolders)) {
                        listener.onInstalled(installArtifact);
                    }
                } finally {
                    ungetListeners(listenerHolders);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void onResolving(InstallArtifact installArtifact) throws DeploymentException {
        State state = this.artifactState.getState();
        if (state.equals(State.INITIAL) || state.equals(State.INSTALLING) || state.equals(State.INSTALLED)) {
            if (this.artifactState.setResolving()) {
                List<OsgiServiceHolder<InstallArtifactLifecycleListener>> listenerHolders = getListenerHolders();
                try {
                    for (InstallArtifactLifecycleListener listener : getListeners(listenerHolders)) {
                        listener.onResolving(installArtifact);
                    }
                } finally {
                    ungetListeners(listenerHolders);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void onResolveFailed(InstallArtifact installArtifact) throws DeploymentException {
        State state = this.artifactState.getState();
        if (state.equals(State.INITIAL) || state.equals(State.INSTALLING) || state.equals(State.INSTALLED) || state.equals(State.RESOLVING) || state.equals(State.STOPPING)) {
            if (this.artifactState.setInstalled()) {
                List<OsgiServiceHolder<InstallArtifactLifecycleListener>> listenerHolders = getListenerHolders();
                try {
                    for (InstallArtifactLifecycleListener listener : getListeners(listenerHolders)) {
                        listener.onResolveFailed(installArtifact);
                    }
                } finally {
                    ungetListeners(listenerHolders);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void onResolved(InstallArtifact installArtifact) throws DeploymentException {
        State state = this.artifactState.getState();
        if (state.equals(State.INITIAL) || state.equals(State.INSTALLING) || state.equals(State.INSTALLED) || state.equals(State.RESOLVING) || state.equals(State.STOPPING)) {
            if (this.artifactState.setResolved()) {
                List<OsgiServiceHolder<InstallArtifactLifecycleListener>> listenerHolders = getListenerHolders();
                try {
                    for (InstallArtifactLifecycleListener listener : getListeners(listenerHolders)) {
                        listener.onResolved(installArtifact);
                    }
                } finally {
                    ungetListeners(listenerHolders);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean onStarting(InstallArtifact installArtifact) throws DeploymentException {
        boolean stateChanged = this.artifactState.setStarting();
        if (stateChanged) {
            List<OsgiServiceHolder<InstallArtifactLifecycleListener>> listenerHolders = getListenerHolders();
            try {
                for (InstallArtifactLifecycleListener listener : getListeners(listenerHolders)) {
                    listener.onStarting(installArtifact);
                }
            } finally {
                ungetListeners(listenerHolders);
            }
        }
        return stateChanged;
    }

    /**
     * {@inheritDoc}
     */
    public void onStartFailed(InstallArtifact installArtifact, Throwable cause) throws DeploymentException {
        List<OsgiServiceHolder<InstallArtifactLifecycleListener>> listenerHolders = getListenerHolders();
        try {
            for (InstallArtifactLifecycleListener listener : getListeners(listenerHolders)) {
                listener.onStartFailed(installArtifact, cause);
            }
        } finally {
            ungetListeners(listenerHolders);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void onStartAborted(InstallArtifact installArtifact) throws DeploymentException {
        List<OsgiServiceHolder<InstallArtifactLifecycleListener>> listenerHolders = getListenerHolders();
        try {
            for (InstallArtifactLifecycleListener listener : getListeners(listenerHolders)) {
                listener.onStartAborted(installArtifact);
            }
        } finally {
            ungetListeners(listenerHolders);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void onStarted(InstallArtifact installArtifact) throws DeploymentException {
        if (this.artifactState.setActive()) {
            List<OsgiServiceHolder<InstallArtifactLifecycleListener>> listenerHolders = getListenerHolders();
            try {
                for (InstallArtifactLifecycleListener listener : getListeners(listenerHolders)) {
                    listener.onStarted(installArtifact);
                }
            } finally {
                ungetListeners(listenerHolders);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void onStopping(InstallArtifact installArtifact) {
        if (this.artifactState.setStopping()) {
            List<OsgiServiceHolder<InstallArtifactLifecycleListener>> listenerHolders = getListenerHolders();
            try {
                for (InstallArtifactLifecycleListener listener : getListeners(listenerHolders)) {
                    listener.onStopping(installArtifact);
                }
            } finally {
                ungetListeners(listenerHolders);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void onStopFailed(InstallArtifact installArtifact, Throwable cause) throws DeploymentException {
        if (this.artifactState.setActive()) {
            List<OsgiServiceHolder<InstallArtifactLifecycleListener>> listenerHolders = getListenerHolders();
            try {
                for (InstallArtifactLifecycleListener listener : getListeners(listenerHolders)) {
                    listener.onStopFailed(installArtifact, cause);
                }
            } finally {
                ungetListeners(listenerHolders);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void onStopped(InstallArtifact installArtifact) {
        if (this.artifactState.setResolved()) {
            List<OsgiServiceHolder<InstallArtifactLifecycleListener>> listenerHolders = getListenerHolders();
            try {
                for (InstallArtifactLifecycleListener listener : getListeners(listenerHolders)) {
                    listener.onStopped(installArtifact);
                }
            } finally {
                ungetListeners(listenerHolders);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void onUnresolved(InstallArtifact installArtifact) throws DeploymentException {
        if (this.artifactState.setInstalled()) {
            List<OsgiServiceHolder<InstallArtifactLifecycleListener>> listenerHolders = getListenerHolders();
            try {
                for (InstallArtifactLifecycleListener listener : getListeners(listenerHolders)) {
                    listener.onUnresolved(installArtifact);
                }
            } finally {
                ungetListeners(listenerHolders);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void onUninstalling(InstallArtifact installArtifact) throws DeploymentException {
        if (this.artifactState.setUninstalling()) {
            List<OsgiServiceHolder<InstallArtifactLifecycleListener>> listenerHolders = getListenerHolders();
            try {
                for (InstallArtifactLifecycleListener listener : getListeners(listenerHolders)) {
                    listener.onUninstalling(installArtifact);
                }
            } finally {
                ungetListeners(listenerHolders);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void onUninstallFailed(InstallArtifact installArtifact, Throwable cause) throws DeploymentException {
        if (this.artifactState.setResolved()) {
            List<OsgiServiceHolder<InstallArtifactLifecycleListener>> listenerHolders = getListenerHolders();
            try {
                for (InstallArtifactLifecycleListener listener : getListeners(listenerHolders)) {
                    listener.onUninstallFailed(installArtifact, cause);
                }
            } finally {
                ungetListeners(listenerHolders);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void onUninstalled(InstallArtifact installArtifact) throws DeploymentException {
        if (this.artifactState.setUninstalled()) {
            List<OsgiServiceHolder<InstallArtifactLifecycleListener>> listenerHolders = getListenerHolders();
            try {
                for (InstallArtifactLifecycleListener listener : getListeners(listenerHolders)) {
                    listener.onUninstalled(installArtifact);
                }
            } finally {
                ungetListeners(listenerHolders);
            }
        }
    }

    private List<OsgiServiceHolder<InstallArtifactLifecycleListener>> getListenerHolders() {
        // Avoid InvalidStateException if our bundle is stopped, typically during shutdown.
        return this.bundle.getState() != Bundle.RESOLVED ? OsgiFrameworkUtils.getServices(this.bundleContext, InstallArtifactLifecycleListener.class)
            : EMPTY_LISTENER_HOLDER_LIST;
    }

    private List<InstallArtifactLifecycleListener> getListeners(List<OsgiServiceHolder<InstallArtifactLifecycleListener>> listenerHolders) {
        List<InstallArtifactLifecycleListener> listeners;
        listeners = new ArrayList<InstallArtifactLifecycleListener>(listenerHolders.size());

        for (OsgiServiceHolder<InstallArtifactLifecycleListener> listenerHolder : listenerHolders) {
            listeners.add(listenerHolder.getService());
        }
        return listeners;
    }

    private void ungetListeners(List<OsgiServiceHolder<InstallArtifactLifecycleListener>> listenerHolders) {
        for (OsgiServiceHolder<InstallArtifactLifecycleListener> listenerHolder : listenerHolders) {
            this.bundleContext.ungetService(listenerHolder.getServiceReference());
        }
    }

}
