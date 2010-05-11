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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.virgo.kernel.shell.internal.ExitCallback;
import org.eclipse.virgo.kernel.shell.internal.LocalShell;



/**
 */
public class StubLocalShell implements LocalShell {

    private Set<ExitCallback> callBacks = new HashSet<ExitCallback>();

    /**
     * {@inheritDoc}
     */
    public void addExitCallback(ExitCallback exitCallback) {
        this.callBacks.add(exitCallback);
    }

    /**
     * {@inheritDoc}
     */
    public void run() {
   
    }

}
