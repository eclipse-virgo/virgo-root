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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.virgo.kernel.dmfragment.ModuleBeanFactoryPostProcessor;
import org.osgi.framework.BundleContext;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.eclipse.gemini.blueprint.extender.OsgiBeanFactoryPostProcessor;
import org.eclipse.gemini.blueprint.service.importer.support.ImportContextClassLoaderEnum;
import org.eclipse.gemini.blueprint.service.importer.support.OsgiServiceCollectionProxyFactoryBean;
import org.eclipse.gemini.blueprint.service.importer.support.OsgiServiceProxyFactoryBean;


/**
 * {@link OsgiBeanFactoryPostProcessor} that ensures that all service references are in unmanaged mode for thread
 * context class loader propagation.<p/>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe.
 * 
 */
final class ContextClassLoaderPostProcessor implements ModuleBeanFactoryPostProcessor {

    private static final String PROPERTY_CONTEXT_CLASS_LOADER = "importContextClassLoader";

    private static final Set<String> IMPORTER_CLASS_NAMES = createImportClassNames();

    /**
     * {@inheritDoc}
     */
    public void postProcess(BundleContext bundleContext, ConfigurableListableBeanFactory beanFactory) {
        String[] beanDefinitionNames = beanFactory.getBeanDefinitionNames();
        for (String name : beanDefinitionNames) {
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(name);
            if (isServiceImportDefinition(beanDefinition)) {
                MutablePropertyValues propertyValues = beanDefinition.getPropertyValues();
                propertyValues.addPropertyValue(PROPERTY_CONTEXT_CLASS_LOADER, ImportContextClassLoaderEnum.UNMANAGED);
            }
        }
    }

    private static Set<String> createImportClassNames() {
        Set<String> names = new HashSet<String>();
        names.add(OsgiServiceProxyFactoryBean.class.getName());
        names.add(OsgiServiceCollectionProxyFactoryBean.class.getName());
        return names;
    }

    private boolean isServiceImportDefinition(BeanDefinition beanDefinition) {
        return IMPORTER_CLASS_NAMES.contains(beanDefinition.getBeanClassName());
    }

}
