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

package org.eclipse.virgo.kernel.model.management;

import javax.management.ObjectName;

import org.eclipse.equinox.region.Region;
import org.eclipse.virgo.kernel.model.Artifact;
import org.eclipse.virgo.kernel.model.internal.AbstractArtifact;
import org.osgi.framework.Version;

/**
 * An strategy interface for creating object names based on an input {@link Artifact}
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations must be threadsafe
 * 
 */
public interface RuntimeArtifactModelObjectNameCreator {

    /**
     * Create an ArtifactModel {@link ObjectName} based on an input {@link Artifact}. Names generated from equal (
     * {@link AbstractArtifact#equals(Object)}) {@link Artifact}s should be equal as well.
     * 
     * @param artifact The artifact to generate an {@link ObjectName} for
     * @return The {@link ObjectName} for the {@link Artifact}
     */
    ObjectName createArtifactModel(Artifact artifact);

    /**
     * Creates an ArtifactModel {@link ObjectName} based on an input type, name, version, and region.
     * 
     * @param type The type of the object to create an @{link ObjectName} for
     * @param name The name of the object to create an @{link ObjectName} for
     * @param version The version of the object to create an @{link ObjectName} for
     * @param region The {@link Region} of the object to create an @{link ObjectName} for
     * @return An {@link ObjectName} for the runtime artifact represented by this type, name, version, and region
     */
    ObjectName createArtifactModel(String type, String name, Version version, Region region);

    /**
     * Creates a query {@link ObjectName} that can be used to enumerate all of the artifacts in the runtime artifact models
     * for both user and kernel regions
     *
     * @return An {@link ObjectName} that can be used for querying
     */
    ObjectName createAllArtifactsQuery();

    /**
     * Creates a query {@link ObjectName} that can be used to enumerate all of the artifacts of a given type in the
     * runtime artifact model
     * 
     * @param type The type of artifacts to query for
     * @return An {@link ObjectName} that can be used for querying
     */
    ObjectName createArtifactsOfTypeQuery(String type);

    /**
     * Creates a query {@link ObjectName} that can be used to enumerate all of the versions of a given artifact type and
     * name in the runtime artifact model
     * 
     * @param type The type of artifacts to query for
     * @param name The name of artifacts to query for
     * @return An {@link ObjectName} that can be used for querying
     */
    ObjectName createArtifactVersionsQuery(String type, String name);

    /**
     * Gets the type of an artifact identified by an {@link ObjectName}
     * 
     * @param objectName The identifying {@link ObjectName}
     * @return The type of the artifact
     */
    //String getType(ObjectName objectName);

    /**
     * Gets the name of an artifact identified by an {@link ObjectName}
     * 
     * @param objectName The identifying {@link ObjectName}
     * @return The name of the artifact
     */
    String getName(ObjectName objectName);

    /**
     * Gets the version of an artifact identified by an {@link ObjectName}
     * 
     * @param objectName The identifying {@link ObjectName}
     * @return The version of the artifact
     */
    String getVersion(ObjectName objectName);

    /**
     * Gets the version of an artifact identified by an {@link ObjectName}
     * 
     * @param objectName The identifying {@link ObjectName}
     * @return The version of the artifact
     */
    String getRegion(ObjectName objectName);

}
