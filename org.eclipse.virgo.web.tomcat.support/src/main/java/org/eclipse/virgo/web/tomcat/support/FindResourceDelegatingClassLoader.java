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

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Vector;

import org.osgi.framework.Bundle;

/**
 * A {@link ClassLoader} that delegates resource requests to one or more
 * {@link Bundle Bundles}.
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * Thread-safe.
 *
 */
final class FindResourceDelegatingClassLoader extends ClassLoader {
    
    private final Bundle[] delegates;
    
    /**
     * @param delegates
     */
    FindResourceDelegatingClassLoader(Bundle[] delegates) {
    	super(null);
        this.delegates = delegates;
    }    

    @Override
    protected URL findResource(String name) {
        for (Bundle delegate : delegates) {
            try {
                URL resource = delegate.getResource(name);
                if (resource != null) {
                    return resource;
                }
            } catch (IllegalStateException ignored) {
            }
        }
        return null;
    }

    @Override
    protected Enumeration<URL> findResources(String name) throws IOException {
        Vector<URL> resources = new Vector<URL>();
        
        for (Bundle delegate : delegates) {
            try {
                Enumeration<URL> urls = delegate.getResources(name);
                if (urls != null) {
                    while (urls.hasMoreElements()) {
                        resources.add(urls.nextElement());
                    }
                }                
            } catch (IllegalStateException ignored) {
            }
        }
        
        return resources.elements();
    }        
}
