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

package org.eclipse.virgo.kernel.userregion.internal.equinox;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;
import java.util.Enumeration;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

import org.eclipse.virgo.kernel.osgi.framework.UnableToSatisfyDependenciesException;

/**
 *
 */
public class KernelBundleClassLoaderTests extends AbstractOsgiFrameworkLaunchingTests {

    private Bundle dependant;
    
    @Override
    protected String getRepositoryConfigDirectory() {
        return new File("src/test/resources/config/KernelBundleClassLoaderTests").getAbsolutePath();
    }

	/**
	 * Test method for {@link org.eclipse.virgo.kernel.userregion.internal.equinox.KernelBundleClassLoader#getResources(java.lang.String)}.
	 * @throws UnableToSatisfyDependenciesException 
	 * @throws Exception 
	 */
	@Test
	public void testGetResourcesStringFromBundle() throws Exception {
        Enumeration<URL> resources = this.dependant.getResources("/META-INF/GET_ME");
        
        assertNotNull(resources);
        assertTrue(resources.hasMoreElements());
        assertTrue(resources.nextElement().getPath().endsWith("bundlefile!/META-INF/GET_ME"));
        
	}

	/**
	 * Test method for {@link org.eclipse.virgo.kernel.userregion.internal.equinox.KernelBundleClassLoader#getResource(java.lang.String)}.
	 * @throws UnableToSatisfyDependenciesException 
	 * @throws Exception 
	 */
	@Test
	public void testGetResourceStringFromBundle() throws Exception {
        URL resource = this.dependant.getResource("/META-INF/GET_ME");

        assertNotNull(resource);
        assertTrue(resource.getPath().endsWith("bundlefile!/META-INF/GET_ME"));
	}

	/**
	 * Test method for {@link org.eclipse.virgo.kernel.userregion.internal.equinox.KernelBundleClassLoader#getResources(java.lang.String)}.
	 * @throws UnableToSatisfyDependenciesException 
	 * @throws Exception 
	 */
	@Test
	public void testGetResourcesStringFromBundleClassLoader() throws Exception {
        Bundle bundle = this.dependant;
        ClassLoader loader = this.framework.getBundleClassLoader(bundle);
        Enumeration<URL> resources = loader.getResources("/META-INF/GET_ME");
        
        assertNotNull(resources);
        assertTrue(resources.hasMoreElements());
        assertTrue(resources.nextElement().getPath().endsWith("bundlefile!/META-INF/GET_ME"));
        
	}

	/**
	 * Test method for {@link org.eclipse.virgo.kernel.userregion.internal.equinox.KernelBundleClassLoader#getResource(java.lang.String)}.
	 * @throws UnableToSatisfyDependenciesException 
	 * @throws Exception 
	 */
	@Test
	public void testGetResourceStringFromBundleClassLoader() throws Exception {
        ClassLoader loader = this.framework.getBundleClassLoader(this.dependant);
        URL resource = loader.getResource("/META-INF/GET_ME");

        assertNotNull(resource);
        assertTrue(resource.getPath().endsWith("bundlefile!/META-INF/GET_ME"));
	}
	

	@Before
	public void installDependant() throws BundleException {
	    this.dependant = this.framework.getBundleContext().installBundle(new File("src/test/resources/KernelBundleClassLoaderTests/dependant.jar").toURI().toString());
	}
	
	@After
	public void uninstallDependant() throws BundleException {
	    this.dependant.uninstall();
	}
}
