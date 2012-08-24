/*******************************************************************************
 * Copyright (c) 2008, 2012 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *   SAP AG - re-factoring
 *******************************************************************************/

package org.eclipse.virgo.nano.deployer.api;

import java.beans.ConstructorProperties;

/**
 * Identifies an artifact as a type, name, and version tuple.
 * 
 * <strong>Concurrent Semantics</strong><br />
 * Thread-safe.
 * 
 */
public final class ArtifactIdentity {

    private final String type;

    private final String name;

    private final String version;

    /**
     * Create a new <code>ArtifactIdentity</code>.
     * 
     * @param type The type of the artifact
     * @param name The name of the artifact
     * @param version The version of the artifact
     */
    @ConstructorProperties( { "type", "name", "version" })
    public ArtifactIdentity(String type, String name, String version) {
        this.type = type;
        this.name = name;
        this.version = version;
    }

    /**
     * Returns the type of the artifact
     * 
     * @return the artifact's type
     */
    public String getType() {
        return type;
    }

    /**
     * Returns the name of the artifact
     * 
     * @return the artifact's name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the version of the artifact
     * 
     * @return the artifact's version
     */
    public String getVersion() {
        return version;
    }
    
    public String toString() {
        return String.format("%s:%s:%s", type, name, version);
    }
}
