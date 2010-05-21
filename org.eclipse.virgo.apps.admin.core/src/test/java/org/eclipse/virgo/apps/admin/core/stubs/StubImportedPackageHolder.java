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

package org.eclipse.virgo.apps.admin.core.stubs;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.virgo.apps.admin.core.BundleHolder;
import org.eclipse.virgo.apps.admin.core.ExportedPackageHolder;
import org.eclipse.virgo.apps.admin.core.ImportedPackageHolder;



/**
 */
public final class StubImportedPackageHolder implements ImportedPackageHolder {
    
    private final String packageName;

    public StubImportedPackageHolder(String packageName) {
        this.packageName = packageName;
    }
    
    /** 
     * {@inheritDoc}
     */
    public String getPackageName() {
        return this.packageName;
    }

    /** 
     * {@inheritDoc}
     */
    public String getVersionConstraint() {
        return null;
    }

    /** 
     * {@inheritDoc}
     */
    public boolean isResolved() {
        return false;
    }

    /** 
     * {@inheritDoc}
     */
    public ExportedPackageHolder getProvider() {
        return null;
    }

    /** 
     * {@inheritDoc}
     */
    public BundleHolder getImportingBundle() {
        return null;
    }
    
    /** 
     * {@inheritDoc}
     */
    public Map<String, String> getAttributes() {
        return new HashMap<String, String>();
    }

    /** 
     * {@inheritDoc}
     */
    public Map<String, String> getDirectives() {
        return new HashMap<String, String>();
    }

}
