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

import org.eclipse.virgo.osgi.console.telnet.ANSITerminalTypeMappings;

public class VT220TerminalTypeMappings extends ANSITerminalTypeMappings {

    public VT220TerminalTypeMappings() {
        super();

        BACKSPACE = 127;
        DEL = -1;
    }
}
