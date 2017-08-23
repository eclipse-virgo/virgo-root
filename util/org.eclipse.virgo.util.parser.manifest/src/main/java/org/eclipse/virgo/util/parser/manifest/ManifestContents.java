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

package org.eclipse.virgo.util.parser.manifest;



import java.util.List;

import java.util.Map;



/**

 * <p>

 * Encapsulates the section and header name/value information parsed from a

 * manifest.

 * 

 * <strong>Concurrent Semantics</strong><br/>

 * 

 * This class is thread safe.

 * 


 */

public interface ManifestContents {



	/**

	 * @return the version number discovered in the manifest

	 */

	String getVersion();



	/**

	 * @return the main attributes discovered in the manifest

	 */

	Map<String, String> getMainAttributes();



	/**

	 * @return the section names discovered in the manifest

	 */

	List<String> getSectionNames();



	/**

	 * @param sectionName 
	 * @return the attributes discovered for a particular section in the manifest

	 */

	Map<String, String> getAttributesForSection(String sectionName);



}

