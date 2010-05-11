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

package org.eclipse.virgo.kernel.deployer.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;

import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifactLifecycleListener;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifactLifecycleListenerSupport;

import org.eclipse.virgo.kernel.deployer.core.DeploymentException;
import org.eclipse.virgo.kernel.deployer.core.DeploymentIdentity;
import org.eclipse.virgo.util.math.Sets;

/**
 * Test refresh of artifacts in a Par
 *
 */
public class ParRefreshTests extends AbstractDeployerIntegrationTest {
    
    private static final File PAR_FILE = new File("src/test/resources/ParRefreshPar.par");
    private static final String PAR_SYMBOLIC_NAME = "RefreshPar";
    private static final Version PAR_VERSION = new Version(1,0,0);
    
    private static final String BUNDLE_A_SYMBOLIC_NAME = PAR_SYMBOLIC_NAME + "-1-bundleA";
    private static final String BUNDLE_B_SYMBOLIC_NAME = PAR_SYMBOLIC_NAME + "-1-bundleB";
    private static final Version BUNDLE_VERSION = new Version(1,0,0);

    private static final String SYNTHETIC_CONTEXT_BUNDLE_SYMBOLIC_NAME = PAR_SYMBOLIC_NAME + "-1-synthetic.context";

    private ArtifactListener artifactListener = new ArtifactListener();
    
    @Before
    public void parRefreshSetup() throws Exception {
        this.context.registerService(InstallArtifactLifecycleListener.class.getName(), artifactListener, null);
    }
    
