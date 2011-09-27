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

package org.eclipse.virgo.repository.internal;

import java.lang.management.ManagementFactory;
import java.util.Map;
import java.util.Set;

import javax.management.JMException;
import javax.management.ObjectInstance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.virgo.repository.ArtifactDescriptor;
import org.eclipse.virgo.repository.Query;
import org.eclipse.virgo.repository.Repository;
import org.eclipse.virgo.repository.RepositoryAwareArtifactDescriptor;
import org.eclipse.virgo.repository.configuration.RepositoryConfiguration;
import org.eclipse.virgo.repository.internal.management.RepositoryObjectNameFactory;
import org.eclipse.virgo.repository.management.RepositoryInfo;
import org.eclipse.virgo.util.osgi.manifest.VersionRange;

/**
 * <strong>Concurrent Semantics</strong><br />
 * Thread-safe. Subclasses <strong>must</strong> be thread-safe.
 * 
 */
public abstract class BaseRepository implements Repository {

    // Domain name for management beans: if null, no beans are registered.
    private final String mBeanDomain;
    
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseRepository.class);

    private final String name;

    private final ArtifactDescriptorDepository artifactDepository;

    private volatile ObjectInstance mBean;

    protected BaseRepository(RepositoryConfiguration configuration, ArtifactDescriptorDepository artifactDepository) {
        this.name = configuration.getName();
        this.mBeanDomain = configuration.getMBeanDomain();
        this.artifactDepository = artifactDepository;
    }

    protected void start() {
        if (this.mBeanDomain!=null) {
            try {
                this.mBean = ManagementFactory.getPlatformMBeanServer().registerMBean(createMBean(),
                    RepositoryObjectNameFactory.createObjectName(this.mBeanDomain, this.name));
            } catch (JMException jme) {
                LOGGER.warn(String.format("Management Bean for repository '%s' not registered.", this.name), jme);
            }
        }
    }

    protected abstract RepositoryInfo createMBean() throws JMException;
    
    public void stop() {
        if (this.mBean != null) {
            try {
                ManagementFactory.getPlatformMBeanServer().unregisterMBean(this.mBean.getObjectName());
            } catch (JMException jme) {
                LOGGER.warn(String.format("Management Bean for repository '%s' cannot be unregistered.", this.name), jme);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public final String getName() {
        return this.name;
    }

    /**
     * {@inheritDoc}
     */
    public final RepositoryAwareArtifactDescriptor get(String type, String name, VersionRange versionRange) {
        Set<RepositoryAwareArtifactDescriptor> artifacts = createQuery(ArtifactDescriptor.TYPE, type).addFilter(ArtifactDescriptor.NAME, name).run();
        return RepositoryUtils.selectHighestVersionInRange(artifacts, versionRange);
    }

    /**
     * {@inheritDoc}
     */
    public Query createQuery(String key, String value) {
        return new StandardQuery(this.artifactDepository, key, value);
    }

    /**
     * {@inheritDoc}
     */
    public Query createQuery(String key, String value, Map<String, Set<String>> properties) {
        return new StandardQuery(this.artifactDepository, key, value, properties);
    }

    public final ArtifactDescriptorDepository getDepository() {
        return this.artifactDepository;
    }

}
