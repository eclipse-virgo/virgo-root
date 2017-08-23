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

package org.eclipse.virgo.shell.internal.commands;

import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.List;

import javax.management.JMX;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.eclipse.virgo.nano.deployer.api.ArtifactIdentity;
import org.eclipse.virgo.nano.deployer.api.Deployer;
import org.eclipse.virgo.nano.deployer.api.core.ApplicationDeployer;
import org.eclipse.virgo.shell.Command;


/**
 * A Shell command that allows artifacts to be installed using an {@link ApplicationDeployer}.
 * 
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread-safe.
 * 
 */
@Command("install")
public final class InstallCommand {

    private static final String ARTIFACT_INSTALLATION_FAILED = "Artifact installation failed: %s";

    private static final String ARTIFACT_INSTALLED = "Artifact %s %s %s installed";

    private final MBeanServer server = ManagementFactory.getPlatformMBeanServer();

    private final ObjectName deployerObjectName;

    public InstallCommand() throws MalformedObjectNameException, NullPointerException {
        this.deployerObjectName = new ObjectName("org.eclipse.virgo.kernel:category=Control,type=Deployer");
    }

    @Command("")
    public List<String> install(String artifactLocation) {
        ArtifactIdentity artifactIdentity;
        try {
            artifactIdentity = getDeployer().install(artifactLocation);
        } catch (Exception e) {
            return Arrays.asList(String.format(ARTIFACT_INSTALLATION_FAILED, e.getMessage()));
        }

        return Arrays.asList(String.format(ARTIFACT_INSTALLED, artifactIdentity.getType(), artifactIdentity.getName(), artifactIdentity.getVersion()));
    }

    private Deployer getDeployer() {
        return JMX.newMXBeanProxy(this.server, this.deployerObjectName, Deployer.class);
    }
}
