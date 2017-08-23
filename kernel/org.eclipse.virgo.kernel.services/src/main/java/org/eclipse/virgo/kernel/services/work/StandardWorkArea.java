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

package org.eclipse.virgo.kernel.services.work;

import java.io.File;

import org.osgi.framework.Bundle;

import org.eclipse.virgo.util.io.PathReference;

/**
 * Standard implementation of {@link WorkArea}.
 * <p/>
 * 
 * <strong>Concurrent Semantics</strong><br/>
 * 
 * Threadsafe.
 * 
 */
final class StandardWorkArea implements WorkArea {

    private static final String KERNEL_PREFIX = "org.eclipse.virgo.kernel.";

    private final PathReference workDirectory;

    private final Bundle owner;

    /**
     * Creates a new <code>StandardWorkAreaManager</code>.
     * 
     * @param workDirectory the root work directory
     * @param owner the owning <code>Bundle</code>
     */
    public StandardWorkArea(File workDirectory, Bundle owner) {
        this.owner = owner;
        this.workDirectory = new PathReference(workDirectory).newChild(createOwnerDirectoryName(owner));
        this.workDirectory.createDirectory();
    }

    private String createOwnerDirectoryName(Bundle owner) {
        String ownerSymbolicName = owner.getSymbolicName();
        if (ownerSymbolicName.startsWith(KERNEL_PREFIX)) {
            // Give kernel bundles short work area names to reduce path name lengths on Windows.
            return ownerSymbolicName.substring(KERNEL_PREFIX.length());
        } else {
            return String.format("%s_%s", ownerSymbolicName, owner.getVersion());
        }
    }

    /**
     * {@inheritDoc}
     */
    public Bundle getOwner() {
        return this.owner;
    }

    /**
     * {@inheritDoc}
     */
    public PathReference getWorkDirectory() {
        return this.workDirectory;
    }

}
