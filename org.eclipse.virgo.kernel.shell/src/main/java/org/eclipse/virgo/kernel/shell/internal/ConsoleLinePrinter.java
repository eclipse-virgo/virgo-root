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


package org.eclipse.virgo.kernel.shell.internal;

import java.io.IOException;

import jline.ConsoleReader;

import org.eclipse.virgo.kernel.shell.LinePrinter;


/**
 * Implementation of {@link LinePrinter} that prints to a {@link ConsoleReader}
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 * thread-safe
 *
 * @author Steve Powell
 */
public class ConsoleLinePrinter implements LinePrinter {

    private static final String CR_LF = "\r\n";

    private final ConsoleReader consoleReader;
    
    ConsoleLinePrinter(ConsoleReader consoleReader) {
        this.consoleReader = consoleReader;
    }

    /** 
     * {@inheritDoc}
     */
    public LinePrinter println(String line) throws IOException {
        this.consoleReader.printString(String.format("%s%s", line, CR_LF));
        return this;
    }
    /** 
     * {@inheritDoc}
     */
    public LinePrinter println() throws IOException {
        this.consoleReader.printString(CR_LF);
        return this;
    }
}
