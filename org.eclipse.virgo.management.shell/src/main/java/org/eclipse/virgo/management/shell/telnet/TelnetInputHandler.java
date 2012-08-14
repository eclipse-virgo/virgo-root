/*******************************************************************************
 * Copyright (c) 2011 SAP AG
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Lazar Kirchev, SAP AG - initial contribution
 ******************************************************************************/

package org.eclipse.virgo.management.shell.telnet;

import org.eclipse.virgo.management.shell.telnet.Callback;
import org.eclipse.virgo.management.shell.telnet.TelnetInputScanner;
import org.eclipse.virgo.management.shell.common.ConsoleInputStream;
import org.eclipse.virgo.management.shell.common.ConsoleOutputStream;
import org.eclipse.virgo.management.shell.common.InputHandler;

import java.io.InputStream;

/**
 * This class customizes the generic handler with a concrete content processor, which provides telnet protocol handling.
 */
public class TelnetInputHandler extends InputHandler {

    public TelnetInputHandler(InputStream input, ConsoleInputStream in, ConsoleOutputStream out, Callback callback) {
        super(input, in, out);
        inputScanner = new TelnetInputScanner(in, out, callback);
    }
}
