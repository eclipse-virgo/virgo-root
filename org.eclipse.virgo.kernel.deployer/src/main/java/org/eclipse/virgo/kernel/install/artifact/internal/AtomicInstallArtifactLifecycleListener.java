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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.tree.TreeNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifactLifecycleListenerSupport;
import org.eclipse.virgo.kernel.install.artifact.PlanInstallArtifact;
import org.eclipse.virgo.util.common.GraphNode;

/**
 * <code>AtomicInstallArtifactLifecycleListener</code> is an InstallArtifactLifecycleListener which initiates state
 * changes on an atomic ancestor of an artifact when the artifact changes state.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * Thread-safe
 * 
 */
final class AtomicInstallArtifactLifecycleListener extends InstallArtifactLifecycleListenerSupport {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStarting(InstallArtifact installArtifact) throws DeploymentException {
        logger.debug("Processing atomic starting event for {}", installArtifact);

        for (InstallArtifact atomicParent : getAtomicParents(installArtifact)) {
            if (aChildIsRefreshing(atomicParent)) {
                logger.info("Atomic starting event not propagated from {} as a child of {} is refreshing.", installArtifact, atomicParent);
            } else {
                logger.info("Propagating atomic starting event from {} to {}", installArtifact, atomicParent);
                atomicParent.start();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStartFailed(InstallArtifact installArtifact, Throwable cause) throws DeploymentException {
        logger.debug("Processing atomic start failed (stop) event for {}", installArtifact);

        for (InstallArtifact atomicParent : getAtomicParents(installArtifact)) {
            if (aChildIsRefreshing(atomicParent)) {
                logger.info("Atomic start failed event not propagated from {} as a child of {} is refreshing.", installArtifact, atomicParent);
            } else {
                logger.info("Propagating atomic start failed (stop) event from {} to {}", installArtifact, atomicParent);
                atomicParent.stop();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStopped(InstallArtifact installArtifact) {
        logger.debug("Processing atomic stopped event for {}", installArtifact);

        for (InstallArtifact atomicParent : getAtomicParents(installArtifact)) {
            if (aChildIsRefreshing(atomicParent)) {
                logger.info("Atomic stopped event not propagated from {} as a child of {} is refreshing.", installArtifact, atomicParent);
            } else {
                logger.info("Propagating atomic stopped event from {} to {}", installArtifact, atomicParent);
                try {
                    atomicParent.stop();
                } catch (DeploymentException e) {
                    logger.warn("Unable to propagate stopped event to an atomic root due to an exception", e);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onUninstalled(InstallArtifact installArtifact) throws DeploymentException {
        logger.debug("Processing atomic uninstalled event for {}", installArtifact);

        for (InstallArtifact atomicParent : getAtomicParents(installArtifact)) {
            if (aChildIsRefreshing(atomicParent)) {
                logger.info("Atomic uninstalled event not propagated from {} as a child of {} is refreshing.", installArtifact, atomicParent);
            } else {
                logger.info("Propagating atomic uninstalled event from {} to {}", installArtifact, atomicParent);
                atomicParent.uninstall();
            }
        }
    }

    private Set<InstallArtifact> getAtomicParents(InstallArtifact installArtifact) {
        Set<InstallArtifact> atomicParents = new HashSet<InstallArtifact>();
        for (InstallArtifact parent : getParentInstallArtifacts(installArtifact)) {
            if (isAtomicInstallArtifact(parent)) {
                atomicParents.add(parent);
            }
        }
        return atomicParents;
    }

    /**
     * Return true if this is an {@link InstallArtifact} and contains others (in the {@link TreeNode}) and is an atomic
     * container.
     * 
     * @param installArtifact
     * @return true iff this is an artifact that has the {@link PlanInstallArtifact#isAtomic} attribute equal to true
     */
    private static final boolean isAtomicInstallArtifact(InstallArtifact installArtifact) {
        if (installArtifact instanceof PlanInstallArtifact) {
            return ((PlanInstallArtifact) installArtifact).isAtomic();
        }
        return false;
    }

    /**
     * Get the parent {@link InstallArtifact}s in the {@link GraphNode} associated with the {@link InstallArtifact}, if
     * there are any.
     * 
     * @param installArtifact to find the parents of
     * @return the parent artifacts in the graph, never <code>null</code>
     */
    private static final Set<InstallArtifact> getParentInstallArtifacts(InstallArtifact installArtifact) {
        Set<InstallArtifact> parentInstallArtifacts = new HashSet<InstallArtifact>();
        GraphNode<InstallArtifact> iaGraph = installArtifact.getGraph();
        if (iaGraph != null) {
            List<GraphNode<InstallArtifact>> parents = iaGraph.getParents();
            for (GraphNode<InstallArtifact> parent : parents) {
                parentInstallArtifacts.add(parent.getValue());
            }
        }
        return parentInstallArtifacts;
    }

    /**
     * Determines if <em>any</em> child of this {@link InstallArtifact} has
     * {@link AbstractInstallArtifact#isRefreshing() isRefreshing()} which returns true.
     * 
     * @param atomicParent whose children are checked
     * @return true if any child is refreshing, otherwise false.
     */
    private static boolean aChildIsRefreshing(InstallArtifact atomicParent) {
        for (InstallArtifact child : childrenOf(atomicParent)) {
            if (child instanceof AbstractInstallArtifact) {
                AbstractInstallArtifact aChild = (AbstractInstallArtifact) child;
                if (aChild.isRefreshing()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @param parent
     * @return an array of the children of the parent (which can be zero length)
     */
    private static InstallArtifact[] childrenOf(InstallArtifact parent) {
        List<InstallArtifact> children = new ArrayList<InstallArtifact>();
        if (parent != null) {
            GraphNode<InstallArtifact> graph = parent.getGraph();
            if (graph != null) {
                for (GraphNode<InstallArtifact> childBranch : graph.getChildren()) {
                    InstallArtifact child = childBranch.getValue();
                    if (child != null) {
                        children.add(child);
                    }
                }
            }
        }
        return children.toArray(new InstallArtifact[children.size()]);
    }
}
