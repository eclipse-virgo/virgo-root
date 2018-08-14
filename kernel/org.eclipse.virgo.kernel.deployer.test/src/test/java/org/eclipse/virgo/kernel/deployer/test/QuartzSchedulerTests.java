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

package org.eclipse.virgo.kernel.deployer.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import java.io.File;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

/**
 * Integration tests for <em>OSGi-ready</em> Quartz Scheduler configuration with Spring 2.5.6 and higher.
 * 
 * <p>
 * See <a href="http://jira.springframework.org/browse/SPR-5220" target="_blank">SPR-5220</a> for details.
 * </p>
 * 
 */
@Ignore
public class QuartzSchedulerTests extends AbstractParTests {

    private static final String SCHEDULER_FACTORY_BEAN_NAME = "schedulerFactoryBean";

    private static final File BUNDLE_SAME1 = new File("src/test/resources/quartz/quartz.bundle.same1.jar");

    private static final String BSN_SAME1 = "quartz.bundle.same1";

    private static final File BUNDLE_SAME2 = new File("src/test/resources/quartz/quartz.bundle.same2.jar");

    private static final String BSN_SAME2 = "quartz.bundle.same2";

    private static final File BUNDLE_A = new File("src/test/resources/quartz/quartz.bundle.a.jar");

    private static final String BSN_A = "quartz.bundle.a";

    private static final String QUARTZ_SCHEDULER_A = "QuartzScheduler-A";

    private static final File BUNDLE_B = new File("src/test/resources/quartz/quartz.bundle.b.jar");

    private static final String BSN_B = "quartz.bundle.b";

    private static final String QUARTZ_SCHEDULER_B = "QuartzScheduler-B";

    private Object verifySchedulerConfiguration(final String bsn, final File bundle, final String expectedSchedulerName) throws Throwable {
        deploy(bundle);
        
        ApplicationContext appCtx = ApplicationContextUtils.getApplicationContext(this.context, bsn);
        
        Object scheduler = appCtx.getBean(SCHEDULER_FACTORY_BEAN_NAME);
        assertEquals("Verifying the name of the Quartz Scheduler for " + bsn, expectedSchedulerName, scheduler.getClass().getMethod(
            "getSchedulerName").invoke(scheduler));

        return scheduler;
    }

    @Test
    public void testQuartzSchedulerSupportForBundlesWithSameAppCtxConfig() throws Throwable {
        Object schedulerSame1 = verifySchedulerConfiguration(BSN_SAME1, BUNDLE_SAME1, SCHEDULER_FACTORY_BEAN_NAME);
        Object schedulerSame2 = verifySchedulerConfiguration(BSN_SAME2, BUNDLE_SAME2, SCHEDULER_FACTORY_BEAN_NAME);
        assertNotSame("The QuartzSchedulers for bundles " + BSN_SAME1 + " and " + BSN_SAME2 + " should not be the same.", schedulerSame1,
            schedulerSame2);
    }

    @Test
    public void testQuartzSchedulerSupportForBundlesWithDifferentAppCtxConfig() throws Throwable {
        Object schedulerA = verifySchedulerConfiguration(BSN_A, BUNDLE_A, QUARTZ_SCHEDULER_A);
        Object schedulerB = verifySchedulerConfiguration(BSN_B, BUNDLE_B, QUARTZ_SCHEDULER_B);
        assertNotSame("The QuartzSchedulers for bundles " + BSN_A + " and " + BSN_B + " should not be the same.", schedulerA, schedulerB);
    }

}
