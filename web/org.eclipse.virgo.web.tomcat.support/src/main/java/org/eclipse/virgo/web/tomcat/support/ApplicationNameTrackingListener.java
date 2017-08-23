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

package org.eclipse.virgo.web.tomcat.support;

import java.util.Set;

import org.apache.catalina.Container;
import org.apache.catalina.ContainerEvent;
import org.apache.catalina.ContainerListener;
import org.apache.catalina.Context;
import org.apache.catalina.Host;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;

import org.eclipse.virgo.util.math.Sets;

public final class ApplicationNameTrackingListener implements ContainerListener, LifecycleListener {

    private static final Set<String> BEFORE_EVENTS = Sets.asSet("beforeSessionCreated", "beforeSessionDestroyed", "beforeSessionAttributeRemoved",
        "beforeSessionAttributeReplaced", "beforeSessionAttributeAdded");

    private static final Set<String> AFTER_EVENTS = Sets.asSet("afterSessionCreated", "afterSessionDestroyed", "afterSessionAttributeRemoved",
        "afterSessionAttributeReplaced", "afterSessionAttributeAdded");

    private final ApplicationNameTrackingDelegate delegate = ApplicationNameTrackingDelegate.getInstance();

    public void containerEvent(ContainerEvent event) {
        Container container = event.getContainer();
        String type = event.getType();
        if (container instanceof Host) {
            handleHostContainerEvents(event, type);
        } else if (container instanceof Context) {
            handleContextContainerEvents(container, type);

        }
    }

    private void handleHostContainerEvents(ContainerEvent event, String type) {
        Object data = event.getData();
        if (data instanceof Context) {
            Context child = (Context) data;
            if (Container.ADD_CHILD_EVENT.equals(type)) {
                child.addContainerListener(this);
            } else if (Container.REMOVE_CHILD_EVENT.equals(type)) {
                child.removeContainerListener(this);
            }

        }
    }

    private void handleContextContainerEvents(Container container, String type) {
        if (BEFORE_EVENTS.contains(type)) {
            Context context = (Context) container;
            this.delegate.setApplicationNameForContextPath(context.getPath());
        } else if (AFTER_EVENTS.contains(type)) {
            this.delegate.clearName();
        }
    }

    public void lifecycleEvent(LifecycleEvent event) {
        Object source = event.getSource();
        if (source instanceof Host) {
            Host host = (Host) source;
            if (Lifecycle.BEFORE_START_EVENT.equals(event.getType())) {
                host.addContainerListener(this);
            } else if (Lifecycle.AFTER_STOP_EVENT.equals(event.getType())) {
                host.removeContainerListener(this);
            }
        }
    }
}
