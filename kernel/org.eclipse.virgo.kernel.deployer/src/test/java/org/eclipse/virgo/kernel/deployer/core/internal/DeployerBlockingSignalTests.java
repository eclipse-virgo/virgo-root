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

import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.kernel.deployer.core.internal.BlockingSignal;
import org.junit.Test;


/**
 */
public class DeployerBlockingSignalTests {

    private static final long TEST_PERIOD = 2; //Seconds

    /**
     * Test method for {@link org.eclipse.virgo.kernel.deployer.core.internal.BlockingSignal#awaitCompletion(long)}.
     * @throws DeploymentException 
     */
    @Test
    public void testAwaitCompletionBlock() throws DeploymentException {
        BlockingSignal blockingSignal = new BlockingSignal(true);
        long start = System.nanoTime();
        boolean awaitCompletion = blockingSignal.awaitCompletion(TEST_PERIOD);
        long finish = System.nanoTime();
        assertFalse(awaitCompletion);
        assertTrue(finish - start > TEST_PERIOD * 1000 * 1000);
    }
    
    /**
     * Test method for {@link org.eclipse.virgo.kernel.deployer.core.internal.BlockingSignal#awaitCompletion(long)}.
     * @throws DeploymentException 
     */
    @Test
    public void testAwaitCompletionBlockSucceed() throws DeploymentException {
        BlockingSignal blockingSignal = new BlockingSignal(true);
        long start = System.currentTimeMillis();
        this.launchThread(blockingSignal, TEST_PERIOD/2);
        boolean awaitCompletion = blockingSignal.awaitCompletion(TEST_PERIOD);
        long finish = System.currentTimeMillis();
        assertTrue(awaitCompletion);
        assertTrue(finish - start < TEST_PERIOD * 1000);
    }
    
    /**
     * Test method for {@link org.eclipse.virgo.kernel.deployer.core.internal.BlockingSignal#awaitCompletion(long)}.
     * @throws DeploymentException 
     */
    @Test
    public void testAwaitCompletionNoBlock() throws DeploymentException {
        BlockingSignal blockingSignal = new BlockingSignal(false);
        long start = System.currentTimeMillis();
        boolean awaitCompletion = blockingSignal.awaitCompletion(TEST_PERIOD);        
        long finish = System.currentTimeMillis();
        assertFalse(awaitCompletion);
        assertTrue(finish - start < TEST_PERIOD * 1000);
    }

    /**
     * Test method for {@link org.eclipse.virgo.kernel.deployer.core.internal.BlockingSignal#checkComplete()}.
     * @throws DeploymentException 
     */
    @Test
    public void testCheckCompleteBlockFast() throws DeploymentException {
        BlockingSignal blockingSignal = new BlockingSignal(true);
        long start = System.currentTimeMillis();
        this.launchThread(blockingSignal, TEST_PERIOD/2);
        boolean checkCompletion = blockingSignal.checkComplete();
        long finish = System.currentTimeMillis();
        assertTrue(checkCompletion);
        assertTrue(finish - start < TEST_PERIOD * 1000);
    }
    
    /**
     * Test method for {@link org.eclipse.virgo.kernel.deployer.core.internal.BlockingSignal#checkComplete()}.
     * @throws DeploymentException 
     */
    @Test
    public void testCheckCompleteBlockSlow() throws DeploymentException {
        BlockingSignal blockingSignal = new BlockingSignal(true);
        long start = System.currentTimeMillis();
        this.launchThread(blockingSignal, TEST_PERIOD + 1);
        boolean checkCompletion = blockingSignal.checkComplete();
        long finish = System.currentTimeMillis();
        assertTrue(checkCompletion);
        assertTrue(finish - start > TEST_PERIOD * 1000);
    }
    
    /**
     * Test method for {@link org.eclipse.virgo.kernel.deployer.core.internal.BlockingSignal#checkComplete()}.
     * @throws DeploymentException 
     */
    @Test
    public void testCheckCompleteNoBlock() throws DeploymentException {
        BlockingSignal blockingSignal = new BlockingSignal(false);
        long start = System.currentTimeMillis();
        boolean checkCompletion = blockingSignal.checkComplete();      
        long finish = System.currentTimeMillis();
        assertFalse(checkCompletion);
        assertTrue(finish - start < TEST_PERIOD * 1000);
    }

    
    private void launchThread(BlockingSignal blockingSignal, long seconds) {
        SignalSucceeder signalSucceeder = new SignalSucceeder(blockingSignal, seconds * 1000);
        new Thread(signalSucceeder).start();
    }

    private static class SignalSucceeder implements Runnable{

        private final long timeOut;
        private final BlockingSignal blockingSignal;

        public SignalSucceeder(BlockingSignal blockingSignal, long timeOut) {
            this.blockingSignal = blockingSignal;
            this.timeOut = timeOut;
        }
        
        public void run() {
            try {
                Thread.sleep(this.timeOut);
            } catch (InterruptedException e) {
                System.out.println("Sleep interupted");
            }
            this.blockingSignal.signalSuccessfulCompletion();
        }
        
    }
    
}
