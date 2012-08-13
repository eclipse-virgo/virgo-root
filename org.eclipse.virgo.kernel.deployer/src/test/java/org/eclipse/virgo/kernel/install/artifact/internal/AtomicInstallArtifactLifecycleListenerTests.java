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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.eclipse.virgo.kernel.artifact.ArtifactSpecification;
import org.eclipse.virgo.kernel.artifact.fs.ArtifactFS;
import org.eclipse.virgo.kernel.artifact.plan.PlanDescriptor.Provisioning;
import org.eclipse.virgo.nano.core.AbortableSignal;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.PlanInstallArtifact;
import org.eclipse.virgo.util.common.DirectedAcyclicGraph;
import org.eclipse.virgo.util.common.GraphNode;
import org.eclipse.virgo.util.common.ThreadSafeDirectedAcyclicGraph;
import org.junit.Test;
import org.osgi.framework.Version;

public class AtomicInstallArtifactLifecycleListenerTests {

    private static enum Methods {
        ASYNC_START, //
        START, //
        STOP, //
        UNINSTALL
    }

    private final AtomicInstallArtifactLifecycleListener listener = new AtomicInstallArtifactLifecycleListener();

    private final StubInstallArtifact oneAtomicNoTree = new StubInstallArtifact(true);

    private final StubInstallArtifact oneNonAtomicNoTree = new StubInstallArtifact(false);

    private final StubInstallArtifact oneAtomic = makeChain(new StubInstallArtifact(true));

    private final StubInstallArtifact oneNonAtomic = makeChain(new StubInstallArtifact(false));

    private final StubInstallArtifact twoNonAtomic = makeChain(new StubInstallArtifact(false), new StubInstallArtifact(false));

    private final StubInstallArtifact oneNonAtomicOneAtomic = makeChain(new StubInstallArtifact(true), new StubInstallArtifact(false));

    private final StubInstallArtifact twoNonAtomicOneAtomic = makeChain(new StubInstallArtifact(true), new StubInstallArtifact(false),
        new StubInstallArtifact(false));

    private final StubInstallArtifact oneNonAtomicOneAtomicOneNonAtomic = makeChain(new StubInstallArtifact(false), new StubInstallArtifact(true),
        new StubInstallArtifact(false));

    @Test
    public void onStarting() throws DeploymentException {
        listener.onStarting(oneAtomicNoTree);
        assertMethodCalls(oneAtomicNoTree);

        listener.onStarting(oneNonAtomicNoTree);
        assertMethodCalls(oneNonAtomicNoTree);

        listener.onStarting(oneAtomic);
        assertMethodCalls(oneAtomic);

        listener.onStarting(oneNonAtomic);
        assertMethodCalls(oneNonAtomic);

        listener.onStarting(twoNonAtomic);
        assertMethodCalls(twoNonAtomic);
        assertMethodCalls(getParent(twoNonAtomic));

        listener.onStarting(oneNonAtomicOneAtomic);
        assertMethodCalls(oneNonAtomicOneAtomic);
        assertMethodCalls(getParent(oneNonAtomicOneAtomic), Methods.START);

        listener.onStarting(twoNonAtomicOneAtomic);
        assertMethodCalls(twoNonAtomicOneAtomic);
        assertMethodCalls(getParent(twoNonAtomicOneAtomic));
        assertMethodCalls(getParent(getParent(twoNonAtomicOneAtomic)));

        listener.onStarting(oneNonAtomicOneAtomicOneNonAtomic);
        assertMethodCalls(oneNonAtomicOneAtomicOneNonAtomic);
        assertMethodCalls(getParent(oneNonAtomicOneAtomicOneNonAtomic), Methods.START);
        assertMethodCalls(getParent(getParent(oneNonAtomicOneAtomicOneNonAtomic)));
    }

