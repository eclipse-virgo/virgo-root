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

import org.eclipse.virgo.kernel.core.AbortableSignal;
import org.junit.Before;
import org.junit.Test;


/**
 */
public class SignalJunctionTests {

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
        new SignalJunction(this.signal, 0);
        assertTrue(this.signalCompleted);
        assertFalse(this.signalFailed);
    }
    
    @Test
    public void testUnaryJunction() {
        SignalJunction sj = new SignalJunction(this.signal, 1);
        sj.getSignals().get(0).signalSuccessfulCompletion();
        assertTrue(this.signalCompleted);
        assertFalse(this.signalFailed);
    }
    
    @Test
    public void testBinaryJunction() {
        SignalJunction sj = new SignalJunction(this.signal, 2);
        sj.getSignals().get(0).signalSuccessfulCompletion();
        sj.getSignals().get(1).signalSuccessfulCompletion();
        assertTrue(this.signalCompleted);
        assertFalse(this.signalFailed);
    }
    
    @Test
    public void testNestedBinaryJunctionOrder1() {
        SignalJunction top = new SignalJunction(this.signal, 2);
        AbortableSignal top1 = top.getSignals().get(0);
        AbortableSignal top2 = top.getSignals().get(1);
        SignalJunction bottom = new SignalJunction(top2, 2);
        AbortableSignal bottom1 = bottom.getSignals().get(0);
        AbortableSignal bottom2 = bottom.getSignals().get(1);
        top1.signalSuccessfulCompletion();
        bottom1.signalSuccessfulCompletion();
        bottom2.signalSuccessfulCompletion();
        assertTrue(this.signalCompleted);
        assertFalse(this.signalFailed);
    }
    
    @Test
    public void testNestedBinaryJunctionOrder2() {
        SignalJunction top = new SignalJunction(this.signal, 2);
        AbortableSignal top1 = top.getSignals().get(0);
        AbortableSignal top2 = top.getSignals().get(1);
        SignalJunction bottom = new SignalJunction(top2, 2);
        AbortableSignal bottom1 = bottom.getSignals().get(0);
        AbortableSignal bottom2 = bottom.getSignals().get(1);
        bottom1.signalSuccessfulCompletion();
        top1.signalSuccessfulCompletion();
        bottom2.signalSuccessfulCompletion();
        assertTrue(this.signalCompleted);
        assertFalse(this.signalFailed);
    }
    
    @Test
    public void testNestedBinaryJunctionOrder3() {
        SignalJunction top = new SignalJunction(this.signal, 2);
        AbortableSignal top1 = top.getSignals().get(0);
        AbortableSignal top2 = top.getSignals().get(1);
        SignalJunction bottom = new SignalJunction(top2, 2);
        AbortableSignal bottom1 = bottom.getSignals().get(0);
        AbortableSignal bottom2 = bottom.getSignals().get(1);
        bottom1.signalSuccessfulCompletion();
        bottom2.signalSuccessfulCompletion();
        top1.signalSuccessfulCompletion();
        assertTrue(this.signalCompleted);
        assertFalse(this.signalFailed);
    }
    
    @Test
    public void testNestedBinaryJunctionOrder4() {
        SignalJunction top = new SignalJunction(this.signal, 2);
        AbortableSignal top1 = top.getSignals().get(0);
        AbortableSignal top2 = top.getSignals().get(1);
        SignalJunction bottom = new SignalJunction(top2, 2);
        AbortableSignal bottom1 = bottom.getSignals().get(0);
        AbortableSignal bottom2 = bottom.getSignals().get(1);
        top1.signalSuccessfulCompletion();
        bottom2.signalSuccessfulCompletion();
        bottom1.signalSuccessfulCompletion();
        assertTrue(this.signalCompleted);
        assertFalse(this.signalFailed);
    }
    
    @Test
    public void testNestedBinaryJunctionOrder5() {
        SignalJunction top = new SignalJunction(this.signal, 2);
        AbortableSignal top1 = top.getSignals().get(0);
        AbortableSignal top2 = top.getSignals().get(1);
        SignalJunction bottom = new SignalJunction(top2, 2);
        AbortableSignal bottom1 = bottom.getSignals().get(0);
        AbortableSignal bottom2 = bottom.getSignals().get(1);
        bottom2.signalSuccessfulCompletion();
        top1.signalSuccessfulCompletion();
        bottom1.signalSuccessfulCompletion();
        assertTrue(this.signalCompleted);
        assertFalse(this.signalFailed);
    }
    
    @Test
    public void testNestedBinaryJunctionOrder6() {
        SignalJunction top = new SignalJunction(this.signal, 2);
        AbortableSignal top1 = top.getSignals().get(0);
        AbortableSignal top2 = top.getSignals().get(1);
        SignalJunction bottom = new SignalJunction(top2, 2);
        AbortableSignal bottom1 = bottom.getSignals().get(0);
        AbortableSignal bottom2 = bottom.getSignals().get(1);
        bottom2.signalSuccessfulCompletion();
        bottom1.signalSuccessfulCompletion();
        top1.signalSuccessfulCompletion();
        assertTrue(this.signalCompleted);
        assertFalse(this.signalFailed);
    }

}
