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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.virgo.kernel.artifact.ArtifactSpecification;
import org.eclipse.virgo.kernel.artifact.plan.PlanDescriptor.Provisioning;
import org.eclipse.virgo.nano.core.AbortableSignal;
import org.eclipse.virgo.nano.deployer.api.core.DeployerLogEvents;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.kernel.deployer.core.internal.AbortableSignalJunction;
import org.eclipse.virgo.kernel.install.artifact.ArtifactIdentity;
import org.eclipse.virgo.kernel.install.artifact.ArtifactStorage;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.PlanInstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.ScopeServiceRepository;
import org.eclipse.virgo.kernel.install.artifact.internal.scoping.ArtifactIdentityScoper;
import org.eclipse.virgo.nano.serviceability.NonNull;
import org.eclipse.virgo.nano.shim.scope.Scope;
import org.eclipse.virgo.nano.shim.scope.ScopeFactory;
import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.virgo.util.common.GraphNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link StandardPlanInstallArtifact} is the standard implementation of {@link PlanInstallArtifact}.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is thread safe.
 * 
 */
public class StandardPlanInstallArtifact extends AbstractInstallArtifact implements PlanInstallArtifact {

    private static final Logger LOGGER = LoggerFactory.getLogger(StandardPlanInstallArtifact.class);

    private final Object monitor = new Object();

    private final ScopeServiceRepository scopeServiceRepository;

    private final ScopeFactory scopeFactory;

    private final InstallArtifactRefreshHandler refreshHandler;

    private final boolean atomic;

    private final boolean scoped;

    private final Provisioning provisioning;

    private final List<ArtifactSpecification> artifactSpecifications;

    protected final EventLogger eventLogger;

    private Scope applicationScope;

    protected StandardPlanInstallArtifact(@NonNull ArtifactIdentity artifactIdentity, boolean atomic, boolean scoped,
        @NonNull Provisioning provisioning, @NonNull ArtifactStorage artifactStorage, @NonNull ArtifactStateMonitor artifactStateMonitor,
        @NonNull ScopeServiceRepository scopeServiceRepository, @NonNull ScopeFactory scopeFactory, @NonNull EventLogger eventLogger,
        @NonNull InstallArtifactRefreshHandler refreshHandler, String repositoryName, List<ArtifactSpecification> artifactSpecifications)
        throws DeploymentException {
        super(artifactIdentity, artifactStorage, artifactStateMonitor, repositoryName, eventLogger);

        policeNestedScopes(artifactIdentity, scoped, eventLogger);

        this.scopeServiceRepository = scopeServiceRepository;
        this.scopeFactory = scopeFactory;
        this.eventLogger = eventLogger;
        this.refreshHandler = refreshHandler;
        this.atomic = atomic;
        this.scoped = scoped;
        if (provisioning == Provisioning.INHERIT) {
            throw new IllegalArgumentException("A plan's provisoning behaviour may not be set to INHERIT");
        }
        this.provisioning = provisioning;
        this.artifactSpecifications = artifactSpecifications;
    }

    private void policeNestedScopes(ArtifactIdentity artifactIdentity, boolean scoped, EventLogger eventLogger) throws DeploymentException {
        if (artifactIdentity.getScopeName() != null && scoped) {
            eventLogger.log(DeployerLogEvents.NESTED_SCOPES_NOT_SUPPORTED, artifactIdentity.getType(),
                ArtifactIdentityScoper.getUnscopedName(artifactIdentity), artifactIdentity.getVersion(), artifactIdentity.getScopeName());
            throw new DeploymentException("Nested scope detected", true);
        }
    }

    protected final List<GraphNode<InstallArtifact>> getChildrenSnapshot() {
        List<GraphNode<InstallArtifact>> children = new ArrayList<GraphNode<InstallArtifact>>();
        synchronized (this.monitor) {
            children.addAll(getGraph().getChildren());
        }
        return children;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final void doStart(AbortableSignal signal) throws DeploymentException {
        List<GraphNode<InstallArtifact>> children = getChildrenSnapshot();
        int numChildren = children.size();

        // The SignalJunction constructor will drive the signal if numChildren == 0.
        AbortableSignalJunction signalJunction = new AbortableSignalJunction(signal, numChildren);

        LOGGER.debug("Created {} that will notify {} to track start of {}", new Object[] { signalJunction, signal, this });

        List<AbortableSignal> subSignals = signalJunction.getSignals();

        for (int childIndex = 0; childIndex < numChildren && !signalJunction.failed(); childIndex++) {
            InstallArtifact childArtifact = children.get(childIndex).getValue();
            AbortableSignal subSignal = subSignals.get(childIndex);

            LOGGER.debug("Starting {} with signal {} from {}", new Object[] { childArtifact, subSignal, signalJunction });

            childArtifact.start(subSignal);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final void doStop() throws DeploymentException {
        DeploymentException firstFailure = null;
        for (GraphNode<InstallArtifact> child : getChildrenSnapshot()) {
            try {
                child.getValue().stop();
            } catch (DeploymentException e) {
                firstFailure = e;
            }
        }
        if (firstFailure != null) {
            throw firstFailure;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final void doUninstall() throws DeploymentException {
        deScope(); // TODO this was placed here in copying from the old deployer, but it is insufficient. Need to
        // consider stop/start/stop etc. for package and service scoping.

        DeploymentException firstFailure = null;
        for (GraphNode<InstallArtifact> child : getChildrenSnapshot()) {
            getGraph().removeChild(child);
            // Avoid uninstalling shared child
            if (!inUse(child)) {
                try {
                    child.getValue().uninstall();
                } catch (DeploymentException e) {
                    firstFailure = e;
                }
            }
        }
        if (firstFailure != null) {
            throw firstFailure;
        }
    }

    private boolean inUse(GraphNode<InstallArtifact> child) {
        AbstractInstallArtifact installArtifact = (AbstractInstallArtifact) child.getValue();
        boolean isTopLevelDeployed = installArtifact.getTopLevelDeployed();
        return isTopLevelDeployed || !child.getParents().isEmpty();
    }

    public void scope() throws DeploymentException {
        if (isScoped()) {
            List<InstallArtifact> scopeMembers = new PlanMemberCollector().collectPlanMembers(this);
            PlanScoper planScoper = new PlanScoper(scopeMembers, getName(), getVersion(), this.scopeServiceRepository, this.eventLogger);
            String scopeName = planScoper.getScopeName();

            synchronized (this.monitor) {
                this.applicationScope = this.scopeFactory.getApplicationScope(scopeName);
                // TODO Do we really need to hold this lock while we're driving the planScoper?
                planScoper.scope();
            }
        }
    }

    private void deScope() {
        synchronized (this.monitor) {
            if (this.applicationScope != null) {
                this.scopeFactory.destroyApplicationScope(this.applicationScope);
                this.scopeServiceRepository.clearScope(this.applicationScope.getScopeName());
                this.applicationScope = null;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean refreshScope() {
        return this.refreshHandler.refresh(this);
    }

    @Override
    public boolean refresh() throws DeploymentException {
        this.eventLogger.log(DeployerLogEvents.INSTALL_ARTIFACT_REFRESH_NOT_SUPPORTED, getType(), getName(), getVersion(), getType());
        return false;
    }

    public final boolean isAtomic() {
        return this.atomic;
    }

    public final boolean isScoped() {
        return this.scoped;
    }

    /**
     * {@inheritDoc}
     */
    public Provisioning getProvisioning() {
        return this.provisioning;
    }

    public final List<ArtifactSpecification> getArtifactSpecifications() {
        return this.artifactSpecifications;
    }
}
