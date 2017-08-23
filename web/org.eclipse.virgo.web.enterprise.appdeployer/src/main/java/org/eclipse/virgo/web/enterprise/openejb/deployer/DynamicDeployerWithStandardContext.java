/*******************************************************************************
 * Copyright (c) 2012 SAP AG
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   SAP AG - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.web.enterprise.openejb.deployer;

import org.apache.catalina.core.StandardContext;
import org.apache.openejb.config.DynamicDeployer;

public interface DynamicDeployerWithStandardContext extends DynamicDeployer {
	public void setStandardContext(StandardContext standardContext);
}
