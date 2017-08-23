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

package org.eclipse.virgo.util.parser.manifest.internal;



import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.virgo.util.parser.manifest.internal.StandardVisitor;


/**


 */

// CLOVER:OFF

public class TestVisitor extends StandardVisitor {



	@Override

	public boolean visitSection(boolean isMain, String name) {

		if (!isMain && terminateAfterMainSection) {

			return false;

		}

		boolean b = super.visitSection(isMain, name);

		return b;

	}



	public void assertHeaderCount(int i) {

		if (getMainAttributes().size() != i) {

			throw new RuntimeException("Expected " + i + " headers but found " + getMainAttributes().size());

		}

	}



	public void assertSecondarySectionsCount(int i) {

		if (getSectionNames().size() != i) {

			throw new RuntimeException("Expected " + i + " sections but found " + getSectionNames().size());

		}



	}



	public Map<String, String> getAllHeaders() {

		Map<String, String> newMap = new HashMap<String, String>();

		newMap.putAll(getMainAttributes());

		List<String> names = getSectionNames();

		for (String name : names) {

			newMap.putAll(getAttributesForSection(name));

		}

		return newMap;

	}



}

