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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import org.eclipse.virgo.test.stubs.framework.StubBundle;
import org.eclipse.virgo.test.stubs.framework.StubBundleContext;
import org.eclipse.virgo.test.stubs.framework.StubServiceReference;
import org.eclipse.virgo.test.stubs.framework.StubServiceRegistration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogService;
import org.slf4j.Logger;

/**
 * 
 *  OSGiLogServiceImplTests
 */
public class OSGiLogServiceListenerTests {

    private static final String TEST_MESSAGE = "Danger Will Robinson, Danger!!!";
    
    private static final StubBundle BUNDLE = new StubBundle("org.eclipse.virgo.not.here", Version.emptyVersion);
    
    private static final ServiceReference<Object> SERVICE_REF = new StubServiceReference<Object>(3l, 3, new StubServiceRegistration<Object>(new StubBundleContext(BUNDLE), "org.eclipse.virgo.not.here"));

    private static final String SERVICE_PREFIX = "Service 3, ";
    
    private static final String BUNDLE_PREFIX = "Bundle org.eclipse.virgo.not.here_0.0.0, ";
    
    private static final String INVALID_PREFIX = "Log Message of unknown severity 99: ";
    
    private static final Throwable FAIL = new Throwable("Got lost in space");
    
    private OSGiLogServiceListener osgiLogListener;

