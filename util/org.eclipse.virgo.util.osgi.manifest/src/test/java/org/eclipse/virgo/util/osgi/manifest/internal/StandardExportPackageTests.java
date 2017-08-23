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

package org.eclipse.virgo.util.osgi.manifest.internal;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.virgo.util.osgi.manifest.ExportPackage;
import org.eclipse.virgo.util.osgi.manifest.ExportedPackage;
import org.eclipse.virgo.util.osgi.manifest.internal.StandardExportPackage;
import org.eclipse.virgo.util.osgi.manifest.parse.HeaderDeclaration;
import org.junit.Test;


public class StandardExportPackageTests {
    
    @Test
    public void packageAddition() {
        StubHeaderParser parser = new StubHeaderParser();
        
        ExportPackage exportPackage = new StandardExportPackage(parser);
        assertEquals(0, exportPackage.getExportedPackages().size());
        
        exportPackage.addExportedPackage("foo");
        List<ExportedPackage> exportedPackages = exportPackage.getExportedPackages();
        assertEquals(1, exportedPackages.size());
        assertEquals("foo", exportedPackages.get(0).getPackageName());
    }
    
    @Test
    public void resetFromParseString() {
        StubHeaderParser parser = new StubHeaderParser();
        
        List<HeaderDeclaration> headers = new ArrayList<HeaderDeclaration>();
        headers.add(new StubHeaderDeclaration("foo"));
        headers.add(new StubHeaderDeclaration("bar"));
        
        ExportPackage exportPackage = new StandardExportPackage(parser);
        parser.setExportPackage(headers);
        
        exportPackage.resetFromParseString("");
        
        assertEquals(2, exportPackage.getExportedPackages().size());
        
        headers.clear();
        headers.add(new StubHeaderDeclaration("foo", "bar"));
        
        exportPackage.resetFromParseString("");
        
        assertEquals(2, exportPackage.getExportedPackages().size());
    }
}
