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

import org.eclipse.virgo.kernel.shell.internal.LocalShell;
import org.eclipse.virgo.kernel.shell.internal.LocalShellFactory;



/**
 */
public class StubLocalShellFactory implements LocalShellFactory {

    /** 
     * {@inheritDoc}
     */
    public LocalShell newShell(InputStream in, PrintStream out, PrintStream err) {
        return new StubLocalShell();
    }

}
