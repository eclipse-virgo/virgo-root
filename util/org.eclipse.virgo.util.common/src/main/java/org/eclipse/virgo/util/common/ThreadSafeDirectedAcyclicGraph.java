/*******************************************************************************
 * Copyright (c) 2011 VMware Inc. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution (ThreadSafeArrayListTree.java)
 *   EclipseSource - reworked from generic tree to DAG (Bug 358697)
 *******************************************************************************/

package org.eclipse.virgo.util.common;

import java.util.ArrayList;
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
 * This class is thread safe.
 * 
 * @param <V> type of values in the graph
 */
public class ThreadSafeDirectedAcyclicGraph<V> implements DirectedAcyclicGraph<V> {

    private final Object monitor = new Object();

    private static final Object tieMonitor = new Object();

    private final List<ThreadSafeGraphNode<V>> nodes = new ArrayList<ThreadSafeGraphNode<V>>();

    /**
     * {@inheritDoc}
     */
    @Override
    public ThreadSafeGraphNode<V> createRootNode(V value) {
        synchronized (this.monitor) {
            ThreadSafeGraphNode<V> node = new ThreadSafeGraphNode<V>(value, this.monitor);
            this.nodes.add(node);
            return node;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteRootNode(GraphNode<V> node) {
        assertTypeAndMembership(node);
        synchronized (this.monitor) {
            Assert.isTrue(node.getChildren().isEmpty(), "Cannot delete node '%s'. Node has children. Please remove the children first.", node);
            Assert.isTrue(node.getParents().isEmpty(),
                "Cannot delete node '%s'. Node is still in use. Please remove it from the other node(s) first.", node);
            boolean removed = this.nodes.remove(node);
            return removed;
        }
    }

    private ThreadSafeGraphNode<V> assertTypeAndMembership(GraphNode<V> child) {
        Assert.isInstanceOf(ThreadSafeGraphNode.class, child, "A child must be of type %s.", this.getClass().getName());
        ThreadSafeGraphNode<V> concreteChild = (ThreadSafeGraphNode<V>) child;
        Assert.isTrue(concreteChild.belongsToSameGraph(this.monitor), "The node '%s' does not belong to the graph '%s'", concreteChild, this);
        return concreteChild;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<GraphNode<V>> getRootNodes() {
        List<GraphNode<V>> rootNodes = new ArrayList<GraphNode<V>>();
        synchronized (this.monitor) {
            for (GraphNode<V> node : this.nodes) {
                if (node.isRootNode()) {
                    rootNodes.add(node);
                }
            }
            return rootNodes;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuffer result = new StringBuffer();
        result.append("<");
        synchronized (this.monitor) {
            boolean first = true;
            for (GraphNode<V> child : getRootNodes()) {
                if (!first) {
                    result.append(", ");
                }
                result.append(child.toString());
                first = false;
            }
        }
        result.append(">");
        return result.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        synchronized (this.monitor) {
            final int prime = 31;
            int result = 1;
            result = prime * result + this.nodes.hashCode();
            return result;
        }
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ThreadSafeDirectedAcyclicGraph<V> other = (ThreadSafeDirectedAcyclicGraph<V>) obj;
        int thisHash = System.identityHashCode(this);
        int otherHash = System.identityHashCode(other);
        if (thisHash < otherHash) {
            synchronized (this.monitor) {
                synchronized (other.monitor) {
                    if (!this.nodes.equals(other.nodes)) {
                        return false;
                    }
                }
            }
        } else if (thisHash > otherHash) {
            synchronized (other.monitor) {
                synchronized (this.monitor) {
                    if (!this.nodes.equals(other.nodes)) {
                        return false;
                    }
                }
            }
        } else {
            synchronized (tieMonitor) {
                synchronized (this.monitor) {
                    synchronized (other.monitor) {
                        if (!this.nodes.equals(other.nodes)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

}
