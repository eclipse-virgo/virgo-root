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

package org.eclipse.virgo.nano.serviceability.dump.internal;

import java.io.File;
import java.io.IOException;

import org.eclipse.osgi.service.resolver.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.virgo.util.io.PathReference;
import org.eclipse.virgo.util.io.ZipUtils;

/**
 * Utility class that writes the current Equinox resolver {@link State} to a ZIP file.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe.
 * 
 */
final class ResolutionStateDumper {

    private static final String ENTRY_NAME_STATE = "state/";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final SystemStateAccessor systemStateAccessor;
    
    private final StateWriter stateWriter;

    /**
     * Creates a new <code>ResolutionStateDumper</code>.
     * @param systemStateAccessor to access live system {@link State}
     * @param stateWriter to write a {@link State} to permanent storage
     */
    public ResolutionStateDumper(SystemStateAccessor systemStateAccessor, StateWriter stateWriter) {
        this.systemStateAccessor = systemStateAccessor;
        this.stateWriter = stateWriter;
    }

    /**
     * Dump the global resolver {@link State} into a ZIP file at the supplied location.
     * 
     * @param outputFile the location to create the ZIP file at.
     */
    public void dump(File outputFile) {
        dump(outputFile, this.systemStateAccessor.getSystemState());
    }

    /**
     * Dump a resolver {@link State} into a ZIP file at the supplied location.
     * 
     * @param outputFile the location to create the ZIP file at.
     * @param state the state to dump
     */
    public void dump(File outputFile, State state) {        
        File outdir = new File(getTmpDir(), "resolve-" + System.currentTimeMillis());
        if (outdir.mkdirs()) {
            try {
                this.stateWriter.writeState(state, outdir);
            } catch (IOException e) {
                this.logger.error("Unable to write resolver state.", e);
            }
        } else {
            throw new RuntimeException("Unable to create temporary directory '" + outdir.getAbsolutePath() + "'.");
        }

        try {
            zipStateDirectory(outputFile, outdir);
        } catch (IOException e) {
            this.logger.error("Unable to create ZIP of state dump", e);
        } finally {
            if (!new PathReference(outdir).delete(true)) {
                this.logger.warn("Temporary state directory '%s' was not removed after use.", outdir.getAbsolutePath());
            }            
        }
    }

    private void zipStateDirectory(File outputFile, File dumpDir) throws IOException {
        PathReference output = new PathReference(outputFile);
        PathReference toZip = new PathReference(dumpDir);

        ZipUtils.zipTo(toZip, output, ENTRY_NAME_STATE);
    }

    private String getTmpDir() {
        String path = System.getProperty("java.io.tmpdir");
        return path;
    }
}
