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

package org.eclipse.virgo.web.enterprise.javax.persistence.provider.initializer;

import java.util.List;

import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceProviderResolver;
import javax.persistence.spi.PersistenceProviderResolverHolder;

import org.easymock.EasyMock;
import org.junit.Test;
import org.junit.Assert;
import org.osgi.framework.BundleContext;

import org.eclipse.virgo.web.enterprise.javax.persistence.extension.CompositePersistenceProviderResolver;

public class ActivatorTest {
	@Test
	public void testStart() throws Exception {
		BundleContext context = EasyMock.createMock(BundleContext.class);
		Activator activator = new Activator();
		activator.start(context);
		PersistenceProviderResolver providerResolver = PersistenceProviderResolverHolder.getPersistenceProviderResolver();
		Assert.assertNotNull("PersistenceProviderResolver not set", providerResolver);
		Assert.assertTrue("PersistenceProviderResolver not of the correct type. Expected com.sap.core.services.accessor.javax.persistence.extension.CompositePersistenceProviderResolver, but actually is [" + providerResolver.getClass().getName() + "]", (providerResolver instanceof CompositePersistenceProviderResolver));
	}
	
	@Test
	public void testStop() throws Exception {
		BundleContext context = EasyMock.createMock(BundleContext.class);
		Activator activator = new Activator();
		activator.start(context);
		PersistenceProviderResolver providerResolver = PersistenceProviderResolverHolder.getPersistenceProviderResolver();
		Assert.assertNotNull("PersistenceProviderResolver not set", providerResolver);
		Assert.assertTrue("PersistenceProviderResolver not of the correct type. Expected com.sap.core.services.accessor.javax.persistence.extension.CompositePersistenceProviderResolver, but actually is [" + providerResolver.getClass().getName() + "]", (providerResolver instanceof CompositePersistenceProviderResolver));
		activator.stop(context);
		List<PersistenceProvider> providers = providerResolver.getPersistenceProviders();
		Assert.assertEquals("There should be no persistence providers", 0, providers.size());
	}	
}
