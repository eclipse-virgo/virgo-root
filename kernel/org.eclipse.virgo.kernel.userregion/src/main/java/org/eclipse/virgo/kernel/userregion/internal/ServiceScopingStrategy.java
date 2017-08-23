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

package org.eclipse.virgo.kernel.userregion.internal;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.virgo.nano.shim.scope.Scope;
import org.eclipse.virgo.nano.shim.scope.ScopeFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.virgo.kernel.install.artifact.ScopeServiceRepository;

/**
 * {@link ServiceScopingStrategy} encapsulates the service scoping algorithms used by {@link ServiceScopingRegistryHook}
 * .
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread safe.
 * 
 */
final class ServiceScopingStrategy {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ScopeFactory scopeFactory;

    private ScopeServiceRepository scopeServiceRepository;

    public ServiceScopingStrategy(ScopeFactory scopeFactory, ScopeServiceRepository scopeServiceRepository) {
        this.scopeFactory = scopeFactory;
        this.scopeServiceRepository = scopeServiceRepository;
    }

    /**
     * Returns true if and only the given service reference is potentially visible from the given bundle context. This
     * is the case if and only if the service reference is in the same scope as the bundle context or the service
     * reference is in the global scope.
     * <p/>
     * The given service reference may not be actually visible from the given bundle context if the given service
     * reference is in the global scope, the given bundle context is scoped, and there is another service in the scope
     * which shadows the given service reference.
     */
    boolean isPotentiallyVisible(ServiceReference<?> serviceReference, BundleContext consumingBundleContext) {
        boolean matchesScope = true;
        Scope serviceScope = getServiceScope(serviceReference);
        if (serviceScope != null && !serviceScope.isGlobal()) {
            Scope bundleScope = this.scopeFactory.getBundleScope(consumingBundleContext.getBundle());
            if (!bundleScope.equals(serviceScope)) {
                matchesScope = false;
            }
        }
        return matchesScope;
    }

    /**
     * Takes the given collection of service references and restricts the collection to the scope of the given bundle
     * context. It is not sufficient simply to discard service references in non-matching scopes because the global
     * scope may (see below) be searched after an application scope.
     * <p/>
     * The exact behaviour depends on the {@link ScopeServiceRepository} which models, with varying degrees of accuracy,
     * the services which are published in a particular scope. One variation in accuracy is due to the fact that web
     * bundles typically have their application context files in a directory other than <code>META-INF/spring</code> and
     * this is not currently taken into account when the repository is built. Another variation is due to the fact that
     * Spring DM supports a manifest header which specifies the directory containing application context files. Again
     * this is not currently taken into account when the repository is built.
     */
    void scopeReferences(Collection<ServiceReference<?>> references, BundleContext consumingBundleContext, String className, String filter) {
        Bundle consumingBundle = consumingBundleContext.getBundle();
        Scope lookupScope = getLookupScope(consumingBundle, className, filter);
        Scope consumerScope = getBundleScope(consumingBundle);
        
        /*
         * If the consumer is scoped, look in the consumer's scope before looking in the global scope. This avoids wrongly using
         * the global scope when the service model did not include services which are nevertheless present in the application scope.
         * If some of the service references are in the consumer's scope, restrict the set to just those.
         */
        if (lookupScope.isGlobal() && !consumerScope.isGlobal()) {
            Collection<ServiceReference<?>> scopedReferences = getScopedReferences(references, consumerScope);
            if (!scopedReferences.isEmpty()) {
                removeAllExcept(references, scopedReferences);
                return;
            }
        }
        restrictServicesToScope(references, lookupScope);
    }

    private void removeAllExcept(Collection<ServiceReference<?>> references, Collection<ServiceReference<?>> scopedReferences) {
        /*
         * The simple implementation of clearing references and then using addAll to add
         * in scopedReferences is no good as the find hook is passed a shrinkable collection
         * that does not support add or addAll.
         */
        Iterator<ServiceReference<?>> iterator = references.iterator();
        while (iterator.hasNext()) {
            ServiceReference<?> ref = iterator.next();
            if (!scopedReferences.contains(ref)) {
                iterator.remove();
            }
        }
    }

    private Collection<ServiceReference<?>> getScopedReferences(Collection<ServiceReference<?>> references, Scope scope) {
        Collection<ServiceReference<?>> scopedReferences = new HashSet<ServiceReference<?>>();
        logger.debug("References input to getScopedReferences: {}", references.size());
        Iterator<ServiceReference<?>> iterator = references.iterator();
        while (iterator.hasNext()) {
            ServiceReference<?> ref = iterator.next();
            Scope serviceScope = getServiceScope(ref);
            if (scope.equals(serviceScope)) {
                logger.debug("Adding {} ", ref);
                scopedReferences.add(ref);
            }
        }
        logger.debug("References output from getScopedReferences: {}", scopedReferences.size());
        return scopedReferences;
    }

    private void restrictServicesToScope(Collection<ServiceReference<?>> references, Scope scope) {
        logger.debug("Before filtering: {}", references.size());
        Iterator<ServiceReference<?>> iterator = references.iterator();
        while (iterator.hasNext()) {
            ServiceReference<?> ref = (ServiceReference<?>) iterator.next();
            Scope serviceScope = getServiceScope(ref);
            if (!scope.equals(serviceScope)) {
                logger.debug("Removing {} ", ref);
                iterator.remove();
            }
        }
        logger.debug("After filtering: {}", references.size());
    }

    /**
     * Gets the {@link Scope} in which this lookup is being performed.
     */
    private Scope getLookupScope(Bundle consumer, String name, String filter) {
        Scope consumerScope = getBundleScope(consumer);
        /*
         * The lookup scope is that of the consuming bundle unless the consuming bundle is in an application scope with
         * no service matching the given name and filter in which case the lookup scope is global.
         */
        Scope lookupScope = !consumerScope.isGlobal() && !scopeHasMatchingService(consumerScope, name, filter) ? this.scopeFactory.getGlobalScope()
            : consumerScope;
        logger.debug("{} > {} [{}] ({})", new Object[] { lookupScope, name, filter, consumer });
        return lookupScope;
    }

    private Scope getBundleScope(Bundle consumer) {
        return this.scopeFactory.getBundleScope(consumer);
    }

    private boolean scopeHasMatchingService(Scope scope, String name, String filter) {
        try {
            return this.scopeServiceRepository.scopeHasMatchingService(scope.getScopeName(), name, filter);
        } catch (InvalidSyntaxException e) {
            logger.warn("Filter '{}' is not valid", e, filter);
            return false;
        }
    }

    private Scope getServiceScope(ServiceReference<?> ref) {
        try {
            return this.scopeFactory.getServiceScope(ref);
        } catch (IllegalStateException ise) {
            return null;
        }
    }

}
