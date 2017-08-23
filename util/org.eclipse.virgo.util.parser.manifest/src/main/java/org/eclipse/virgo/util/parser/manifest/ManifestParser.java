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



import java.io.IOException;

import java.io.Reader;

import java.util.List;



/**

 * <p>

 * Strategy for parsing manifests. The only format enforced is that defined for

 * regular JDK manifests.

 * </p>

 * 

 * <strong>Concurrent Semantics</strong><br />

 * 

 * This class is thread safe.

 * 


 */

public interface ManifestParser {



	/**

	 * Parse an input string of data as a manifest.
	 * @param manifestData 
	 * @return manifest contents

	 */

	ManifestContents parse(String manifestData);



	/**

	 * Parse a manifest from the supplied reader.
	 * @param manifestReader 
	 * @return ManifestContents

	 * 

	 * @throws IOException if there is a problem with the Reader

	 */

	ManifestContents parse(Reader manifestReader) throws IOException;



	/**

	 * @return true if problems were found during parsing

	 */

	boolean foundProblems();



	/**

	 * @return the list of problems that occurred during parsing

	 */

	List<ManifestProblem> getProblems();



	void setTerminateAfterMainSection(boolean shouldTerminate);



}

