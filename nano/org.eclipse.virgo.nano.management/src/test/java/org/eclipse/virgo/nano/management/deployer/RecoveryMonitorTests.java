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

package org.eclipse.virgo.nano.management.deployer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.management.ManagementFactory;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.management.MBeanServer;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.eclipse.virgo.nano.management.deployer.StandardRecoveryMonitor;
import org.junit.Before;
import org.junit.Test;
import org.osgi.service.event.Event;

/**
 */
public class RecoveryMonitorTests {

    private ObjectName objectName;

    private final MBeanServer server = ManagementFactory.getPlatformMBeanServer();

    @Before public void before() throws Exception {
        this.objectName = ObjectName.getInstance("deployer:name=Recovery,env=Test");
    }

    public void after() throws Exception {
        if (this.server.isRegistered(this.objectName)) {
            this.server.unregisterMBean(this.objectName);
        }
    }

    @Test public void testRecoveryNotification() throws Exception {
        StandardRecoveryMonitor mbean = new StandardRecoveryMonitor();
        this.server.registerMBean(mbean, this.objectName);
        assertFalse(queryRecoveryComplete());
        final AtomicInteger counter = new AtomicInteger(0);
        this.server.addNotificationListener(this.objectName, new NotificationListener() {

            public void handleNotification(Notification notification, Object handback) {
                counter.incrementAndGet();
            }

        }, null, null);
        mbean.handleEvent(new Event("org/eclipse/virgo/kernel/deployer/recovery/COMPLETED", (Map<String, ?>)null));
        assertTrue(queryRecoveryComplete());
        assertEquals(1, counter.get());
    }

    private boolean queryRecoveryComplete() throws Exception {
        return (Boolean) this.server.getAttribute(this.objectName, "RecoveryComplete");
    }
}
