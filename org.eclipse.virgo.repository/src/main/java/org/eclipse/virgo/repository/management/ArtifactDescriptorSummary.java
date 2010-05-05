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

import java.beans.ConstructorProperties;

/**
 * The type, name, and version of an ArtifactDescriptor
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe
 * 
 */
public class ArtifactDescriptorSummary {

    private static final String TO_STRING_FORMAT = "type: %s, name: %s, version: %s";

    private final String type;

    private final String name;

    private final String version;

    private final int hash;

    @ConstructorProperties( { "type", "name", "version" })
    public ArtifactDescriptorSummary(String type, String name, String version) {
        this.type = type;
        this.name = name;
        this.version = version;
        this.hash = hash(type, name, version);
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public int hashCode() {
        return this.hash;
    }

    private final static int hash(String type, String name, String version) {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + ((version == null) ? 0 : version.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ArtifactDescriptorSummary other = (ArtifactDescriptorSummary) obj;
        if (this.hash != other.hash) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (type == null) {
            if (other.type != null) {
                return false;
            }
        } else if (!type.equals(other.type)) {
            return false;
        }
        if (version == null) {
            if (other.version != null) {
                return false;
            }
        } else if (!version.equals(other.version)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return String.format(TO_STRING_FORMAT, this.type, this.name, this.version);
    }

}
