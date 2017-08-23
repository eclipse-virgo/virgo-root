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

package org.eclipse.virgo.shell.internal.util;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.eclipse.virgo.kernel.osgi.quasi.QuasiExportPackage;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiImportPackage;
import org.eclipse.virgo.shell.internal.util.PackageHolder;
import org.eclipse.virgo.shell.stubs.StubQuasiExportPackage;
import org.eclipse.virgo.shell.stubs.StubQuasiImportPackage;


/**
 */
public class PackageHolderTests {

    private static final String TEST_PACKAGE_NAME = "test.package";
    
    private PackageHolder standardQuasiPackage;
    
    @Before
    public void setUp() throws Exception {
        List<QuasiExportPackage> exports = new ArrayList<QuasiExportPackage>();
        exports.add(new StubQuasiExportPackage(TEST_PACKAGE_NAME));
        List<QuasiImportPackage> imports = new ArrayList<QuasiImportPackage>();
        imports.add(new StubQuasiImportPackage(TEST_PACKAGE_NAME));
        this.standardQuasiPackage = new PackageHolder(exports, imports, TEST_PACKAGE_NAME);
    }

    @Test
    public void testGetExporters() {
        assertNotNull(this.standardQuasiPackage.getExporters());
        assertEquals(1, this.standardQuasiPackage.getExporters().size());
    }

    @Test
    public void testGetImporters() {
        assertNotNull(this.standardQuasiPackage.getImporters());
        assertEquals(1, this.standardQuasiPackage.getImporters().size());
    }

    @Test
    public void testGetPackageName() {
        assertNotNull(this.standardQuasiPackage.getPackageName());
        assertEquals(TEST_PACKAGE_NAME, this.standardQuasiPackage.getPackageName());
    }

    @Test
    public void testGetExportersNull() {
        this.standardQuasiPackage = new PackageHolder(null, new ArrayList<QuasiImportPackage>(), TEST_PACKAGE_NAME);
        assertNotNull(this.standardQuasiPackage.getExporters());
    }

    @Test
    public void testGetImportersNull() {
        this.standardQuasiPackage = new PackageHolder(new ArrayList<QuasiExportPackage>(), null, TEST_PACKAGE_NAME);
        assertNotNull(this.standardQuasiPackage.getImporters());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testGetPackageNameNull() {
        this.standardQuasiPackage = new PackageHolder(new ArrayList<QuasiExportPackage>(), new ArrayList<QuasiImportPackage>(), null);
    }

}
