/*******************************************************************************
 * Copyright (c) 2011 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.kernel.dmfragment.internal;

import org.osgi.framework.BundleContext;
import org.eclipse.gemini.blueprint.context.DelegatedExecutionOsgiBundleApplicationContext;
import org.eclipse.gemini.blueprint.context.support.OsgiBundleXmlApplicationContext;
import org.eclipse.gemini.blueprint.extender.support.DefaultOsgiApplicationContextCreator;

/**
 * {@link KernelOsgiApplicationContextCreator} creates user region application contexts.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * Thread safe.
 */
final class KernelOsgiApplicationContextCreator extends DefaultOsgiApplicationContextCreator {

    /** 
     * {@inheritDoc}
     */
    @Override
    public DelegatedExecutionOsgiBundleApplicationContext createApplicationContext(BundleContext bundleContext) throws Exception {
        DelegatedExecutionOsgiBundleApplicationContext applicationContext = super.createApplicationContext(bundleContext);
        if (applicationContext instanceof OsgiBundleXmlApplicationContext) {
            OsgiBundleXmlApplicationContext osgiBundleXmlApplicationContext = (OsgiBundleXmlApplicationContext) applicationContext;
            osgiBundleXmlApplicationContext.setContextClassLoaderProvider(new KernelOsgiContextClassLoaderProvider(osgiBundleXmlApplicationContext.getBundle()));
        }
        return applicationContext;
    }

}
