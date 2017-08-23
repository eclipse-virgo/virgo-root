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

package org.eclipse.virgo.medic.dump.impl.thread;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.virgo.medic.dump.Dump;
import org.eclipse.virgo.medic.dump.DumpContributionFailedException;
import org.eclipse.virgo.medic.dump.DumpContributor;
import org.eclipse.virgo.medic.dump.impl.StubDump;
import org.eclipse.virgo.medic.dump.impl.thread.Java5ThreadInfoWriter;
import org.eclipse.virgo.medic.dump.impl.thread.Java5ThreadMXBeanDelegate;
import org.eclipse.virgo.medic.dump.impl.thread.StandardThreadInfoWriter;
import org.eclipse.virgo.medic.dump.impl.thread.StandardThreadMXBeanDelegate;
import org.eclipse.virgo.medic.dump.impl.thread.ThreadDumpContributor;
import org.junit.Test;


public class ThreadDumpContributorTests {

    private final File dumpDirectory = new File("build");

    @Test
    public void java5ThreadDump() throws DumpContributionFailedException {
        DumpContributor contributor = new ThreadDumpContributor(new Java5ThreadMXBeanDelegate(), new Java5ThreadInfoWriter());
        createContribution(contributor);
    }

    @Test
    public void standardThreadDump() throws DumpContributionFailedException {
        DumpContributor contributor = new ThreadDumpContributor(new StandardThreadMXBeanDelegate(), new StandardThreadInfoWriter());
        createContribution(contributor);
    }

    private void createContribution(DumpContributor contributor) throws DumpContributionFailedException {
        String cause = "failure";
        long timestamp = System.currentTimeMillis();
        Map<String, Object> context = new HashMap<String, Object>();

        Dump dump = new StubDump(cause, timestamp, context, new Throwable[0], this.dumpDirectory);

        contributor.contribute(dump);
    }
}
