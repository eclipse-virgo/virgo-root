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

package org.eclipse.virgo.kernel.shell.model.helper;

import java.util.List;

/**
 * <p>
 * RamAccessorHelper provides easy to use methods for getting information from the 
 * Runtime Artifact Model MBeans.
 * </p>
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * RamAccessorHelper implementations should be thread safe
 *
 */
public interface RamAccessorHelper {

    /**
     * Start the Artifact with the given type, name and version.
     * @param type 
     * @param name 
     * @param version 
     * @return a response string indicating success or failure
     */
    public String start(String type, String name, String version);

    /**
     * Stop the Artifact with the given type, name and version.
     * @param type 
     * @param name 
     * @param version 
     * @return a response string indicating success or failure
     */
    public String stop(String type, String name, String version);

    /**
     * Uninstall the Artifact with the given type, name and version.
     * @param type 
     * @param name 
     * @param version 
     * @return a response string indicating success or failure
     */
    public String uninstall(String type, String name, String version);

    /**
     * Refresh the Artifact with the given type, name and version.
     * @param type 
     * @param name 
     * @param version 
     * @return a response string indicating success or failure
     */
    public String refresh(String type, String name, String version);

    /**
     * Return a list of all the types of the user installed artifacts in the system.
     * 
     * @return a list of types, never <code>null</code>, possibly empty
     */
    public List<String> getTypes();

    /**
     * Return a list of {@link ArtifactAccessorPointer}s of the artifacts in the system of the given type 
     * that are user installed. If there are no artifacts of the given type then the empty list is returned.
     * 
     * @param type of {@link org.eclipse.virgo.kernel.model.Artifact}
     * @return list of {@link ArtifactAccessorPointer}s of given type
     */
    public List<ArtifactAccessorPointer> getArtifactsOfType(String type);

    /**
     * Return a list of {@link ArtifactAccessorPointer}s of all the artifacts in the system of the given 
     * type. If there are no artifacts of the given type then the empty list is returned.
     * 
     * @param type of {@link org.eclipse.virgo.kernel.model.Artifact}
     * @return list of {@link ArtifactAccessorPointer}s of given type
     */
    public List<ArtifactAccessorPointer> getAllArtifactsOfType(String type);

    /**
     * Return a representation of the requested artifact as an {@link ArtifactAccessor}. If no such artifact exists null
     * may be returned.
     * 
     * @param type of {@link org.eclipse.virgo.kernel.model.Artifact artifact}
     * @param name of {@link org.eclipse.virgo.kernel.model.Artifact artifact}
     * @param version of {@link org.eclipse.virgo.kernel.model.Artifact artifact}
     * @return an {@link ArtifactAccessor} for the identified artifact, or <code>null</code> if there isn't one
     */
    public ArtifactAccessor getArtifact(String type, String name, String version);

    /**
     * Return a representation of the requested artifact as an {@link ArtifactAccessor}. If no such artifact exists null
     * may be returned.
     * 
     * @param type of {@link org.eclipse.virgo.kernel.model.Artifact artifact}
     * @param name of {@link org.eclipse.virgo.kernel.model.Artifact artifact}
     * @param version of {@link org.eclipse.virgo.kernel.model.Artifact artifact}
     * @param region of {@link org.eclipse.virgo.kernel.model.Artifact artifact}
     * @return an {@link ArtifactAccessor} for the identified artifact, or <code>null</code> if there isn't one
     */
    public ArtifactAccessor getArtifact(String type, String name, String version, String region);

}
