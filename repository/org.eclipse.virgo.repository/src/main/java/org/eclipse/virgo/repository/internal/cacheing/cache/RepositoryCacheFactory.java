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

package org.eclipse.virgo.repository.internal.cacheing.cache;

import org.eclipse.virgo.repository.internal.remote.RemoteRepository;

/**
 * {@link RepositoryCacheFactory} provides a way of creating {@link RepositoryCache RepositoryCaches}.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations of this interface must be thread safe.
 * 
 */
public interface RepositoryCacheFactory {

    /**
     * Creates and returns a new {@link RepositoryCache} for the given {@link RemoteRepository}.
     * 
     * @param remoteRepository the <code>RemoteRepository</code>
     * @return the created <code>RepositoryCache</code>
     */
    RepositoryCache createRepositoryCache(RemoteRepository remoteRepository);

}
