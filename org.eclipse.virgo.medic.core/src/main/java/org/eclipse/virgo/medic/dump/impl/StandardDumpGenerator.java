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

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.virgo.medic.dump.DumpContributionFailedException;
import org.eclipse.virgo.medic.dump.DumpContributor;
import org.eclipse.virgo.medic.dump.DumpGenerationFailedException;
import org.eclipse.virgo.medic.dump.DumpGenerator;
import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.virgo.medic.impl.MedicLogEvents;
import org.eclipse.virgo.medic.impl.config.ConfigurationProvider;


public final class StandardDumpGenerator implements DumpGenerator {

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd-HH-mm-SSS");

    private static final Map<String, Object> NO_CONTEXT = new HashMap<String, Object>();

    private final DumpContributorResolver dumpContributorsAccessor;

    private final ConfigurationProvider configurationProvider;

    private final EventLogger eventLogger;
    
    private String latestDumpId = null;

    public StandardDumpGenerator(DumpContributorResolver accessor, ConfigurationProvider configurationProvider, EventLogger eventLogger) {
        this.dumpContributorsAccessor = accessor;
        this.configurationProvider = configurationProvider;
        this.eventLogger = eventLogger;
    }

    public void generateDump(String cause, Throwable... throwables) {
        this.generateDump(cause, NO_CONTEXT, throwables);
    }

    public void generateDump(String cause, Map<String, Object> context, Throwable... throwables) {
        StandardDump dump;
        try {
            dump = createDump(cause, context, throwables);
        } catch (DumpGenerationFailedException e) {
            return;
        }

        for (DumpContributor dumpContributor : getDumpContributors(cause)) {
            try {
                dumpContributor.contribute(dump);
            } catch (DumpContributionFailedException e) {
                this.eventLogger.log(MedicLogEvents.CONTRIBUTION_FAILED, e, dumpContributor.getName(), dump.getTimestamp());
            } catch (RuntimeException e) {
                this.eventLogger.log(MedicLogEvents.CONTRIBUTION_ERROR, e, dumpContributor.getName(), dump.getTimestamp());
            }
        }
        
        this.eventLogger.log(MedicLogEvents.DUMP_GENERATED, dump.getDumpDirectory());
    }

    private StandardDump createDump(String cause, Map<String, Object> context, Throwable... throwables) throws DumpGenerationFailedException {        
        File dumpDirectory = null;
        
        long timestamp = System.currentTimeMillis();
        
        while ((dumpDirectory = getDumpDirectory(timestamp)) == null) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
            timestamp = System.currentTimeMillis();
        }        

        return new StandardDump(cause, timestamp, context, throwables, dumpDirectory);
    }

    private List<DumpContributor> getDumpContributors(String cause) {
        Dictionary<?, ?> configuration = this.configurationProvider.getConfiguration();
        String excludedContributorsProperty = (String) configuration.get("dump.exclusions." + cause);
        List<String> excludedContributors = toList(excludedContributorsProperty);
        excludedContributorsProperty = (String) configuration.get("dump.exclusions.*");
        excludedContributors.addAll(toList(excludedContributorsProperty));

        List<DumpContributor> candidateContributors = this.dumpContributorsAccessor.getDumpContributors();

        List<DumpContributor> includedContributors = new ArrayList<DumpContributor>();

        for (DumpContributor candidate : candidateContributors) {
            if (!excludedContributors.contains(candidate.getName())) {
                includedContributors.add(candidate);
            }
        }

        return includedContributors;
    }

    private List<String> toList(String property) {
        List<String> list = new ArrayList<String>();
        if (property != null) {
            String[] components = property.split(",");
            for (String component : components) {
                String trimmed = component.trim();
                list.add(trimmed);
            }
        }
        return list;
    }

    private String getDumpId(long timestamp) {
        Date date = new Date(timestamp);
        synchronized (DATE_FORMAT) {
            String dumpId = DATE_FORMAT.format(date);
            if (dumpId.equals(latestDumpId)) {
                dumpId = null;
            } else {
                latestDumpId = dumpId;
            }            
            return dumpId;
        }
    }

    private File getDumpDirectory(long timestamp) throws DumpGenerationFailedException {
        File dumpDirectory = null;
        
        String dumpId = getDumpId(timestamp);
        
        if (dumpId != null) {
            dumpDirectory = new File(getRootDumpDirectory(), dumpId);
            if (dumpDirectory.exists()) {
                dumpDirectory = null;
            } else {
                createDirectory(dumpDirectory);
            }
        }
        
        return dumpDirectory;                        
    }

    private String getRootDumpDirectory() {
        Dictionary<?, ?> configuration = this.configurationProvider.getConfiguration();
        return (String) configuration.get(ConfigurationProvider.KEY_DUMP_ROOT_DIRECTORY);
    }

    private void createDirectory(File file) throws DumpGenerationFailedException {
        if (!file.mkdirs()) {
            this.eventLogger.log(MedicLogEvents.DIRECTORY_CREATION_FAILED, file.getAbsolutePath());
            throw new DumpGenerationFailedException("Directory creation failed");
        }
    }

    public void close() {
        this.dumpContributorsAccessor.close();
    }
}
