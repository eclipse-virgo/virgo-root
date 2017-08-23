/*******************************************************************************
 * Copyright (c) 2011 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.shell.osgicommand.internal;


import java.io.IOException;

import org.eclipse.virgo.shell.LinePrinter;

/**
 * {@link GogoLinePrinter} is an implementation of the Virgo shell {@link LinePrinter} for use with Gogo.
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 * Thread safe.
 */
final class GogoLinePrinter implements LinePrinter {

    /** 
     * {@inheritDoc}
     */
    @Override
    public LinePrinter println(String line) throws IOException {
        System.out.println(line);
        return null;
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public LinePrinter println() throws IOException {
        System.out.println();
        return null;
    }

}
