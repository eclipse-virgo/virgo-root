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

package org.eclipse.virgo.apps.repository.core.internal;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

/**
 * Generate {@link ObjectName}s for the hosted repository instances
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * Thread-safe
 * 
 */
public class HostedRepositoryObjectNameFactory {

    private static final String OBJECT_NAME_PATTERN = "%s:type=HostedRepository,name=%s";

    private final String domain;

    public HostedRepositoryObjectNameFactory(String domain) {
        this.domain = domain;
    }

    /**
     * Creates a uniform object name based on the name of the hosted repository
     * 
     * @param repositoryName The name of the repository
     * @return The uniform object name
     * @throws NullPointerException but this should never happen
     * @throws MalformedObjectNameException if the generated object name would be badly formed
     */
    public ObjectName createObjectName(String repositoryName) throws MalformedObjectNameException, NullPointerException {
        return new ObjectName(String.format(OBJECT_NAME_PATTERN, this.domain, repositoryName));
    }
}
