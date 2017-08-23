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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceProviderResolver;

import org.eclipse.persistence.javax.persistence.osgi.OSGiProviderResolver;
import org.osgi.framework.BundleContext;

/**
 * This is a composite persistence provider resolver, which implements the support of both OSGi and META-INF/services
 * lookup of persistence providers. For this it delegates the searches to two other provider resolvers, each implementing
 * one of the two search mechanisms.
 * 
 * @author I043832
 *
 */
public class CompositePersistenceProviderResolver implements
		PersistenceProviderResolver {

	private PersistenceProviderResolver osgiPersistenceProviderResolver;
	private PersistenceProviderResolver defaultPersistenceProviderResolver;
	
	private static final Logger logger = Logger.getLogger("com.sap.core.services.accessor.javax.persistence.extension.CompositePersistenceProviderResolver");

	public CompositePersistenceProviderResolver(BundleContext context) {
		osgiPersistenceProviderResolver = new OSGiProviderResolver(context);
		defaultPersistenceProviderResolver = new DefaultPersistenceProviderResolver();
		log(Level.FINEST, "Persistence providers created");
	}

	@Override
	public List<PersistenceProvider> getPersistenceProviders() {
		
		List<PersistenceProvider> defaultPersistenceProviders = defaultPersistenceProviderResolver
				.getPersistenceProviders();

		List<PersistenceProvider> persistenceProviders = new ArrayList<PersistenceProvider>();

		if (osgiPersistenceProviderResolver != null) {
			List<PersistenceProvider> osgiPersistenceProviders = osgiPersistenceProviderResolver
					.getPersistenceProviders();
			for (PersistenceProvider provider : osgiPersistenceProviders) {
				persistenceProviders.add(provider);
			}
		} else {
			log(Level.FINEST, "Osgi Persistence Provider Resolver does not exist, getting persistence providers only from the default resolver");
		}

		for (PersistenceProvider provider : defaultPersistenceProviders) {
			persistenceProviders.add(provider);
		}
		
		log(Level.FINEST, "Found " + persistenceProviders.size() + " PersistenceProviders");
		return persistenceProviders;
	}

	@Override
	public void clearCachedProviders() {
		osgiPersistenceProviderResolver.clearCachedProviders();
		defaultPersistenceProviderResolver.clearCachedProviders();
		log(Level.FINEST, "Cleared providers cache");
	}
	
	public void removeOsgiProviderResolver() {
		osgiPersistenceProviderResolver = null;
		log(Level.FINEST, "Removed OSGi Persistence Provider Resolver");
	}
	
	private void log(Level level, String message) {
		if(logger.isLoggable(level)) {
			logger.log(level, message);
		}
	}

}
