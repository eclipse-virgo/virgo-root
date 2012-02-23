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

import org.eclipse.equinox.region.RegionDigraph;
import org.eclipse.gemini.mgmt.ObjectNameTranslator;

public class VirgoObjectNameTranslator implements ObjectNameTranslator{

	private static final String REGION_KEY = "region";
	
	private final RegionDigraph regionDigraph;

	public VirgoObjectNameTranslator(RegionDigraph regionDigraph) {
		this.regionDigraph = regionDigraph;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public ObjectName translate(ObjectName originalName) {
		Hashtable<String, String> keyPropertyList = originalName.getKeyPropertyList();
		keyPropertyList.put(REGION_KEY, this.regionDigraph.getRegion(7l).getName());
		try {
			return new ObjectName(originalName.getDomain(), keyPropertyList);
		} catch (Exception e) {
			throw new RuntimeException("Error modifying ObjectName for '" + originalName.getCanonicalName() + "'", e);
		}
	}

}
