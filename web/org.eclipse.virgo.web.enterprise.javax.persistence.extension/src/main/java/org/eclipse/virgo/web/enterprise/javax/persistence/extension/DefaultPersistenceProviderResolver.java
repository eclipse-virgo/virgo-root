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

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.PersistenceException;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceProviderResolver;

/**
 * Persistence provider resolver, which searches for persistence providers through META-INF/services mechanism.
 * 
 */
class DefaultPersistenceProviderResolver implements PersistenceProviderResolver {

	private volatile Map<ClassLoader, List<PersistenceProvider>> providers = new WeakHashMap<>();

	@Override
    public List<PersistenceProvider> getPersistenceProviders() {
        ClassLoader loader = getContextClassLoader();
        List<PersistenceProvider> loadedProviders = this.providers.get(loader);

        if (loadedProviders == null) {
            Collection<ProviderName> providerNames = getProviderNames(loader);
            loadedProviders = new ArrayList<PersistenceProvider>();

            for (ProviderName providerName : providerNames) {
                try {
                    PersistenceProvider provider = (PersistenceProvider) loader.loadClass(providerName.getName()).newInstance();
                    loadedProviders.add(provider);
                } catch (ClassNotFoundException cnfe) {
                    log(Level.FINEST, cnfe + ": " + providerName);
                } catch (InstantiationException ie) {
                    log(Level.FINEST, ie + ": " + providerName);
                } catch (IllegalAccessException iae) {
                    log(Level.FINEST, iae + ": " + providerName);
                } catch (ClassCastException cce) {
                    log(Level.FINEST, cce + ": " + providerName);
                }
            }

            // If none are found we'll log the provider names for diagnostic
            // purposes.
            if (loadedProviders.isEmpty() && !providerNames.isEmpty()) {
                log(Level.WARNING, "No valid providers found using:");
                for (ProviderName name : providerNames) {
                    log(Level.WARNING, name.toString());
                }
            }

            this.providers.put(loader, loadedProviders);
        }

        return loadedProviders;
    }

    /**
     * Wraps <code>Thread.currentThread().getContextClassLoader()</code> into a doPrivileged block if security manager is present
     */
    private static ClassLoader getContextClassLoader() {
        if (System.getSecurityManager() == null) {
            return Thread.currentThread().getContextClassLoader();
        }
        else {
            return  (ClassLoader) java.security.AccessController.doPrivileged(
                    new java.security.PrivilegedAction<Object>() {
                        public java.lang.Object run() {
                            return Thread.currentThread().getContextClassLoader();
                        }
                    }
            );
        }
    }


    private static final String LOGGER_SUBSYSTEM = "javax.persistence.spi";

    private Logger logger;

    private void log(Level level, String message) {
        if (this.logger == null) {
            this.logger = Logger.getLogger(LOGGER_SUBSYSTEM);
        }
        this.logger.log(level, LOGGER_SUBSYSTEM + "::" + message);
    }

    private static final String SERVICE_PROVIDER_FILE = "META-INF/services/javax.persistence.spi.PersistenceProvider";

    /**
     * Locate all JPA provider services files and collect all of the
     * provider names available.
     */
    private Collection<ProviderName> getProviderNames(ClassLoader loader) {
        Enumeration<URL> resources = null;

        try {
            resources = loader.getResources(SERVICE_PROVIDER_FILE);
        } catch (IOException ioe) {
            throw new PersistenceException("IOException caught: " + loader + ".getResources(" + SERVICE_PROVIDER_FILE + ")", ioe);
        }

        Collection<ProviderName> providerNames = new ArrayList<ProviderName>();

        while (resources.hasMoreElements()) {
            URL url = resources.nextElement();
            addProviderNames(url, providerNames);
        }

        return providerNames;
    }

    private static final Pattern nonCommentPattern = Pattern.compile("^([^#]+)");

    /**
     * For each services file look for uncommented provider names on each
     * line.
     */
    private void addProviderNames(URL url, Collection<ProviderName> providerNames) {
        try (InputStream in = url.openStream()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, UTF_8));

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                Matcher m = nonCommentPattern.matcher(line);
                if (m.find()) {
                    providerNames.add(new ProviderName(m.group().trim(), url));
                }
            }
        } catch (IOException ioe) {
            throw new PersistenceException("IOException caught reading: " + url, ioe);
        }
    }

    /**
     * Clear all cached providers
     */
    @Override
    public void clearCachedProviders() {
        this.providers.clear();
    }

    /**
     * A ProviderName captures each provider name found in a service file as
     * well as the URL for the service file it was found in. This
     * information is only used for diagnostic purposes.
     */
    private class ProviderName {

        /** Provider name **/
        private String name;

        /** URL for the service file where the provider name was found **/
        private URL source;

        public ProviderName(String name, URL sourceURL) {
            this.name = name;
            this.source = sourceURL;
        }

        public String getName() {
            return name;
        }

        public URL getSource() {
            return source;
        }

        public String toString() {
            return getName() + " - " + getSource();
        }
    }

}


