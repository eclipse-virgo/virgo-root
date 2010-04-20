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



import java.util.ArrayList;

import java.util.HashMap;

import java.util.List;

import java.util.Map;

import org.eclipse.virgo.util.parser.manifest.ManifestContents;





// TODO [later] make the StandardVisitor configurable to terminate after parsing the main section

/**

 * <strong>Concurrent Semantics</strong><br/>

 * 

 * This class is thread safe.

 * 


 */

public class StandardVisitor implements ManifestVisitor, ManifestContents {

	private String version;

	private Map<String, String> mainAttributes = new HashMap<String, String>();

	private List<String> otherSectionNames = new ArrayList<String>();

	private Map<String, Map<String, String>> otherSectionAttributes = new HashMap<String, Map<String, String>>();



	private Map<String, String> currentAttributes;

	protected boolean terminateAfterMainSection = false;



	public void visitManifestVersion(String version) {

		mainAttributes.put("Manifest-Version", version);

		this.version = version;

	}



	public void visitHeader(String name, String value) {	  
	    if (this.currentAttributes == this.mainAttributes && "Manifest-Version".equals(name)) {
	        this.version = value;
	    }

		currentAttributes.put(name, value);

	}



	public List<String> getSectionNames() {

		return otherSectionNames;

	}



	public boolean visitSection(boolean isMain, String name) {

		currentAttributes = new HashMap<String, String>();

		if (isMain) {

			mainAttributes = currentAttributes;

		} else {

			if (terminateAfterMainSection) {

				return false;

			}

			otherSectionNames.add(name);

			otherSectionAttributes.put(name, currentAttributes);

		}

		return true;

	}



	public String getVersion() {

		return version;

	}



	public Map<String, String> getMainAttributes() {

		return mainAttributes;

	}



	public Map<String, String> getAttributesForSection(String sectionName) {

		return otherSectionAttributes.get(sectionName);

	}



	public void setTerminateAfterMainSection(boolean b) {

		this.terminateAfterMainSection = b;

	}



	public ManifestContents getManifestContents() {

		return this;

	}

}

