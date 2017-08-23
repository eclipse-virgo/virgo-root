/*******************************************************************************
 * Copyright (c) 2012 SAP AG
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   SAP AG - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.web.enterprise.services.accessor;

import java.lang.instrument.ClassFileTransformer;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.apache.tomcat.JarScanner;
import org.eclipse.gemini.web.tomcat.spi.ClassLoaderCustomizer;
import org.eclipse.gemini.web.tomcat.spi.JarScannerCustomizer;
import org.osgi.framework.Bundle;
import org.osgi.service.component.ComponentContext;
import org.osgi.util.tracker.BundleTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.virgo.kernel.equinox.extensions.hooks.PluggableDelegatingClassLoaderDelegateHook;

public class WebAppBundleClassloaderCustomizer implements ClassLoaderCustomizer, JarScannerCustomizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebAppBundleClassloaderCustomizer.class);

    private final WebAppBundleClassLoaderDelegateHook wabClassLoaderDelegateHook = new WebAppBundleClassLoaderDelegateHook();

    private BundleTracker<String> bundleTracker;

    private final WebAppBundleTrackerCustomizer webAppBundleCustomizer = new WebAppBundleTrackerCustomizer(this.wabClassLoaderDelegateHook);

    @Override
    public void addClassFileTransformer(ClassFileTransformer cft, Bundle bundle) {
        // no-op
    }

    @Override
    public ClassLoader createThrowawayClassLoader(Bundle bundle) {
        // no-op
        return null;
    }

    @Override
    public ClassLoader[] extendClassLoaderChain(Bundle bundle) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Extending web application bundle " + bundle);
        }

        this.webAppBundleCustomizer.processAdditionalAPIBundles(this.bundleTracker.getBundles());

        // TODO when we will remove the bundle?
        this.wabClassLoaderDelegateHook.addWebAppBundle(bundle);

        return AccessController.doPrivileged(new PrivilegedAction<ClassLoader[]>() {

            @Override
            public ClassLoader[] run() {
                return WebAppBundleClassloaderCustomizer.this.wabClassLoaderDelegateHook.getImplBundlesClassloaders();
            }
        });
    }

    protected final void activate(ComponentContext componentContext) {
        this.bundleTracker = new BundleTracker<String>(componentContext.getBundleContext(), Bundle.RESOLVED | Bundle.STARTING | Bundle.ACTIVE,
            this.webAppBundleCustomizer);
        this.bundleTracker.open();

        PluggableDelegatingClassLoaderDelegateHook.getInstance().addDelegate(this.wabClassLoaderDelegateHook);
    }

    protected final void deactivate(ComponentContext componentContext) {
    	PluggableDelegatingClassLoaderDelegateHook.getInstance().removeDelegate(this.wabClassLoaderDelegateHook);

        this.bundleTracker.close();

    }
    
    @Override
	public JarScanner[] extendJarScannerChain(Bundle arg0) {
		return new JarScanner[] { new ClassLoaderJarScanner(webAppBundleCustomizer.getBundlesForJarScanner()) };
	}

    WebAppBundleClassLoaderDelegateHook getWebAppBundleClassLoaderDelegateHook() {
        return this.wabClassLoaderDelegateHook;
    }

    WebAppBundleTrackerCustomizer getWebAppBundleTrackerCustomizer() {
        return this.webAppBundleCustomizer;
    }
}
