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

package org.eclipse.virgo.shell.internal.completers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.management.ManagementFactory;
import java.util.Set;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.eclipse.virgo.shell.internal.commands.StubRuntimeArtifactModelObjectNameCreator;
import org.eclipse.virgo.shell.internal.completers.AbstractInstallArtifactCompleter;
import org.eclipse.virgo.shell.internal.formatting.StubManageableCompositeArtifact;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class AbztractInstallArtifactCompleterTests {

    private final StubInstallArtifactCompleter completer = new StubInstallArtifactCompleter();

    private final StubManageableCompositeArtifact artifact = new StubManageableCompositeArtifact();

    private volatile ObjectName name;
    {
        try {
            this.name = new ObjectName("test:type=ArtifactModel,artifact-type=test,name=test1,version=0.0.0");
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
        assertEquals(0, this.completer.getCompletionCandidates("list").size());
    }

    @Test
    public void name() {
        assertEquals(1, this.completer.getCompletionCandidates("examine", "tes").size());
    }

    @Test
    public void version() {
        assertEquals(1, this.completer.getCompletionCandidates("examine", "test1", "").size());
    }

    @Test
    public void tooManyArgs() {
        assertEquals(0, this.completer.getCompletionCandidates("testCommand", "", "", "").size());
    }

    @Test
    public void filter() {
        assertTrue(this.completer.getCompletionCandidates("testFilter", "tes").contains("filtered"));
    }

    private static class StubInstallArtifactCompleter extends AbstractInstallArtifactCompleter {

        public StubInstallArtifactCompleter() {
            super("test", new StubRuntimeArtifactModelObjectNameCreator());
        }

        @Override
        protected void filter(Set<String> candidates, String subcommand, String... tokens) {
            if ("testFilter".equals(subcommand)) {
                candidates.add("filtered");
            }
        }

    }
}
