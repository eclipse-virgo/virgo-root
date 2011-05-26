/*
 * This file is part of the Eclipse Virgo project.
 *
 * Copyright (c) 2011 copyright_holder
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    cgfrost - initial contribution
 */

package org.eclipse.virgo.medic.log.osgi;

import static org.junit.Assert.*;

import org.eclipse.virgo.teststubs.osgi.framework.StubBundleContext;
import org.eclipse.virgo.teststubs.osgi.framework.StubServiceReference;
import org.eclipse.virgo.teststubs.osgi.framework.StubServiceRegistration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;
import org.slf4j.Logger;

import static org.easymock.EasyMock.*;

/**
 * 
 *  OSGiLogServiceImplTests
 */
public class OSGiLogServiceImplTests {

    private static final String TEST_MESSAGE = "Danger Will Robinson, Danger!!!";
    
    private static final ServiceReference<Object> SERVICE_REF = new StubServiceReference<Object>(3l, 3, new StubServiceRegistration<Object>(new StubBundleContext(), "org.eclipse.virgo.not.here"));
    
    private static final String SERVICE_PREFIX = "{Service 3}: ";
    
    private static final String INVALID_PREFIX = "Log Message of unknown severity 99: ";
    
    private static final Throwable FAIL = new Throwable("Got lost in space");
    
    private OSGiLogServiceImpl osgiLogService;

