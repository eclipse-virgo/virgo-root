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

package org.eclipse.virgo.repository.management;

import javax.management.MXBean;

/**
 * A specialization of {@link RepositoryInfo} for JMX management of watched storage repositories.
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 * Implementations must be thread-safe.
 *
 */
@MXBean
@Repository(type="watched")
public interface WatchedStorageRepositoryInfo extends LocalRepositoryInfo {
    
    /**
     * Force the check of the repository directory.
     * @throws RuntimeException if file system checker fails.
     */
    void forceCheck() throws RuntimeException;

}
