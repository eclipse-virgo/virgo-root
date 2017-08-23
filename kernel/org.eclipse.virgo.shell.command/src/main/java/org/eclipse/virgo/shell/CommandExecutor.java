/*******************************************************************************
 * This file is part of the Virgo Web Server.
 *
 * Copyright (c) 2010 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SpringSource, a division of VMware - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.virgo.shell;

import java.io.IOException;

/**
 * Interface for service to allow kernel commands to be invoked from the user-region
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 * implementations must be thread-safe
 *
 * @author Steve Powell
 */
public interface CommandExecutor {
    
    /**
     * Execute the given command with the supplied parameters, and output results serially on the linePrinter.
     * @param commandLine the command line to execute
     * @param linePrinter the linePrinter to use for outputting results of executing the command
     * @return false if the command requests termination of execution context; true otherwise
     * @throws IOException if linePrinter does; otherwise should throw unchecked exceptions for command failures
     */
    boolean execute(String commandLine, LinePrinter linePrinter) throws IOException;

}
