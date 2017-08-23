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

package org.eclipse.virgo.kernel.dmfragment.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.eclipse.gemini.blueprint.service.importer.support.OsgiServiceProxyFactoryBean;

/**
 * {@link ApplicationContextShutdownBean} manages the shutting down of application contexts in the dm Server. In
 * particular it is responsible for ensuring that Spring DM service proxies do not hold up application context shutdown.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is thread safe.
 * 
 */
// Note that this class must not implement ApplicationListener<ContextClosedEvent> since it needs to work with Spring
// 2.5.x as well as Spring 3 and beyond.
final class ApplicationContextShutdownBean implements ApplicationListener<ApplicationEvent> {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationContextShutdownBean.class);

    /**
     * {@inheritDoc}
     */
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ContextClosedEvent) {
            logger.info("Processing ContextClosedEvent '{}'", event);
            ApplicationContext applicationContext = ((ApplicationContextEvent) event).getApplicationContext();
            disableServiceProxyRetry(applicationContext);
        }
    }

    static void disableServiceProxyRetry(ApplicationContext applicationContext) {
        for (OsgiServiceProxyFactoryBean proxyBean : BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext,
            OsgiServiceProxyFactoryBean.class, true, false).values()) {
            logger.info("Setting timeout to 0 for proxy '{}' of application context '{}'", proxyBean, applicationContext);
            proxyBean.setTimeout(0);
        }
    }
}
