/*******************************************************************************
 * Copyright (c) 2011 VMware Inc. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution (ThreadSafeArrayListTreeTests.java)
 *   EclipseSource - reworked from generic tree to DAG (Bug 358697)
 *******************************************************************************/

package org.eclipse.virgo.util.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.concurrent.CyclicBarrier;

import org.junit.Before;
import org.junit.Test;

public class ThreadSafeAcyclicDirectedGraphTests {

    private DirectedAcyclicGraph<String> graph;

    private static DirectedAcyclicGraph<String> getDAG() {
        return getDAG("We");
    }

    private static DirectedAcyclicGraph<String> getDAG(String rootValue) {
        DirectedAcyclicGraph<String> graph = new ThreadSafeDirectedAcyclicGraph<String>();

        // shared nodes
        GraphNode<String> lo = graph.createRootNode("Lo");
        GraphNode<String> fi = graph.createRootNode("Fi");

        // We.add(Pa)
        GraphNode<String> we = graph.createRootNode("We");
        GraphNode<String> pa = graph.createRootNode("Pa");
        we.addChild(pa);
        // Pa.add(Cr)
        GraphNode<String> cr = graph.createRootNode("Cr");
        pa.addChild(cr);
        // Cr.add(B1,Lo,Fi)
        cr.addChild(graph.createRootNode("B1"));
        cr.addChild(lo);
        cr.addChild(fi);

        // Pa.add(Cu)
        GraphNode<String> cu = graph.createRootNode("Cu");
        pa.addChild(cu);
        cu.addChild(graph.createRootNode("B2"));
        cu.addChild(lo);
        cu.addChild(fi);

        // Pa.add(B3)
        pa.addChild(graph.createRootNode("B3"));
        // Pa.add(Lo)
        pa.addChild(lo);

        return graph;
    }

    @Before
    public void setUp() {
        this.graph = getDAG();
    }

    @Test
    public void testEmptyGraph() throws Exception {
        DirectedAcyclicGraph<String> emptyGraph = new ThreadSafeDirectedAcyclicGraph<String>();

        assertNotNull(emptyGraph);
        assertNotNull(emptyGraph.getRootNodes());
        assertEquals("<>", emptyGraph.toString());
        assertTrue(emptyGraph.getRootNodes().isEmpty());
    }

    @Test
    public void testGraphWithSingleRootNode() throws Exception {
        DirectedAcyclicGraph<String> smallGraph = new ThreadSafeDirectedAcyclicGraph<String>();

        GraphNode<String> rootNode = smallGraph.createRootNode("root");

        assertEquals("<root<>>", smallGraph.toString());
        assertEquals(1, smallGraph.getRootNodes().size());

        smallGraph.deleteRootNode(rootNode);

        assertEquals(0, smallGraph.getRootNodes().size());
    }

    @Test
    public void testGraphWithSingleNullRootNode() throws Exception {
        DirectedAcyclicGraph<String> smallGraph = new ThreadSafeDirectedAcyclicGraph<String>();

        GraphNode<String> rootNode = smallGraph.createRootNode(null);

        assertEquals("<null<>>", smallGraph.toString());
        assertEquals(1, smallGraph.getRootNodes().size());

        smallGraph.deleteRootNode(rootNode);

        assertEquals(0, smallGraph.getRootNodes().size());
    }

