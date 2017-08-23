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

package org.eclipse.virgo.kernel.artifact.library;

import java.net.URI;
import java.util.List;

import org.osgi.framework.Version;

import org.eclipse.virgo.util.osgi.manifest.ImportedBundle;

/**
 * Defines a library that can be installed into the OSGi framework. <p/>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations must be threadsafe.
 * 
 */
public interface LibraryDefinition {
    
    public static final String LIBRARY_TYPE = "library";

     /**
     * Gets the symbolic name of the library.
     * 
     * @return the library's symbolic name
     */
    String getSymbolicName();

    /**
     * Gets the description of the library. <p/>
     * 
     * May return <code>null</code> if no description is defined.
     * 
     * @return the library's description
     */
    String getDescription();

    /**
     * Gets the name of the library.
     * 
     * @return the library's name.
     */
    String getName();
    
    /**
     * Gets the version of the library.
     * 
     * @return the library's version.
     */
    Version getVersion();

    /**
     * Gets the bundles in this library.
     * 
     * @return the library's bundles.
     */
    List<ImportedBundle> getLibraryBundles();
    
    /**
     * Returns the location of the library
     * @return the library's location
     */
    URI getLocation();
}
