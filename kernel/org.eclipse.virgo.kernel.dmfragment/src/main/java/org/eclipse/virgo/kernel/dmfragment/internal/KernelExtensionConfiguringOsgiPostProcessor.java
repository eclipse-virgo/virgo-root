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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.virgo.kernel.dmfragment.ModuleBeanFactoryPostProcessor;
import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.eclipse.gemini.blueprint.extender.OsgiBeanFactoryPostProcessor;


/**
 * {@link OsgiBeanFactoryPostProcessor} implementation that plugs in Server extensions implementation when needed.<p/>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe.
 * 
 */
final class KernelExtensionConfiguringOsgiPostProcessor implements OsgiBeanFactoryPostProcessor {

    private final List<ModuleBeanFactoryPostProcessor> postProcessors = new CopyOnWriteArrayList<ModuleBeanFactoryPostProcessor>();

    KernelExtensionConfiguringOsgiPostProcessor(List<ModuleBeanFactoryPostProcessor> defaultPostProcessors) {
        this.postProcessors.addAll(defaultPostProcessors);

    }

    /**
     * {@inheritDoc}
     */
    public void postProcessBeanFactory(BundleContext bundleContext, ConfigurableListableBeanFactory beanFactory) {
        for (ModuleBeanFactoryPostProcessor postProcessor : this.postProcessors) {
            postProcessor.postProcess(bundleContext, beanFactory);
        }
    }

}
