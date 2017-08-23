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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.net.URI;

import javax.management.JMException;
import javax.management.ObjectInstance;

import org.eclipse.virgo.apps.repository.core.HostedRepositoryInfo;
import org.eclipse.virgo.apps.repository.core.RepositoryIndex;
import org.eclipse.virgo.repository.ArtifactDescriptor;
import org.eclipse.virgo.repository.Repository;
import org.eclipse.virgo.util.osgi.manifest.VersionRange;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread-safe
 * 
 */
class HostedRepository {

    private final String name; // hosted repository name

    private final Repository repository;

    private final ExportingArtifactDescriptorPersister persister;

    private final HostedRepositoryUriMapper uriMapper;
    
    private final HostedRepositoryObjectNameFactory objectNameFactory;

    private static final Logger LOGGER = LoggerFactory.getLogger(HostedRepository.class);

    private volatile ObjectInstance mBean;

    HostedRepository(Repository repository, ExportingArtifactDescriptorPersister persister, HostedRepositoryUriMapper uriMapper, HostedRepositoryObjectNameFactory objectNameFactory) {
        this.name = (null != repository) ? repository.getName() : "";
        this.repository = repository;
        this.persister = persister;
        this.uriMapper = uriMapper;
        this.objectNameFactory = objectNameFactory;
    }

    URI inverseMapping(String type, String name, String version) {
        ArtifactDescriptor descriptor = this.repository.get(type, name, new VersionRange("[" + version + "," + version + "]"));
        if (descriptor != null) {
            return this.uriMapper.inverseMapping(descriptor.getUri());
        }
        return null;
    }

    RepositoryIndex getRepositoryIndex() throws IOException {
        return new ImmutableHostedRepositoryIndex(this.persister);
    }

    private void registerMBean() {
        try {
            this.mBean = ManagementFactory.getPlatformMBeanServer().registerMBean(this.createMBean(),
                this.objectNameFactory.createObjectName(this.name));
        } catch (JMException jme) {
            LOGGER.warn(String.format("Management Bean for hosted repository '%s' not registered.", this.name), jme);
        }
    }

    void start() {
        this.registerMBean();
    }
    
    void stop() {
        this.deRegisterMBean();
    }

    private void deRegisterMBean() {
        if (this.mBean != null) {
            try {
                ManagementFactory.getPlatformMBeanServer().unregisterMBean(this.mBean.getObjectName());
            } catch (JMException jme) {
                LOGGER.warn(String.format("Management Bean for hosted repository '%s' cannot be deregistered.", this.name), jme);
            }
        }
    }

    HostedRepositoryInfo createMBean() {
        return new StandardHostedRepositoryInfo(this.uriMapper.getUriPrefix(), this.repository.getName());
    }
    
    String getName() {
        return name;
    }
    

    private static class ImmutableHostedRepositoryIndex implements RepositoryIndex {

        private final File indexFile;
        private final int length;
        private final long eTag;

        private ImmutableHostedRepositoryIndex(ExportingArtifactDescriptorPersister persister) throws IOException {
            File indexFile = null;
            try {
                indexFile = persister.exportIndexFile();
            } catch (IOException e) {
                LOGGER.error("Cannot get indexFile from lazy persister",e);
                throw e;
            }
            if (indexFile==null) {
                LOGGER.error("Cannot get indexFile from lazy persister");
                throw new IllegalArgumentException("Exporting persister did not supply an index file");
            }
            this.eTag = indexFile.lastModified();
            this.length = (int) indexFile.length();
            this.indexFile = indexFile;
        }

        /**
         * {@inheritDoc}
         */
        public String getETag() {
            return Long.toString(this.eTag);
        }

        /**
         * {@inheritDoc}
         */
        public InputStream getInputStream() throws IOException {
            return new FileInputStream(this.indexFile);
        }

        /**
         * {@inheritDoc}
         */
        public int getLength() {
            return this.length;
        }
    }
}
