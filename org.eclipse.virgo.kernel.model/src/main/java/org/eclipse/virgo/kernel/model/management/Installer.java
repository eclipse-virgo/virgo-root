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

package org.eclipse.virgo.kernel.model.management;

import javax.management.MXBean;
import javax.management.ObjectName;

/**
 * Allows the installation of an artifact from a URI.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations must be threadsafe
 * 
 */
@MXBean
public interface Installer {

    /**
     * Install an artifact from a URI. This URI's scheme can be something like <code>file:</code> or
     * <code>repository:</code>.
     * 
     * @param uri The artifacts URI
     * @return The name of the Object in the model
     * @throws InstallException 
     */
    ObjectName install(String uri) throws InstallException;
}
