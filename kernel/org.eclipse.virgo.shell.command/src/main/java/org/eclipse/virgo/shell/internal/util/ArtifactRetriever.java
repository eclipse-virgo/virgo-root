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

package org.eclipse.virgo.shell.internal.util;

import java.lang.management.ManagementFactory;

import javax.management.InstanceNotFoundException;
import javax.management.JMX;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.eclipse.equinox.region.Region;
import org.eclipse.virgo.kernel.model.management.ManageableArtifact;
import org.eclipse.virgo.kernel.model.management.RuntimeArtifactModelObjectNameCreator;
import org.osgi.framework.Version;

/**
 * <p>
 * ArtifactRetriever is responsible for obtaining artifacts from the MBean server 
 * MBeans published by the Runtime Artifact Model. One instance of this class can
 * only retrieve MBeans backed by the given type T.
 * </p>
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * ArtifactRetriever is threadsafe
 *
 * @param <T> type of artifact retrieved
 */
public final class ArtifactRetriever<T extends ManageableArtifact> {

    private final MBeanServer server = ManagementFactory.getPlatformMBeanServer();

    private final String type;

    private final RuntimeArtifactModelObjectNameCreator objectNameCreator;
    
    private final Class<T> artifactType;

    /**
     * 
     * @param type
     * @param objectNameCreator
     * @param artifactType
     */
    public ArtifactRetriever(String type, RuntimeArtifactModelObjectNameCreator objectNameCreator, Class<T> artifactType) {
        this.type = type;
        this.objectNameCreator = objectNameCreator;
        this.artifactType = artifactType;
    }
    
    /**
     * @param name
     * @param version
     * @param region
     * @return
     * @throws InstanceNotFoundException
     */
    public T getArtifact(String name, Version version, Region region) throws InstanceNotFoundException {
        return getArtifact(this.objectNameCreator.createArtifactModel(this.type, name, version, region));
    }

    /**
     * 
     * @param objectName
     * @return artifact
     * @throws InstanceNotFoundException
     */
    public T getArtifact(final ObjectName objectName) throws InstanceNotFoundException {
        if (this.server.isRegistered(objectName)) {
            return JMX.newMXBeanProxy(this.server, objectName, this.artifactType);
        }
        throw new InstanceNotFoundException(String.format("Instance '%s' not found", objectName.getCanonicalName()));
    }
}
