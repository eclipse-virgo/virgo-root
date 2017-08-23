/*******************************************************************************
 * Copyright (c) 2008, 2010 SAP AG
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   SAP AG - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.nano.core;

/**
 * 
 * Interface defining a way to access kernel's configuration.
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 * The implementations of this interface must be thread safe.
 */
public interface KernelConfig {
    
    /**
     * Obtains the specified property from the ConfigAdmin's configuration provided by the core bundle.
     * @param name - the name of the wanted property
     * @return the property's value
     */
	public String getProperty(String name);

}
