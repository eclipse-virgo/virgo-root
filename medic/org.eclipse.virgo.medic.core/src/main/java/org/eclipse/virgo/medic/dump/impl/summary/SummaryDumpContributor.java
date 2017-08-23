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

package org.eclipse.virgo.medic.dump.impl.summary;

import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.Date;

import org.eclipse.virgo.medic.dump.Dump;
import org.eclipse.virgo.medic.dump.DumpContributionFailedException;
import org.eclipse.virgo.medic.dump.DumpContributor;


public final class SummaryDumpContributor implements DumpContributor {

    private final DateFormat dateFormat = DateFormat.getDateInstance();

    private final DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.LONG);

    private final Object formatterLock = new Object();

    public void contribute(Dump dump) throws DumpContributionFailedException {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(dump.createFileWriter("summary.txt"));
            processHeader(writer, dump.getTimestamp());
            processCause(writer, dump.getCause());
            processThrowables(writer, dump.getThrowables());
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    public String getName() {
        return "summary";
    }

    private void processHeader(PrintWriter dump, long timestamp) {
        synchronized (formatterLock) {
            dump.println("Date:\t\t" + dateFormat.format(new Date(timestamp)));
            dump.println("Time:\t\t" + timeFormat.format(new Date(timestamp)));
        }
    }

    private static void processCause(PrintWriter dump, String cause) {
        dump.println();
        dump.println("Cause: " + cause);
    }

    private static void processThrowables(PrintWriter dump, Throwable... throwables) {
        if (throwables.length == 0) {
            dump.println();
            dump.println("Exception: None");
        } else {
            for (Throwable throwable : throwables) {
                dump.println();
                dump.println("Exception:");
                throwable.printStackTrace(dump);
            }
        }
    }
}
