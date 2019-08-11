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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DefaultPersistenceProviderResolverTest {

	private static final String TEST_CLASSES = "./build/classes/java/test";

	@Before
	public void prepare() {
		clean();
	}
	
	@Test
	public void testGetPersistenceProvidersPositive() throws Exception {
		prepareServicesFile(new String[] {"org.eclipse.virgo.web.enterprise.javax.persistence.extension.TestPersistenceProvider"});
		DefaultPersistenceProviderResolver resolver = new DefaultPersistenceProviderResolver();
		List<PersistenceProvider> persistenceProviders = resolver.getPersistenceProviders();
		Assert.assertEquals("Found Persistence Providers are not the correct number", 1, persistenceProviders.size());
		Assert.assertEquals("Provider not Test Persistence Provider", persistenceProviders.get(0).getClass(),  TestPersistenceProvider.class);
	}
	
	@Test
	public void testGetPersistenceProvidersNoPersistenceProviderDefined() {
		DefaultPersistenceProviderResolver resolver = new DefaultPersistenceProviderResolver();
		List<PersistenceProvider> persistenceProviders = resolver.getPersistenceProviders();
		Assert.assertEquals("No Persistence Providers should be found", 0, persistenceProviders.size());
	}
	
	@Test
	public void testGetPersistenceProvidersNonexistentPersistenceProviderDefined() throws Exception {
		prepareServicesFile(new String[] {"com.sap.core.non.existent.PersistenceProvider"});
		DefaultPersistenceProviderResolver resolver = new DefaultPersistenceProviderResolver();
		List<PersistenceProvider> persistenceProviders = resolver.getPersistenceProviders();
		Assert.assertEquals("No Persistence Providers should be found", 0, persistenceProviders.size());
	}
	
	private void prepareServicesFile(String[] persistenceProviderNames) throws Exception {
		new File(TEST_CLASSES + "/META-INF").mkdir();
		File servicesDir = new File(TEST_CLASSES + "/META-INF/services");
		servicesDir.mkdir();
		
		File persistenceProviderFile = new File(TEST_CLASSES + "/META-INF/services/javax.persistence.spi.PersistenceProvider");

		try (PrintWriter writer = new PrintWriter(persistenceProviderFile)) {
			for (String persistenceProviderName : persistenceProviderNames) {
				writer.println(persistenceProviderName);
			}
			writer.flush();
		}
	}

	@After
	public void clean() {
		File persistenceProviderFile = new File(TEST_CLASSES + "/META-INF/services/javax.persistence.spi.PersistenceProvider");
		if (persistenceProviderFile.exists()) {
			persistenceProviderFile.delete();
		}

		File servicesDir = new File(TEST_CLASSES + "/META-INF/services");
		if (servicesDir.exists()) {
			servicesDir.delete();
		}

		File metaInfDir = new File(TEST_CLASSES + "/META-INF");
		if (metaInfDir.exists()) {
			metaInfDir.delete();
		}
	}
}