    @Test
    public void testRefreshOfBundleInPar() throws DeploymentException {
        
        DeploymentIdentity parIdentity = this.deployer.deploy(PAR_FILE.toURI());

        assertBundlePresent(BUNDLE_A_SYMBOLIC_NAME, BUNDLE_VERSION);

        this.artifactListener.clear();

        Set<ArtifactLifecycleEvent> expectedEventSet = new HashSet<ArtifactLifecycleEvent>();
        //events expected due to explicit refresh;
        expectedEventSet.add(new ArtifactLifecycleEvent(TestLifecycleEvent.STOPPING, "bundle", BUNDLE_A_SYMBOLIC_NAME, BUNDLE_VERSION));
        expectedEventSet.add(new ArtifactLifecycleEvent(TestLifecycleEvent.STOPPED, "bundle", BUNDLE_A_SYMBOLIC_NAME, BUNDLE_VERSION));
        expectedEventSet.add(new ArtifactLifecycleEvent(TestLifecycleEvent.UNRESOLVED, "bundle", BUNDLE_A_SYMBOLIC_NAME, BUNDLE_VERSION));
        expectedEventSet.add(new ArtifactLifecycleEvent(TestLifecycleEvent.RESOLVED, "bundle", BUNDLE_A_SYMBOLIC_NAME, BUNDLE_VERSION));
        expectedEventSet.add(new ArtifactLifecycleEvent(TestLifecycleEvent.STARTING, "bundle", BUNDLE_A_SYMBOLIC_NAME, BUNDLE_VERSION));
        expectedEventSet.add(new ArtifactLifecycleEvent(TestLifecycleEvent.STARTED, "bundle", BUNDLE_A_SYMBOLIC_NAME, BUNDLE_VERSION));
        
        //events caused by PackageAdmin propagation
        expectedEventSet.add(new ArtifactLifecycleEvent(TestLifecycleEvent.STOPPING, "bundle", SYNTHETIC_CONTEXT_BUNDLE_SYMBOLIC_NAME, BUNDLE_VERSION));
        expectedEventSet.add(new ArtifactLifecycleEvent(TestLifecycleEvent.STOPPED, "bundle", SYNTHETIC_CONTEXT_BUNDLE_SYMBOLIC_NAME, BUNDLE_VERSION));
        expectedEventSet.add(new ArtifactLifecycleEvent(TestLifecycleEvent.UNRESOLVED, "bundle", SYNTHETIC_CONTEXT_BUNDLE_SYMBOLIC_NAME, BUNDLE_VERSION));
        expectedEventSet.add(new ArtifactLifecycleEvent(TestLifecycleEvent.RESOLVED, "bundle", SYNTHETIC_CONTEXT_BUNDLE_SYMBOLIC_NAME, BUNDLE_VERSION));
        expectedEventSet.add(new ArtifactLifecycleEvent(TestLifecycleEvent.STARTING, "bundle", SYNTHETIC_CONTEXT_BUNDLE_SYMBOLIC_NAME, BUNDLE_VERSION));
        expectedEventSet.add(new ArtifactLifecycleEvent(TestLifecycleEvent.STARTED, "bundle", SYNTHETIC_CONTEXT_BUNDLE_SYMBOLIC_NAME, BUNDLE_VERSION));
        // bundleB depends on (imports) bundleA
        expectedEventSet.add(new ArtifactLifecycleEvent(TestLifecycleEvent.STOPPING, "bundle", BUNDLE_B_SYMBOLIC_NAME, BUNDLE_VERSION));
        expectedEventSet.add(new ArtifactLifecycleEvent(TestLifecycleEvent.STOPPED, "bundle", BUNDLE_B_SYMBOLIC_NAME, BUNDLE_VERSION));
        expectedEventSet.add(new ArtifactLifecycleEvent(TestLifecycleEvent.UNRESOLVED, "bundle", BUNDLE_B_SYMBOLIC_NAME, BUNDLE_VERSION));
        expectedEventSet.add(new ArtifactLifecycleEvent(TestLifecycleEvent.RESOLVED, "bundle", BUNDLE_B_SYMBOLIC_NAME, BUNDLE_VERSION));
        expectedEventSet.add(new ArtifactLifecycleEvent(TestLifecycleEvent.STARTING, "bundle", BUNDLE_B_SYMBOLIC_NAME, BUNDLE_VERSION));
        expectedEventSet.add(new ArtifactLifecycleEvent(TestLifecycleEvent.STARTED, "bundle", BUNDLE_B_SYMBOLIC_NAME, BUNDLE_VERSION));
        
        // Refresh the bundle, get back 'new' par identity
        DeploymentIdentity newIdentity = this.deployer.refresh(PAR_FILE.toURI(), BUNDLE_A_SYMBOLIC_NAME);

        waitForAndCheckEventsReceived(expectedEventSet, 10000L); // ten seconds backstop
        
        Set<ArtifactLifecycleEvent> actualEventSet = new HashSet<ArtifactLifecycleEvent>(artifactListener.extract());
        
        Set<ArtifactLifecycleEvent> extraEvents = Sets.difference(actualEventSet, expectedEventSet);
        Set<ArtifactLifecycleEvent> missingEvents = Sets.difference(expectedEventSet, actualEventSet);
        
        assertTrue("Extra events were received: " + extraEvents, extraEvents.isEmpty());
        assertTrue("Events were missing: " + missingEvents, missingEvents.isEmpty());
        
        assertBundlePresent(BUNDLE_A_SYMBOLIC_NAME, BUNDLE_VERSION);

        assertDeploymentIdentityEquals(newIdentity, PAR_SYMBOLIC_NAME, "par", PAR_SYMBOLIC_NAME, PAR_VERSION.toString());
        
        this.deployer.undeploy(parIdentity);

        assertBundleNotPresent(BUNDLE_A_SYMBOLIC_NAME, BUNDLE_VERSION);
    }
    
