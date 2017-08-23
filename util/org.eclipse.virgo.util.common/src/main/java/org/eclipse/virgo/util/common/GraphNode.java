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
 * {@link GraphNode} is a node in a {@link DirectedAcyclicGraph}. Each node has a value.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations of this interface may or may not be thread safe.
 * 
 * @param <V> type of values in the graph nodes.
 */
public interface GraphNode<V> {

    /**
     * {@link DirectedAcyclicGraphVisitor} is used to visit a node and, at the option of the visitor, its subgraphs
     * recursively. <strong>Note</strong>: Shared nodes are only visited once.
     * <p />
     * 
     * <strong>Concurrent Semantics</strong><br />
     * 
     * Implementations of this interface should be thread safe when used with a thread safe
     * <code>DirectedAcyclicGraph</code> implementation.
     * 
     * @param <V> type of values in graph nodes
     */
    interface DirectedAcyclicGraphVisitor<V> {

        /**
         * Visits the given {@link GraphNode}. The return value determines whether or not any subgraphs of the given
         * node are visited.
         * 
         * @param node the <code>GraphNode</code> to be visited
         * @return <code>true</code> if and only if the subgraphs of the given node should be visited.
         */
        boolean visit(GraphNode<V> node);

    }

    /**
     * An <code>ExceptionThrowingDirectedAcyclicGraphVisitor</code> is used to visit a node and, at the option of the
     * visitor, its subgraphs recursively if the {@link #visit(GraphNode)} implementation needs to be able to throw a
     * checked {@link Exception}. <strong>Note</strong>: Shared nodes are only visited once.
     * <p />
     * 
     * <strong>Concurrent Semantics</strong><br />
     * 
     * Implementations of this interface should be thread safe when used with a thread safe
     * <code>DirectedAcyclicGraph</code> implementation.
     * 
     * @param <V> type of values in graph nodes
     * @param <E> type of exceptions possibly thrown
     */
    interface ExceptionThrowingDirectedAcyclicGraphVisitor<V, E extends Exception> {

        /**
         * Visits the given {@link GraphNode}. The return value determines whether or not any subgraphs of the given
         * node are visited.
         * 
         * @param node the <code>GraphNode</code> to be visited
         * @throws E if an error occurs when visiting the given node
         * @return <code>true</code> if and only if the subgraphs of the given node should be visited.
         */
        boolean visit(GraphNode<V> node) throws E;

    }

    /**
     * Returns the node's value. If there is no value associated with this node, returns <code>null</code>.
     * 
     * @return the value, which may be <code>null</code>
     */
    V getValue();

    /**
     * Returns a list of this node's children (not copies of the children). If the node has no children, returns an
     * empty list. Never returns <code>null</code> .
     * 
     * @return this node's children
     */
    List<GraphNode<V>> getChildren();

    /**
     * Adds the given node as child to this node. The child node is <strong>not</strong> copied.
     * 
     * @param child the node to add
     * @throws IllegalArgumentException if the given node does not belong to the same {@link DirectedAcyclicGraph}.
     * @throws IllegalArgumentException if the given node is already a child of this node.
     */
    void addChild(GraphNode<V> child);

    /**
     * Removes the occurrence of the given node from this node's children. Returns <code>true</code> if the child was
     * found and removed, otherwise <code>false</code>.
     * 
     * @param child the node to remove
     * @throws IllegalArgumentException if the given node does not belong to the same {@link DirectedAcyclicGraph}.
     * @return <code>true</code> if the node was removed successfully, otherwise <code>false</code>.
     * @see java.util.List#remove
     */
    boolean removeChild(GraphNode<V> child);

    /**
     * Returns a list of this node's parents (not copies of the parents). If this node does not have any parents,
     * returns an empty list. Never returns <code>null</code>.
     * 
     * @return this node's parents
     */
    List<GraphNode<V>> getParents();

    /**
     * Traverse this {@link GraphNode} in preorder (see below) and call the visit method of the given
     * {@link DirectedAcyclicGraphVisitor} at each node. The visitor determines whether the subgraphs of each visited
     * node should also be visited. <strong>Note</strong>: Shared nodes are only visited once.
     * <p/>
     * Preorder traversal visits the node and then visits, in preorder, each subgraph of the node.
     * 
     * @param visitor a {@link DirectedAcyclicGraphVisitor}
     */
    void visit(DirectedAcyclicGraphVisitor<V> visitor);

    /**
     * Traverse this {@link GraphNode} in preorder (see below) and call the visit method of the given
     * {@link ExceptionThrowingDirectedAcyclicGraphVisitor} at each node. The visitor determines whether the subgraph of
     * each visited node should also be visited. <strong>Note</strong>: Shared nodes are only visited once.
     * <p/>
     * Preorder traversal visits the node and then visits, in preorder, each subgraph of the node.
     * 
     * @param <E> type of exception possibly thrown
     * @param visitor a {@link ExceptionThrowingDirectedAcyclicGraphVisitor}
     * @throws E if an error occurs when visiting the node
     */
    <E extends Exception> void visit(ExceptionThrowingDirectedAcyclicGraphVisitor<V, E> visitor) throws E;

    /**
     * Returns the number of nodes in the subgraph of this node. The subgraph includes the node itself as the only root
     * node.
     * 
     * <p />
     * <strong>Note</strong>: Nodes may be shared and shared nodes are counted only once.
     * 
     * <p />
     * If there are more than <tt>Integer.MAX_VALUE</tt> nodes, the return value is undefined and the user should seek
     * professional help.
     * 
     * @return the number of nodes in the subgraph
     */
    int size();

    /**
     * Check if this node is a root node (a node without parents).
     * 
     * @return <code>true</code> if the node has no parents. Otherwise <code>false</code>.
     */
    boolean isRootNode();

}
