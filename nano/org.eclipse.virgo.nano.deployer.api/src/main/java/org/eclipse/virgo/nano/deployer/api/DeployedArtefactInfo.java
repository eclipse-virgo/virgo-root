/*******************************************************************************
 * Copyright (c) 2008, 2012 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *   SAP AG - re-factoring
 *******************************************************************************/

package org.eclipse.virgo.nano.deployer.api;

import java.util.Date;
import java.util.Map;

import javax.management.MXBean;

/**
 * Interface used to expose deployed artefact information
 */
@MXBean
public interface DeployedArtefactInfo {

	/**
	 * @return The type of the deployed artefact
	 */
	String getType();
	
	/**
	 * @return The name of the deployed artefact
	 */
	String getName();

	/**
	 * @return The version of the deployed artefact
	 */
	String getVersion();

	/**
	 * @return The time when this artefact was deployed
	 */
	Date getDeployTime();

	/**
	 * @return The source URI of the deployed artefact
	 */
	String getSourceUri();
	
	/**
	 * Returns the properties of the deployed artefact
	 * @return the artefact's properties.
	 */
	Map<String, String> getProperties();
	
	/**
	 * @return The local name of the repository from which the artefact was deployed
	 */
	String getRepositoryName();
}