    @Test
    public void testRefreshOfDependentBundleInPar() throws DeploymentException {
        
        DeploymentIdentity parIdentity = this.deployer.deploy(PAR_FILE.toURI());

        assertBundlePresent(BUNDLE_A_SYMBOLIC_NAME, BUNDLE_VERSION);
        assertBundlePresent(BUNDLE_B_SYMBOLIC_NAME, BUNDLE_VERSION);

        this.artifactListener.clear();

        Set<ArtifactLifecycleEvent> expectedEventSet = new HashSet<ArtifactLifecycleEvent>();
        //events expected due to explicit refresh;
        expectedEventSet.add(new ArtifactLifecycleEvent(TestLifecycleEvent.STOPPING, "bundle", BUNDLE_B_SYMBOLIC_NAME, BUNDLE_VERSION));
        expectedEventSet.add(new ArtifactLifecycleEvent(TestLifecycleEvent.STOPPED, "bundle", BUNDLE_B_SYMBOLIC_NAME, BUNDLE_VERSION));
        expectedEventSet.add(new ArtifactLifecycleEvent(TestLifecycleEvent.UNRESOLVED, "bundle", BUNDLE_B_SYMBOLIC_NAME, BUNDLE_VERSION));
        expectedEventSet.add(new ArtifactLifecycleEvent(TestLifecycleEvent.RESOLVED, "bundle", BUNDLE_B_SYMBOLIC_NAME, BUNDLE_VERSION));
        expectedEventSet.add(new ArtifactLifecycleEvent(TestLifecycleEvent.STARTING, "bundle", BUNDLE_B_SYMBOLIC_NAME, BUNDLE_VERSION));
        expectedEventSet.add(new ArtifactLifecycleEvent(TestLifecycleEvent.STARTED, "bundle", BUNDLE_B_SYMBOLIC_NAME, BUNDLE_VERSION));
        
        // Refresh the configuration, get back 'new' par identity
        DeploymentIdentity newIdentity = this.deployer.refresh(PAR_FILE.toURI(), BUNDLE_B_SYMBOLIC_NAME);

        waitForAndCheckEventsReceived(expectedEventSet, 10000L); // ten seconds backstop
        
        assertBundlePresent(BUNDLE_A_SYMBOLIC_NAME, BUNDLE_VERSION);
        assertBundlePresent(BUNDLE_B_SYMBOLIC_NAME, BUNDLE_VERSION);

        assertDeploymentIdentityEquals(newIdentity, PAR_SYMBOLIC_NAME, "par", PAR_SYMBOLIC_NAME, PAR_VERSION.toString());

        this.deployer.undeploy(parIdentity);

        assertBundleNotPresent(BUNDLE_A_SYMBOLIC_NAME, BUNDLE_VERSION);
        assertBundleNotPresent(BUNDLE_B_SYMBOLIC_NAME, BUNDLE_VERSION);
    }

    private void waitForAndCheckEventsReceived(Set<ArtifactLifecycleEvent> expectedEventSet, long timeout) {
        artifactListener.waitForEvents(expectedEventSet, timeout);
        
        Set<ArtifactLifecycleEvent> actualEventSet = new HashSet<ArtifactLifecycleEvent>(this.artifactListener.extract());
        
        Set<ArtifactLifecycleEvent> extraEvents = Sets.difference(actualEventSet, expectedEventSet);
        Set<ArtifactLifecycleEvent> missingEvents = Sets.difference(expectedEventSet, actualEventSet);
        
        assertTrue("Extra events were received: " + extraEvents, extraEvents.isEmpty());
        assertTrue("Events were missing: " + missingEvents, missingEvents.isEmpty());
    }
    
    private void assertBundlePresent(String symbolicName, Version version) {
        Bundle[] bundles = this.context.getBundles();
        
        for (Bundle bundle : bundles) {
            if (symbolicName.equals(bundle.getSymbolicName()) && version.equals(bundle.getVersion())) {
                return;
            }
        }
        
        fail("The bundle " + symbolicName + " " + version + " was not found.");
    }
    
    private void assertBundleNotPresent(String symbolicName, Version version) {
        Bundle[] bundles = this.context.getBundles();
        
        for (Bundle bundle : bundles) {
            if (symbolicName.equals(bundle.getSymbolicName()) && version.equals(bundle.getVersion())) {
                fail("Bundle " + bundle + " should not be present");
            }
        }
    }  
    
    private static enum TestLifecycleEvent {
        INSTALLED, INSTALLING, RESOLVED, RESOLVING, STARTED, STARTING, STOPPED, STOPPING, UNINSTALLED, UNINSTALLING, UNRESOLVED
    }

    private static class ArtifactLifecycleEvent {
        public ArtifactLifecycleEvent(TestLifecycleEvent lifecycleEvent, String type, String name, Version version) {
            this.lifeCycleEvent = lifecycleEvent;
            this.type = type;
            this.name = name;
            this.version = version;
        }
        public boolean equals(Object obj) {
            if (obj instanceof ArtifactLifecycleEvent) {
                ArtifactLifecycleEvent other = (ArtifactLifecycleEvent) obj;
                return (this.lifeCycleEvent.equals(other.lifeCycleEvent)
                    && this.type.equals(other.type)
                    && this.name.equals(other.name)
                    && this.version.equals(other.version));
            }
            return false;
        }
        public int hashCode() {
            final int prime = 17;
            int result = this.lifeCycleEvent.hashCode() + prime
                * (this.name.hashCode() + prime * (this.type.hashCode() + prime * (this.version.hashCode())));
            return result;
        }
        public String toString() {
            StringBuilder sb = new StringBuilder("[");
            sb.append(this.lifeCycleEvent).append(", ");
            sb.append(this.type).append(", ");
            sb.append(this.name).append(", ");
            sb.append(this.version).append("]");
            return sb.toString();
        }
        
