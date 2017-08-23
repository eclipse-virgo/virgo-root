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

package scoping;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.concurrent.atomic.AtomicInteger;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;

public class Listener implements ListenerMBean, ServiceListener {

    private final AtomicInteger counter = new AtomicInteger();

    private final BundleContext context;

    public Listener(BundleContext context) {
        this.context = context;
    }

    public int getCount() {
        return this.counter.get();
    }

    public void registerService() {
        Dictionary<String, String> properties = new Hashtable<String, String>();
        properties.put("test-case", "app-listener");
        this.context.registerService(CharSequence.class.getName(), "test", properties);
    }

    public void serviceChanged(ServiceEvent event) {
        if (event.getType() == ServiceEvent.REGISTERED) {
            this.counter.incrementAndGet();
        }
    }

}
