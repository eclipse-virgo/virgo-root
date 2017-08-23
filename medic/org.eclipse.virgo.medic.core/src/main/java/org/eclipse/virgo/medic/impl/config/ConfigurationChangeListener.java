/*******************************************************************************
 * Copyright (c) 2011 SAP AG
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Hristo Iliev, SAP AG - initial contribution
 *******************************************************************************/
package org.eclipse.virgo.medic.impl.config;

public interface ConfigurationChangeListener {

    /**
     * Called when the configuration changed.
     * <p/>
     * The listener can be called although the configuration is virtually the same - no configuration data
     * comparison is made.
     *
     * @param provider ConfigurationProvider that can be used to obtain the properties.
     */
    public void configurationChanged(ConfigurationProvider provider);

}
