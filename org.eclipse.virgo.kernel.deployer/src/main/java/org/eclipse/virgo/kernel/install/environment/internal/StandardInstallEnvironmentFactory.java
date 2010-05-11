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

package org.eclipse.virgo.kernel.install.environment.internal;

import org.eclipse.virgo.kernel.osgi.quasi.QuasiFrameworkFactory;

import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.install.environment.InstallEnvironment;
import org.eclipse.virgo.kernel.install.environment.InstallEnvironmentFactory;
import org.eclipse.virgo.medic.eventlog.EventLogger;

/**
 * {@link StandardInstallEnvironmentFactory} is the default implementation of {@link InstallEnvironmentFactory}.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is thread safe.
 * 
 */
final class StandardInstallEnvironmentFactory implements InstallEnvironmentFactory {

    private final QuasiFrameworkFactory quasiFrameworkFactory;

    private final EventLogger eventLogger;

    StandardInstallEnvironmentFactory(QuasiFrameworkFactory quasiFrameworkFactory, EventLogger eventLogger) {
        this.quasiFrameworkFactory = quasiFrameworkFactory;
        this.eventLogger = eventLogger;
    }

    /**
     * {@inheritDoc}
     */
    public InstallEnvironment createInstallEnvironment(InstallArtifact installArtifact) {
        return new StandardInstallEnvironment(null, new StandardInstallLog(this.eventLogger, installArtifact), this.quasiFrameworkFactory.create());
    }

}
