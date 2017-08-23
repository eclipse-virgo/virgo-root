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

package org.eclipse.virgo.kernel.osgi.framework;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

/**
 * Utility methods for working the OSGi framework and {@link Bundle Bundles}.
 * <p/>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe.
 * 
 */
public final class OsgiFrameworkUtils {

    private static final String HEADER_MODULE_SCOPE = "Module-Scope";

    /**
     * Gets the scope for the supplied {@link Bundle}.
     * 
     * @param bundle the <code>Bundle</code>.
     * @return the scope, or <code>null</code> if no scope is specified.
     */
    public static String getScopeName(Bundle bundle) {
        return (String) bundle.getHeaders().get(HEADER_MODULE_SCOPE);
    }

    /**
     * Queries whether the supplied {@link Bundle} is scoped.
     * 
     * @param bundle the <code>Bundle</code>.
     * @return <code>true</code> if the <code>Bundle</code> is scoped, otherwise <code>false</code>.
     */
    public static boolean isScoped(Bundle bundle) {
        String scope = getScopeName(bundle);
        return scope != null && scope.length() > 0;
    }

    /**
     * Queries whether both supplied {@link Bundle Bundles} are in the same scope.
     * 
     * @param a the first <code>Bundle</code>.
     * @param b the second <code>Bundle</code>.
     * @return <code>true</code> if both <code>Bundles</code> are in the same scope.
     */
    public static boolean sameScope(Bundle a, Bundle b) {
        String scopeA = getScopeName(a);
        if (scopeA == null || scopeA.length() == 0) {
            return false;
        } else {
            return scopeA.equals(getScopeName(b));
        }
    }

    /**
     * Indicates whether or not the supplied {@link Bundle} is active.
     * 
     * @param bundle The bundle to query
     * @return <code>true</code> if the supplied bundle is active, otherwise <code>false</code>.
     * @see Bundle#getState()
     * @see Bundle#ACTIVE
     */
    public static boolean isBundleActive(Bundle bundle) {
        return bundle.getState() == Bundle.ACTIVE;
    }

    @SuppressWarnings("unchecked")
    public static <T> OsgiServiceHolder<T> getService(BundleContext bundleContext, Class<T> clazz) {
        final ServiceReference<T> reference = (ServiceReference<T>) bundleContext.getServiceReference(clazz.getName());
        if (reference != null) {
            final T service = (T) bundleContext.getService(reference);
            return new StandardOsgiServiceHolder<T>(service, reference);
        } else {
            return null;
        }
    }

    /**
     * Returns an ordered list of {@link OsgiServiceHolder OsgiServiceHolders}, one for each service in the service
     * registry which was published under the supplied <code>serviceType</code>. The service lookup is performed using
     * the supplied <code>bundleContext</code>.
     * <p/>
     * The ordering of the <code>OsgiServiceHolder</code>s is determined by the
     * {@link ServiceReference#compareTo(Object) ordering} of the encapsulated {@link ServiceReference
     * ServiceReferences}.
     * <p/>
     * @param bundleContext in which to lookup services
     * @param serviceType of service to look for
     * @param <T> of service
     * @return list of {@link OsgiServiceHolder}s
     */
    @SuppressWarnings("unchecked")
    public static <T> List<OsgiServiceHolder<T>> getServices(BundleContext bundleContext, Class<T> serviceType) {
        List<OsgiServiceHolder<T>> serviceHolders = new ArrayList<OsgiServiceHolder<T>>();
        try {
            ServiceReference<T>[] serviceReferences = (ServiceReference<T>[]) bundleContext.getServiceReferences(serviceType.getName(), null);
            if (serviceReferences != null) {
                for (ServiceReference<T> serviceReference : serviceReferences) {
                    T service = (T) bundleContext.getService(serviceReference);
                    if (service != null) {
                        serviceHolders.add(new StandardOsgiServiceHolder<T>(service, serviceReference));
                    }
                }
                Collections.sort(serviceHolders);
            }
        } catch (InvalidSyntaxException ise) {
            throw new OsgiFrameworkException(ise);
        }

        return serviceHolders;
    }

    private static class StandardOsgiServiceHolder<T> implements OsgiServiceHolder<T>, Comparable<OsgiServiceHolder<?>> {

        private final T service;

        private final ServiceReference<T> serviceReference;

        private StandardOsgiServiceHolder(T service, ServiceReference<T> serviceReference) {
            this.service = service;
            this.serviceReference = serviceReference;
        }

        /**
         * {@inheritDoc}
         */
        public T getService() {
            return this.service;
        }

        /**
         * {@inheritDoc}
         */
        public ServiceReference<T> getServiceReference() {
            return this.serviceReference;
        }

        /**
         * {@inheritDoc}
         */
        public int compareTo(OsgiServiceHolder<?> o) {
            return this.serviceReference.compareTo(o.getServiceReference());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (this.serviceReference == null ? 0 : this.serviceReference.hashCode());
            return result;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            StandardOsgiServiceHolder<?> other = (StandardOsgiServiceHolder<?>) obj;
            if (this.serviceReference == null) {
                if (other.serviceReference != null) {
                    return false;
                }
            } else if (!this.serviceReference.equals(other.serviceReference)) {
                return false;
            }
            return true;
        }

    }
}
