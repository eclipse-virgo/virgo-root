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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link GraphNode} is a node in a {@link DirectedAcyclicGraph}. Each node has a value.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is thread safe.
 * 
 * @param <V> type of values in the graph
 */
class ThreadSafeGraphNode<V> implements GraphNode<V> {

    private final V value;

    private final Object monitor;

    private static final Object tieMonitor = new Object();

    private final List<ThreadSafeGraphNode<V>> children = new ArrayList<ThreadSafeGraphNode<V>>();

    private final List<ThreadSafeGraphNode<V>> parents = new ArrayList<ThreadSafeGraphNode<V>>();

    /**
     * Construct a {@link ThreadSafeGraphNode} with the given value, which may be <code>null</code>.
     * 
     * @param value the value of the node, which may be <code>null</code>
     * @param monitor the shared monitor of the graph
     */
    ThreadSafeGraphNode(V value, Object monitor) {
        this.value = value;
        this.monitor = monitor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V getValue() {
        return this.value;
    }

    /**
     * Returns a list of this node's children (not copies of the children). If the node has no children, returns an
     * empty list. Never returns <code>null</code> .
     * <p/>
     * The returned list is synchronized to preserve thread safety, but may still result in
     * ConcurrentModificationException being thrown.
     * 
     * @return this node's children
     */
    @Override
    public List<GraphNode<V>> getChildren() {
        synchronized (this.monitor) {
            return new SynchronizedList<GraphNode<V>>(this.children, this.monitor);
        }
    }

    /**
     * Adds the given node as child to this node. The child node is <strong>not</strong> copied.
     * 
     * @param child the node to add
     * @throws IllegalArgumentException if the given node does not belong to the same {@link DirectedAcyclicGraph}.
     * @throws IllegalArgumentException if the given node is already a child of this node.
     * @throws IllegalArgumentException if the given node is not a {@link ThreadSafeGraphNode}.
     */
    @Override
    public void addChild(GraphNode<V> child) {
        ThreadSafeGraphNode<V> concreteChild = assertTypeAndMembership(child);
        synchronized (this.monitor) {
            Assert.isFalse(this.children.contains(child), "The node '%s' is already a child of '%s'", child, this);
            doCycleCheck(concreteChild);
            this.children.add(concreteChild);
            concreteChild.parents.add(this);
        }
    }

    private ThreadSafeGraphNode<V> assertTypeAndMembership(GraphNode<V> child) {
        Assert.isInstanceOf(ThreadSafeGraphNode.class, child, "A child must be of type %s.", this.getClass().getName());
        ThreadSafeGraphNode<V> concreteChild = (ThreadSafeGraphNode<V>) child;
        Assert.isTrue(belongsToSameGraph(concreteChild), "The node '%s' does not belong to the same graph as '%s'", concreteChild, this);
        return concreteChild;
    }

    private boolean belongsToSameGraph(ThreadSafeGraphNode<V> other) {
        return this.monitor == other.monitor;
    }

    protected boolean belongsToSameGraph(Object monitor) {
        return this.monitor == monitor;
    }

    // check for cycles when adding a new child to a parent (is this node a descendant of the new child?).
    private void doCycleCheck(GraphNode<V> child) {
        synchronized (this.monitor) {
            DescendentChecker<V> descendentChecker = new DescendentChecker<V>(this);
            child.visit(descendentChecker);
            Assert.isFalse(descendentChecker.isDescendent(), "Can't add '%s'. This node is a descendent of the new child.", child);
        }
    }

    /**
     * Removes the occurrence of the given node from this node's children. Returns <code>true</code> if the child was
     * found and removed, otherwise <code>false</code>.
     * 
     * @param child the node to remove
     * @throws IllegalArgumentException if the given node does not belong to the same {@link DirectedAcyclicGraph}.
     * @throws IllegalArgumentException if the given node is not a {@link ThreadSafeGraphNode}.
     * @return <code>true</code> if the node was removed successfully, otherwise <code>false</code>.
     * @see java.util.List#remove
     */
    @Override
    public boolean removeChild(GraphNode<V> child) {
        ThreadSafeGraphNode<V> concreteChild = assertTypeAndMembership(child);
        synchronized (this.monitor) {
            boolean removed = this.children.remove(concreteChild);
            if (removed) {
                removeParent(child, this);
            }
            return removed;
        }
    }

    /*
     * All the children in this.children share this.monitor.
     */
    private void removeParent(GraphNode<V> child, GraphNode<V> parent) {
        synchronized (this.monitor) {
            if (child instanceof ThreadSafeGraphNode<?>) {
                ThreadSafeGraphNode<V> concreteChild = (ThreadSafeGraphNode<V>) child;
                concreteChild.parents.remove(parent);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit(DirectedAcyclicGraphVisitor<V> visitor) {
        visitInternal(visitor, new HashMap<ThreadSafeGraphNode<V>, Boolean>());
    }

    private void visitInternal(DirectedAcyclicGraphVisitor<V> visitor, Map<ThreadSafeGraphNode<V>, Boolean> visitedFlags) {
        if (visitedFlags.containsKey(this)) {
            return;
        }
        visitedFlags.put(this, Boolean.TRUE);
        if (visitor.visit(this)) {
            for (int i = 0; i < numChildren(); i++) {
                ThreadSafeGraphNode<V> nextChild = getChild(i);
                if (nextChild != null) {
                    nextChild.visitInternal(visitor, visitedFlags);
                } else {
                    break;
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <E extends Exception> void visit(ExceptionThrowingDirectedAcyclicGraphVisitor<V, E> visitor) throws E {
        visitInternal(visitor, new HashMap<ThreadSafeGraphNode<V>, Boolean>());
    }

    private <E extends Exception> void visitInternal(ExceptionThrowingDirectedAcyclicGraphVisitor<V, E> visitor,
        Map<ThreadSafeGraphNode<V>, Boolean> visitedFlags) throws E {
        if (visitedFlags.containsKey(this)) {
            return;
        }
        visitedFlags.put(this, Boolean.TRUE);
        if (visitor.visit(this)) {
            for (int i = 0; i < numChildren(); i++) {
                ThreadSafeGraphNode<V> nextChild = getChild(i);
                if (nextChild != null) {
                    nextChild.visitInternal(visitor, visitedFlags);
                } else {
                    break;
                }
            }
        }
    }

    private ThreadSafeGraphNode<V> getChild(int i) {
        synchronized (this.monitor) {
            try {
                return this.children.get(i);
            } catch (IndexOutOfBoundsException e) {
                return null;
            }
        }
    }

    private int numChildren() {
        synchronized (this.monitor) {
            return this.children.size();
        }
    }

    private static class DescendentChecker<V> implements DirectedAcyclicGraphVisitor<V> {

        private final ThreadSafeGraphNode<V> prospect;

        private boolean descendent = false;

        public DescendentChecker(ThreadSafeGraphNode<V> prospect) {
            this.prospect = prospect;
        }

        @Override
        public boolean visit(GraphNode<V> node) {
            if (this.descendent) {
                return false;
            }
            if (this.prospect == node) {
                this.descendent = true;
                return false;
            }
            return true;
        }

        public boolean isDescendent() {
            return this.descendent;
        }

    }

    private static class SizeVisitor<V> implements DirectedAcyclicGraphVisitor<V> {

        private int size;

        @Override
        public boolean visit(GraphNode<V> dag) {
            this.size += 1;
            return true;
        }

        public int getSize() {
            return this.size;
        }
    };

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        SizeVisitor<V> sizeVisitor = new SizeVisitor<V>();
        visit(sizeVisitor);
        return sizeVisitor.getSize();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        synchronized (this.monitor) {
            final int prime = 31;
            int result = 1;
            result = prime * result + this.children.hashCode();
            result = prime * result + (this.value == null ? 0 : this.value.hashCode());
            return result;
        }
    }

    /**
     * {@inheritDoc}
     */
    // TODO TSGN.equals regards nodes as equal which have different sets of parents.
    // TODO TSGN.equals regards distinct nodes with no children (no parents once the todo above is fixed) and the same
    // value as equal. These nodes were created separately, possibly even from distinct DAGs as currently coded.
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
        ThreadSafeGraphNode<V> other = (ThreadSafeGraphNode<V>) obj;
        int thisHash = System.identityHashCode(this);
        int otherHash = System.identityHashCode(other);
        if (thisHash < otherHash) {
            synchronized (this.monitor) {
                synchronized (other.monitor) {
                    if (!this.children.equals(other.children)) {
                        return false;
                    }
                }
            }
        } else if (thisHash > otherHash) {
            synchronized (other.monitor) {
                synchronized (this.monitor) {
                    if (!this.children.equals(other.children)) {
                        return false;
                    }
                }
            }
        } else {
            synchronized (tieMonitor) {
                synchronized (this.monitor) {
                    synchronized (other.monitor) {
                        if (!this.children.equals(other.children)) {
                            return false;
                        }
                    }
                }
            }
        }
        if (this.value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (!this.value.equals(other.value)) {
            return false;
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuffer result = new StringBuffer();
        result.append(this.value != null ? this.value : "null").append("<");
        synchronized (this.monitor) {
            boolean first = true;
            for (ThreadSafeGraphNode<V> child : this.children) {
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
     * Returns a list of this graph's parents (not copies of the parents). If the graph has no parents, returns an empty
     * list. Never returns <code>null</code> .
     * <p/>
     * The returned list is synchronized to preserve thread safety, but may still result in
     * ConcurrentModificationException being thrown.
     * 
     * @return this graph's parents
     */
    // TODO TSGN.getParents share its monitor with return values of getParents and getChildren. It feels as if the user
    // might get some surprising deadlocks.
    @Override
    public List<GraphNode<V>> getParents() {
        synchronized (this.monitor) {
            return new SynchronizedList<GraphNode<V>>(this.parents, this.monitor);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRootNode() {
        return this.parents.isEmpty();
    }

}
