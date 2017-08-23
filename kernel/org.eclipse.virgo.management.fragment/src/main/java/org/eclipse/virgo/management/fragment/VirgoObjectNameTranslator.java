/*******************************************************************************
 * Copyright (c) 2011 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *******************************************************************************/
 
package org.eclipse.virgo.management.fragment;

import java.util.Hashtable;

import javax.management.ObjectName;

import org.eclipse.equinox.region.Region;
import org.eclipse.equinox.region.RegionDigraph;
import org.eclipse.gemini.management.ObjectNameTranslator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class VirgoObjectNameTranslator implements ObjectNameTranslator{

	private static final String REGION_KEY = "region";
	
	private final String regionName;

	public VirgoObjectNameTranslator(BundleContext context) {
		ServiceReference<RegionDigraph> serviceReference = context.getServiceReference(RegionDigraph.class);
		RegionDigraph service = context.getService(serviceReference);
		Region region = service.getRegion(context.getBundle().getBundleId());
		this.regionName = region.getName();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public ObjectName translate(ObjectName originalName) {
		Hashtable<String, String> keyPropertyList = originalName.getKeyPropertyList();
		keyPropertyList.put(REGION_KEY, regionName);
		try {
			return new ObjectName(originalName.getDomain(), keyPropertyList);
		} catch (Exception e) {
			throw new RuntimeException("Error modifying ObjectName for '" + originalName.getCanonicalName() + "'", e);
		}
	}

}
