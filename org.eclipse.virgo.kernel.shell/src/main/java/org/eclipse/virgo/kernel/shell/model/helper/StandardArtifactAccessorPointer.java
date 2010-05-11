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

/**
 * <p>
 * Standard impl of ArtifactAccessorPointer, null values are not allowed for type, name or version.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * ArtifactAccessorPointer is threadsafe
 * 
 */
final class StandardArtifactAccessorPointer implements ArtifactAccessorPointer {

    private final String type;

    private final String name;

    private final String version;

    private final String state;


    public StandardArtifactAccessorPointer(String type, String name, String version, String state) {
        if(type == null || name == null || version == null || state == null ) {
            throw new IllegalArgumentException(String.format("Null arguments can not be used for the construction of StandardArtifactAccessorPointer '%s' '%s' '%s'", type, name, version));
        }
        this.type = type;
        this.name = name;
        this.version = version;
        this.state = state;
    }

    /**
     * {@inheritDoc}
     */
    public String getName() {
        return this.name;
    }

    /**
     * {@inheritDoc}
     */
    public String getType() {
        return this.type;
    }

    /**
     * {@inheritDoc}
     */
    public String getVersion() {
        return this.version;
    }
    
    /**
     * {@inheritDoc}
     */
    public String getState() {
        return this.state;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((state == null) ? 0 : state.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + ((version == null) ? 0 : version.hashCode());
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
        StandardArtifactAccessorPointer other = (StandardArtifactAccessorPointer) obj;
        if (!name.equals(other.name))
            return false;
        if (!state.equals(other.state))
            return false;
        if (!type.equals(other.type))
            return false;
        if (!version.equals(other.version))
            return false;
        return true;
    }

    public int compareTo(ArtifactAccessorPointer o) {
        if(o == null) {
            return 0;
        }
        int typeResult = this.type.compareTo(o.getType());
        if (typeResult != 0) {
            return typeResult;
        }

        int nameResult = this.name.compareTo(o.getName());
        if (nameResult != 0) {
            return nameResult;
        }

        int versionResult = this.version.compareTo(o.getVersion());
        if (versionResult != 0) {
            return versionResult;
        }

        return 0;
    }

}
