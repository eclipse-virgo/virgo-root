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

package org.eclipse.virgo.kernel.deployer.core.internal;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.virgo.nano.core.AbortableSignal;
import org.junit.Before;
import org.junit.Test;


/**
 */
public class AbortableSignalJunctionTests {

    private AbortableSignal signal;
    
    private boolean signalCompleted;

    private boolean signalFailed;
    
    private boolean signalAborted;

    @Before
    public void setUp() {
        this.signalCompleted = false;
        this.signalFailed = false;
        
        this.signal = new AbortableSignal(){

            public void signalFailure(Throwable cause) {
                signalFailed = true;
            }

            public void signalSuccessfulCompletion() {
                signalCompleted = true;
            }
            
            public void signalAborted() {
				signalAborted = true;
			}

        };
    }

    @Test
    public void testEmptyJunction() {
        new AbortableSignalJunction(this.signal, 0);
        assertTrue(this.signalCompleted);
        assertFalse(this.signalFailed);
        assertFalse(this.signalAborted);
    }
    
    @Test
    public void testUnaryJunction() {
        AbortableSignalJunction sj = new AbortableSignalJunction(this.signal, 1);
        sj.getSignals().get(0).signalSuccessfulCompletion();
        assertTrue(this.signalCompleted);
        assertFalse(this.signalFailed);
        assertFalse(this.signalAborted);
    }
    
    @Test
    public void testBinaryJunction() {
        AbortableSignalJunction sj = new AbortableSignalJunction(this.signal, 2);
        sj.getSignals().get(0).signalSuccessfulCompletion();
        sj.getSignals().get(1).signalSuccessfulCompletion();
        assertTrue(this.signalCompleted);
        assertFalse(this.signalFailed);
        assertFalse(this.signalAborted);
    }
    
    @Test
    public void testBinaryJunctionFail() {
        AbortableSignalJunction sj = new AbortableSignalJunction(this.signal, 2);
        sj.getSignals().get(0).signalFailure(new RuntimeException());
        assertFalse(this.signalCompleted);
        assertTrue(this.signalFailed);
        assertFalse(this.signalAborted);
    }
    
    @Test
    public void testBinaryJunctionAbort() {
        AbortableSignalJunction sj = new AbortableSignalJunction(this.signal, 2);
        sj.getSignals().get(0).signalAborted();
        assertFalse(this.signalCompleted);
        assertFalse(this.signalFailed);
        assertTrue(this.signalAborted);
    }
    
    @Test
    public void testNestedBinaryJunctionOrder1() {
        AbortableSignalJunction top = new AbortableSignalJunction(this.signal, 2);
        AbortableSignal top1 = top.getSignals().get(0);
        AbortableSignal top2 = top.getSignals().get(1);
        AbortableSignalJunction bottom = new AbortableSignalJunction(top2, 2);
        AbortableSignal bottom1 = bottom.getSignals().get(0);
        AbortableSignal bottom2 = bottom.getSignals().get(1);
        top1.signalSuccessfulCompletion();
        bottom1.signalSuccessfulCompletion();
        bottom2.signalSuccessfulCompletion();
        assertTrue(this.signalCompleted);
        assertFalse(this.signalFailed);
        assertFalse(this.signalAborted);
    }
    
    @Test
    public void testNestedBinaryJunctionOrder2() {
        AbortableSignalJunction top = new AbortableSignalJunction(this.signal, 2);
        AbortableSignal top1 = top.getSignals().get(0);
        AbortableSignal top2 = top.getSignals().get(1);
        AbortableSignalJunction bottom = new AbortableSignalJunction(top2, 2);
        AbortableSignal bottom1 = bottom.getSignals().get(0);
        AbortableSignal bottom2 = bottom.getSignals().get(1);
        bottom1.signalSuccessfulCompletion();
        top1.signalSuccessfulCompletion();
        bottom2.signalSuccessfulCompletion();
        assertTrue(this.signalCompleted);
        assertFalse(this.signalFailed);
        assertFalse(this.signalAborted);
    }
    
