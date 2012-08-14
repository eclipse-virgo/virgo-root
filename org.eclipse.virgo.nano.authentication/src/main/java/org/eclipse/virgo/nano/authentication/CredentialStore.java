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

package org.eclipse.virgo.nano.authentication;

/**
 * A container encapsulating a collection of credentials
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations must be threadsafe
 * 
 */
public interface CredentialStore {

    /**
     * Gets a {@link User} based on a given user name
     * 
     * @param name The name of the {@link User} to retrieve
     * @return The {@link User} represented by the user name or <code>null</code> if one does not exist
     */
    User getUser(String name);
}
