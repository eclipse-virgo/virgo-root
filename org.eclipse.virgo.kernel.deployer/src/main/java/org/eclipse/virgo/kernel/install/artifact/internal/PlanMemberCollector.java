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

import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.PlanInstallArtifact;
import org.eclipse.virgo.util.common.GraphNode;
import org.eclipse.virgo.util.common.GraphNode.DirectedAcyclicGraphVisitor;

/**
 * A simple helper class that can be used to collect all of the members of a plan. Collection is performed by visiting
 * the entire graph beneath the plan.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread-safe.
 * 
 */
final class PlanMemberCollector {

    /**
     * Collects all of the members of the given <code>plan</code>, including any nested plans and their members. Note that the
     * supplied <code>plan</code> will not be included in the returned <code>List</code>.
     * 
     * @param plan the plan for which the members are to be collected
     * @return all the members of the plan, not including the plan itself
     */
    List<InstallArtifact> collectPlanMembers(PlanInstallArtifact plan) {
        ArtifactCollectingGraphVisitor visitor = new ArtifactCollectingGraphVisitor(plan);
        plan.getGraph().visit(visitor);
        return visitor.getMembers();
    }

    private static final class ArtifactCollectingGraphVisitor implements DirectedAcyclicGraphVisitor<InstallArtifact> {

        private final InstallArtifact root;

        private final List<InstallArtifact> members = new ArrayList<InstallArtifact>();

        private final Object monitor = new Object();

        /**
         * @param root
         */
        public ArtifactCollectingGraphVisitor(InstallArtifact root) {
            this.root = root;
        }

        /**
         * {@inheritDoc}
         */
        public boolean visit(GraphNode<InstallArtifact> node) {
            InstallArtifact artifact = node.getValue();

            if (!root.equals(artifact)) {
                synchronized (this.monitor) {
                    this.members.add(artifact);
                }
            }

            return true;
        }

        List<InstallArtifact> getMembers() {
            synchronized (this.monitor) {
                return new ArrayList<InstallArtifact>(this.members);
            }
        }
    }
}
