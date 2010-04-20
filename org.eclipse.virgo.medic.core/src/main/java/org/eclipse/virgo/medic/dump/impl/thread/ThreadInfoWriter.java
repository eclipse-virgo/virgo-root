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

package org.eclipse.virgo.medic.dump.impl.thread;

import java.io.PrintWriter;
import java.lang.management.ThreadInfo;

/**
 * A helper interface for writing {@link ThreadInfo} instances enabling the plugging-in of an implementation that can
 * cope with the differences in {@link ThreadInfo}'s contents on Java 5 and Java 6 virtual machines.
 * <p/>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations <strong>must</strong> be thread-safe.
 * 
 */
interface ThreadInfoWriter {

    /**
     * Writes the supplied {@ThreadInfo} to the supplied {@PrintWriter}.
     * 
     * @param threadInfo The <code>ThreadInfo</code>
     * @param writer the <code>PrintWriter</code> to write to
     */
    void write(ThreadInfo threadInfo, PrintWriter writer);
}

