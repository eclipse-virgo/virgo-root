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

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.Version;

import org.eclipse.virgo.repository.UriMapper;

/**
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread-safe.
 * 
 */
class HostedRepositoryUriMapper implements UriMapper {

    private final String uriPrefix;

    private final Map<URI, URI> mappings = new HashMap<URI, URI>();

    HostedRepositoryUriMapper(int port, String repositoryName) throws UnknownHostException {
        String hostname = InetAddress.getLocalHost().getCanonicalHostName();
        this.uriPrefix = String.format("http://%s:%s/org.eclipse.virgo.apps.repository/%s", hostname, port, repositoryName);
    }

    /**
     * {@inheritDoc}
     */
    public URI map(URI uri, String type, String name, Version version) {
        URI mapping = URI.create(String.format("%s/%s/%s/%s", this.uriPrefix, type, name, version.toString()));
        this.mappings.put(mapping, uri);

        return mapping;
    }

    URI inverseMapping(URI uri) {
        return this.mappings.get(uri);
    }

    public String getUriPrefix() {
        return this.uriPrefix;
    }
}
