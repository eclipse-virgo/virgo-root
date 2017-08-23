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

package org.eclipse.virgo.repository.internal.management;

import org.eclipse.virgo.repository.internal.ArtifactDescriptorDepository;
import org.eclipse.virgo.repository.management.RemoteRepositoryInfo;

/**
 * Standard implementation of {@link RemoteRepositoryInfo}.
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread-safe
 *
 */
public final class StandardRemoteRepositoryInfo extends AbstractRepositoryInfo implements RemoteRepositoryInfo {
    
    private static final String TYPE = "remote";

    public StandardRemoteRepositoryInfo(String name, ArtifactDescriptorDepository artifactDepository) {
        super(name, artifactDepository);
    }

    public String getType() {
        return TYPE;
    }
}
