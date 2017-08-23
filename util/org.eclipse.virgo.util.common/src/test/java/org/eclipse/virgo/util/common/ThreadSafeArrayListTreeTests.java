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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.virgo.util.common.ThreadSafeArrayListTree;
import org.eclipse.virgo.util.common.Tree;
import org.eclipse.virgo.util.common.Tree.ExceptionThrowingTreeVisitor;
import org.eclipse.virgo.util.common.Tree.TreeVisitor;
import org.junit.Before;
import org.junit.Test;

/**
 */
public class ThreadSafeArrayListTreeTests {

    private static void add(List<String> l, String... s) {
        for (String string : s) {
            l.add(string);
        }
    }

    private static final ArrayList<String> EXPECTED_VISITS = new ArrayList<String>();

    static {
        add(EXPECTED_VISITS, "a", "b", "c", "d", "e", "f", "g");
    }

    private Tree<String> top;

    private static Tree<String> getTree() {
        return getTree("a");
    }

    private static Tree<String> getTree(String rootValue) {
        Tree<String> top = new ThreadSafeArrayListTree<String>(rootValue);

        Tree<String> left = new ThreadSafeArrayListTree<String>("b");
        left.addChild(new ThreadSafeArrayListTree<String>("c"));
        left.addChild(new ThreadSafeArrayListTree<String>("d"));

        Tree<String> right = new ThreadSafeArrayListTree<String>("e");
        right.addChild(new ThreadSafeArrayListTree<String>("f"));
        right.addChild(new ThreadSafeArrayListTree<String>("g"));

        top.addChild(left);
        top.addChild(right);

        return top;
    }

    @Before
    public void setUp() {
        this.top = getTree();
    }

    @Test
    public void testToString() {
        assertEquals("a<b<c<>, d<>>, e<f<>, g<>>>", this.top.toString());

        assertEquals("null<>", new ThreadSafeArrayListTree<String>(null).toString());
    }

    @Test
    public void testHashCodeEquals() {
        assertFalse(this.top.equals(null));

        assertFalse(this.top.equals(new Object()));

        Tree<String> top2 = getTree();
        assertEquals(this.top.hashCode(), top2.hashCode());
        assertEquals(this.top, this.top);
        assertEquals(top2, this.top);
        assertEquals(this.top, top2);

        Tree<String> t1 = new ThreadSafeArrayListTree<String>("a");
        {
            Tree<String> a = t1;
            assertFalse(this.top.equals(a));
            assertFalse(a.equals(this.top));
        }

        {
            Tree<String> a = new ThreadSafeArrayListTree<String>(null);
            assertFalse(this.top.equals(a));
            assertFalse(a.equals(this.top));
        }

        assertTrue(new ThreadSafeArrayListTree<String>(null).equals(new ThreadSafeArrayListTree<String>(null)));

        Tree<String> t2 = new ThreadSafeArrayListTree<String>("b");
        assertFalse(t1.equals(t2));
        assertFalse(t2.equals(t1));
    }

    @Test
    public void testSize() {
        assertEquals(7, this.top.size());
    }

    @Test
    public void testChildren() {
        List<Tree<String>> children = this.top.getChildren();
        assertEquals(2, children.size());
        Tree<String> newChild = getTree("newChild");
        Tree<String> childCopy = this.top.addChild(newChild);
        assertEquals(3, children.size());
        assertEquals(newChild, children.get(2));
        assertEquals(newChild, childCopy);
    }

    @Test
    public void testCopyOnAddChild() {
        this.top.addChild(this.top);
        assertEquals(14, this.top.size());

        // Ensure parents are correctly set up in the copy.
        checkParents(this.top);
    }

    private void checkParents(Tree<String> tree) {
        List<Tree<String>> children = tree.getChildren();
        for (Tree<String> child : children) {
            assertEquals(tree, child.getParent());
            checkParents(child);
        }
    }

    @Test
    public void testChildrenListModification() {
        List<Tree<String>> children = this.top.getChildren();
        assertEquals(2, children.size());
        Tree<String> newChild = getTree("newChild");
        children.add(newChild);
        assertEquals(3, children.size());
        assertEquals(newChild, children.get(2));
    }

