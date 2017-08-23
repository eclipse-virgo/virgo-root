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

package org.eclipse.virgo.web.tomcat.support;

import java.lang.instrument.ClassFileTransformer;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.osgi.framework.Bundle;

import org.eclipse.virgo.kernel.osgi.framework.InstrumentableClassLoader;
import org.eclipse.virgo.kernel.osgi.framework.OsgiFramework;
import org.eclipse.gemini.web.tomcat.spi.ClassLoaderCustomizer;

final class ServerClassLoaderCustomizer implements ClassLoaderCustomizer {

    private final OsgiFramework framework;

    public ServerClassLoaderCustomizer(OsgiFramework framework) {
		this.framework = framework;
	}

	public ClassLoader[] extendClassLoaderChain(Bundle bundle) {
	    final Bundle[] dependencies = this.framework.getDirectDependencies(bundle);
	    
	    if (dependencies.length > 0) {
	        return AccessController.doPrivileged(new PrivilegedAction<ClassLoader[]>() {
                public ClassLoader[] run() {
                    return new ClassLoader[] {new FindResourceDelegatingClassLoader(dependencies)};                    
                }	            
            });
	    } else {
	        return new ClassLoader[0];
	    }
    }

    public void addClassFileTransformer(ClassFileTransformer transformer, Bundle bundle) {
        ClassLoader bundleClassLoader = this.framework.getBundleClassLoader(bundle);
        if (bundleClassLoader instanceof InstrumentableClassLoader) {
            ((InstrumentableClassLoader) bundleClassLoader).addClassFileTransformer(transformer);
        }
    }

    public ClassLoader createThrowawayClassLoader(Bundle bundle) {
        ClassLoader bundleClassLoader = this.framework.getBundleClassLoader(bundle);
        if (bundleClassLoader instanceof InstrumentableClassLoader) {
            return ((InstrumentableClassLoader) bundleClassLoader).createThrowAway();
        } else {
            return null;
        }
    }
}
