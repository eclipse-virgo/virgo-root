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

package org.eclipse.virgo.nano.shim.scope.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.virgo.nano.diagnostics.KernelLogEvents;
import org.eclipse.virgo.nano.shim.scope.Scope;
import org.eclipse.virgo.nano.shim.scope.ScopeFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;

/**
 * Creates {@link Scope} instances for {@link ServiceReference ServiceReferences} and for lookups.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe.
 * 
 */
public final class StandardScopeFactory implements ScopeFactory {

    private static final String PROPERTY_BLUEPRINT_CONTEXT_SERVICE_NAME = "org.eclipse.gemini.blueprint.context.service.name";

    private static final String PROPERTY_BEAN_NAME = "org.eclipse.gemini.blueprint.bean.name";

    private final EventLogger eventLogger;

    /*
     * Share application scope properties between equivalent application scope instances
     */
    private final Map<AppScope, ConcurrentHashMap<String, Object>> properties = new HashMap<AppScope, ConcurrentHashMap<String, Object>>();

    public StandardScopeFactory(EventLogger eventLogger) {
        this.eventLogger = eventLogger;
    }

    /**
     * {@inheritDoc}
     */
    public Scope getBundleScope(Bundle bundle) {
        return isBundleScoped(bundle) ? createApplicationScope(bundle) : GlobalScope.INSTANCE;

    }

    /**
     * @param bundle
     * @return
     */
    private Scope createApplicationScope(Bundle bundle) {
        return getApplicationScope(getScopeName(bundle));
    }

    /**
     * {@inheritDoc}
     */
    public Scope getGlobalScope() {
        return GlobalScope.INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    public Scope getServiceScope(ServiceReference ref) {
        return Scope.SCOPE_ID_APP.equals(getScopeIdentifier(ref)) ? getBundleScope(ref.getBundle()) : getGlobalScope();
    }

    /**
     * {@inheritDoc}
     */
    public Scope getApplicationScope(String applicationScopeName) {
        synchronized (this.properties) {
            AppScope appScope = new AppScope(applicationScopeName);
            ConcurrentHashMap<String, Object> props = this.properties.get(appScope);
            if (props == null) {
                props = new ConcurrentHashMap<String, Object>();
                this.properties.put(appScope, props);
            }
            appScope.setProperties(props);

            return appScope;
        }
    }

    private String getScopeIdentifier(ServiceReference ref) {
        String serviceScope = (String) ref.getProperty(Scope.PROPERTY_SERVICE_SCOPE);
        if (serviceScope == null) {
            /*
             * Tolerate the former property name to avoid breaking dm Server 2.0.x users. Post 2.1.0 the former property
             * name need not be supported. Issue a warning message to prompt users to change the name.
             */
            serviceScope = (String) ref.getProperty("com.springsource.service.scope");
            if (serviceScope == null) {
                // Blueprint (application) contexts belong in the global scope.
                if (ref.getProperty(PROPERTY_BLUEPRINT_CONTEXT_SERVICE_NAME) != null) {
                    serviceScope = Scope.SCOPE_ID_GLOBAL;
                }
                else
                if (isBundleScoped(ref.getBundle())) {
                    serviceScope = Scope.SCOPE_ID_APP;
                } else {
                    serviceScope = Scope.SCOPE_ID_GLOBAL;
                }
            } else {
                this.eventLogger.log(KernelLogEvents.OLD_SCOPING_PROPERTY_USED, ref.getBundle().getSymbolicName(), ref.getBundle().getVersion(),
                    ref.getProperty(PROPERTY_BEAN_NAME));
            }
        }
        return serviceScope;
    }

    private static boolean isBundleScoped(Bundle bundle) {
        // TODO: reinstate return OsgiFrameworkUtils.getScopeName(bundle) != null;
        return getScopeName(bundle) != null;
    }

    private static String getScopeName(Bundle bundle) {
        // TODO: use OFUtils instead when in proper location
        return (String) bundle.getHeaders().get("Module-Scope");
    }

    /**
     * {@inheritDoc}
     */
    public void destroyApplicationScope(Scope applicationScope) {
        // TODO: reinstate Assert.isTrue(applicationScope instanceof AppScope, "wrong scope type");
        AppScope appScope = (AppScope) applicationScope;
        synchronized (this.properties) {
            this.properties.remove(appScope);
        }
    }

    private abstract static class StandardScope implements Scope {

        private volatile ConcurrentHashMap<String, Object> properties;

        protected StandardScope() {
        }

        final void setProperties(ConcurrentHashMap<String, Object> properties) {
            // TODO: reinstate Assert.isNull(this.properties, "properties can only be set once");
            this.properties = properties;
        }

        /**
         * {@inheritDoc}
         */
        public final Object getProperty(String propertyName) {
            // TODO: reinstate Assert.isTrue(this.properties != null, "properties not set");
            return this.properties.get(propertyName);
        }

        /**
         * {@inheritDoc}
         */
        public final void setProperty(String propertyName, Object propertyValue) {
            // TODO: reinstate Assert.isTrue(this.properties != null, "properties not set");
            this.properties.put(propertyName, propertyValue);
        }

        /**
         * {@inheritDoc}
         */
        public int hashCode() {
            return 0;
        }

        /**
         * {@inheritDoc}
         */
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
            StandardScope other = (StandardScope) obj;
            /*
             * Instances that have not had their properties set compare equal (unless overridden by subclasses).
             * Instances that have had their properties set must use the same properties object reference to compare
             * equal.
             */
            if (properties == null || other.properties == null) {
                return true;
            } else if (properties != other.properties) {
                return false;
            }
            return true;
        }

    }

    private static class GlobalScope extends StandardScope {

        static GlobalScope INSTANCE = new GlobalScope();

        private GlobalScope() {
            setProperties(new ConcurrentHashMap<String, Object>());
        }

        /**
         * {@inheritDoc}
         */
        public int hashCode() {
            return 316;
        }

        public boolean equals(Object other) {
            return other == this || other instanceof GlobalScope;
        }

        public String toString() {
            return Scope.SCOPE_ID_GLOBAL;
        }

        public boolean isGlobal() {
            return true;
        }

        public String getScopeName() {
            // The global scope does not have a name.
            return null;
        }
    }

    private static class AppScope extends StandardScope {

        private final String scopeName;

        public AppScope(String scopeName) {
            this.scopeName = scopeName;
        }

        /**
         * {@inheritDoc}
         */
        public int hashCode() {
            final int prime = 31;
            int result = super.hashCode();
            result = prime * result + ((scopeName == null) ? 0 : scopeName.hashCode());
            return result;
        }

        /**
         * {@inheritDoc}
         */
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!super.equals(obj)) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            AppScope other = (AppScope) obj;
            if (scopeName == null) {
                if (other.scopeName != null) {
                    return false;
                }
            } else if (!scopeName.equals(other.scopeName)) {
                return false;
            }
            return true;
        }

        /**
         * {@inheritDoc}
         */
        public String toString() {
            return Scope.SCOPE_ID_APP + ":" + this.scopeName;
        }

        /**
         * {@inheritDoc}
         */
        public boolean isGlobal() {
            return false;
        }

        public String getScopeName() {
            return this.scopeName;
        }
    }

}
