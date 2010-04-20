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

package org.eclipse.virgo.medic.test.eventlog;

import java.io.PrintStream;

import org.eclipse.virgo.medic.eventlog.Level;


public class LoggedEvent {

    private final String code;

    private final Level level;

    private final Throwable throwable;

    private final Object[] inserts;

    LoggedEvent(String code, Level level, Object... inserts) {
        this(code, level, null, inserts);
    }

    LoggedEvent(String code, Level level, Throwable throwable, Object... inserts) {
        this.code = code;
        this.level = level;
        this.throwable = throwable;
        this.inserts = inserts.clone();
    }

    /**
     * Returns the code of the event that was logged.
     * 
     * @return the logged event's code.
     */
    public String getCode() {
        return this.code;
    }

    /**
     * Returns the level of the event that was logged.
     * 
     * @return the logged event's level
     */
    public Level getLevel() {
        return this.level;
    }

    /**
     * Returns the <code>Throwable</code> that was logged as part of the event, or <code>null</code> if no
     * <code>Throwable</code> was logged
     * 
     * @return The logged <code>Throwable</code>
     */
    public Throwable getThrowable() {
        return throwable;
    }

    /**
     * Returns the inserts that were logged, or an empty array if no inserts were logged.
     * 
     * @return The logged inserts, never <code>null</code>.
     */
    public Object[] getInserts() {
        return inserts.clone();
    }
    
    /**
     * Print (to outStr) a human-readable form of the LoggedEvent, without fancy formatting.
     * 
     * @param printStream where to print it
     */
    public void print(PrintStream printStream) {
        printStream.print(String.format("Code: '%s' Level: '%s'", this.code, String.valueOf(this.level)));
        printInserts(printStream);
        if (this.throwable!=null) {
            printStream.print("  Exception: ");
            this.throwable.printStackTrace(printStream);
        }
    }
    
    /**
     * Print inserts.
     * 
     * @param outStr where to print them
     */
    private void printInserts(PrintStream outStr) {
        if (inserts.length != 0) {
            outStr.print(String.format(" Inserts: {'%s'", String.valueOf(this.inserts[0])));
            for (int ins = 1; ins < this.inserts.length; ++ins) {
                outStr.print(String.format(", '%s'", String.valueOf(this.inserts[ins])));
            }
            outStr.print("}");
        }
    }
}
