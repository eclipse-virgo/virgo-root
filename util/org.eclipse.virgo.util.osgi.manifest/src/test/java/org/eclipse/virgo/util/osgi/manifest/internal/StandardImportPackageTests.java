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
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.virgo.util.osgi.manifest.ImportPackage;
import org.eclipse.virgo.util.osgi.manifest.ImportedPackage;
import org.eclipse.virgo.util.osgi.manifest.internal.StandardImportPackage;
import org.eclipse.virgo.util.osgi.manifest.parse.HeaderDeclaration;
import org.junit.Test;


public class StandardImportPackageTests {
    
    @Test
    public void packageAddition() {
        StubHeaderParser parser = new StubHeaderParser();
        
        ImportPackage importPackage = new StandardImportPackage(parser);
        assertEquals(0, importPackage.getImportedPackages().size());
        assertNull(importPackage.toParseString());
        
        importPackage.addImportedPackage("foo");
        List<ImportedPackage> importedPackages = importPackage.getImportedPackages();
        assertEquals(1, importedPackages.size());
        assertEquals("foo", importedPackages.get(0).getPackageName());
        assertEquals("foo", importPackage.toParseString());
    }
    
    @Test
    public void resetFromParseString() {
        StubHeaderParser parser = new StubHeaderParser();
        
        List<HeaderDeclaration> headers = new ArrayList<HeaderDeclaration>();
        headers.add(new StubHeaderDeclaration("foo"));
        headers.add(new StubHeaderDeclaration("bar"));
        
        ImportPackage importPackage = new StandardImportPackage(parser);
        parser.setImportPackage(headers);
        
        importPackage.resetFromParseString("");
        
        assertEquals(2, importPackage.getImportedPackages().size());
        assertEquals("foo,bar", importPackage.toParseString());
        
        headers.clear();
        headers.add(new StubHeaderDeclaration("bar", "foo"));
        
        importPackage.resetFromParseString("");
        
        assertEquals(2, importPackage.getImportedPackages().size());
        assertEquals("bar,foo", importPackage.toParseString());
    }
}
