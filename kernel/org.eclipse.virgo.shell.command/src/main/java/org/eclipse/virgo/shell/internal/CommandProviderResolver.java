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

package org.eclipse.virgo.shell.internal;

/**
 * A <code>CommandProviderResolver</code> is responsible for resolving
 * command providers by command name.
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * Implementations must be thread-safe.
 *
 */
public interface CommandProviderResolver {

    /**
     * Returns an Object that provides the command with the given <code>command</code> name.
     * @param command The command for which a provider is required
     * @return The provider of the command, or <code>null</code> if no provider exists.
     */
    public abstract Object getCommandProvider(String command);
}
