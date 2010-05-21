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

package org.eclipse.virgo.apps.admin.core.state;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.virgo.apps.admin.core.ExportedPackageHolder;
import org.eclipse.virgo.apps.admin.core.ImportedPackageHolder;
import org.eclipse.virgo.apps.admin.core.state.StandardPackagesCollection;
import org.eclipse.virgo.apps.admin.core.stubs.StubExportedPackageHolder;
import org.eclipse.virgo.apps.admin.core.stubs.StubImportedPackageHolder;
import org.junit.Before;
import org.junit.Test;



/**
 */
public class StandardPackagesCollectionTests {

    private static final String TEST_PACKAGE = "testPackage";
    private StandardPackagesCollection standardPackagesCollection;
    
    @Before
    public void setUp() {
        List<ImportedPackageHolder> imported = new ArrayList<ImportedPackageHolder>();
        imported.add(new StubImportedPackageHolder(TEST_PACKAGE));
        List<ExportedPackageHolder> exported = new ArrayList<ExportedPackageHolder>();
        exported.add(new StubExportedPackageHolder(TEST_PACKAGE));
        this.standardPackagesCollection = new StandardPackagesCollection(TEST_PACKAGE, imported, exported);
    }

    /**
     * Test method for {@link org.eclipse.virgo.apps.admin.core.state.StandardPackagesCollection#getPackageName()}.
     */
    @Test
    public void testGetPackageName() {
        assertEquals(TEST_PACKAGE, this.standardPackagesCollection.getPackageName());
    }
    
    /**
     * Test method for {@link org.eclipse.virgo.apps.admin.core.state.StandardPackagesCollection#getPackageName()}.
     */
    @Test(expected=IllegalArgumentException.class)
    public void testGetPackageNameNullName() {
        this.standardPackagesCollection = new StandardPackagesCollection(null, new ArrayList<ImportedPackageHolder>(), new ArrayList<ExportedPackageHolder>());
    }

    @Test
    public void testNullLists() {
        this.standardPackagesCollection = new StandardPackagesCollection(TEST_PACKAGE, null, null);
        List<ImportedPackageHolder> imported = this.standardPackagesCollection.getImported();
        assertNotNull(imported);
        assertEquals(0, imported.size());
        List<ExportedPackageHolder> exported = this.standardPackagesCollection.getExported();
        assertNotNull(exported);
        assertEquals(0, exported.size());
    }

    /**
     * Test method for {@link org.eclipse.virgo.apps.admin.core.state.StandardPackagesCollection#getImported()}.
     */
    @Test
    public void testGetImported() {
        List<ImportedPackageHolder> imported = this.standardPackagesCollection.getImported();
        assertNotNull(imported);
        assertEquals(1, imported.size());
        assertEquals(TEST_PACKAGE, imported.get(0).getPackageName());
    }
    
    /**
     * Test method for {@link org.eclipse.virgo.apps.admin.core.state.StandardPackagesCollection#getExported()}.
     */
    @Test
    public void testGetExported() {
        List<ExportedPackageHolder> exported = this.standardPackagesCollection.getExported();
        assertNotNull(exported);
        assertEquals(1, exported.size());
        assertEquals(TEST_PACKAGE, exported.get(0).getPackageName());
    }

}
