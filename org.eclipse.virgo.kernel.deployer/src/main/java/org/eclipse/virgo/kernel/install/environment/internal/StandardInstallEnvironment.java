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

package org.eclipse.virgo.kernel.install.environment.internal;

import org.eclipse.virgo.kernel.osgi.quasi.QuasiFramework;

import org.eclipse.virgo.kernel.install.environment.InstallEnvironment;
import org.eclipse.virgo.kernel.install.environment.InstallLog;
import org.eclipse.virgo.repository.Repository;


/**
 * {@link StandardInstallEnvironment} is the default {@link InstallEnvironment} implementation.
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * This class is thread safe.
 *
 */
final class StandardInstallEnvironment implements InstallEnvironment {
    
    private final Repository repository;
    
    private final InstallLog installLog;
    
    private final QuasiFramework quasiFramework;

    public StandardInstallEnvironment(Repository repository, InstallLog installLog, QuasiFramework quasiFramework) {
        this.repository = repository;
        this.installLog = installLog;
        this.quasiFramework = quasiFramework;
    }

    /** 
     * {@inheritDoc}
     */
    public Repository getRepository() {
        return this.repository;
    }

    /** 
     * {@inheritDoc}
     */
    public InstallLog getInstallLog() {
        return this.installLog;
    }

    /** 
     * {@inheritDoc}
     */
    public QuasiFramework getQuasiFramework() {
        return this.quasiFramework;
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
        this.quasiFramework.destroy();
    }
    
}
