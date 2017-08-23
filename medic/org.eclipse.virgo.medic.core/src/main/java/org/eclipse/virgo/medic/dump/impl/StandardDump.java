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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import org.eclipse.virgo.medic.dump.Dump;
import org.eclipse.virgo.medic.dump.DumpContributionFailedException;


final class StandardDump implements Dump {

    private final File dumpDirectory;

    private final String cause;

    private final Map<String, Object> context;

    private final Throwable[] throwables;

    private final long timestamp;

    StandardDump(String cause, long timestamp, Map<String, Object> context, Throwable[] throwables, File dumpDirectory) {
        this.cause = cause;
        this.timestamp = timestamp;
        this.context = context;
        this.throwables = throwables;
        this.dumpDirectory = dumpDirectory;
    }

    public String getCause() {
        return this.cause;
    }

    public Map<String, Object> getContext() {
        return this.context;
    }

    public Throwable[] getThrowables() {
        return this.throwables;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public File createFile(String name) {
        return new File(this.dumpDirectory, name);
    }

    public FileOutputStream createFileOutputStream(String name) throws DumpContributionFailedException {
        try {
            return new FileOutputStream(createFile(name));
        } catch (FileNotFoundException e) {
            throw new DumpContributionFailedException("Unable to open output stream '" + name + "'");
        }
    }

    public FileWriter createFileWriter(String name) throws DumpContributionFailedException {
        try {
            return new FileWriter(createFile(name));
        } catch (IOException e) {
            throw new DumpContributionFailedException("Unable to open file writer '" + name + "'");
        }
    }

	File getDumpDirectory() {
		return this.dumpDirectory;
	}
}
