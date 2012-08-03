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

package org.eclipse.virgo.osgi.console.telnet;

import org.eclipse.virgo.osgi.console.telnet.Callback;
import org.eclipse.virgo.osgi.console.telnet.TelnetConsoleSession;

public class NegotiationFinishedCallback implements Callback {

    private TelnetConsoleSession consoleSession;

    public NegotiationFinishedCallback(TelnetConsoleSession consoleSession) {
        this.consoleSession = consoleSession;
    }

    @Override
    public void finished() {
        consoleSession.telnetNegotiationFinished();
    }

}
