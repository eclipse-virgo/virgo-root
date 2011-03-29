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

package org.eclipse.virgo.kernel.shell.state;

import java.io.File;
import java.util.List;

import org.eclipse.virgo.kernel.osgi.quasi.QuasiBundle;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiResolutionFailure;

/**
 * <p>
 * <code>StateService</code>
 * </p>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations must be thread-safe.
 * 
 */
public interface StateService {

    /**
     * Obtains {@link QuasiBundle} representations of all the bundles present in the live state or the given dump
     * directory.
     * 
     * @param source can be <code>null</code> to request the live state
     * @return all the bundles or the empty list if the source can't be found
     */
    public List<QuasiBundle> getAllBundles(File source);

    /**
     * Obtains {@link QuasiLiveService} representations of all the services present in the live state or the given dump
     * directory.
     * 
     * @param source can be <code>null</code> to request the live state
     * @return all the services or the empty list if the source can't be found
     */
    public List<QuasiLiveService> getAllServices(File source);

    /**
     * Looks at all the bundles in the live state or the given dump directory and return the one with the given id, if
     * no such bundle exists then <code>null</code> is returned.
     * 
     * @param source can be <code>null</code> to request the live state
     * @param bundleId of required bundle
     * @return the requested bundle or <code>null</code>
     */
    public QuasiBundle getBundle(File source, long bundleId);

    /**
     * Returns the {@link QuasiLiveService} that represents the service with the requested service Id in the live state
     * or the given dump directory. If no such service exists then <code>null</code> is returned.
     * 
     * @param source can be <code>null</code> to request the live state
     * @param serviceId
     * @return the service with id serviceId
     */
    public QuasiLiveService getService(File source, long serviceId);

    /**
     * Returns {@link QuasiResolutionFailure} for the requested bundle in the live state or the given dump
     * directory. If there are no resolution failures then <code>null</code> will be returned.
     * 
     * @param source can be <code>null</code> to request the live state
     * @param bundleId of the fail bundle
     * @return <code>QuasiResolutionFailure</code>
     */
    public List<QuasiResolutionFailure> getResolverReport(File source, long bundleId);

    /**
     * Returns the {@link QuasiPackage QuasiPackages} with the given name in the live state or the given dump directory.
     * 
     * @param source can be <code>null</code> to request the live state
     * @param packageName to find
     * @return the package found
     */
    public QuasiPackage getPackages(File source, String packageName);

    /**
     * Searches the live state or the given dump directory for bundles matching the given string.
     * 
     * @param source can be <code>null</code> to request the live state
     * @param term to search for
     * @return a list of matched bundles
     */
    public List<QuasiBundle> search(File source, String term);

}