    @Test
    public void onStartFailed() throws DeploymentException {
        listener.onStartFailed(oneAtomicNoTree, null);
        assertMethodCalls(oneAtomicNoTree);

        listener.onStartFailed(oneNonAtomicNoTree, null);
        assertMethodCalls(oneNonAtomicNoTree);

        listener.onStartFailed(oneAtomic, null);
        assertMethodCalls(oneAtomic);

        listener.onStartFailed(oneNonAtomic, null);
        assertMethodCalls(oneNonAtomic);

        listener.onStartFailed(twoNonAtomic, null);
        assertMethodCalls(twoNonAtomic);
        assertMethodCalls(getParent(twoNonAtomic));

        listener.onStartFailed(oneNonAtomicOneAtomic, null);
        assertMethodCalls(oneNonAtomicOneAtomic);
        assertMethodCalls(getParent(oneNonAtomicOneAtomic), Methods.STOP);

        listener.onStartFailed(twoNonAtomicOneAtomic, null);
        assertMethodCalls(twoNonAtomicOneAtomic);
        assertMethodCalls(getParent(twoNonAtomicOneAtomic));
        assertMethodCalls(getParent(getParent(twoNonAtomicOneAtomic)));

        listener.onStartFailed(oneNonAtomicOneAtomicOneNonAtomic, null);
        assertMethodCalls(oneNonAtomicOneAtomicOneNonAtomic);
        assertMethodCalls(getParent(oneNonAtomicOneAtomicOneNonAtomic), Methods.STOP);
        assertMethodCalls(getParent(getParent(oneNonAtomicOneAtomicOneNonAtomic)));
    }

    @Test
    public void onStopped() {
        listener.onStopped(oneAtomicNoTree);
        assertMethodCalls(oneAtomicNoTree);

        listener.onStopped(oneNonAtomicNoTree);
        assertMethodCalls(oneNonAtomicNoTree);

        listener.onStopped(oneAtomic);
        assertMethodCalls(oneAtomic);

        listener.onStopped(oneNonAtomic);
        assertMethodCalls(oneNonAtomic);

        listener.onStopped(twoNonAtomic);
        assertMethodCalls(twoNonAtomic);
        assertMethodCalls(getParent(twoNonAtomic));

        listener.onStopped(oneNonAtomicOneAtomic);
        assertMethodCalls(oneNonAtomicOneAtomic);
        assertMethodCalls(getParent(oneNonAtomicOneAtomic), Methods.STOP);

        listener.onStopped(twoNonAtomicOneAtomic);
        assertMethodCalls(twoNonAtomicOneAtomic);
        assertMethodCalls(getParent(twoNonAtomicOneAtomic));
        assertMethodCalls(getParent(getParent(twoNonAtomicOneAtomic)));

        listener.onStopped(oneNonAtomicOneAtomicOneNonAtomic);
        assertMethodCalls(oneNonAtomicOneAtomicOneNonAtomic);
        assertMethodCalls(getParent(oneNonAtomicOneAtomicOneNonAtomic), Methods.STOP);
        assertMethodCalls(getParent(getParent(oneNonAtomicOneAtomicOneNonAtomic)));
    }

    @Test
    public void onUninstalled() throws DeploymentException {
        listener.onUninstalled(oneAtomicNoTree);
        assertMethodCalls(oneAtomicNoTree);

        listener.onUninstalled(oneNonAtomicNoTree);
        assertMethodCalls(oneNonAtomicNoTree);

        listener.onUninstalled(oneAtomic);
        assertMethodCalls(oneAtomic);

        listener.onUninstalled(oneNonAtomic);
        assertMethodCalls(oneNonAtomic);

        listener.onUninstalled(twoNonAtomic);
        assertMethodCalls(twoNonAtomic);
        assertMethodCalls(getParent(twoNonAtomic));

        listener.onUninstalled(oneNonAtomicOneAtomic);
        assertMethodCalls(oneNonAtomicOneAtomic);
        assertMethodCalls(getParent(oneNonAtomicOneAtomic), Methods.UNINSTALL);

        listener.onUninstalled(twoNonAtomicOneAtomic);
        assertMethodCalls(twoNonAtomicOneAtomic);
        assertMethodCalls(getParent(twoNonAtomicOneAtomic));
        assertMethodCalls(getParent(getParent(twoNonAtomicOneAtomic)));

        listener.onUninstalled(oneNonAtomicOneAtomicOneNonAtomic);
        assertMethodCalls(oneNonAtomicOneAtomicOneNonAtomic);
        assertMethodCalls(getParent(oneNonAtomicOneAtomicOneNonAtomic), Methods.UNINSTALL);
        assertMethodCalls(getParent(getParent(oneNonAtomicOneAtomicOneNonAtomic)));
    }

    private StubInstallArtifact getParent(StubInstallArtifact artifact) {
        GraphNode<InstallArtifact> tree = artifact.getGraph();
        if (tree != null) {
        		List<GraphNode<InstallArtifact>> parents = tree.getParents();
            // TODO DAG: when the Tree is generalised to a DAG, this testcase can assume the DAG is still a tree in structure because the testcase constructs the tree.
            // TODO DAG Test case uses tree. -> get first parent.
            if (!parents.isEmpty()) {
            		GraphNode<InstallArtifact> parent = tree.getParents().get(0);
                return (StubInstallArtifact) parent.getValue();
            }
        }
        return null;
    }

