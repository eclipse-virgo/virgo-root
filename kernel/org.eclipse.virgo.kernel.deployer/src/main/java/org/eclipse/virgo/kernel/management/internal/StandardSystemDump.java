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

package org.eclipse.virgo.kernel.management.internal;

import org.eclipse.virgo.kernel.management.SystemDump;
import org.eclipse.virgo.medic.dump.DumpGenerator;

/**
 * 
 * This class is an MBean allowing the system state to be dumped out on demand. Standard implementation of
 * <code>DumpControl</code>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is thread safe
 * 
 */
public class StandardSystemDump implements SystemDump {

    private final DumpGenerator dumpGenerator;

    public StandardSystemDump(DumpGenerator dumpGenerator) {
        this.dumpGenerator = dumpGenerator;
    }

    /**
     * {@inheritDoc}
     */
    public void generateDump() {
        this.dumpGenerator.generateDump("manual");
    }

}
