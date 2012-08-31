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
 * An interface used to output lines serially
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 * Implementations should be thread-safe
 *
 * @author Steve Powell
 */
public interface LinePrinter {
    
    /**
     * Print the string as a single line. May contain newline characters, but should not end with one.
     * @param line the line to print
     * @return self, as an aid to chaining
     * @throws IOException to indicate printing failures
     */
    LinePrinter println(String line) throws IOException;

    /**
     * Print an empty line.
     * @return self, as an aid to chaining
     * @throws IOException to indicate printing failures
     */
    LinePrinter println() throws IOException;

}
