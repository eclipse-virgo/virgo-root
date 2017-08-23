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

import static org.junit.Assert.assertEquals;

import java.lang.management.ManagementFactory;
import java.util.List;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.eclipse.virgo.nano.deployer.api.ArtifactIdentity;
import org.eclipse.virgo.nano.deployer.api.Deployer;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentIdentity;
import org.eclipse.virgo.shell.internal.commands.InstallCommand;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class InstallCommandTests {

    public final InstallCommand command;

    private final ObjectName deployerObjectName;

    public InstallCommandTests() throws MalformedObjectNameException, NullPointerException {
        this.command = new InstallCommand();
        this.deployerObjectName = new ObjectName("org.eclipse.virgo.kernel:category=Control,type=Deployer");
    }

    @Before
    public void export() throws InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException {
        ManagementFactory.getPlatformMBeanServer().registerMBean(new StubDeployer(), this.deployerObjectName);
    }

    @After
    public void unexport() throws MBeanRegistrationException, InstanceNotFoundException {
        ManagementFactory.getPlatformMBeanServer().unregisterMBean(this.deployerObjectName);
    }

    @Test
    public void install() {
        List<String> lines = this.command.install("test1");
        assertEquals("Artifact testType testName testVersion installed", lines.get(0));
    }

    @Test
    public void installException() {
        List<String> lines = this.command.install("test2");
        assertEquals("Artifact installation failed: test", lines.get(0));
    }

    private static class StubDeployer implements Deployer {

        public DeploymentIdentity deploy(String uri) {
            throw new UnsupportedOperationException();
        }

        public DeploymentIdentity deploy(String uri, boolean recoverable) throws DeploymentException {
            throw new UnsupportedOperationException();
        }

        public ArtifactIdentity install(String artifactUri) throws DeploymentException {
            if ("test1".equals(artifactUri)) {
                return new ArtifactIdentity("testType", "testName", "testVersion");
            } else if ("test2".equals(artifactUri)) {
                throw new IllegalArgumentException("test");
            }
            return null;
        }

        public ArtifactIdentity install(String artifactUri, boolean recover) throws DeploymentException {
            throw new UnsupportedOperationException();
        }

        public ArtifactIdentity install(String type, String name, String version) throws DeploymentException {
            throw new UnsupportedOperationException();
        }

        public ArtifactIdentity install(String type, String name, String version, boolean recover) throws DeploymentException {
            throw new UnsupportedOperationException();
        }

        public void refresh(String uri, String symbolicName) throws DeploymentException {
            throw new UnsupportedOperationException();
        }

        public void refreshBundle(String bundleSymbolicName, String bundleVersion) throws DeploymentException {
            throw new UnsupportedOperationException();
        }

        public void start(ArtifactIdentity artifactIdentity) throws DeploymentException, IllegalStateException {
            throw new UnsupportedOperationException();
        }

        public void start(String type, String name, String version) throws DeploymentException, IllegalStateException {
            throw new UnsupportedOperationException();
        }

        public void stop(ArtifactIdentity artifactIdentity) throws DeploymentException, IllegalStateException {
            throw new UnsupportedOperationException();
        }

        public void stop(String type, String name, String version) throws DeploymentException, IllegalStateException {
            throw new UnsupportedOperationException();
        }

        public void undeploy(String applicationSymbolicName, String version) throws DeploymentException {
            throw new UnsupportedOperationException();
        }

        public void uninstall(ArtifactIdentity artifactIdentity) throws DeploymentException, IllegalStateException {
            throw new UnsupportedOperationException();
        }

        public void uninstall(String type, String name, String version) throws DeploymentException, IllegalStateException {
            throw new UnsupportedOperationException();
        }

    }
}
