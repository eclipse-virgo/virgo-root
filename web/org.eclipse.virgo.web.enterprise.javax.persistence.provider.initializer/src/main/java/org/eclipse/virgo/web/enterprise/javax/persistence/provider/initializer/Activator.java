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

import javax.persistence.spi.PersistenceProviderResolverHolder;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;


import org.eclipse.virgo.web.enterprise.javax.persistence.extension.CompositePersistenceProviderResolver;

/**
 * This class initializes the PersistenceProviderResolver in javax.persistence; since both OSGi lookup of 
 * PersistenceProviders and lookup through META-INF/services mechanism should be supported, a custom provider
 * resolver is created, which delegates to two other provider resolvers, which support the two lookup mechanisms.
 * 
 * Since the javax.persistence bundle is not activated and therefore has no bundle context, the custom provider resolver
 * is initialized with the bundle context of the initializer bundle. This context will be used for OSGi lookup of
 * persistence providers.
 *
 */
public class Activator implements BundleActivator {
	
	private static final Logger logger = LoggerFactory.getLogger("com.sap.core.services.accessor.javax.persistence.provider.initializer.Activator");

	private CompositePersistenceProviderResolver providerResolver = null;
	
	public void start(BundleContext bundleContext) throws Exception {
		providerResolver = new CompositePersistenceProviderResolver(bundleContext);
		PersistenceProviderResolverHolder.setPersistenceProviderResolver(providerResolver);
		if (logger.isDebugEnabled()) {
			logger.debug("CompositePersistenceProviderResolver created");
		}
	}

	public void stop(BundleContext bundleContext) throws Exception {
		providerResolver.removeOsgiProviderResolver();
		if (logger.isDebugEnabled()) {
			logger.debug("Removed OSGiPersistenceProvider from CompositePersistenceProviderResolver created");
		}
	}

}
