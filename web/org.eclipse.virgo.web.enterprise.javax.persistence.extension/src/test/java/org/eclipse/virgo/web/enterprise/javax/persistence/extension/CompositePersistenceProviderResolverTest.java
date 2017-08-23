/*******************************************************************************
 * Copyright (c) 2013 SAP AG
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   SAP AG - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.web.enterprise.javax.persistence.extension;

import java.io.File;
import java.io.PrintWriter;
import java.util.List;

import javax.persistence.spi.PersistenceProvider;

import org.easymock.EasyMock;
import org.eclipse.persistence.javax.persistence.osgi.OSGiProviderResolver;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;

import org.eclipse.virgo.web.enterprise.javax.persistence.extension.CompositePersistenceProviderResolver;

public class CompositePersistenceProviderResolverTest {
	
	@Before
	public void prepare() throws Exception {
		clean();
		prepareServicesFile();
	}
	
	@Test
	public void testGetPersistenceProviders() throws Exception {
		BundleContext context = EasyMock.createMock(BundleContext.class);
		CompositePersistenceProviderResolver compositePersistenceProviderResolver = new CompositePersistenceProviderResolver(context);
		List<PersistenceProvider> persistenceProviders = compositePersistenceProviderResolver.getPersistenceProviders();
		Assert.assertEquals("Found Persistence Providers are not the correct number", 2, persistenceProviders.size());
		Assert.assertEquals("First provider not OSGi Persistence Provider", persistenceProviders.get(0).getClass(),  OSGiProviderResolver.class);
		Assert.assertEquals("Second provider not Test Persistence Provider", persistenceProviders.get(1).getClass(),  TestPersistenceProvider.class);
	}
	
	@Test
	public void testGetPersistenceProvidersNoOSGiProvider() throws Exception {
		BundleContext context = EasyMock.createMock(BundleContext.class);
		CompositePersistenceProviderResolver compositePersistenceProviderResolver = new CompositePersistenceProviderResolver(context);
		compositePersistenceProviderResolver.removeOsgiProviderResolver();
		List<PersistenceProvider> persistenceProviders = compositePersistenceProviderResolver.getPersistenceProviders();
		Assert.assertEquals("Found Persistence Providers are not the correct number", 1, persistenceProviders.size());
		Assert.assertEquals("Provider not Test Persistence Provider", persistenceProviders.get(0).getClass(),  TestPersistenceProvider.class);
	}
	
	private void prepareServicesFile() throws Exception {
		new File("./build/classes/test/META-INF").mkdir();
		File servicesDir = new File("./build/classes/test/META-INF/services");
		servicesDir.mkdir();
		
		File persistenceProviderFile = new File("./build/classes/test/META-INF/services/javax.persistence.spi.PersistenceProvider");
		
		PrintWriter writer = null; 
		try {
			writer = new PrintWriter(persistenceProviderFile);
			writer.println("org.eclipse.virgo.web.enterprise.javax.persistence.extension.TestPersistenceProvider");
			writer.flush();
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (Exception e) {
					// nothing
				}
			}
			
		}	
	}
	
	@After
	public void clean() {
		File persistenceProviderFile = new File("./build/classes/test/META-INF/services/javax.persistence.spi.PersistenceProvider");
		if (persistenceProviderFile.exists()) {
			persistenceProviderFile.delete();
		}
		
		File servicesDir = new File("./build/classes/test/META-INF/services");
		if (servicesDir.exists()) {
			servicesDir.delete();
		}
		
		File metaInfDir = new File("./build/classes/test/META-INF");
		if (metaInfDir.exists()) {
			metaInfDir.delete();
		}
	}
}
