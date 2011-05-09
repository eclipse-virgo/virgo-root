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

package org.eclipse.virgo.apps.admin.core;

import java.util.List;

/**
 * <p>
 * <code>StateInspectorService</code> 
 * </p>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations should be thread-safe.
 * 
 */
public interface StateHolder {
    
    /**
     * Obtains {@link BundleHolder} representations of all the bundles present
     * in the underlying state. 
     * 
     * @param dumpName can be null to request the live state
     * @return all the bundles or the empty list if the source can't be found
     */
    public List<BundleHolder> getAllBundles(String dumpName);

    /**
     * Obtains {@link ServiceHolder} representations of all the services present
     * in the underlying osgi instance. 
     * 
     * @param dumpName can be null to request the live state
     * @return all the services or the empty list if the source can't be found
     */
    public List<ServiceHolder> getAllServices(String dumpName);

    /**
     * Looks at all the bundles in the underlying state and return the one with 
     * the given id, if no such bundle exists then null is returned.
     * 
     * @param dumpName can be null to request the live state
     * @param bundleId of required bundle
     * @return the requested bundle or null
     */
    public BundleHolder getBundle(String dumpName, long bundleId);
    
    /**
     * Looks at all the bundles in the underlying state and return the one with 
     * the given name and version, if no such bundle exists then null is returned.
     * 
     * @param source dump name
     * @param name of bundle
     * @param version of bundle
     * @param region to look in
     * @return holder holding bundle
     */
    public BundleHolder getBundle(String source, String name, String version, String region);
    
    /**
     * Returns the {@link ServiceHolder} that represents the service with
     * the requested service Id. If no such service exists then null is
     * returned.
     * 
     * @param dumpName can be null to request the live state
     * @param serviceId of service to look for
     * @return service representation
     */
    public ServiceHolder getService(String dumpName, long serviceId) ;
    
    /**
     * Will return {@link FailedResolutionHolder} for the requested bundle. 
     * If there are no resolution failures then null will be returned.
     * 
     * @param dumpName can be null to request the live state
     * @param bundleId of the fail bundle
     * @return <code>FailedResolutionHolder</code>
     */
    public List<FailedResolutionHolder> getResolverReport(String dumpName, long bundleId);
    
    /**
     * 
     * @param dumpName can be null to request the live state
     * @param packageName to find
     * @return packages matching packageName
     */
    public PackagesCollection getPackages(String dumpName, String packageName);
    
    /**
     * Search the requested state for bundles matching the given string
     * 
     * @param dumpName can be null to request the live state
     * @param term to search for
     * @return list of matching bundles
     */
    public List<BundleHolder> search(String dumpName, String term);
    
}
