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

import static org.springframework.context.ConfigurableApplicationContext.LOAD_TIME_WEAVER_BEAN_NAME;

import org.eclipse.virgo.kernel.dmfragment.ModuleBeanFactoryPostProcessor;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;


final class LoadTimeWeaverPostProcessor implements ModuleBeanFactoryPostProcessor {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public void postProcess(BundleContext bundleContext, ConfigurableListableBeanFactory beanFactory) {
        if (beanFactory.containsBean(LOAD_TIME_WEAVER_BEAN_NAME)) {
            AbstractBeanDefinition ltwBean = (AbstractBeanDefinition) beanFactory.getBeanDefinition(LOAD_TIME_WEAVER_BEAN_NAME);
            ltwBean.setBeanClass(KernelLoadTimeWeaver.class);
            logger.info("Found load-time weaver bean for bundle '{}'. Switching to ServerLoadTimeWeaver.", bundleContext.getBundle());
        } else {
            logger.info("Load-time weaving not enabled for bundle '{}',", bundleContext.getBundle());
        }
    }

}
