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

import java.util.List;

/**
 * {@link Tree} is a value with an ordered collection of subtrees of the same type as the main tree.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations of this interface may or may not be thread safe.
 * 
 * @param <V> type of values in tree nodes
 */

public interface Tree<V> {

    /**
     * {@link Tree.TreeVisitor} is an interface used to visit a tree and, at the option of the visitor, its children and so
     * on recursively.
     * <p />
     * 
     * <strong>Concurrent Semantics</strong><br />
     * 
     * Implementations of this interface should be thread safe when used with a thread safe <code>Tree</code>
     * implementation.
     * @param <V> type of values in tree nodes
     */
    public interface TreeVisitor<V> {

        /**
         * Visits the given {@link Tree}. The return value determines whether or not any children of the given tree are
         * visited.
         * 
         * @param tree a <code>Tree</code>
         * @return <code>true</code> if and only if the children of the given tree should be visited.
         */
        boolean visit(Tree<V> tree);

    }

    /**
     * An <code>ExceptionThrowingTreeVisitor</code> is used to visit a tree when the {@link #visit(Tree)} implementation
     * needs to be able to throw a checked {@link Exception}.
     * <p />
     * 
     * <strong>Concurrent Semantics</strong><br />
     * 
     * Implementations of this interface should be thread safe when used with a thread safe <code>Tree</code>
     * implementation.
     * @param <V> type of values in tree nodes
     * @param <E> type of exceptions possibly thrown
     */
    public interface ExceptionThrowingTreeVisitor<V, E extends Exception> {

        /**
         * Visits the given {@link Tree}. The return value determines whether or not any children of the given tree are
         * visited.
         * 
         * @param tree a <code>Tree</code>
         * @throws E if an error occurs when visiting the tree
         * @return <code>true</code> if and only if the children of the given tree should be visited.
         */
        boolean visit(Tree<V> tree) throws E;

    }

    /**
     * Returns the tree's value. If there is no value associated with this tree, returns <code>null</code>.
     * 
     * @return the value, which may be <code>null</code>
     */
    V getValue();

    /**
     * Returns a list of this tree's children (not copies of the children). If the tree has no children, returns an
     * empty list. Never returns <code>null</code> .
     * 
     * @return this tree's children
     */
    List<Tree<V>> getChildren();

    /**
     * Adds a new child tree to this node's children. The child tree is copied, although its values are not.
     * 
     * @param child the child tree to add
     * @return the copy of the child tree
     */
    Tree<V> addChild(Tree<V> child);

    /**
     * Removes the first occurrence of the given child tree from this node's children. Returns <code>true</code> if the
     * child was found and removed, otherwise <code>false</code>.
     * 
     * @param child the child tree to remove
     * @return <code>true</code> if the child tree was removed successfully, otherwise <code>false</code>.
     * @see java.util.List#remove
     */
    boolean removeChild(Tree<V> child);
    
    /**
     * Returns this tree's parent. If this tree does not have a parent, returns <code>null</code>.
     * 
     * @return this tree's parent
     */
    Tree<V> getParent();

    /**
     * Traverse this {@link Tree} in preorder (see below) and call the visit method of the given {@link TreeVisitor} at
     * each node. The visitor determines whether the children of each visited tree should also be visited.
     * <p/>
     * Preorder traversal visits the tree and then visits, in preorder, each child of the tree.
     * 
     * @param visitor a {@link TreeVisitor}
     */
    void visit(TreeVisitor<V> visitor);

    /**
     * Traverse this {@link Tree} in preorder (see below) and call the visit method of the given
     * {@link ExceptionThrowingTreeVisitor} at each node. The visitor determines whether the children of each visited
     * tree should also be visited.
     * <p/>
     * Preorder traversal visits the tree and then visits, in preorder, each child of the tree.
     * @param <E> type of exception possibly thrown
     * 
     * @param visitor the tree's visitor
     * @throws E if an error occurs when visiting the tree
     */
    <E extends Exception> void visit(ExceptionThrowingTreeVisitor<V, E> visitor) throws E;

    /**
     * Returns the number of nodes in the tree. This is one plus the sum of the number of nodes in each of the children.
     * <p/>
     * If there are more than <tt>Integer.MAX_VALUE</tt> node, the return value is undefined and the user should seek
     * professional help.
     * 
     * @return the number of non-<code>null</code> nodes in the tree
     */
    int size();

}
