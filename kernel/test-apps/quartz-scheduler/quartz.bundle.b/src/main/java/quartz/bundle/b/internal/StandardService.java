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

package quartz.bundle.b.internal;

import java.util.concurrent.atomic.AtomicInteger;

import org.quartz.Scheduler;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import quartz.bundle.b.Service;

/**
 */
public class StandardService implements Service, ApplicationContextAware {

    private final AtomicInteger count = new AtomicInteger(0);

    private ApplicationContext applicationContext;

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public int getCount() {
        return this.count.get();
    }

    public Scheduler getScheduler() {
        return (Scheduler) this.applicationContext.getBean("schedulerFactoryBean");
    }

    public void process() {
        System.err.println(getClass().getName() + " called " + this.count.incrementAndGet() + " times.");
    }

}
