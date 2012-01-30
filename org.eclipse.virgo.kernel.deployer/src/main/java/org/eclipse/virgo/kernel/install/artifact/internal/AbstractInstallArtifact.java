/*******************************************************************************
 * Copyright (c) 2008, 2011 VMware Inc. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *   EclipseSource - Bug 358442 Change InstallArtifact graph from a tree to a DAG
 *******************************************************************************/

package org.eclipse.virgo.kernel.install.artifact.internal;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.virgo.kernel.artifact.fs.ArtifactFS;
import org.eclipse.virgo.kernel.core.AbortableSignal;
import org.eclipse.virgo.kernel.core.Signal;
import org.eclipse.virgo.kernel.deployer.core.DeployerLogEvents;
import org.eclipse.virgo.kernel.deployer.core.DeploymentException;
import org.eclipse.virgo.kernel.install.artifact.ArtifactIdentity;
import org.eclipse.virgo.kernel.install.artifact.ArtifactState;
import org.eclipse.virgo.kernel.install.artifact.ArtifactStorage;
import org.eclipse.virgo.kernel.install.artifact.GraphAssociableInstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.serviceability.NonNull;
import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.virgo.util.common.GraphNode;
import org.osgi.framework.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link AbstractInstallArtifact} is a base class for implementations of {@link InstallArtifact}.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is thread safe.
 * 
 */
public abstract class AbstractInstallArtifact implements GraphAssociableInstallArtifact {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Object monitor = new Object();

    private final ArtifactIdentity identity;

    protected final ArtifactStorage artifactStorage;

    private final Map<String, String> properties = new ConcurrentHashMap<String, String>();

    private final Map<String, String> deploymentProperties = new ConcurrentHashMap<String, String>();

    private final ArtifactStateMonitor artifactStateMonitor;

    private final String repositoryName;

    protected final EventLogger eventLogger;

    private GraphNode<InstallArtifact> graph;

    private volatile boolean isRefreshing;

    /**
     * Construct an {@link AbstractInstallArtifact} from the given type, name, version, {@link ArtifactFS}, and
     * {@link ArtifactState}, none of which may be null.
     * 
     * @param type a non-<code>null</code> artifact type
     * @param name a non-<code>null</code> artifact name
     * @param version a non-<code>null</code> artifact {@link Version}
     * @param artifactFS a non-<code>null</code> <code>ArtifactFS</code>
     * @param repositoryName the name of the source repository, or <code>null</code> if the artifact is not from a
     *        repository
     */
    protected AbstractInstallArtifact(@NonNull ArtifactIdentity identity, @NonNull ArtifactStorage artifactStorage,
        @NonNull ArtifactStateMonitor artifactStateMonitor, String repositoryName, EventLogger eventLogger) {
        this.identity = identity;
        this.artifactStorage = artifactStorage;
        this.artifactStateMonitor = artifactStateMonitor;
        this.repositoryName = repositoryName;
        this.eventLogger = eventLogger;
        this.isRefreshing = false;
    }

    final ArtifactIdentity getIdentity() {
        return this.identity;
    }

    public final boolean isRefreshing() {
        return this.isRefreshing;
    }

    public void beginInstall() throws DeploymentException {
        try {
            this.artifactStateMonitor.onInstalling(this);
        } catch (DeploymentException de) {
            failInstall();
            throw de;
        }

    }

    public void failInstall() throws DeploymentException {
        this.artifactStateMonitor.onInstallFailed(this);
    }

    public void endInstall() throws DeploymentException {
        this.artifactStateMonitor.onInstalled(this);
    }

    public void beginResolve() throws DeploymentException {
        pushThreadContext();
        try {
            this.artifactStateMonitor.onResolving(this);
        } finally {
            popThreadContext();
        }
    }

    public void failResolve() throws DeploymentException {
        pushThreadContext();
        try {
            this.artifactStateMonitor.onResolveFailed(this);
        } finally {
            popThreadContext();
        }
    }