    @Test
    public void testGraphWithOnlyChildren() throws Exception {
        DirectedAcyclicGraph<String> onlyChildGraph = new ThreadSafeDirectedAcyclicGraph<String>();

        GraphNode<String> rootNode = onlyChildGraph.createRootNode("root");
        assertEquals("<root<>>", onlyChildGraph.toString());
        assertEquals(1, onlyChildGraph.getRootNodes().size());

        GraphNode<String> child = onlyChildGraph.createRootNode("child");
        rootNode.addChild(child);
        assertEquals("<root<child<>>>", onlyChildGraph.toString());

        GraphNode<String> grandchild = onlyChildGraph.createRootNode("grandchild");
        child.addChild(grandchild);
        assertEquals("<root<child<grandchild<>>>>", onlyChildGraph.toString());
        assertEquals(1, onlyChildGraph.getRootNodes().size());

        assertTrue(child.removeChild(grandchild));
        onlyChildGraph.deleteRootNode(grandchild);
        assertTrue(rootNode.removeChild(child));
        onlyChildGraph.deleteRootNode(child);
        onlyChildGraph.deleteRootNode(rootNode);
        assertEquals(0, onlyChildGraph.getRootNodes().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDirectCycle() throws Exception {
        DirectedAcyclicGraph<Integer> smallGraph = new ThreadSafeDirectedAcyclicGraph<Integer>();
        GraphNode<Integer> root = smallGraph.createRootNode(Integer.valueOf(42));

        root.addChild(root);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParentNodeIsNotADescendantOfTheNewChild() throws Exception {
        DirectedAcyclicGraph<String> dag = new ThreadSafeDirectedAcyclicGraph<String>();
        GraphNode<String> rootNode = dag.createRootNode("root");
        GraphNode<String> child = dag.createRootNode("child");

        rootNode.addChild(child);
        child.addChild(rootNode);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParentIsNotADescendantOfTheNewChildDistanceTwo() throws Exception {
        DirectedAcyclicGraph<String> dag = new ThreadSafeDirectedAcyclicGraph<String>();
        GraphNode<String> rootNode = dag.createRootNode("root");

        GraphNode<String> child = dag.createRootNode("child");
        rootNode.addChild(child);
        GraphNode<String> child2 = dag.createRootNode("child2");
        rootNode.addChild(child2);
        GraphNode<String> grandchild = dag.createRootNode("grandchild");
        child.addChild(grandchild);

        grandchild.addChild(rootNode);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeleteRootNodeFromOtherGraph() {
        DirectedAcyclicGraph<String> smallGraph = new ThreadSafeDirectedAcyclicGraph<String>();
        GraphNode<String> r1 = smallGraph.createRootNode("R1");

        this.graph.deleteRootNode(r1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeleteRootNodeOfWrongType() {
        this.graph.deleteRootNode(new ThreadSafeGraphNodeTests.NoopGraphNode());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeleteRootNodeWithChildren() {
        DirectedAcyclicGraph<String> smallGraph = new ThreadSafeDirectedAcyclicGraph<String>();
        GraphNode<String> r1 = smallGraph.createRootNode("R1");
        GraphNode<String> c1 = smallGraph.createRootNode("C1");
        r1.addChild(c1);

        this.graph.deleteRootNode(r1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeleteNonRootNode() {
        DirectedAcyclicGraph<String> smallGraph = new ThreadSafeDirectedAcyclicGraph<String>();
        GraphNode<String> r1 = smallGraph.createRootNode("R1");
        GraphNode<String> c1 = smallGraph.createRootNode("C1");
        r1.addChild(c1);

        this.graph.deleteRootNode(c1);
    }

    @Test
    public void testAddSharedChildWithMultipleThreads() throws Exception {

        final DirectedAcyclicGraph<String> sharedChildGraph = new ThreadSafeDirectedAcyclicGraph<String>();
        final int THREAD_COUNT = 150;
        final CyclicBarrier barrier = new CyclicBarrier(THREAD_COUNT + 1);
        final GraphNode<String> sharedChild = sharedChildGraph.createRootNode("shared child");
        assertEquals(0, sharedChild.getParents().size());
        assertEquals(1, sharedChildGraph.getRootNodes().size());

        class AddChildThread extends Thread {

            private final int counter;

            public AddChildThread(int counter) {
                this.counter = counter;
            }

            @Override
            public void run() {
                try {
                    barrier.await(); // 1
                    GraphNode<String> root = sharedChildGraph.createRootNode("root" + this.counter);
                    root.addChild(sharedChild);
                    barrier.await(); // 2
                    barrier.await();
                    assertTrue(root.removeChild(sharedChild));
                    barrier.await(); // 3
                    barrier.await();
                    sharedChildGraph.deleteRootNode(root);
                    barrier.await();
                } catch (Exception e) {
                    fail();
                }
            }
        }

        for (int i = 0; i < THREAD_COUNT; i++) {
            new AddChildThread(i).start();
        }
        barrier.await(); // 1 - wait for all threads to be ready
        barrier.await(); // 2 - wait for all threads to create and add the nodes
        assertEquals(THREAD_COUNT, sharedChild.getParents().size());
        assertEquals(THREAD_COUNT, sharedChildGraph.getRootNodes().size());
        barrier.await(); //
        barrier.await(); // 3 - wait for all threads to be finish
        assertEquals(0, sharedChild.getParents().size());
        assertEquals(THREAD_COUNT + 1, sharedChildGraph.getRootNodes().size());
        barrier.await(); // wait for all threads to be finish
        assertEquals(0, sharedChild.getParents().size());
    }

    @Test
    public void testGraphWithSharedNodes() throws Exception {
        DirectedAcyclicGraph<String> smallGraph = new ThreadSafeDirectedAcyclicGraph<String>();

        GraphNode<String> r1 = smallGraph.createRootNode("R1");
        GraphNode<String> c1 = smallGraph.createRootNode("C1");
        GraphNode<String> c2 = smallGraph.createRootNode("C2");
        smallGraph.createRootNode("C3");
        assertEquals(4, smallGraph.getRootNodes().size());
        assertEquals("<R1<>, C1<>, C2<>, C3<>>", smallGraph.toString());

        r1.addChild(c1);
        assertEquals(3, smallGraph.getRootNodes().size());
        r1.addChild(c2);
        assertEquals(2, smallGraph.getRootNodes().size());
        assertEquals("<R1<C1<>, C2<>>, C3<>>", smallGraph.toString());

        GraphNode<String> r2 = smallGraph.createRootNode("R2");
        assertEquals(3, smallGraph.getRootNodes().size());
        assertEquals("<R1<C1<>, C2<>>, C3<>, R2<>>", smallGraph.toString());

        r2.addChild(c1);
        assertEquals(3, smallGraph.getRootNodes().size());
        assertEquals("<R1<C1<>, C2<>>, C3<>, R2<C1<>>>", smallGraph.toString());
    }

    @Test
    public void testDisassembleGraphWithSharedNodes() throws Exception {
        DirectedAcyclicGraph<String> smallGraph = new ThreadSafeDirectedAcyclicGraph<String>();
        GraphNode<String> r1 = smallGraph.createRootNode("R1");
        GraphNode<String> c1 = smallGraph.createRootNode("C1");
        GraphNode<String> c2 = smallGraph.createRootNode("C2");
        GraphNode<String> c3 = smallGraph.createRootNode("C3");
        r1.addChild(c1);
        r1.addChild(c2);
        GraphNode<String> r2 = smallGraph.createRootNode("R2");
        r2.addChild(c1);
        assertEquals("<R1<C1<>, C2<>>, C3<>, R2<C1<>>>", smallGraph.toString());
        assertEquals(3, smallGraph.getRootNodes().size());

        // remove shared node
        assertTrue(r2.removeChild(c1));
        assertEquals("<R1<C1<>, C2<>>, C3<>, R2<>>", smallGraph.toString());
        assertEquals(3, smallGraph.getRootNodes().size());
        assertTrue(r1.removeChild(c2));
        assertEquals(4, smallGraph.getRootNodes().size());
        assertTrue(r1.removeChild(c1));
        assertEquals(5, smallGraph.getRootNodes().size());
        assertEquals("<R1<>, C1<>, C2<>, C3<>, R2<>>", smallGraph.toString());
        assertTrue(smallGraph.deleteRootNode(r2));
        assertEquals(4, smallGraph.getRootNodes().size());
        assertEquals("<R1<>, C1<>, C2<>, C3<>>", smallGraph.toString());
        assertTrue(smallGraph.deleteRootNode(r1));
        assertEquals(3, smallGraph.getRootNodes().size());
        assertEquals("<C1<>, C2<>, C3<>>", smallGraph.toString());
        smallGraph.deleteRootNode(smallGraph.getRootNodes().get(0));
        assertEquals(2, smallGraph.getRootNodes().size());
        assertEquals("<C2<>, C3<>>", smallGraph.toString());
        assertTrue(smallGraph.deleteRootNode(c2));
        assertEquals(1, smallGraph.getRootNodes().size());
        assertTrue(smallGraph.deleteRootNode(c3));
        assertTrue(smallGraph.getRootNodes().isEmpty());
    }

    @Test
    public void testRemovalOfAnAlreadyRemovedRootNode() throws Exception {
        DirectedAcyclicGraph<String> smallGraph = new ThreadSafeDirectedAcyclicGraph<String>();
        GraphNode<String> r1 = smallGraph.createRootNode("R1");

        assertTrue(smallGraph.deleteRootNode(r1));

        assertFalse(smallGraph.deleteRootNode(r1));
    }

    @Test
    public void testHashCodeEquals() {
        assertFalse(this.graph.equals(null));

        assertFalse(this.graph.equals(new Object()));

        DirectedAcyclicGraph<String> graph2 = getDAG();
        assertEquals(this.graph.hashCode(), graph2.hashCode());
        assertEquals(this.graph, this.graph);
        assertEquals(graph2, this.graph);
        assertEquals(this.graph, graph2);

        DirectedAcyclicGraph<String> g1 = new ThreadSafeDirectedAcyclicGraph<String>();
        assertFalse(this.graph.equals(g1));
        assertFalse(g1.equals(this.graph));

        assertTrue(new ThreadSafeDirectedAcyclicGraph<String>().equals(new ThreadSafeDirectedAcyclicGraph<String>()));
    }

}
