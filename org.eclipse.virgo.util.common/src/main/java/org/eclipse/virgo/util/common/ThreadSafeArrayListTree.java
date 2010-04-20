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

package org.eclipse.virgo.util.common;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link ThreadSafeArrayListTree} is a value with an ordered collection of subtrees of the same type as the main tree.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is thread safe.
 * 
 * @param <V> type of values in tree
 */

public final class ThreadSafeArrayListTree<V> implements Tree<V> {

    private volatile V value;

    private final Object monitor;

    private static final Object tieMonitor = new Object();

    private final List<ThreadSafeArrayListTree<V>> children = new ArrayList<ThreadSafeArrayListTree<V>>();

    private Tree<V> parent;

    /**
     * Construct a tree with the given value, which may be <code>null</code>.
     * 
     * @param value the value of the tree, which may be <code>null</code>
     */
    public ThreadSafeArrayListTree(V value) {
        this(value, new Object());
    }

    protected ThreadSafeArrayListTree(V value, Object monitor) {
        this.value = value;
        this.monitor = monitor;
    }

    /**
     * Construct a tree by deeply copying the given tree, using the given parent, and inheriting the given monitor.
     * 
     * @param tree the tree to copy
     * @param parent the parent of the new tree or <code>null</code>
     * @param monitor the monitor to inherit
     */
    protected ThreadSafeArrayListTree(Tree<V> tree, Tree<V> parent, Object monitor) {
        this.value = tree.getValue();
        this.monitor = monitor;
        this.parent = parent;
        for (Tree<V> child : tree.getChildren()) {
            this.children.add(new ThreadSafeArrayListTree<V>(child, this, this.monitor));
        }
    }

    /**
     * Returns the tree's value. If there is no value associated with this tree, returns <code>null</code>.
     * 
     * @return the value, which may be <code>null</code>
     */
    public final V getValue() {
        return this.value;
    }

    /**
     * Returns a list of this tree's children (not copies of the children). If the tree has no children, returns an
     * empty list. Never returns <code>null</code> .
     * <p/>
     * The returned list is synchronized to preserve thread safety, but may still result in
     * ConcurrentModificationException being thrown.
     * 
     * @return this tree's children
     */
    public List<Tree<V>> getChildren() {
        synchronized (this.monitor) {
            return new SynchronizedList<Tree<V>>(this.children, this.monitor);
        }
    }

    /**
     * Adds a new child tree to this node's children. The child tree is copied, although its values are not. The copy
     * shares this tree's monitor.
     * 
     * @param child the child tree to add
     * @return the copy of the child tree
     */
    public Tree<V> addChild(Tree<V> child) {
        synchronized (this.monitor) {
            ThreadSafeArrayListTree<V> childCopy = new ThreadSafeArrayListTree<V>(child, this, this.monitor);
            this.children.add(childCopy);
            return childCopy;
        }
    }

    /**
     * Removes the first occurrence of the given child tree from this node's children. Returns <code>true</code> if the
     * child was found and removed, otherwise <code>false</code>.
     * 
     * @param child the child tree to remove
     * @return <code>true</code> if the child tree was removed successfully, otherwise <code>false</code>.
     * @see java.util.List#remove
     */
    public boolean removeChild(Tree<V> child) {
        synchronized (this.monitor) {
            boolean removed = this.children.remove(child);
            if (removed) {
                setParent(child, null);
            }
            return removed;
        }
    }

    /*
     * All the children in this.children share this.monitor.
     */
    private void setParent(Tree<V> child, Tree<V> parent) {
        synchronized (this.monitor) {
            if (child instanceof ThreadSafeArrayListTree<?>) {
                ThreadSafeArrayListTree<V> concreteChild = (ThreadSafeArrayListTree<V>) child;
                concreteChild.parent = parent;
            }
        }
    }

    /**
     * Traverse this {@link ThreadSafeArrayListTree} in preorder (see below) and call the visit method of the given
     * {@link Tree.TreeVisitor} at each node. The visitor determines whether the children of each visited tree should
     * also be visited.
     * <p/>
     * Preorder traversal visits the tree and then visits, in preorder, each child of the tree.
     * 
     * @param visitor a {@link Tree.TreeVisitor}
     */
    public void visit(TreeVisitor<V> visitor) {
        if (visitor.visit(this)) {
            for (int i = 0; i < numChildren(); i++) {
                ThreadSafeArrayListTree<V> nextChild = getChild(i);
                if (nextChild != null) {
                    nextChild.visit(visitor);
                } else {
                    break;
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public <E extends Exception> void visit(ExceptionThrowingTreeVisitor<V, E> visitor) throws E {
        if (visitor.visit(this)) {
            for (int i = 0; i < numChildren(); i++) {
                ThreadSafeArrayListTree<V> nextChild = getChild(i);
                if (nextChild != null) {
                    nextChild.visit(visitor);
                } else {
                    break;
                }
            }
        }
    }

    private ThreadSafeArrayListTree<V> getChild(int i) {
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

    /**
     * Returns the number of nodes in the tree. This is one plus the sum of the number of nodes in each of the children.
     * <p/>
     * If there are more than <tt>Integer.MAX_VALUE</tt> node, the return value is undefined and the user should seek
     * professional help.
     * 
     * @return the number of non-<code>null</code> nodes in the tree
     */
    public int size() {
        int size = 1;
        synchronized (this.monitor) {
            for (ThreadSafeArrayListTree<V> child : this.children) {
                size += child.size();
            }
        }
        return size;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        synchronized (this.monitor) {
            final int prime = 31;
            int result = 1;
            result = prime * result + children.hashCode();
            result = prime * result + ((value == null) ? 0 : value.hashCode());
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
        ThreadSafeArrayListTree<V> other = (ThreadSafeArrayListTree<V>) obj;
        int thisHash = System.identityHashCode(this);
        int otherHash = System.identityHashCode(other);
        if (thisHash < otherHash) {
            synchronized (this.monitor) {
                synchronized (other.monitor) {
                    if (!children.equals(other.children)) {
                        return false;
                    }
                }
            }
        } else if (thisHash > otherHash) {
            synchronized (other.monitor) {
                synchronized (this.monitor) {
                    if (!children.equals(other.children)) {
                        return false;
                    }
                }
            }
        } else {
            synchronized (tieMonitor) {
                synchronized (this.monitor) {
                    synchronized (other.monitor) {
                        if (!children.equals(other.children)) {
                            return false;
                        }
                    }
                }
            }
        }
        if (value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (!value.equals(other.value)) {
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
            for (ThreadSafeArrayListTree<V> child : this.children) {
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
    public Tree<V> getParent() {
        return this.parent;
    }
}
