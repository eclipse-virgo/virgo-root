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

package org.eclipse.virgo.nano.shutdown;

/**
 * A <code>ShutdownCommandParser</code> is used to parse a series of arguments
 * into a {@link ShutdownCommand}.
 *
 * <p />
 * 
 * <strong>Concurrent Semantics</strong> <br />
 * Thread-safe.
 * 
 */
final class ShutdownCommandParser {
	
    private static final String OPTION_DOMAIN = "-domain";

    private static final String OPTION_IMMEDIATE = "-immediate";

    private static final String OPTION_JMXPORT = "-jmxport";

    private static final String OPTION_PASSWORD = "-password";

    private static final String OPTION_USERNAME = "-username";
	
	static ShutdownCommand parse(String... args) {
		
		ShutdownCommand command = new ShutdownCommand();
		
		if (args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                if (args[i].equals(OPTION_USERNAME)) {
                    if (i < args.length - 1) {
                        command.setUsername(args[++i]);
                    } else {
                        return null;
                    }
                } else if (args[i].equals(OPTION_PASSWORD)) {
                    if (i < args.length - 1) {
                        command.setPassword(args[++i]);
                    } else {
                        return null;
                    }
                } else if (args[i].equals(OPTION_JMXPORT)) {
                    if (i < args.length - 1) {
                        try {
                            command.setPort(Integer.parseInt(args[++i]));
                        } catch (NumberFormatException nfe) {
                            return null;
                        }
                    } else {
                        return null;
                    }
                } else if (args[i].equals(OPTION_IMMEDIATE)) {
                    command.setImmediate(true);
                } else if (args[i].equals(OPTION_DOMAIN)) {
                    if (i < args.length - 1) {
                        command.setDomain(args[++i]);
                    } else {
                        return null;
                    }
                } else {
                    return null;
                }
            }
        }
		
		return command;
	}
}
