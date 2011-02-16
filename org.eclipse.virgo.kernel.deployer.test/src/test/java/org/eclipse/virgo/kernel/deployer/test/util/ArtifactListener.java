/*******************************************************************************
 * Copyright (c) 2008, 2011 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.kernel.deployer.test.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifactLifecycleListenerSupport;

public class ArtifactListener extends InstallArtifactLifecycleListenerSupport {

    private final Object monitor = new Object();

    List<ArtifactLifecycleEvent> eventList = new ArrayList<ArtifactLifecycleEvent>();

    public void clear() {
        synchronized (this.monitor) {
            this.eventList.clear();
        }
    }

    public boolean waitForEvents(final Set<ArtifactLifecycleEvent> expectedEventSet, long timeout) {
        boolean allReceived = eventsReceived(expectedEventSet);
        while (!allReceived && timeout > 0) {
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
            this.eventList.add(new ArtifactLifecycleEvent(event, installArtifact.getType(), installArtifact.getName(), installArtifact.getVersion()));
        }
    }
}