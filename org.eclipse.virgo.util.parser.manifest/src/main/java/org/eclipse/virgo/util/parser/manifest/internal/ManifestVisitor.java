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



import java.util.List;

import java.util.Map;

import org.eclipse.virgo.util.parser.manifest.ManifestContents;





/**

 * <p>

 * Visited whilst processing the manifest, to build an object implementing

 * {@link ManifestContents}

 * </p>

 * 

 * <strong>Concurrent Semantics</strong><br/>

 * 

 * This class is thread safe.

 * 


 */

public interface ManifestVisitor {



	public void visitManifestVersion(String version);



	/**

	 * @param isMainSection or not
	 * @param name of section
	 * @return true if parsing should continue

	 */

	public boolean visitSection(boolean isMainSection, String name);



	public void visitHeader(String name, String value);



	/**

	 * @return the version number discovered during the visit

	 */

	String getVersion();



	/**

	 * @return the main attributes discovered during the visit

	 */

	Map<String, String> getMainAttributes();



	/**

	 * @return the section names discovered during the visit

	 */

	List<String> getSectionNames();



	/**

	 * @param sectionName 
	 * @return the attributes discovered for a particular section during the visit

	 */

	Map<String, String> getAttributesForSection(String sectionName);



	/**

	 * @return the contents of the manifested as constructed during the visit

	 */

	public ManifestContents getManifestContents();



	/**

	 * Force the parsing to finish once the main section is completed (ignoring

	 * the rest of the data)

	 * 

	 * @param shouldTerminate true if parsing should finish after the main

	 *            section is visited

	 */

	public void setTerminateAfterMainSection(boolean shouldTerminate);

}