    private void assertMethodCalls(StubInstallArtifact artifact, Methods... methods) {
        List<Methods> calledMethods = Arrays.asList(methods);
        if (calledMethods.contains(Methods.START)) {
            assertTrue(artifact.getStartCalled());
        } else {
            assertFalse(artifact.getStartCalled());
        }

        if (calledMethods.contains(Methods.STOP)) {
            assertTrue(artifact.getStopCalled());
        } else {
            assertFalse(artifact.getStopCalled());
        }

        if (calledMethods.contains(Methods.UNINSTALL)) {
            assertTrue(artifact.getUninstallCalled());
        } else {
            assertFalse(artifact.getUninstallCalled());
        }
    }

    /**
     * Create a chain (linear tree) behind a list of values:
     * 
     * <pre>
     * makeChain([a,b,c])
     * </pre>
     * 
     * produces
     * 
     * <pre>
     * (a)->(b)->(c)
     * </pre>
     * 
     * and returns <code><em>c</em></code>, the value of the last node (with tree attached).
     * 
     * @param installArtifactArray array of values to be in nodes of chain tree
     * @return value (with tree attached) at last leaf of chain tree
     */
    private final static StubInstallArtifact makeChain(StubInstallArtifact... installArtifactArray) {
        DirectedAcyclicGraph<InstallArtifact> dag = new ThreadSafeDirectedAcyclicGraph<InstallArtifact>();
        GraphNode<InstallArtifact> graph = null;
        for (StubInstallArtifact installArtifact : installArtifactArray) {
            GraphNode<InstallArtifact> leaf = dag.createRootNode(installArtifact);
			if (graph == null) {
				graph = leaf;
			} else {
                graph.addChild(leaf);
                graph = leaf;
            }
			installArtifact.setGraph(graph);
        }
        
        if(graph == null){
        		return null;
        }
        return (StubInstallArtifact) graph.getValue();
    }

    private static class StubInstallArtifact implements PlanInstallArtifact {

        private final boolean atomic;

        private volatile GraphNode<InstallArtifact> graph;

        private volatile boolean startCalled = false;

        private volatile boolean stopCalled = false;

        private volatile boolean uninstallCalled = false;

        public StubInstallArtifact(boolean atomic) {
            this.atomic = atomic;
        }

        public boolean getStartCalled() {
            return startCalled;
        }

        public boolean getStopCalled() {
            return stopCalled;
        }

        public boolean getUninstallCalled() {
            return uninstallCalled;
        }

        public GraphNode<InstallArtifact> getGraph() {
            return this.graph;
        }

        public void setGraph(GraphNode<InstallArtifact> graph) {
            this.graph = graph;
        }

        public void stop() throws DeploymentException {
            this.stopCalled = true;
        }

        public void start() throws DeploymentException {
            start(null);
        }

        public void start(AbortableSignal signal) throws DeploymentException {
            this.startCalled = true;
        }

        public void uninstall() throws DeploymentException {
            this.uninstallCalled = true;
        }

        public boolean isAtomic() {
            return this.atomic;
        }

        public ArtifactFS getArtifactFS() {
            throw new UnsupportedOperationException();
        }

        public String getName() {
            throw new UnsupportedOperationException();
        }

        public String getRepositoryName() {
            throw new UnsupportedOperationException();
        }

        public State getState() {
            throw new UnsupportedOperationException();
        }

        public String getType() {
            throw new UnsupportedOperationException();
        }

        public Version getVersion() {
            throw new UnsupportedOperationException();
        }

        public boolean refresh() {
            throw new UnsupportedOperationException();
        }

        public List<ArtifactSpecification> getArtifactSpecifications() {
            throw new UnsupportedOperationException();
        }

        public boolean isScoped() {
            throw new UnsupportedOperationException();
        }

        public boolean refresh(String symbolicName) throws DeploymentException {
            throw new UnsupportedOperationException();
        }

        public boolean refreshScope() {
            throw new UnsupportedOperationException();
        }

        public String getProperty(String name) {
            throw new UnsupportedOperationException();
        }

        public Set<String> getPropertyNames() {
            throw new UnsupportedOperationException();
        }

        public String setProperty(String name, String value) {
            throw new UnsupportedOperationException();
        }

        public String getScopeName() {
            return null;
        }

        public Provisioning getProvisioning() {
            throw new UnsupportedOperationException();
        }
    }
}
