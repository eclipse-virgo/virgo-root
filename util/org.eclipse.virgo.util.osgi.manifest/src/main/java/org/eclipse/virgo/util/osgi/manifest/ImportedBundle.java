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

package org.eclipse.virgo.util.osgi.manifest;

/**
 * Represents a single entry in a bundle's <code>Import-Bundle</code> header.
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 * May not be thread-safe.
 * 
 */
public interface ImportedBundle extends Imported {

    /**
     * Returns the symbolic name of the bundle that is imported, never <code>null</code>.
     * 
     * @return the imported bundle's symbolic name.
     */
    String getBundleSymbolicName();
    
    /**
     * Sets the symbolic name of the bundle that is imported
     * @param bundleSymbolicName the imported bundle's symbolic name
     * @throws IllegalArgumentException if the supplied bundleSymbolicName is <code>null</code>
     */
    void setBundleSymbolicName(String bundleSymbolicName) throws IllegalArgumentException;

    /**
     * Returns the value of the import's <code>sharing</code> directive. If no such directive is specified the
     * default value of {@link Sharing#AUTOMATIC} is returned.
     * 
     * @return the value of the import's sharing directive.
     */
    Sharing getSharing();
    
    /**
     * Sets the value of the import's <code>sharing</code> directive.
     * 
     * @param sharing the value of the import's sharing directive
     */
    void setSharing(Sharing sharing);

    /**
     * Returns <code>true</code> if the import's <code>import-scope</code> directive is present and has 
     * a value of <code>application</code>, otherwise <code>false</code> is returned.
     * 
     * @return <code>true</code> if and only if the import-scope directive has a value of application
     */
    boolean isApplicationImportScope();
    
    /**
     * Sets the <code>import-scope</code> directive
     * 
     * @param applicationImportScope Whether or not the import's scope is application
     */
    void setApplicationImportScope(boolean applicationImportScope);
}
