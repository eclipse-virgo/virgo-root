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

package org.eclipse.virgo.kernel.shell.internal;

import java.io.InputStream;
import java.io.PrintStream;


/**
 * <p>
 * ShellFactory implementations will create a new shell that reads and writes using the provided streams.
 * </p>
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * ShellFactory implementations should be thread safe
 *
 */
interface LocalShellFactory {

    /**
     * Create a new {@link Runnable} shell using the given streams. The new shell is returned.
     * 
     * @param in
     * @param out
     * @param err
     * @return The new shell
     */
    LocalShell newShell(InputStream in, PrintStream out, PrintStream err);
    
}
