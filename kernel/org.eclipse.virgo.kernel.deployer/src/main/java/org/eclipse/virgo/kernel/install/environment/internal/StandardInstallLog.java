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

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.install.environment.InstallLog;
import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.virgo.medic.eventlog.Level;
import org.eclipse.virgo.medic.eventlog.LogEvent;

/**
 * {@link StandardInstallLog} is the default implementation of {@link InstallLog}.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is thread safe.
 * 
 */
public final class StandardInstallLog implements InstallLog {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final EventLogger eventLogger;

    private final InstallArtifact installArtifact;

    public StandardInstallLog(EventLogger eventLogger, InstallArtifact installArtifact) {
        this.eventLogger = eventLogger;
        this.installArtifact = installArtifact;
    }

    /**
     * {@inheritDoc}
     */
    public void log(Object source, String message, String... arguments) {
        doLog(source, message, arguments);
    }

    /**
     * {@inheritDoc}
     */
    public void log(LogEvent event, Object... inserts) {
        this.eventLogger.log(event, inserts);
        doLog(event, "event log message issued", stringify(inserts));
    }

    public void log(String code, Level level, Object... inserts) {
        throw new UnsupportedOperationException();
    }

    public void log(String code, Level level, Throwable throwable, Object... inserts) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    public void log(LogEvent event, Throwable throwable, Object... inserts) {
        this.eventLogger.log(event, throwable, inserts);
        String[] stringInserts = new String[inserts.length + 1];
        stringInserts[0] = throwable.getMessage();
        System.arraycopy(stringify(inserts), 0, stringInserts, 1, inserts.length);

        doLog(event, "event log message issued", stringInserts);
    }

    private void doLog(Object source, String message, String... arguments) {
        String[] stringInserts = new String[arguments.length + 1];
        stringInserts[arguments.length] = source.toString();
        System.arraycopy(arguments, 0, stringInserts, 0, arguments.length);
        logger.debug(message + " (source '{}')", Arrays.toString(stringInserts));
    }

    private String[] stringify(Object[] inserts) {
        String[] strings = new String[inserts.length];
        for (int i = 0; i < inserts.length; i++) {
            if (inserts[i] != null) {
                strings[i] = inserts[i].toString();
            } else {
                strings[i] = null;
            }
        }
        return strings;
    }

    /** 
     * {@inheritDoc}
     */
    public void logFailure(LogEvent logEvent, Throwable cause, Object... insert) {
        this.eventLogger.log(logEvent, cause, this.installArtifact.getType(), this.installArtifact.getName(), this.installArtifact.getVersion(), insert);
    }

}
