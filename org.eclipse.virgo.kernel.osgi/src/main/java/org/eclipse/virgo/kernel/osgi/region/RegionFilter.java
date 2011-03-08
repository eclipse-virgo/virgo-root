/*******************************************************************************
 * This file is part of the Virgo Web Server.
 *
 * Copyright (c) 2010 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SpringSource, a division of VMware - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.virgo.kernel.osgi.region;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleRevision;

/**
 * A {@link RegionFilter} is associated with a connection from one region to another and determines the bundles,
 * packages, and services which are visible across the connection.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations must be thread safe.
 * 
 */
public interface RegionFilter {
	/**
	 * Name space for sharing package capabilities.
	 * @see BundleRevision#PACKAGE_NAMESPACE
	 */
	public static final String VISIBLE_PACKAGE_NAMESPACE = BundleRevision.PACKAGE_NAMESPACE;

	/**
	 * Name space for sharing bundle capabilities for require bundle constraints.
	 * @see BundleRevision#BUNDLE_NAMESPACE
	 */
	public static final String VISIBLE_REQUIRE_NAMESPACE = BundleRevision.BUNDLE_NAMESPACE;

	/**
	 * Name space for sharing host capabilities.
	 * @see BundleRevision#HOST_NAMESPACE
	 */
	public static final String VISIBLE_HOST_NAMESPACE = BundleRevision.HOST_NAMESPACE;

	/**
	 * Name space for sharing services.  The filters specified in this name space will
	 * be used to match {@link ServiceReference services}. 
	 */
	public static final String VISIBLE_SERVICE_NAMESPACE = "org.eclipse.equinox.allow.service";

	/**
	 * Name space for sharing bundles.  The filters specified in this name space will
	 * be use to match against a bundle's symbolic name and version.  The attribute
	 * {@link Constants#BUNDLE_SYMBOLICNAME_ATTRIBUTE bundle-symbolic-name} is used
	 * for the symbolic name and the attribute {@link Constants#BUNDLE_VERSION_ATTRIBUTE 
	 * bundle-version} is used for the bundle version.
	 */
	public static final String VISIBLE_BUNDLE_NAMESPACE = "org.eclipse.equinox.allow.bundle";

	/**
	 * Name space for matching against all capabilities.  The filters specified in this
	 * name space will be used to match all capabilities.
	 */
	public static final String VISIBLE_ALL_NAMESPACE = "org.eclipse.equinox.allow.all";

	/**
	 * A filter specification that matches all package capabilities in the 
	 * {@link VISIBLE_PACKAGE_NAMESPACE} name space.
	 */
    public static final String ALL_PACKAGES = '(' + VISIBLE_PACKAGE_NAMESPACE + "=*)";

    /**
	 * A filter specification that matches all require bundle capabilities in the 
	 * {@link VISIBLE_REQUIRE_NAMESPACE} name space.
	 */
    public static final String ALL_REQUIRES = '(' + VISIBLE_REQUIRE_NAMESPACE + "=*)";

	/**
	 * A filter specification that matches all host capabilities in the 
	 * {@link VISIBLE_HOST_NAMESPACE} name space.
	 */
    public static final String ALL_HOSTS = '(' + VISIBLE_HOST_NAMESPACE + "=*)";

	/**
	 * A filter specification that matches all service in the 
	 * {@link VISIBLE_SERVICE_NAMESPACE} name space.
	 */
    public static final String ALL_SERVICES = '(' + org.osgi.framework.Constants.SERVICE_ID + "=*)";

	/**
	 * A filter specification that matches all bundles in the 
	 * {@link VISIBLE_BUNDLE_NAMESPACE} name space.
	 */
    public static final String ALL_BUNDLES = '(' + org.osgi.framework.Constants.BUNDLE_SYMBOLICNAME_ATTRIBUTE + "=*)";

    /**
     * A filter specification that matches all capabilities in all name spaces.
     * This is useful if you want to use the {@link VISIBLE_ALL_NAMESPACE} to
     * match all capabilities.
     */
    public static final String ALL = "(|(x=*)(!(x=*)))";

    /**
     * Determines whether this filter allows the given bundle
     * 
     * @param bundle the bundle
     * @return <code>true</code> if the bundle is allowed and <code>false</code>otherwise
     */
    public boolean isBundleAllowed(Bundle bundle);

    /**
     * Determines whether this filter allows the given bundle
     * 
     * @param bundle the bundle revision
     * @return <code>true</code> if the bundle is allowed and <code>false</code>otherwise
     */
    public boolean isBundleAllowed(BundleRevision bundle);

    /**
     * Determines whether this filter allows the given service reference.
     * 
     * @param service the service reference of the service
     * @return <code>true</code> if the service is allowed and <code>false</code>otherwise
     */
    public boolean isServiceAllowed(ServiceReference<?> service);

    /**
     * Determines whether this filter allows the given capability.
     * 
     * @param capability the bundle capability
     * @return <code>true</code> if the capability is allowed and <code>false</code>otherwise
     */
    public boolean isCapabilityAllowed(BundleCapability capability);

    public Map<String, Collection<String>> getSharingPolicy();
}