    @Test
    public void testNestedBinaryJunctionOrder3() {
        AbortableSignalJunction top = new AbortableSignalJunction(this.signal, 2);
        AbortableSignal top1 = top.getSignals().get(0);
        AbortableSignal top2 = top.getSignals().get(1);
        AbortableSignalJunction bottom = new AbortableSignalJunction(top2, 2);
        AbortableSignal bottom1 = bottom.getSignals().get(0);
        AbortableSignal bottom2 = bottom.getSignals().get(1);
        bottom1.signalSuccessfulCompletion();
        bottom2.signalSuccessfulCompletion();
        top1.signalSuccessfulCompletion();
        assertTrue(this.signalCompleted);
        assertFalse(this.signalFailed);
        assertFalse(this.signalAborted);
    }
    
    @Test
    public void testNestedBinaryJunctionOrder4() {
        AbortableSignalJunction top = new AbortableSignalJunction(this.signal, 2);
        AbortableSignal top1 = top.getSignals().get(0);
        AbortableSignal top2 = top.getSignals().get(1);
        AbortableSignalJunction bottom = new AbortableSignalJunction(top2, 2);
        AbortableSignal bottom1 = bottom.getSignals().get(0);
        AbortableSignal bottom2 = bottom.getSignals().get(1);
        top1.signalSuccessfulCompletion();
        bottom2.signalSuccessfulCompletion();
        bottom1.signalSuccessfulCompletion();
        assertTrue(this.signalCompleted);
        assertFalse(this.signalFailed);
        assertFalse(this.signalAborted);
    }
    
    @Test
    public void testNestedBinaryJunctionOrder5() {
        AbortableSignalJunction top = new AbortableSignalJunction(this.signal, 2);
        AbortableSignal top1 = top.getSignals().get(0);
        AbortableSignal top2 = top.getSignals().get(1);
        AbortableSignalJunction bottom = new AbortableSignalJunction(top2, 2);
        AbortableSignal bottom1 = bottom.getSignals().get(0);
        AbortableSignal bottom2 = bottom.getSignals().get(1);
        bottom2.signalSuccessfulCompletion();
        top1.signalSuccessfulCompletion();
        bottom1.signalSuccessfulCompletion();
        assertTrue(this.signalCompleted);
        assertFalse(this.signalFailed);
        assertFalse(this.signalAborted);
    }
    
    @Test
    public void testNestedBinaryJunctionOrder6() {
        AbortableSignalJunction top = new AbortableSignalJunction(this.signal, 2);
        AbortableSignal top1 = top.getSignals().get(0);
        AbortableSignal top2 = top.getSignals().get(1);
        AbortableSignalJunction bottom = new AbortableSignalJunction(top2, 2);
        AbortableSignal bottom1 = bottom.getSignals().get(0);
        AbortableSignal bottom2 = bottom.getSignals().get(1);
        bottom2.signalSuccessfulCompletion();
        bottom1.signalSuccessfulCompletion();
        top1.signalSuccessfulCompletion();
        assertTrue(this.signalCompleted);
        assertFalse(this.signalFailed);
        assertFalse(this.signalAborted);
    }
    
    @Test
    public void testNestedBinaryJunctionOrderFail() {
        AbortableSignalJunction top = new AbortableSignalJunction(this.signal, 2);
        AbortableSignal top1 = top.getSignals().get(0);
        AbortableSignal top2 = top.getSignals().get(1);
        AbortableSignalJunction bottom = new AbortableSignalJunction(top2, 2);
        AbortableSignal bottom1 = bottom.getSignals().get(0);
        AbortableSignal bottom2 = bottom.getSignals().get(1);
        top1.signalSuccessfulCompletion();
        bottom1.signalFailure(new RuntimeException());
        bottom2.signalSuccessfulCompletion();
        assertFalse(this.signalCompleted);
        assertTrue(this.signalFailed);
        assertFalse(this.signalAborted);
    }
    
    @Test
    public void testNestedBinaryJunctionOrderAbort() {
        AbortableSignalJunction top = new AbortableSignalJunction(this.signal, 2);
        AbortableSignal top1 = top.getSignals().get(0);
        AbortableSignal top2 = top.getSignals().get(1);
        AbortableSignalJunction bottom = new AbortableSignalJunction(top2, 2);
        AbortableSignal bottom1 = bottom.getSignals().get(0);
        AbortableSignal bottom2 = bottom.getSignals().get(1);
        top1.signalSuccessfulCompletion();
        bottom1.signalAborted();
        bottom2.signalSuccessfulCompletion();
        assertFalse(this.signalCompleted);
        assertFalse(this.signalFailed);
        assertTrue(this.signalAborted);
    }

}