    private Logger logService;
    
    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        this.logService = createMock(Logger.class);
        this.osgiLogListener = new OSGiLogServiceListener(this.logService);
    }
    
    @After
    public void setDown() {
        verify(this.logService);
    }
    
    @Test
    public void testDebugLog() {
        this.logService.debug(TEST_MESSAGE);
        expectLastCall().once();
        replay(this.logService);
        this.osgiLogListener.logged(buildLogEntry(null, null, LogService.LOG_DEBUG, TEST_MESSAGE, null));
    }

    @Test
    public void testDebugLogThrowable() {
        this.logService.debug(TEST_MESSAGE, FAIL);
        expectLastCall().once();
        replay(this.logService);
        this.osgiLogListener.logged(buildLogEntry(null, null, LogService.LOG_DEBUG, TEST_MESSAGE, FAIL));
    }

    @Test
    public void testDebugLogServiceReference() {
        this.logService.debug(SERVICE_PREFIX +TEST_MESSAGE);
        expectLastCall().once();
        replay(this.logService);
        this.osgiLogListener.logged(buildLogEntry(SERVICE_REF, null, LogService.LOG_DEBUG, TEST_MESSAGE, null));
    }

    @Test
    public void testDebugLogBundle() {
        this.logService.debug(BUNDLE_PREFIX + TEST_MESSAGE);
        expectLastCall().once();
        replay(this.logService);
        this.osgiLogListener.logged(buildLogEntry(null, BUNDLE, LogService.LOG_DEBUG, TEST_MESSAGE, null));
    }

    @Test
    public void testDebugLogServiceReferenceThrowableBundle() {
        this.logService.debug(BUNDLE_PREFIX + SERVICE_PREFIX + TEST_MESSAGE, FAIL);
        expectLastCall().once();
        replay(this.logService);
        this.osgiLogListener.logged(buildLogEntry(SERVICE_REF, BUNDLE, LogService.LOG_DEBUG, TEST_MESSAGE, FAIL));
    }
    
    @Test
    public void testInfoLog() {
        this.logService.info(TEST_MESSAGE);
        expectLastCall().once();
        replay(this.logService);
        this.osgiLogListener.logged(buildLogEntry(null, null, LogService.LOG_INFO, TEST_MESSAGE, null));
    }

    @Test
    public void testInfoLogThrowable() {
        this.logService.info(TEST_MESSAGE, FAIL);
        expectLastCall().once();
        replay(this.logService);
        this.osgiLogListener.logged(buildLogEntry(null, null, LogService.LOG_INFO, TEST_MESSAGE, FAIL));
    }

    @Test
    public void testInfoLogServiceReference() {
        this.logService.info(SERVICE_PREFIX +TEST_MESSAGE);
        expectLastCall().once();
        replay(this.logService);
        this.osgiLogListener.logged(buildLogEntry(SERVICE_REF, null, LogService.LOG_INFO, TEST_MESSAGE, null));
    }

    @Test
    public void testInfoLogBundle() {
        this.logService.info(BUNDLE_PREFIX + TEST_MESSAGE);
        expectLastCall().once();
        replay(this.logService);
        this.osgiLogListener.logged(buildLogEntry(null, BUNDLE, LogService.LOG_INFO, TEST_MESSAGE, null));
    }

    @Test
    public void testInfoLogServiceReferenceThrowableBundle() {
        this.logService.info(BUNDLE_PREFIX + SERVICE_PREFIX + TEST_MESSAGE, FAIL);
        expectLastCall().once();
        replay(this.logService);
        this.osgiLogListener.logged(buildLogEntry(SERVICE_REF, BUNDLE, LogService.LOG_INFO, TEST_MESSAGE, FAIL));
    }

    @Test
    public void testWarningLog() {
        this.logService.warn(TEST_MESSAGE);
        expectLastCall().once();
        replay(this.logService);
        this.osgiLogListener.logged(buildLogEntry(null, null, LogService.LOG_WARNING, TEST_MESSAGE, null));
    }

    @Test
    public void testWarningLogThrowable() {
        this.logService.warn(TEST_MESSAGE, FAIL);
        expectLastCall().once();
        replay(this.logService);
        this.osgiLogListener.logged(buildLogEntry(null, null, LogService.LOG_WARNING, TEST_MESSAGE, FAIL));
    }

    @Test
    public void testWarningLogServiceReference() {
        this.logService.warn(SERVICE_PREFIX +TEST_MESSAGE);
        expectLastCall().once();
        replay(this.logService);
        this.osgiLogListener.logged(buildLogEntry(SERVICE_REF, null, LogService.LOG_WARNING, TEST_MESSAGE, null));
    }

    @Test
    public void testWarningLogBundle() {
        this.logService.warn(BUNDLE_PREFIX + TEST_MESSAGE);
        expectLastCall().once();
        replay(this.logService);
        this.osgiLogListener.logged(buildLogEntry(null, BUNDLE, LogService.LOG_WARNING, TEST_MESSAGE, null));
    }

    @Test
    public void testWarningLogServiceReferenceThrowableBundle() {
        this.logService.warn(BUNDLE_PREFIX + SERVICE_PREFIX + TEST_MESSAGE, FAIL);
        expectLastCall().once();
        replay(this.logService);
        this.osgiLogListener.logged(buildLogEntry(SERVICE_REF, BUNDLE, LogService.LOG_WARNING, TEST_MESSAGE, FAIL));
    }

    @Test
    public void testErrorLog() {
        this.logService.error(TEST_MESSAGE);
        expectLastCall().once();
        replay(this.logService);
        this.osgiLogListener.logged(buildLogEntry(null, null, LogService.LOG_ERROR, TEST_MESSAGE, null));
    }

    @Test
    public void testErrorLogThrowable() {
        this.logService.error(TEST_MESSAGE, FAIL);
        expectLastCall().once();
        replay(this.logService);
        this.osgiLogListener.logged(buildLogEntry(null, null, LogService.LOG_ERROR, TEST_MESSAGE, FAIL));
    }

    @Test
    public void testErrorLogServiceReference() {
        this.logService.error(SERVICE_PREFIX +TEST_MESSAGE);
        expectLastCall().once();
        replay(this.logService);
        this.osgiLogListener.logged(buildLogEntry(SERVICE_REF, null, LogService.LOG_ERROR, TEST_MESSAGE, null));
    }

    @Test
    public void testErrorLogBundle() {
        this.logService.error(BUNDLE_PREFIX + TEST_MESSAGE);
        expectLastCall().once();
        replay(this.logService);
        this.osgiLogListener.logged(buildLogEntry(null, BUNDLE, LogService.LOG_ERROR, TEST_MESSAGE, null));
    }

    @Test
    public void testErrorLogServiceReferenceThrowableBundle() {
        this.logService.error(BUNDLE_PREFIX + SERVICE_PREFIX + TEST_MESSAGE, FAIL);
        expectLastCall().once();
        replay(this.logService);
        this.osgiLogListener.logged(buildLogEntry(SERVICE_REF, BUNDLE, LogService.LOG_ERROR, TEST_MESSAGE, FAIL));
    }

    @Test
    public void testInvalidLog() {
        this.logService.error(INVALID_PREFIX + TEST_MESSAGE);
        expectLastCall().once();
        replay(this.logService);
        this.osgiLogListener.logged(buildLogEntry(null, null, 99, TEST_MESSAGE, null));
    }

    @Test
    public void testInvalidLogThrowable() {
        this.logService.error(INVALID_PREFIX + TEST_MESSAGE, FAIL);
        expectLastCall().once();
        replay(this.logService);
        this.osgiLogListener.logged(buildLogEntry(null, null, 99, TEST_MESSAGE, FAIL));
    }

    @Test
    public void testInvalidLogServiceReference() {
        this.logService.error(INVALID_PREFIX + SERVICE_PREFIX +TEST_MESSAGE);
        expectLastCall().once();
        replay(this.logService);
        this.osgiLogListener.logged(buildLogEntry(SERVICE_REF, null, 99, TEST_MESSAGE, null));
    }

    @Test
    public void testInvalidLogBundle() {
        this.logService.error(INVALID_PREFIX + BUNDLE_PREFIX + TEST_MESSAGE);
        expectLastCall().once();
        replay(this.logService);
        this.osgiLogListener.logged(buildLogEntry(null, BUNDLE, 99, TEST_MESSAGE, null));
    }

    @Test
    public void testInvalidLogServiceReferenceThrowableBundle() {
        this.logService.error(INVALID_PREFIX + BUNDLE_PREFIX + SERVICE_PREFIX + TEST_MESSAGE, FAIL);
        expectLastCall().once();
        replay(this.logService);
        this.osgiLogListener.logged(buildLogEntry(SERVICE_REF, BUNDLE, 99, TEST_MESSAGE, FAIL));
    }
    
    private LogEntry buildLogEntry(final ServiceReference<Object> ref, final Bundle bundle, final int level, final String message, final Throwable fail){
    	return new LogEntry() {
			
			@Override
			public long getTime() {
				return 0;
			}
			
			@Override
			public ServiceReference<?> getServiceReference() {
				return ref;
			}
			
			@Override
			public String getMessage() {
				return message;
			}
			
			@Override
			public int getLevel() {
				return level;
			}
			
			@Override
			public Throwable getException() {
				return fail;
			}
			
			@Override
			public Bundle getBundle() {
				return bundle;
			}
		};
    }

}
