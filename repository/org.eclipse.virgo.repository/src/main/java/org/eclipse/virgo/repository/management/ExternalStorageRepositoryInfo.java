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
 * A specialization of {@link RepositoryInfo} for JMX management of external storage repositories.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * Implementations must be thread-safe.
 * 
 */
@MXBean
@Repository(type = "external")
public interface ExternalStorageRepositoryInfo extends LocalRepositoryInfo {

    /**
     * Publishes the artifact, identified by the given <code>uri</code>, to the repository.
     * <p/>
     * Note that publishing an artifact to an external storage repository makes no change to that storage. If the
     * publication of the artifact is to be persistent, in addition to publishing it, it should also be copied to a
     * location that is within the external storage area that is scanned by the repository at startup.
     * 
     * @param uri The uri, as a <code>String</code>, of the artifact to be published to the repository
     * @return an {@link ArtifactDescriptorSummary} that describes the published artifact, or <code>null</code> if the
     *         artifact was not published
     */
    ArtifactDescriptorSummary publish(String uri);

    /**
     * Retracts the artifact, identified by the given <code>type</code>, <code>name</code> and <code>version</code> from
     * the repository.
     * <p/>
     * Note that retracting an artifact from an external storage repository makes no change to that storage. If the
     * retraction of the artifact is to be persistent, in addition to retracting it, it should also be deleted from the
     * external storage area that is scanned by the repository at startup.
     * 
     * @param type The type of the artifact to be retracted from the repository
     * @param name The name of the artifact to be retracted from the repository
     * @param version The version of the artifact to be retracted from the repository
     * 
     * @return <code>true</code> if the artifact was retracted, otherwise <code>false</code>.
     */
    boolean retract(String type, String name, String version);
}
