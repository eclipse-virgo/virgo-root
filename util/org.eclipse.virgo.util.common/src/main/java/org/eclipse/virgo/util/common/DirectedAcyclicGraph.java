/*******************************************************************************
 * Copyright (c) 2011 VMware Inc. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution (Tree.java)
 *   EclipseSource - reworked from generic tree to DAG (Bug 358697)
 *******************************************************************************/

package org.eclipse.virgo.util.common;

import java.util.List;

/**
 * {@link DirectedAcyclicGraph} is a set of {@link GraphNode}s with a parent child relationship. The nodes are connected
 * to each other in a way that it is impossible to start at a node n and follow a child relationship that loops back to
 * n. The DAG may have multiple root nodes (nodes with no parents) and nodes may share children.
 * <p />
 * Once created a root node can become a non-root node by adding the node as a child to another node. This can be done
 * by calling the method addChild on a node. All nodes of a DAG are reachable from its root nodes.
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations of this interface may or may not be thread safe.
 * 
 * @param <V> type of values in the {@link GraphNode}s.
 */
public interface DirectedAcyclicGraph<V> {

    /**
     * Create a new {@link GraphNode} and add it to this {@link DirectedAcyclicGraph}'s root nodes. The value is not
     * copied.
     * 
     * @param value of the node to create
     * @return a node with the given value
     */
    GraphNode<V> createRootNode(V value);

    /**
     * Removes the occurrence of the given {@link GraphNode} from this {@link DirectedAcyclicGraph}'s root nodes.
     * Returns <code>true</code> if the node was found and removed, otherwise <code>false</code>.
     * 
     * <strong>Note</strong>: Removal is only allowed if the root node has no children.
     * 
     * @param node the node to remove
     * @throws IllegalArgumentException if the given node is not a root node (the node has one or more parents)
     * @throws IllegalArgumentException if the given node has children
     * @throws IllegalArgumentException if the given node does not belong to the same {@link DirectedAcyclicGraph}.
     * @return <code>true</code> if the node was removed successfully, otherwise <code>false</code>.
     * @see java.util.List#remove
     */
    boolean deleteRootNode(GraphNode<V> node);

    /**
     * Returns a list of this {@link DirectedAcyclicGraph}'s root nodes (not copies of the nodes). If the graph has no
     * root nodes (and is therefore empty), returns an empty list. Never returns <code>null</code>.
     * 
     * @return this graph's root nodes
     */
    List<GraphNode<V>> getRootNodes();

}
