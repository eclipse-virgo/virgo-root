/*******************************************************************************
 * Copyright (c) 2008, 2011 VMware Inc. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution (TreeUtils)
 *   EclipseSource - Bug 358442 Change InstallArtifact graph from a tree to a DAG
 *******************************************************************************/

package org.eclipse.virgo.kernel.deployer.core.internal;


import org.eclipse.virgo.kernel.deployer.core.DeploymentException;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.internal.AbstractInstallArtifact;
import org.eclipse.virgo.util.common.GraphNode;
import org.eclipse.virgo.util.common.GraphNode.ExceptionThrowingDirectedAcyclicGraphVisitor;

/**
 * Helper methods for manipulating {@link GraphNode GraphNode&lt;InstallArtifact&gt;} instances.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is thread safe.
 * 
 */
// TODO DAG - maybe we do not need this helper class anymore.
public class GraphUtils {

    /**
     * Adds the given child graph as a subgraph of the given parent graph and fixes up the subgraph artifacts to refer to
     * the copies.
     * 
     * @param parent to get new child
     * @param child to add to parent
     * @throws DeploymentException not really
     */
    public static void addChild(GraphNode<InstallArtifact> parent, GraphNode<InstallArtifact> child) throws DeploymentException {
        parent.addChild(child);
        setGraphReferences(child);
    }

    // TODO DAG - Is this necessary?! We do not copy any more!
    /**
     * The given graph was deeply copied. This method sets the references in each install artifact to its
     * corresponding copied tree.
     */
    private static void setGraphReferences(GraphNode<InstallArtifact> graph) throws DeploymentException {
        graph.visit(new ExceptionThrowingDirectedAcyclicGraphVisitor<InstallArtifact, DeploymentException>() {
            public boolean visit(GraphNode<InstallArtifact> node) throws DeploymentException {
                InstallArtifact value = node.getValue();
                if (value instanceof AbstractInstallArtifact) {
                    ((AbstractInstallArtifact)value).setGraph(node);
                }
                return true;
            }
        });
    }

}
