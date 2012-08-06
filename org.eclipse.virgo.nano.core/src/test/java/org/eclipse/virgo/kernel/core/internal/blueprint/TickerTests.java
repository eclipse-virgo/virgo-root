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

package org.eclipse.virgo.nano.core.internal.blueprint;


import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.eclipse.virgo.nano.core.internal.blueprint.StandardTicker;
import org.eclipse.virgo.nano.core.internal.blueprint.Ticker;
import org.junit.Assert;
import org.junit.Test;


/**
 */
public class TickerTests {
    
    private volatile int ticks;
    
    @Test public void testTicker() throws InterruptedException {
        this.ticks = 0;
        Ticker ticker = StandardTicker.createExponentialTicker(20, 100, 60 * 1000, new Callable<Void>() {

            public Void call() throws Exception {
                TickerTests.this.ticks++;
                return null;
            }}, new ScheduledThreadPoolExecutor(1));
        
        Thread.sleep(2000);
        ticker.cancel();
        
        Assert.assertEquals(6, ticks);
    }
}
