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

package org.eclipse.virgo.medic.dump.impl;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;

import org.eclipse.virgo.medic.dump.Dump;
import org.eclipse.virgo.medic.dump.DumpContributor;
import org.eclipse.virgo.medic.dump.DumpGenerationFailedException;
import org.eclipse.virgo.medic.dump.DumpGenerator;
import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.virgo.medic.impl.config.ConfigurationProvider;
import org.junit.Test;

public class StandardDumpGeneratorTests {

    @Test
    public void dumpGeneration() throws DumpGenerationFailedException {

        EventLogger eventLogger = createMock(EventLogger.class);

        DumpContributorResolver accessor = createMock(DumpContributorResolver.class);
        DumpContributor dumpContributor1 = createMock(DumpContributor.class);

        expect(accessor.getDumpContributors()).andReturn(Arrays.asList(dumpContributor1));
        dumpContributor1.contribute(isA(Dump.class));
        expectLastCall();

        dumpContributor1.contribute(isA(Dump.class));
        expect(dumpContributor1.getName()).andReturn("dc1").anyTimes();

        DumpContributor dumpContributor2 = createMock(DumpContributor.class);
        dumpContributor2.contribute(isA(Dump.class));
        expect(dumpContributor2.getName()).andReturn("dc2").anyTimes();

        DumpContributor dumpContributor3 = createMock(DumpContributor.class);
        dumpContributor3.contribute(isA(Dump.class));
        expectLastCall().andThrow(new RuntimeException());
        expect(dumpContributor3.getName()).andReturn("dc3").anyTimes();

        expect(accessor.getDumpContributors()).andReturn(Arrays.asList(dumpContributor1, dumpContributor2, dumpContributor3));

        ConfigurationProvider configurationProvider = createMock(ConfigurationProvider.class);
        Dictionary<String, Object> configuration = new Hashtable<String, Object>();
        configuration.put(ConfigurationProvider.KEY_DUMP_ROOT_DIRECTORY, "build");
        expect(configurationProvider.getConfiguration()).andReturn(configuration).anyTimes();

        replay(accessor, dumpContributor1, dumpContributor2, dumpContributor3, configurationProvider);

        DumpGenerator dumpGenerator = new StandardDumpGenerator(accessor, configurationProvider, eventLogger);
        dumpGenerator.generateDump("foo");
        dumpGenerator.generateDump("bar", new HashMap<String, Object>());

        verify(accessor, dumpContributor1, dumpContributor2, dumpContributor3, configurationProvider);
    }

    @Test
    public void singleExclusion() throws DumpGenerationFailedException {
        EventLogger eventLogger = createMock(EventLogger.class);
        DumpContributorResolver accessor = createMock(DumpContributorResolver.class);
        DumpContributor dumpContributor1 = createMock(DumpContributor.class);

        expect(accessor.getDumpContributors()).andReturn(Arrays.asList(dumpContributor1)).times(2);
        dumpContributor1.contribute(isA(Dump.class));
        expect(dumpContributor1.getName()).andReturn("dc1").times(2);

        ConfigurationProvider configurationProvider = createMock(ConfigurationProvider.class);
        Dictionary<String, Object> configuration = new Hashtable<String, Object>();
        configuration.put("dump.root.directory", "build");
        configuration.put("dump.exclusions.foo", "dc1");
        expect(configurationProvider.getConfiguration()).andReturn(configuration).anyTimes();

        replay(accessor, dumpContributor1, configurationProvider);

        DumpGenerator dumpGenerator = new StandardDumpGenerator(accessor, configurationProvider, eventLogger);
        dumpGenerator.generateDump("foo");
        dumpGenerator.generateDump("bar");

        verify(accessor, dumpContributor1, configurationProvider);
    }

    @Test
    public void multipleExclusion() throws DumpGenerationFailedException {
        EventLogger eventLogger = createMock(EventLogger.class);
        DumpContributorResolver accessor = createMock(DumpContributorResolver.class);
        DumpContributor dumpContributor1 = createMock(DumpContributor.class);
        DumpContributor dumpContributor2 = createMock(DumpContributor.class);

        expect(accessor.getDumpContributors()).andReturn(Arrays.asList(dumpContributor1, dumpContributor2)).times(2);
        dumpContributor1.contribute(isA(Dump.class));
        dumpContributor2.contribute(isA(Dump.class));
        expect(dumpContributor1.getName()).andReturn("dc1").times(2);
        expect(dumpContributor2.getName()).andReturn("dc2").times(2);

        ConfigurationProvider configurationProvider = createMock(ConfigurationProvider.class);
        Dictionary<String, Object> configuration = new Hashtable<String, Object>();
        configuration.put("dump.root.directory", "build");
        configuration.put("dump.exclusions.foo", "dc1, dc2");
        expect(configurationProvider.getConfiguration()).andReturn(configuration).anyTimes();

        replay(accessor, dumpContributor1, dumpContributor2, configurationProvider);

        DumpGenerator dumpGenerator = new StandardDumpGenerator(accessor, configurationProvider, eventLogger);
        dumpGenerator.generateDump("foo");
        dumpGenerator.generateDump("bar");

        verify(accessor, dumpContributor1, dumpContributor2, configurationProvider);
    }

    @Test
    public void close() {
        EventLogger eventLogger = createMock(EventLogger.class);
        DumpContributorResolver accessor = createMock(DumpContributorResolver.class);
        ConfigurationProvider configurationProvider = createMock(ConfigurationProvider.class);
        accessor.close();

        replay(accessor, configurationProvider);

        StandardDumpGenerator dumpGenerator = new StandardDumpGenerator(accessor, configurationProvider, eventLogger);
        dumpGenerator.close();

        verify(accessor, configurationProvider);
    }
}
