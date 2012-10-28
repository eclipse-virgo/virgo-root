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

import javax.management.ListenerNotFoundException;
import javax.management.MBeanNotificationInfo;
import javax.management.Notification;
import javax.management.NotificationBroadcaster;
import javax.management.NotificationBroadcasterSupport;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;

import org.eclipse.virgo.nano.deployer.api.RecoveryMonitor;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;


/**
 * Monitors the process of the deployer recovery process.
 * <p/>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe.
 * 
 */
public class StandardRecoveryMonitor implements NotificationBroadcaster, RecoveryMonitor, EventHandler {
    
    private static final String TOPIC_RECOVERY_COMPLETED = "org/eclipse/virgo/kernel/deployer/recovery/COMPLETED";

	private final NotificationBroadcasterSupport broadcasterSupport = new NotificationBroadcasterSupport();

	private final Object monitor = new Object();

	private boolean recoveryComplete = false;

	/**
	 * {@inheritDoc}
	 */
	public boolean isRecoveryComplete() {
		synchronized (this.monitor) {
			return this.recoveryComplete;
		}
	}

	private void signalRecoveryComplete() {
	    boolean sendNotification = false;
	    
		synchronized (this.monitor) {
			if (!this.recoveryComplete) {
				this.recoveryComplete = true;
				sendNotification = true;
			}
		} 
		
		if (sendNotification) { 
			this.broadcasterSupport.sendNotification(new Notification(NOTIFICATION_TYPE, this, 1));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void addNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback) throws IllegalArgumentException {
		this.broadcasterSupport.addNotificationListener(listener, filter, handback);
	}

	/**
	 * {@inheritDoc}
	 */
	public MBeanNotificationInfo[] getNotificationInfo() {
		return new MBeanNotificationInfo[] { new MBeanNotificationInfo(new String[] { NOTIFICATION_TYPE }, Notification.class.getName(),
		        "Deployer Recovery Complete Notification.") };
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeNotificationListener(NotificationListener listener) throws ListenerNotFoundException {
		this.broadcasterSupport.removeNotificationListener(listener);
	}

    /** 
     * {@inheritDoc}
     */
    public void handleEvent(Event event) {
        if (TOPIC_RECOVERY_COMPLETED.equals(event.getTopic())) {
            signalRecoveryComplete();
        }        
    }
}