    public void endResolve() throws DeploymentException {
        pushThreadContext();
        try {
            this.artifactStateMonitor.onResolved(this);
        } finally {
            popThreadContext();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String getType() {
        return this.identity.getType();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String getName() {
        return this.identity.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Version getVersion() {
        return this.identity.getVersion();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String getScopeName() {
        return this.identity.getScopeName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public State getState() {
        return this.artifactStateMonitor.getState();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start() throws DeploymentException {
        start(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(AbortableSignal signal) throws DeploymentException {
        // If ACTIVE, signal successful completion immediately, otherwise
        // proceed with start processing.
        if (getState().equals(State.ACTIVE)) {
            if (signal != null) {
                signal.signalSuccessfulCompletion();
            }
        } else {
            pushThreadContext();
            try {
                boolean stateChanged = this.artifactStateMonitor.onStarting(this);
                if (stateChanged || signal != null) {
                    driveDoStart(signal);
                }
            } finally {
                popThreadContext();
            }
        }
    }

    protected final void driveDoStart(AbortableSignal signal) throws DeploymentException {
        AbortableSignal stateMonitorSignal = createStateMonitorSignal(signal);
        doStart(stateMonitorSignal);
    }

    protected final AbortableSignal createStateMonitorSignal(AbortableSignal signal) {
        return new StateMonitorSignal(signal);
    }

    private final class StateMonitorSignal implements AbortableSignal {

        private final AbortableSignal signal;

        public StateMonitorSignal(AbortableSignal signal) {
            this.signal = signal;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void signalSuccessfulCompletion() {
            try {
                asyncStartSucceeded();
                AbstractInstallArtifact.signalSuccessfulCompletion(this.signal);
            } catch (DeploymentException de) {
                signalFailure(de);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void signalFailure(Throwable cause) {
            asyncStartFailed(cause);
            try {
                stop();
            } catch (DeploymentException de) {
                AbstractInstallArtifact.this.logger.error("Stop failed", de);
            }
            AbstractInstallArtifact.signalFailure(this.signal, cause);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void signalAborted() {
            asyncStartAborted();
            try {
                stop();
            } catch (DeploymentException de) {
                AbstractInstallArtifact.this.logger.error("Stop aborted", de);
            }
            AbstractInstallArtifact.signalAbortion(this.signal);
        }

    }

    protected static void signalSuccessfulCompletion(Signal signal) {
        if (signal != null) {
            signal.signalSuccessfulCompletion();
        }
    }

    protected static void signalFailure(Signal signal, Throwable e) {
        if (signal != null) {
            signal.signalFailure(e);
        }
    }

    protected static void signalAbortion(AbortableSignal signal) {
        if (signal != null) {
            signal.signalAborted();
        }
    }

    /**
     * Perform the actual start of this {@link InstallArtifact} and drive the given {@link Signal} on successful or
     * unsuccessful completion.
     * 
     * @param signal the <code>Signal</code> to be driven
     * @throws DeploymentException if the start fails synchronously
     */
    protected abstract void doStart(AbortableSignal signal) throws DeploymentException;

    private final void asyncStartSucceeded() throws DeploymentException {
        pushThreadContext();
        try {
            this.artifactStateMonitor.onStarted(this);
        } finally {
            popThreadContext();
        }
    }

    private final void asyncStartFailed(Throwable cause) {
        pushThreadContext();
        try {
            this.artifactStateMonitor.onStartFailed(this, cause);
        } catch (DeploymentException e) {
            logger.error(String.format("listener for %s threw DeploymentException", this), e);
        } finally {
            popThreadContext();
        }
    }

    private final void asyncStartAborted() {
        pushThreadContext();
        try {
            this.artifactStateMonitor.onStartAborted(this);
        } catch (DeploymentException e) {
            logger.error(String.format("listener for %s threw DeploymentException", this), e);
        } finally {
            popThreadContext();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop() throws DeploymentException {
        // Only stop if ACTIVE or STARTING and this artifact should stop given its parent's states.
        if ((getState().equals(State.ACTIVE) || getState().equals(State.STARTING)) && shouldStop()) {
            pushThreadContext();
            try {
                this.artifactStateMonitor.onStopping(this);
                try {
                    doStop();
                    this.artifactStateMonitor.onStopped(this);
                } catch (DeploymentException e) {
                    this.artifactStateMonitor.onStopFailed(this, e);
                }
            } finally {
                popThreadContext();
            }
        }
    }

    protected boolean shouldStop() {
        /*
         * This artifact should stop if it was explicitly stopped independently of its parents (that is, it has no
         * STOPPING parents) or it has no parents that are ACTIVE or STARTING.
         */
        return !hasStoppingParent() || !hasActiveParent();
    }

    private boolean hasActiveParent() {
        for (GraphNode<InstallArtifact> parent : this.graph.getParents()) {
            State parentState = parent.getValue().getState();
            if (parentState.equals(State.ACTIVE) || parentState.equals(State.STARTING)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasStoppingParent() {
        for (GraphNode<InstallArtifact> parent : this.graph.getParents()) {
            State parentState = parent.getValue().getState();
            if (parentState.equals(State.STOPPING)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @see stop
     */
    protected abstract void doStop() throws DeploymentException;

    /**
     * {@inheritDoc}
     */
    @Override
    public void uninstall() throws DeploymentException {
        if (getState().equals(State.STARTING) || getState().equals(State.ACTIVE) || getState().equals(State.RESOLVED)
            || getState().equals(State.INSTALLED) || getState().equals(State.INITIAL)) {
            try {
                if (!getState().equals(State.INITIAL)) {
                    pushThreadContext();
                    try {
                        if (getState().equals(State.ACTIVE) || getState().equals(State.STARTING)) {
                            stop();
                        }
                        this.artifactStateMonitor.onUninstalling(this);
                        try {
                            doUninstall();
                            this.artifactStateMonitor.onUninstalled(this);
                        } catch (DeploymentException e) {
                            this.artifactStateMonitor.onUninstallFailed(this, e);
                        }
                    } finally {
                        popThreadContext();
                    }
                }
            } finally {
                this.artifactStorage.delete();
            }
        }
    }

    /**
     * @see uninstall
     */
    protected abstract void doUninstall() throws DeploymentException;

    /**
     * {@inheritDoc}
     */
    @Override
    public final ArtifactFS getArtifactFS() {
        return this.artifactStorage.getArtifactFS();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return this.identity.toString();
    }

    /**
     * Push the thread context including any application trace name and thread context class loader. The caller is
     * responsible for calling <code>popThreadContext</code>.
     */
    public void pushThreadContext() {
        // There is no default thread context. Subclasses must override to
        // provide one.
    }

    /**
     * Pop a previously pushed thread context.
     */
    public void popThreadContext() {
        // There is no default thread context. Subclasses must override to
        // provide one.
    }

    protected final ArtifactStateMonitor getStateMonitor() {
        return this.artifactStateMonitor;
    }

    /**
     * @return false
     */
    @Override
    public boolean refresh() throws DeploymentException {
        try {
            this.isRefreshing = true;
            this.eventLogger.log(DeployerLogEvents.REFRESHING, getType(), getName(), getVersion());
            this.artifactStorage.synchronize();

            boolean refreshed = doRefresh();

            if (refreshed) {
                this.eventLogger.log(DeployerLogEvents.REFRESHED, getType(), getName(), getVersion());
            } else {
                failRefresh();
            }

            return refreshed;
        } catch (DeploymentException de) {
            failRefresh(de);
            throw de;
        } catch (RuntimeException re) {
            failRefresh(re);
            throw re;
        } finally {
            this.isRefreshing = false;
        }
    }

    private void failRefresh() {
        failRefresh(null);
    }

    private void failRefresh(Exception ex) {
        this.artifactStorage.rollBack();
        if (ex == null) {
            this.eventLogger.log(DeployerLogEvents.REFRESH_FAILED, getType(), getName(), getVersion());
        } else {
            this.eventLogger.log(DeployerLogEvents.REFRESH_FAILED, ex, getType(), getName(), getVersion());
        }
    }

    protected abstract boolean doRefresh() throws DeploymentException;

    /**
     * {@inheritDoc}
     */
    @Override
    public final String getProperty(@NonNull String name) {
        return this.properties.get(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Set<String> getPropertyNames() {
        HashSet<String> propertyNames = new HashSet<String>(this.properties.keySet());
        return Collections.unmodifiableSet(propertyNames);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String setProperty(String name, String value) {
        return this.properties.put(name, value);
    }

    public Map<String, String> getDeploymentProperties() {
        return this.deploymentProperties;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String getRepositoryName() {
        return this.repositoryName;
    }

    /**
     * @param graph to set
     * @throws DeploymentException possible from overriding methods
     */
    public void setGraph(GraphNode<InstallArtifact> graph) throws DeploymentException {
        synchronized (this.monitor) {
            this.graph = graph;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final GraphNode<InstallArtifact> getGraph() {
        synchronized (this.monitor) {
            return this.graph;
        }
    }

}
