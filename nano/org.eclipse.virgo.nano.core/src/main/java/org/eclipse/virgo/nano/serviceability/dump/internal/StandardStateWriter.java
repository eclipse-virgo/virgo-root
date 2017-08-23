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

package org.eclipse.virgo.nano.serviceability.dump.internal;

import java.io.File;
import java.io.IOException;

import org.eclipse.osgi.service.resolver.State;
import org.eclipse.osgi.service.resolver.StateObjectFactory;


/**
 * Standard implementation of {@link StateWriter}.
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * Thread-safe.
 *
 */
class StandardStateWriter implements StateWriter {
    
    private final StateObjectFactory factory;
    
    StandardStateWriter(StateObjectFactory factory) {
        this.factory = factory;
    }

    /** 
     * {@inheritDoc} 
     */
    public void writeState(State state, File output) throws IOException {
        this.factory.writeState(state, output);
    }
}
