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


/**
 * A <code>StateWriter</code> is used to write a {@link State} to disk.
 * 
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 * Implementations must be thread-safe.
 * 
 */
public interface StateWriter {
    
    /**
     * Writes the given <code>State</code> to the given <code>output</code> location.
     * 
     * @param state The <code>State</code> to write
     * @param outputDir The directory to which it should be written.
     * 
     * @throws IOException if a failure occurs when writing the state
     */
    void writeState(State state, File outputDir) throws IOException;
}
