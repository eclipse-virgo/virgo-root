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

package org.eclipse.virgo.kernel.install.artifact.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.virgo.kernel.artifact.fs.ArtifactFS;
import org.eclipse.virgo.nano.core.AbortableSignal;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.util.common.DirectedAcyclicGraph;
import org.eclipse.virgo.util.common.ThreadSafeDirectedAcyclicGraph;
import org.eclipse.virgo.kernel.install.artifact.ArtifactIdentity;
import org.eclipse.virgo.kernel.install.artifact.ArtifactStorage;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact.State;
import org.eclipse.virgo.medic.test.eventlog.MockEventLogger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Version;

/**
 */
public class AbstractInstallArtifactTests {

    private static final String PROPERTY_NAME = "name";

    private static final String PROPERTY_VALUE = "value";

    private static final String PROPERTY_VALUE_2 = "value2";

    private StubInstallArtifact installArtifact;
    
    private StubArtifactStateMonitor artifactStateMonitor;

    @Before
    public void setUp() throws Exception {
        this.artifactStateMonitor = new StubArtifactStateMonitor();
        DirectedAcyclicGraph<InstallArtifact> dag = new ThreadSafeDirectedAcyclicGraph<InstallArtifact>();
        this.installArtifact = new StubInstallArtifact(artifactStateMonitor);
        this.installArtifact.setGraph(dag.createRootNode(installArtifact));
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testGetProperty() {
        assertNull(this.installArtifact.getProperty(PROPERTY_NAME));

        this.installArtifact.setProperty(PROPERTY_NAME, PROPERTY_VALUE);
        assertEquals(PROPERTY_VALUE, this.installArtifact.getProperty(PROPERTY_NAME));
    }

    @Test
    public void testGetPropertyNames() {
        assertEquals(0, this.installArtifact.getPropertyNames().size());

        this.installArtifact.setProperty(PROPERTY_NAME, PROPERTY_VALUE);
        Set<String> expectedNames = new HashSet<String>();
        expectedNames.add(PROPERTY_NAME);
        assertEquals(expectedNames, this.installArtifact.getPropertyNames());
    }

    @Test
    public void testSetProperty() {
        assertNull(this.installArtifact.setProperty(PROPERTY_NAME, PROPERTY_VALUE));
        assertEquals(PROPERTY_VALUE, this.installArtifact.setProperty(PROPERTY_NAME, PROPERTY_VALUE_2));
    }

    @Test
    public void testStart() throws DeploymentException {
        this.artifactStateMonitor.setState(State.RESOLVED);
        this.installArtifact.start(new StubAbortableSignal());

        assertEquals(0, this.installArtifact.doRefreshCount);
        assertEquals(1, this.installArtifact.doStartCount);
        assertEquals(0, this.installArtifact.doStopCount);
        assertEquals(0, this.installArtifact.doUninstallCount);
    }

    @Test
    public void testStartWhenAlreadyStarted() throws DeploymentException {
        this.artifactStateMonitor.setState(State.ACTIVE);
        this.installArtifact.start(new StubAbortableSignal());

        assertEquals(0, this.installArtifact.doRefreshCount);
        assertEquals(0, this.installArtifact.doStartCount);
        assertEquals(0, this.installArtifact.doStopCount);
        assertEquals(0, this.installArtifact.doUninstallCount);
    }

    @Test
    public void testStop() throws DeploymentException {
        this.artifactStateMonitor.setState(State.ACTIVE);
        this.installArtifact.stop();

        assertEquals(0, this.installArtifact.doRefreshCount);
        assertEquals(0, this.installArtifact.doStartCount);
        assertEquals(1, this.installArtifact.doStopCount);
        assertEquals(0, this.installArtifact.doUninstallCount);
    }

    @Test
    public void testStopWhenNotStarted() throws DeploymentException {
        this.artifactStateMonitor.setState(State.INSTALLED);
        this.installArtifact.stop();

        assertEquals(0, this.installArtifact.doRefreshCount);
        assertEquals(0, this.installArtifact.doStartCount);
        assertEquals(0, this.installArtifact.doStopCount);
        assertEquals(0, this.installArtifact.doUninstallCount);
    }

    @Test
    public void testRefresh() throws DeploymentException {
        this.artifactStateMonitor.setState(State.ACTIVE);
        assertTrue(this.installArtifact.refresh());

        assertEquals(1, this.installArtifact.doRefreshCount);
        assertEquals(0, this.installArtifact.doStartCount);
        assertEquals(0, this.installArtifact.doStopCount);
        assertEquals(0, this.installArtifact.doUninstallCount);
    }

    @Test
    public void testUninstall() throws DeploymentException {
        this.artifactStateMonitor.setState(State.ACTIVE);
        this.installArtifact.uninstall();

        assertEquals(0, this.installArtifact.doRefreshCount);
        assertEquals(0, this.installArtifact.doStartCount);
        assertEquals(1, this.installArtifact.doStopCount);
        assertEquals(1, this.installArtifact.doUninstallCount);
    }
    
    private static final class StubInstallArtifact extends AbstractInstallArtifact {

        public StubInstallArtifact(ArtifactStateMonitor artifactStateMonitor) {
            super(new ArtifactIdentity("type", "name", Version.emptyVersion, null), new StubArtifactStorage(), artifactStateMonitor, null, new MockEventLogger());
        }

        private int doStopCount = 0;
        
        private int doStartCount = 0;
        
        private int doUninstallCount = 0;

        private int doRefreshCount = 0;

        /**
         * {@inheritDoc}
         */
        @Override
        protected void doStop() throws DeploymentException {
            this.doStopCount++;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void doUninstall() throws DeploymentException {
            this.doUninstallCount++;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void doStart(AbortableSignal signal) throws DeploymentException {
            this.doStartCount++;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected boolean doRefresh() throws DeploymentException {
            this.doRefreshCount++;
            return true;
        }

    }
    
    
    
    
    
    static class StubArtifactStorage implements ArtifactStorage {

        public void delete() {
        }

        public ArtifactFS getArtifactFS() {
            return null;
        }

        public void synchronize() {
        }

        public void synchronize(URI sourceUri) {
        }

        public void rollBack() {
        }

    }

}
