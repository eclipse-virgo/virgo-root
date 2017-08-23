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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.virgo.kernel.osgi.quasi.QuasiBundle;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiExportPackage;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiImportPackage;
import org.osgi.framework.Version;


/**
 */
public class StubQuasiExportPackage implements QuasiExportPackage {

    private final String packageName;

    public StubQuasiExportPackage(String packageName) {
        this.packageName = packageName;
    }
    
    /** 
     * {@inheritDoc}
     */
    public List<QuasiImportPackage> getConsumers() {
        return new ArrayList<QuasiImportPackage>();
    }

    /** 
     * {@inheritDoc}
     */
    public QuasiBundle getExportingBundle() {
        return new StubQuasiBundle(5l, "", new Version("1.0.0"));
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
    public Version getVersion() {
        return Version.emptyVersion;
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
