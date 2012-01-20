/*******************************************************************************
 * Copyright (c) 2008, 2012 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *   SAP AG - re-factoring
 *******************************************************************************/

package org.eclipse.virgo.nano.smoketest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Test;

public class NanoSmokeTests extends AbstractNanoTests {

	@Test
    public void testNanoStartAndStop() throws Exception {
        new Thread(new NanoStartUpThread()).start();
        waitForNanoStartFully();
        Thread.sleep(20000); // wait for startup to complete in case it fails
        assertEquals(STATUS_STARTED, getNanoStartUpStatus());
        
        new Thread(new NanoShutdownThread()).start();
        waitForNanoShutdownFully();
        Thread.sleep(10000); // wait for startup to complete in case it fails
    }

    @Test
    public void testNanoJavaProfileSetCorrectly() throws Exception {
        new Thread(new NanoStartUpThread()).start();
        waitForNanoStartFully();
        Thread.sleep(20000); // wait for startup to complete in case it fails
        assertEquals(STATUS_STARTED, getNanoStartUpStatus());
        
        //deploy bundle that should kill the server if the test is successful
        hotDeployTestBundles("java.profile.tester_1.0.0.jar");
        
        Thread.sleep(10000); //wait for deployment
        
        try {
            getNanoStartUpStatus();
            //the server is still running - shutdown and fail
            new Thread(new NanoShutdownThread()).start();
            waitForNanoShutdownFully();
            Thread.sleep(10000);
            fail("Virgo java profile not properly set - Nano was supposed to be killed if the test was successful.");
        } catch (IOException e) { 
            //Expected exception
        }
    }
}
