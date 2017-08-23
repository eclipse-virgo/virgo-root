/*******************************************************************************
 * Copyright (c) 2012 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.kernel.deployer.core.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMock;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.util.common.GraphNode;
import org.eclipse.virgo.util.osgi.manifest.VersionRange;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Version;

public class ExistingNodeLocatorTests {

    private static final String TEST_SCOPE_NAME = "scope";

    private static final Version TEST_VERSION = Version.emptyVersion;

    private static final String TEST_NAME = "name";

    private static final String TEST_TYPE = "type";

    private InstallArtifact installArtifact;

    @Before
    public void setUp() throws Exception {
        this.installArtifact = EasyMock.createMock(InstallArtifact.class);
        EasyMock.expect(this.installArtifact.getType()).andReturn(TEST_TYPE).anyTimes();
        EasyMock.expect(this.installArtifact.getName()).andReturn(TEST_NAME).anyTimes();
        EasyMock.expect(this.installArtifact.getVersion()).andReturn(TEST_VERSION).anyTimes();
        EasyMock.expect(this.installArtifact.getScopeName()).andReturn(TEST_SCOPE_NAME).anyTimes();
        EasyMock.replay(this.installArtifact);
    }

    @After
    public void tearDown() throws Exception {
        EasyMock.verify(this.installArtifact);
        EasyMock.reset(this.installArtifact);
    }

    @Test
    public void testMatch() {
        ExistingNodeLocator visitor = new ExistingNodeLocator(TEST_TYPE, TEST_NAME,
            VersionRange.createExactRange(TEST_VERSION), TEST_SCOPE_NAME);
        assertFound(visitor);
    }

    @Test
    public void testTypeMismatch() {
        ExistingNodeLocator visitor = new ExistingNodeLocator("a", TEST_NAME, VersionRange.createExactRange(TEST_VERSION),
            TEST_SCOPE_NAME);
        assertNotFound(visitor);
    }

    @Test
    public void testNameMismatch() {
        ExistingNodeLocator visitor = new ExistingNodeLocator(TEST_TYPE, "a", VersionRange.createExactRange(TEST_VERSION),
            TEST_SCOPE_NAME);
        assertNotFound(visitor);
    }

    @Test
    public void testVersionMismatch() {
        ExistingNodeLocator visitor = new ExistingNodeLocator(TEST_TYPE, TEST_NAME, new VersionRange("[1,1]"),
            TEST_SCOPE_NAME);
        assertNotFound(visitor);
    }

    @Test
    public void testNonNullScopeNameMismatch() {
        ExistingNodeLocator visitor = new ExistingNodeLocator(TEST_TYPE, TEST_NAME,
            VersionRange.createExactRange(TEST_VERSION), "a");
        assertNotFound(visitor);
    }

    @Test
    public void testNullScopeNameMismatch() {
        ExistingNodeLocator visitor = new ExistingNodeLocator(TEST_TYPE, TEST_NAME,
            VersionRange.createExactRange(TEST_VERSION), null);
        assertNotFound(visitor);
    }
 
    @Test
    public void testNullScopeNameMismatchAgainstNull() {
        this.installArtifact = EasyMock.createMock(InstallArtifact.class);
        EasyMock.expect(this.installArtifact.getType()).andReturn(TEST_TYPE).anyTimes();
        EasyMock.expect(this.installArtifact.getName()).andReturn(TEST_NAME).anyTimes();
        EasyMock.expect(this.installArtifact.getVersion()).andReturn(TEST_VERSION).anyTimes();
        EasyMock.expect(this.installArtifact.getScopeName()).andReturn(null).anyTimes();
        EasyMock.replay(this.installArtifact);

        ExistingNodeLocator visitor = new ExistingNodeLocator(TEST_TYPE, TEST_NAME,
            VersionRange.createExactRange(TEST_VERSION), TEST_SCOPE_NAME);
        assertNotFound(visitor);
    }
    
    @Test
    public void testNullScopeNameMatchAgainstNull() {
        this.installArtifact = EasyMock.createMock(InstallArtifact.class);
        EasyMock.expect(this.installArtifact.getType()).andReturn(TEST_TYPE).anyTimes();
        EasyMock.expect(this.installArtifact.getName()).andReturn(TEST_NAME).anyTimes();
        EasyMock.expect(this.installArtifact.getVersion()).andReturn(TEST_VERSION).anyTimes();
        EasyMock.expect(this.installArtifact.getScopeName()).andReturn(null).anyTimes();
        EasyMock.replay(this.installArtifact);

        ExistingNodeLocator visitor = new ExistingNodeLocator(TEST_TYPE, TEST_NAME,
            VersionRange.createExactRange(TEST_VERSION), null);
        assertFound(visitor);
    }
    
    private void assertFound(ExistingNodeLocator visitor) {
        TestGraphNode node = new TestGraphNode(this.installArtifact);
        visitor.visit(node);
        GraphNode<InstallArtifact> foundNode = visitor.getFoundNode();
        assertEquals(node, foundNode);
    }
 
    private void assertNotFound(ExistingNodeLocator visitor) {
        TestGraphNode node = new TestGraphNode(this.installArtifact);
        visitor.visit(node);
        GraphNode<InstallArtifact> foundNode = visitor.getFoundNode();
        assertNull(foundNode);
    }

    private static class TestGraphNode implements GraphNode<InstallArtifact> {

        private final InstallArtifact installArtifact;

        TestGraphNode(InstallArtifact installArtifact) {
            this.installArtifact = installArtifact;
        }

        @Override
        public InstallArtifact getValue() {
            return this.installArtifact;
        }

        @Override
        public List<GraphNode<InstallArtifact>> getChildren() {
            return new ArrayList<GraphNode<InstallArtifact>>();
        }

        @Override
        public void addChild(GraphNode<InstallArtifact> child) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean removeChild(GraphNode<InstallArtifact> child) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<GraphNode<InstallArtifact>> getParents() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void visit(org.eclipse.virgo.util.common.GraphNode.DirectedAcyclicGraphVisitor<InstallArtifact> visitor) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <E extends Exception> void visit(
            org.eclipse.virgo.util.common.GraphNode.ExceptionThrowingDirectedAcyclicGraphVisitor<InstallArtifact, E> visitor) throws E {
            throw new UnsupportedOperationException();
        }

        @Override
        public int size() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isRootNode() {
            throw new UnsupportedOperationException();
        }

    }

}