        private final TestLifecycleEvent lifeCycleEvent;
        private final String type;
        private final String name;
        private final Version version;
    }
    
    private static class ArtifactListener extends InstallArtifactLifecycleListenerSupport {

        private final Object monitor = new Object();
        
        List<ArtifactLifecycleEvent> eventList = new ArrayList<ArtifactLifecycleEvent>();
        
        public void clear() {
            synchronized (this.monitor) {
                this.eventList.clear();
            }
        }        
        
        public boolean waitForEvents(final Set<ArtifactLifecycleEvent> expectedEventSet, long timeout) {
            boolean allReceived = eventsReceived(expectedEventSet);
            while (!allReceived && timeout>0) {
                timeout -= 50L;
                try {
                    Thread.sleep(50L);
                } catch (InterruptedException _) {
                    // do nothing
                }
                allReceived = eventsReceived(expectedEventSet);
            }
            return allReceived;
        }

        private boolean eventsReceived(Set<ArtifactLifecycleEvent> eventSet) {
            synchronized (this.monitor) {
                for (ArtifactLifecycleEvent event : eventSet) {
                    if (!this.eventList.contains(event)) {
                        return false;
                    }
                }
                return true;
            }
        }
        
        public List<ArtifactLifecycleEvent> extract() {
            synchronized (this.monitor) {
                return new ArrayList<ArtifactLifecycleEvent>(this.eventList);
            }
        }
        
        /** 
         * {@inheritDoc}
         */
        @Override
        public void onInstalled(InstallArtifact installArtifact) {
            addEvent(TestLifecycleEvent.INSTALLED, installArtifact);
        }

        /** 
         * {@inheritDoc}
         */
        @Override
        public void onInstalling(InstallArtifact installArtifact) {
            addEvent(TestLifecycleEvent.INSTALLING, installArtifact);
        }

        /** 
         * {@inheritDoc}
         */
        @Override
        public void onResolved(InstallArtifact installArtifact) {
            addEvent(TestLifecycleEvent.RESOLVED, installArtifact);
        }

        /** 
         * {@inheritDoc}
         */
        @Override
        public void onResolving(InstallArtifact installArtifact) {
            addEvent(TestLifecycleEvent.RESOLVING, installArtifact);
        }

        /** 
         * {@inheritDoc}
         */
        @Override
        public void onStarted(InstallArtifact installArtifact) {
            addEvent(TestLifecycleEvent.STARTED, installArtifact);
        }

        /** 
         * {@inheritDoc}
         */
        @Override
        public void onStarting(InstallArtifact installArtifact) {
            addEvent(TestLifecycleEvent.STARTING, installArtifact);
        }

        /** 
         * {@inheritDoc}
         */
        @Override
        public void onStopped(InstallArtifact installArtifact) {
            addEvent(TestLifecycleEvent.STOPPED, installArtifact);
        }

        /** 
         * {@inheritDoc}
         */
        @Override
        public void onStopping(InstallArtifact installArtifact) {
            addEvent(TestLifecycleEvent.STOPPING, installArtifact);
        }

        /** 
         * {@inheritDoc}
         */
        @Override
        public void onUninstalled(InstallArtifact installArtifact) {
            addEvent(TestLifecycleEvent.UNINSTALLED, installArtifact);
        }

        /** 
         * {@inheritDoc}
         */
        @Override
        public void onUninstalling(InstallArtifact installArtifact) {
            addEvent(TestLifecycleEvent.UNINSTALLING, installArtifact);
        }

        /** 
         * {@inheritDoc}
         */
        @Override
        public void onUnresolved(InstallArtifact installArtifact) {
            addEvent(TestLifecycleEvent.UNRESOLVED, installArtifact);
        }

        private void addEvent(TestLifecycleEvent event, InstallArtifact installArtifact) {
            synchronized (this.monitor) {
                this.eventList.add(new ArtifactLifecycleEvent(event, installArtifact.getType(), installArtifact.getName(),
                    installArtifact.getVersion()));
            }
        }
    }
}
