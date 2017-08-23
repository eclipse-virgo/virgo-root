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

package org.eclipse.virgo.kernel.services.concurrent.monitor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.virgo.kernel.services.concurrent.monitor.DeadlockAnalyser;
import org.eclipse.virgo.kernel.services.concurrent.monitor.DeadlockAnalyser.Deadlock;
import org.junit.Test;


/**
 */
public class DeadlockAnalyserTests {

    @Test public void deadlocks() {
        DeadlockAnalyser da = new DeadlockAnalyser();
        Deadlock[] before = da.findDeadlocks();

        DeadlockCreatorMBean dc = new DeadlockCreator();
        dc.createDeadlock(2, 0);
        dc.createDeadlock(3, 0);
        dc.createDeadlock(4, 2);

        Deadlock[] deadlocks = da.findDeadlocks();
        assertNotNull(deadlocks);
        assertEquals(5 + before.length, deadlocks.length);
        boolean seen2 = false;
        boolean seen3 = false;
        boolean seen4 = false;
        for (Deadlock deadlock : deadlocks) {
            switch (deadlock.getMembers().length) {
                case 2:
                    seen2 = true;
                    break;
                case 3:
                    seen3 = true;
                    break;
                case 4:
                    seen4 = true;
                    break;
            }
        }
        assertTrue(seen2);
        assertTrue(seen3);
        assertTrue(seen4);
    }

}
