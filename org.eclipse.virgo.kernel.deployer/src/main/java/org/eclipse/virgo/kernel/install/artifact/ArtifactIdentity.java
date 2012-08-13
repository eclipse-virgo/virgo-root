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

package org.eclipse.virgo.kernel.install.artifact;

import org.eclipse.virgo.nano.serviceability.NonNull;
import org.osgi.framework.Version;


/**
 * An {@link ArtifactIdentity} encapsulates the identity of an artifact.
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * Thread-safe.
 *
 */
public class ArtifactIdentity {
    
    private final String type;
    
    private final String name;
    
    private final Version version;
    
    private final String scopeName;

    /**
     * Creates a new <code>ArtifactIdentity</code> with the supplied <code>type</code>, <code>name</code>, <code>version</code>, 
     * and <code>scopeName</code>.
     * 
     * @param type the artifact's type
     * @param name the artifact's name
     * @param version the artifact's version
     * @param scopeName the name of the artifact's scope, or <code>null</code> if the artifact is not scoped.
     */
    public ArtifactIdentity(@NonNull String type, @NonNull String name, @NonNull Version version, String scopeName) {
        this.type = type;
        this.name = name;
        this.version = version;
        this.scopeName = scopeName;
    }
    
    /**
     * Returns the type of the artifact
     * @return the artifact's type
     */
    public String getType() {
        return type;
    }

    /**
     * Returns the name of the artifact
     * @return the artifact's name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Returns the version of the artifact
     * @return the artifact's version
     */
    public Version getVersion() {
        return version;
    }
    
    /**
     * Returns the name of the artifact's scope, or <code>null</code> if the artifact is not in a scope
     * @return the artifact's scope name
     */
    public String getScopeName() {
        return scopeName;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + name.hashCode();
        result = prime * result + ((scopeName == null) ? 0 : scopeName.hashCode());
        result = prime * result + type.hashCode();
        result = prime * result + version.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        
        if (obj == null)
            return false;
        
        if (getClass() != obj.getClass())
            return false;
        
        ArtifactIdentity other = (ArtifactIdentity) obj;
        
        if (!name.equals(other.name))
            return false;
        
        if (scopeName == null) {
            if (other.scopeName != null)
                return false;
        } else if (!scopeName.equals(other.scopeName))
            return false;
        
        if (!type.equals(other.type))
            return false;

        if (!version.equals(other.version))
            return false;
        
        return true;
    }
    
    public String toString() {
        return String.format("%s '%s' version '%s' in scope '%s'", this.type, this.name, this.version, this.scopeName);
    }
}