    private Logger logService;
    
    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        this.logService = createMock(Logger.class);
        this.osgiLogService = new OSGiLogServiceImpl(this.logService);
    }
    
    @After
    public void setDown() {
        verify(this.logService);
    }
    
    /**
     * Test method for {@link org.eclipse.virgo.medic.log.osgi.OSGiLogServiceImpl#log(int, java.lang.String)}.
     */
    @Test
    public void testDebugLog() {
        this.logService.debug(TEST_MESSAGE);
        expectLastCall().once();
        replay(this.logService);
        this.osgiLogService.log(LogService.LOG_DEBUG, TEST_MESSAGE);
    }

    /**
     * Test method for {@link org.eclipse.virgo.medic.log.osgi.OSGiLogServiceImpl#log(int, java.lang.String, java.lang.Throwable)}.
     */
    @Test
    public void testDebugLogThrowable() {
        this.logService.debug(TEST_MESSAGE, FAIL);
        expectLastCall().once();
        replay(this.logService);
        this.osgiLogService.log(LogService.LOG_DEBUG, TEST_MESSAGE, FAIL);
    }

    /**
     * Test method for {@link org.eclipse.virgo.medic.log.osgi.OSGiLogServiceImpl#log(org.osgi.framework.ServiceReference, int, java.lang.String)}.
     */
    @Test
    public void testDebugLogServiceReference() {
        this.logService.debug(SERVICE_PREFIX +TEST_MESSAGE);
        expectLastCall().once();
        replay(this.logService);
        this.osgiLogService.log(SERVICE_REF, LogService.LOG_DEBUG, TEST_MESSAGE);
    }

    /**
     * Test method for {@link org.eclipse.virgo.medic.log.osgi.OSGiLogServiceImpl#log(org.osgi.framework.ServiceReference, int, java.lang.String, java.lang.Throwable)}.
     */
    @Test
    public void testDebugLogServiceReferenceThrowable() {
        this.logService.debug(SERVICE_PREFIX + TEST_MESSAGE, FAIL);
        expectLastCall().once();
        replay(this.logService);
        this.osgiLogService.log(SERVICE_REF, LogService.LOG_DEBUG, TEST_MESSAGE, FAIL);
    }

    /**
     * Test method for {@link org.eclipse.virgo.medic.log.osgi.OSGiLogServiceImpl#log(int, java.lang.String)}.
     */
    @Test
    public void testInfoLog() {
        this.logService.info(TEST_MESSAGE);
        expectLastCall().once();
        replay(this.logService);
        this.osgiLogService.log(LogService.LOG_INFO, TEST_MESSAGE);
    }

    /**
     * Test method for {@link org.eclipse.virgo.medic.log.osgi.OSGiLogServiceImpl#log(int, java.lang.String, java.lang.Throwable)}.
     */
    @Test
    public void testInfoLogThrowable() {
        this.logService.info(TEST_MESSAGE, FAIL);
        expectLastCall().once();
        replay(this.logService);
        this.osgiLogService.log(LogService.LOG_INFO, TEST_MESSAGE, FAIL);
    }

    /**
     * Test method for {@link org.eclipse.virgo.medic.log.osgi.OSGiLogServiceImpl#log(org.osgi.framework.ServiceReference, int, java.lang.String)}.
     */
    @Test
    public void testInfoLogServiceReference() {
        this.logService.info(SERVICE_PREFIX +TEST_MESSAGE);
        expectLastCall().once();
        replay(this.logService);
        this.osgiLogService.log(SERVICE_REF, LogService.LOG_INFO, TEST_MESSAGE);
    }

    /**
     * Test method for {@link org.eclipse.virgo.medic.log.osgi.OSGiLogServiceImpl#log(org.osgi.framework.ServiceReference, int, java.lang.String, java.lang.Throwable)}.
     */
    @Test
    public void testInfoLogServiceReferenceThrowable() {
        this.logService.info(SERVICE_PREFIX + TEST_MESSAGE, FAIL);
        expectLastCall().once();
        replay(this.logService);
        this.osgiLogService.log(SERVICE_REF, LogService.LOG_INFO, TEST_MESSAGE, FAIL);
    }

    /**
     * Test method for {@link org.eclipse.virgo.medic.log.osgi.OSGiLogServiceImpl#log(int, java.lang.String)}.
     */
    @Test
    public void testWarningLog() {
        this.logService.warn(TEST_MESSAGE);
        expectLastCall().once();
        replay(this.logService);
        this.osgiLogService.log(LogService.LOG_WARNING, TEST_MESSAGE);
    }

    /**
     * Test method for {@link org.eclipse.virgo.medic.log.osgi.OSGiLogServiceImpl#log(int, java.lang.String, java.lang.Throwable)}.
     */
    @Test
    public void testWarningLogThrowable() {
        this.logService.warn(TEST_MESSAGE, FAIL);
        expectLastCall().once();
        replay(this.logService);
        this.osgiLogService.log(LogService.LOG_WARNING, TEST_MESSAGE, FAIL);
    }

    /**
     * Test method for {@link org.eclipse.virgo.medic.log.osgi.OSGiLogServiceImpl#log(org.osgi.framework.ServiceReference, int, java.lang.String)}.
     */
    @Test
    public void testWarningLogServiceReference() {
        this.logService.warn(SERVICE_PREFIX +TEST_MESSAGE);
        expectLastCall().once();
        replay(this.logService);
        this.osgiLogService.log(SERVICE_REF, LogService.LOG_WARNING, TEST_MESSAGE);
    }

    /**
     * Test method for {@link org.eclipse.virgo.medic.log.osgi.OSGiLogServiceImpl#log(org.osgi.framework.ServiceReference, int, java.lang.String, java.lang.Throwable)}.
     */
    @Test
    public void testWarningLogServiceReferenceThrowable() {
        this.logService.warn(SERVICE_PREFIX + TEST_MESSAGE, FAIL);
        expectLastCall().once();
        replay(this.logService);
        this.osgiLogService.log(SERVICE_REF, LogService.LOG_WARNING, TEST_MESSAGE, FAIL);
    }

    /**
     * Test method for {@link org.eclipse.virgo.medic.log.osgi.OSGiLogServiceImpl#log(int, java.lang.String)}.
     */
    @Test
    public void testErrorLog() {
        this.logService.error(TEST_MESSAGE);
        expectLastCall().once();
        replay(this.logService);
        this.osgiLogService.log(LogService.LOG_ERROR, TEST_MESSAGE);
    }

    /**
     * Test method for {@link org.eclipse.virgo.medic.log.osgi.OSGiLogServiceImpl#log(int, java.lang.String, java.lang.Throwable)}.
     */
    @Test
    public void testErrorLogThrowable() {
        this.logService.error(TEST_MESSAGE, FAIL);
        expectLastCall().once();
        replay(this.logService);
        this.osgiLogService.log(LogService.LOG_ERROR, TEST_MESSAGE, FAIL);
    }

    /**
     * Test method for {@link org.eclipse.virgo.medic.log.osgi.OSGiLogServiceImpl#log(org.osgi.framework.ServiceReference, int, java.lang.String)}.
     */
    @Test
    public void testErrorLogServiceReference() {
        this.logService.error(SERVICE_PREFIX +TEST_MESSAGE);
        expectLastCall().once();
        replay(this.logService);
        this.osgiLogService.log(SERVICE_REF, LogService.LOG_ERROR, TEST_MESSAGE);
    }

    /**
     * Test method for {@link org.eclipse.virgo.medic.log.osgi.OSGiLogServiceImpl#log(org.osgi.framework.ServiceReference, int, java.lang.String, java.lang.Throwable)}.
     */
    @Test
    public void testErrorLogServiceReferenceThrowable() {
        this.logService.error(SERVICE_PREFIX + TEST_MESSAGE, FAIL);
        expectLastCall().once();
        replay(this.logService);
        this.osgiLogService.log(SERVICE_REF, LogService.LOG_ERROR, TEST_MESSAGE, FAIL);
    }

    /**
     * Test method for {@link org.eclipse.virgo.medic.log.osgi.OSGiLogServiceImpl#log(int, java.lang.String)}.
     */
    @Test
    public void testInvalidLog() {
        this.logService.error(INVALID_PREFIX + TEST_MESSAGE);
        expectLastCall().once();
        replay(this.logService);
        this.osgiLogService.log(99, TEST_MESSAGE);
    }

    /**
     * Test method for {@link org.eclipse.virgo.medic.log.osgi.OSGiLogServiceImpl#log(int, java.lang.String, java.lang.Throwable)}.
     */
    @Test
    public void testInvalidLogThrowable() {
        this.logService.error(INVALID_PREFIX + TEST_MESSAGE, FAIL);
        expectLastCall().once();
        replay(this.logService);
        this.osgiLogService.log(99, TEST_MESSAGE, FAIL);
    }

    /**
     * Test method for {@link org.eclipse.virgo.medic.log.osgi.OSGiLogServiceImpl#log(org.osgi.framework.ServiceReference, int, java.lang.String)}.
     */
    @Test
    public void testInvalidLogServiceReference() {
        this.logService.error(INVALID_PREFIX + SERVICE_PREFIX +TEST_MESSAGE);
        expectLastCall().once();
        replay(this.logService);
        this.osgiLogService.log(SERVICE_REF, 99, TEST_MESSAGE);
    }

    /**
     * Test method for {@link org.eclipse.virgo.medic.log.osgi.OSGiLogServiceImpl#log(org.osgi.framework.ServiceReference, int, java.lang.String, java.lang.Throwable)}.
     */
    @Test
    public void testInvalidLogServiceReferenceThrowable() {
        this.logService.error(INVALID_PREFIX + SERVICE_PREFIX + TEST_MESSAGE, FAIL);
        expectLastCall().once();
        replay(this.logService);
        this.osgiLogService.log(SERVICE_REF, 99, TEST_MESSAGE, FAIL);
    }

}
