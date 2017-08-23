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
import org.eclipse.virgo.nano.shim.serviceability.TracingService;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.jmx.export.MBeanExporter;
import org.springframework.jmx.export.annotation.AnnotationMBeanExporter;


final class MBeanExporterPostProcessor implements ModuleBeanFactoryPostProcessor {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final TracingService tracingService;

    public MBeanExporterPostProcessor(TracingService tracingService) {
        this.tracingService = tracingService;
    }

    /**
     * {@inheritDoc}
     */
    public void postProcess(BundleContext bundleContext, ConfigurableListableBeanFactory beanFactory) {
        boolean foundExporter = false;
        String[] beanDefinitionNames = beanFactory.getBeanDefinitionNames();
        for (String beanDefinitionName : beanDefinitionNames) {
            AbstractBeanDefinition definition = (AbstractBeanDefinition) beanFactory.getBeanDefinition(beanDefinitionName);
            if (MBeanExporter.class.getName().equals(definition.getBeanClassName())) {
                definition.setBeanClass(KernelMBeanExporter.class);
                definition.getConstructorArgumentValues().addGenericArgumentValue(this.tracingService);
                foundExporter = true;
            } else if (AnnotationMBeanExporter.class.getName().equals(definition.getBeanClassName())) {
                definition.setBeanClass(KernelAnnotationMBeanExporter.class);
                definition.getConstructorArgumentValues().addGenericArgumentValue(this.tracingService);
                foundExporter = true;
            }
        }
        if (foundExporter) {
            logger.info("Found MBean exporter bean for bundle '{}'. Switching to ServerMBeanExporter or ServerAnnotationMBeanExporter.",
                bundleContext.getBundle());
        } else {
            logger.info("MBean exporting not enabled for bundle '{}',", bundleContext.getBundle());
        }
    }

}
