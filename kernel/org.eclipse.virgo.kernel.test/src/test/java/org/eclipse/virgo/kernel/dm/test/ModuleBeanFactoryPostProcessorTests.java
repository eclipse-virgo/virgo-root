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

package org.eclipse.virgo.kernel.dm.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.eclipse.gemini.blueprint.context.support.OsgiBundleXmlApplicationContext;
import org.eclipse.gemini.blueprint.service.importer.support.ImportContextClassLoaderEnum;
import org.eclipse.gemini.blueprint.service.importer.support.OsgiServiceProxyFactoryBean;
import org.eclipse.virgo.nano.core.BundleStarter;
import org.eclipse.virgo.kernel.osgi.framework.OsgiFrameworkUtils;
import org.eclipse.virgo.kernel.osgi.framework.OsgiServiceHolder;
import org.eclipse.virgo.kernel.test.AbstractKernelIntegrationTest;
import org.eclipse.virgo.kernel.test.TestSignal;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

public class ModuleBeanFactoryPostProcessorTests extends AbstractKernelIntegrationTest {
    
    private OsgiServiceHolder<BundleStarter> bundleStarter;

    @Before
    public void before() {
        this.bundleStarter = OsgiFrameworkUtils.getService(kernelContext, BundleStarter.class);
    }
    
    @After
    public void after() {
        if(this.bundleStarter != null) {
            this.kernelContext.ungetService(this.bundleStarter.getServiceReference());
        }
    }

    @Ignore("Bug 546611") // TODO - Investigate why this tests failed after switch to bnd for generating metadata
    @Test
    public void testInbuiltPostProcessors() throws Exception {
       Bundle bundle = this.context.installBundle(new File("src/test/resources/post-processors/inbuilt").toURI().toString());
       TestSignal signal = new TestSignal();
       
       this.bundleStarter.getService().start(bundle, signal);
       
       signal.assertSuccessfulCompletionSignalled(5000);
       
       ServiceReference<?>[] serviceReferences = this.context.getServiceReferences(ApplicationContext.class.getName(), "(Bundle-SymbolicName=org.eclipse.virgo.kernel.dmfragment.test.inbuilt)");
       assertNotNull(serviceReferences);
       assertEquals(1, serviceReferences.length);
       
       OsgiBundleXmlApplicationContext applicationContext = (OsgiBundleXmlApplicationContext) this.context.getService(serviceReferences[0]);
       
       Object ltwBean = applicationContext.getBean(ConfigurableApplicationContext.LOAD_TIME_WEAVER_BEAN_NAME);
       assertNotNull(ltwBean);
       assertTrue(ltwBean.getClass().getName().startsWith("org.eclipse.virgo.kernel.dm")); // don't want direct class references because I want to avoid an import-package
       
       Object mbeanExporterBean = applicationContext.getBean("mbeanExporter");
       assertNotNull(mbeanExporterBean);
       assertTrue(mbeanExporterBean.getClass().getName().startsWith("org.eclipse.virgo.kernel.dm")); // don't want direct class references because I want to avoid an import-package
       
       OsgiServiceProxyFactoryBean referenceBean = (OsgiServiceProxyFactoryBean) applicationContext.getBean("&reference");
       assertNotNull(referenceBean);
       assertEquals(ImportContextClassLoaderEnum.UNMANAGED, referenceBean.getImportContextClassLoader());
    }
}
