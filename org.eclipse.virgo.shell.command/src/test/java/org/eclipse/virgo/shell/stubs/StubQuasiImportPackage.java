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

package org.eclipse.virgo.shell.stubs;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.virgo.kernel.osgi.quasi.QuasiBundle;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiExportPackage;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiImportPackage;
import org.eclipse.virgo.util.osgi.manifest.VersionRange;


/**
 */
public class StubQuasiImportPackage implements QuasiImportPackage {

    private final String packageName;

    public StubQuasiImportPackage(String packageName) {
        this.packageName = packageName;
    }
    
    /** 
     * {@inheritDoc}
     */
    public QuasiBundle getImportingBundle() {
        return new StubQuasiBundle(5l, "name", null);
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
    public QuasiExportPackage getProvider() {
        return new StubQuasiExportPackage(packageName);
    }

    /** 
     * {@inheritDoc}
     */
    public VersionRange getVersionConstraint() {
        return VersionRange.NATURAL_NUMBER_RANGE;
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
    public Map<String, Object> getAttributes() {
        return new HashMap<String, Object>();
    }

    /** 
     * {@inheritDoc}
     */
    public Map<String, Object> getDirectives() {
        return new HashMap<String, Object>();
    }

}