    @Test
    public void testGetValue() {
        assertEquals("a", this.top.getValue());
    }

    @Test
    public void testRemoveChild() {
        List<Tree<String>> children = this.top.getChildren();
        assertTrue(this.top.removeChild(children.get(0)));
        assertEquals(4, this.top.size());
    }

    @Test
    public void testParent() {
        List<Tree<String>> children = this.top.getChildren();
        Tree<String> child = children.get(0);
        assertEquals(this.top, child.getParent());
        this.top.removeChild(child);
        assertNull(child.getParent());
    }

    @Test
    public void testNormalVisit() {
        TestTreeVisitor visitor = new TestTreeVisitor();
        this.top.visit(visitor);
        assertTrue(EXPECTED_VISITS.equals(visitor.getVisited()));
    }

    @Test
    public void testSkippedVisit() {
        TestTreeVisitor visitor = new TestTreeVisitor();
        Tree<String> t = new ThreadSafeArrayListTree<String>("-");
        t.visit(visitor);
        List<String> visited = visitor.getVisited();
        assertEquals(1, visited.size());
        assertEquals("-", visited.get(0));
    }

    @Test
    public void testPartiallySkippedVisit() {
        TestTreeVisitor visitor = new TestTreeVisitor();
        Tree<String> t = new ThreadSafeArrayListTree<String>("-");
        t.addChild(this.top);
        this.top.addChild(t);
        assertEquals(15, this.top.size());
        this.top.visit(visitor);
        assertEquals(8, visitor.getVisited().size());
    }

    @Test
    public void testNormalExceptionVisit() throws Exception {
        TestTreeExceptionVisitor visitor = new TestTreeExceptionVisitor();
        this.top.visit(visitor);
        assertTrue(EXPECTED_VISITS.equals(visitor.getVisited()));
    }

    @Test
    public void testSkippedExceptionVisit() throws Exception {
        TestTreeExceptionVisitor visitor = new TestTreeExceptionVisitor();
        Tree<String> t = new ThreadSafeArrayListTree<String>("-");
        t.visit(visitor);
        List<String> visited = visitor.getVisited();
        assertEquals(1, visited.size());
        assertEquals("-", visited.get(0));
    }

    @Test
    public void testPartiallySkippedDueToStopExceptionVisit() throws Exception {
        TestTreeExceptionVisitor visitor = new TestTreeExceptionVisitor();
        Tree<String> t = new ThreadSafeArrayListTree<String>("-");
        t.addChild(this.top);
        this.top.addChild(t);
        assertEquals(15, this.top.size());
        this.top.visit(visitor);
        assertEquals(8, visitor.getVisited().size());
    }

    @Test(expected = Exception.class)
    public void testPartiallySkippedDueToExceptionExceptionVisit() throws Exception {
        TestTreeExceptionVisitor visitor = new TestTreeExceptionVisitor();
        Tree<String> t = new ThreadSafeArrayListTree<String>("*");
        t.addChild(this.top);
        this.top.addChild(t);
        assertEquals(15, this.top.size());
        try {
            this.top.visit(visitor);
        } finally {
            assertEquals(8, visitor.getVisited().size());
        }
    }

    private static class TestTreeVisitor implements TreeVisitor<String> {

        private List<String> visited = new ArrayList<String>();

        public boolean visit(Tree<String> tree) {
            String value = tree.getValue();
            visited.add(value);
            return !value.startsWith("-");
        }

        public List<String> getVisited() {
            return this.visited;
        }
    }

    private static class TestTreeExceptionVisitor implements ExceptionThrowingTreeVisitor<String, Exception> {

        private List<String> visited = new ArrayList<String>();

        public boolean visit(Tree<String> tree) throws Exception {
            String value = tree.getValue();
            visited.add(value);
            if (value.startsWith("*")) {
                throw new Exception();
            }
            return !value.startsWith("-");
        }

        public List<String> getVisited() {
            return this.visited;
        }
    }

}
