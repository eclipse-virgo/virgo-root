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

import org.eclipse.virgo.kernel.dmfragment.ModuleBeanFactoryPostProcessor;
import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;



final class ApplicationContextShutdownBeanPostProcessor implements ModuleBeanFactoryPostProcessor{

    private static final String APPLICATION_CONTEXT_SHUTDOWN_BEAN_NAME = "org.eclipse.virgo.server.applicationContextShutdownBean";
    
    /** 
     * {@inheritDoc}
     */
    public void postProcess(BundleContext bundleContext, ConfigurableListableBeanFactory beanFactory) {
        beanFactory.registerSingleton(APPLICATION_CONTEXT_SHUTDOWN_BEAN_NAME, new ApplicationContextShutdownBean());
    }

}
