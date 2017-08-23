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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.management.ManagementFactory;
import java.util.List;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.eclipse.virgo.shell.internal.formatting.StubManageableCompositeArtifact;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class AbztractCompositeInstallArtifactBasedCommandsTests {

    private final StubAbstractCompositeInstallArtifactBasedCommands commands = new StubAbstractCompositeInstallArtifactBasedCommands();

    private final StubManageableCompositeArtifact artifact = new StubManageableCompositeArtifact();

    private volatile ObjectName name;
    {
        try {
            this.name = new ObjectName("test:type=ArtifactModel,artifact-type=test,name=test1,version=0.0.0,region=region1");
        } catch (MalformedObjectNameException e) {
        } catch (NullPointerException e) {
        }
    }

    @Before
    public void installTestBean() throws InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException {
        ManagementFactory.getPlatformMBeanServer().registerMBean(this.artifact, this.name);
    }

    @After
    public void uninstallTestBean() throws MBeanRegistrationException, InstanceNotFoundException {
        ManagementFactory.getPlatformMBeanServer().unregisterMBean(this.name);
    }

    @Test
    public void list() {
        List<String> lines = this.commands.list();
        assertFalse(lines.isEmpty());
    }

    @Test
    public void examine() {
        List<String> lines = this.commands.examine("test1", "0.0.0", "region1");
        assertFalse(lines.isEmpty());
    }

    @Test
    public void start() {
        this.commands.start("test1", "0.0.0", "region1");
        assertTrue(this.artifact.getStartCalled());
    }

    @Test
    public void stop() {
        this.commands.stop("test1", "0.0.0", "region1");
        assertTrue(this.artifact.getStopCalled());
    }

    @Test
    public void refreshWork() {
        this.artifact.setShouldRefreshSucceed(true);
        List<String> lines = this.commands.refresh("test1", "0.0.0", "region1");
        assertTrue(this.artifact.getRefreshCalled());
        assertTrue(lines.get(0).contains("refreshed successfully"));
    }

    @Test
    public void refreshFail() {
        this.artifact.setShouldRefreshSucceed(false);
        List<String> lines = this.commands.refresh("test1", "0.0.0", "region1");
        assertTrue(this.artifact.getRefreshCalled());
        assertTrue(lines.get(0).contains("not refreshed"));
    }

    @Test
    public void uninstall() {
        this.commands.uninstall("test1", "0.0.0", "region1");
        assertTrue(this.artifact.getUninstallCalled());
    }
}
