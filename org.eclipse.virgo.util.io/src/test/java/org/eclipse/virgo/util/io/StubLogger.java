/*******************************************************************************
 * This file is part of the Virgo Web Server.
 *
 * Copyright (c) 2010 Eclipse Foundation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SpringSource, a division of VMware - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.virgo.util.io;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.Marker;

/**
 * A version of {@link Logger} suitable for testing.
 * <p/>
 * Debug only; markers ignored; no copies made; getEntries() implemented.
 * 
 * @author Steve Powell
 */
public class StubLogger implements Logger {

    public static class StubLogEntry {

        private final String string;

        private final List<Object> arg1;

        private final Throwable throwable;

        StubLogEntry(String string, Object[] arg1, Throwable throwable) {
            this.string = string;
            List<Object> list = new ArrayList<Object>();
            if (arg1 != null)
                for (Object o : arg1) {
                    list.add(o);
                }
            this.arg1 = list;
            this.throwable = throwable;
        }

        public List<Object> getArg1() {
            return this.arg1;
        }

        public Throwable getThrowable() {
            return this.throwable;
        }

        public String getString() {
            return this.string;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder(this.string).append(" [");
            for (int i = 0; i < this.arg1.size(); ++i) {
                if (i > 0)
                    sb.append(", ");
                sb.append(this.arg1.get(i));
            }
            sb.append("] ").append(throwable);
            return sb.toString();
        }
    }

    private final Object monitor = new Object();

    private List<StubLogEntry> entries;

    private void addDebug(String string, Object[] args, Throwable t) {
        synchronized (monitor) {
            this.entries.add(new StubLogEntry(string, args, t));
        }
    }

    public StubLogger() {
        this.entries = new ArrayList<StubLogEntry>();
    }

    public List<StubLogEntry> getEntries() {
        synchronized (this.monitor) {
            List<StubLogEntry> list = new ArrayList<StubLogEntry>(this.entries);
            return list;
        }
    }

    /**
     * {@inheritDoc}
     */
    public void debug(String arg0) {
        addDebug(arg0, null, null);
    }

    /**
     * {@inheritDoc}
     */
    public void debug(String arg0, Object arg1) {
        Object[] args = new Object[1];
        args[0] = arg1;
        addDebug(arg0, args, null);
    }

    /**
     * {@inheritDoc}
     */
    public void debug(String arg0, Object... arg1) {
        addDebug(arg0, arg1, null);
    }

    /**
     * {@inheritDoc}
     */
    public void debug(String arg0, Throwable arg1) {
        addDebug(arg0, null, arg1);
    }

    /**
     * {@inheritDoc}
     */
    public void debug(Marker arg0, String arg1) {
        addDebug(arg1, null, null);
    }

    /**
     * {@inheritDoc}
     */
    public void debug(String arg0, Object arg1, Object arg2) {
        Object[] args = new Object[2];
        args[0] = arg1;
        args[1] = arg2;
        addDebug(arg0, args, null);
    }

    /**
     * {@inheritDoc}
     */
    public void debug(Marker arg0, String arg1, Object arg2) {
        Object[] args = new Object[1];
        args[0] = arg2;
        addDebug(arg1, args, null);
    }

    /**
     * {@inheritDoc}
     */
    public void debug(Marker arg0, String arg1, Object... arg2) {
        addDebug(arg1, arg2, null);
    }

    /**
     * {@inheritDoc}
     */
    public void debug(Marker arg0, String arg1, Throwable arg2) {
        addDebug(arg1, null, arg2);
    }

    /**
     * {@inheritDoc}
     */
    public void debug(Marker arg0, String arg1, Object arg2, Object arg3) {
        Object[] args = new Object[2];
        args[0] = arg2;
        args[1] = arg3;
        addDebug(arg1, args, null);
    }

    /**
     * {@inheritDoc}
     */
    public void error(String arg0) {
    }

    /**
     * {@inheritDoc}
     */
    public void error(String arg0, Object arg1) {
    }

    /**
     * {@inheritDoc}
     */
    public void error(String arg0, Object... arg1) {
    }

    /**
     * {@inheritDoc}
     */
    public void error(String arg0, Throwable arg1) {
    }

    /**
     * {@inheritDoc}
     */
    public void error(Marker arg0, String arg1) {
    }

    /**
     * {@inheritDoc}
     */
    public void error(String arg0, Object arg1, Object arg2) {
    }

    /**
     * {@inheritDoc}
     */
    public void error(Marker arg0, String arg1, Object arg2) {
    }

    /**
     * {@inheritDoc}
     */
    public void error(Marker arg0, String arg1, Object... arg2) {
    }

    /**
     * {@inheritDoc}
     */
    public void error(Marker arg0, String arg1, Throwable arg2) {
    }

    /**
     * {@inheritDoc}
     */
    public void error(Marker arg0, String arg1, Object arg2, Object arg3) {
    }

    /**
     * {@inheritDoc}
     */
    public String getName() {
        return "StubLogger";
    }

    /**
     * {@inheritDoc}
     */
    public void info(String arg0) {
    }

    /**
     * {@inheritDoc}
     */
    public void info(String arg0, Object arg1) {
    }

    /**
     * {@inheritDoc}
     */
    public void info(String arg0, Object... arg1) {
    }

    /**
     * {@inheritDoc}
     */
    public void info(String arg0, Throwable arg1) {
    }

    /**
     * {@inheritDoc}
     */
    public void info(Marker arg0, String arg1) {
    }

    /**
     * {@inheritDoc}
     */
    public void info(String arg0, Object arg1, Object arg2) {
    }

    /**
     * {@inheritDoc}
     */
    public void info(Marker arg0, String arg1, Object arg2) {
    }

    /**
     * {@inheritDoc}
     */
    public void info(Marker arg0, String arg1, Object... arg2) {
    }

    /**
     * {@inheritDoc}
     */
    public void info(Marker arg0, String arg1, Throwable arg2) {
    }

    /**
     * {@inheritDoc}
     */
    public void info(Marker arg0, String arg1, Object arg2, Object arg3) {
    }

    /**
     * {@inheritDoc}
     */
    public boolean isDebugEnabled() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isDebugEnabled(Marker arg0) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isErrorEnabled() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isErrorEnabled(Marker arg0) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isInfoEnabled() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isInfoEnabled(Marker arg0) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isTraceEnabled() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isTraceEnabled(Marker arg0) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isWarnEnabled() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isWarnEnabled(Marker arg0) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public void trace(String arg0) {
    }

    /**
     * {@inheritDoc}
     */
    public void trace(String arg0, Object arg1) {
    }

    /**
     * {@inheritDoc}
     */
    public void trace(String arg0, Object... arg1) {
    }

    /**
     * {@inheritDoc}
     */
    public void trace(String arg0, Throwable arg1) {
    }

    /**
     * {@inheritDoc}
     */
    public void trace(Marker arg0, String arg1) {
    }

    /**
     * {@inheritDoc}
     */
    public void trace(String arg0, Object arg1, Object arg2) {
    }

    /**
     * {@inheritDoc}
     */
    public void trace(Marker arg0, String arg1, Object arg2) {
    }

    /**
     * {@inheritDoc}
     */
    public void trace(Marker arg0, String arg1, Object... arg2) {
    }

    /**
     * {@inheritDoc}
     */
    public void trace(Marker arg0, String arg1, Throwable arg2) {
    }

    /**
     * {@inheritDoc}
     */
    public void trace(Marker arg0, String arg1, Object arg2, Object arg3) {
    }

    /**
     * {@inheritDoc}
     */
    public void warn(String arg0) {
    }

    /**
     * {@inheritDoc}
     */
    public void warn(String arg0, Object arg1) {
    }

    /**
     * {@inheritDoc}
     */
    public void warn(String arg0, Object... arg1) {
    }

    /**
     * {@inheritDoc}
     */
    public void warn(String arg0, Throwable arg1) {
    }

    /**
     * {@inheritDoc}
     */
    public void warn(Marker arg0, String arg1) {
    }

    /**
     * {@inheritDoc}
     */
    public void warn(String arg0, Object arg1, Object arg2) {
    }

    /**
     * {@inheritDoc}
     */
    public void warn(Marker arg0, String arg1, Object arg2) {
    }

    /**
     * {@inheritDoc}
     */
    public void warn(Marker arg0, String arg1, Object... arg2) {
    }

    /**
     * {@inheritDoc}
     */
    public void warn(Marker arg0, String arg1, Throwable arg2) {
    }

    /**
     * {@inheritDoc}
     */
    public void warn(Marker arg0, String arg1, Object arg2, Object arg3) {
    }

}
