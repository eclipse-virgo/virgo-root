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

import org.eclipse.virgo.util.osgi.manifest.ImportLibrary;
import org.eclipse.virgo.util.osgi.manifest.ImportedLibrary;
import org.eclipse.virgo.util.osgi.manifest.internal.StandardImportLibrary;
import org.eclipse.virgo.util.osgi.manifest.parse.HeaderDeclaration;
import org.junit.Test;


public class StandardImportLibraryTests {
    @Test
    public void libraryAddition() {
        StubHeaderParser parser = new StubHeaderParser();
        
        ImportLibrary importLibrary = new StandardImportLibrary(parser);
        assertEquals(0, importLibrary.getImportedLibraries().size());
        assertNull(importLibrary.toParseString());
        
        importLibrary.addImportedLibrary("foo");
        List<ImportedLibrary> importedLibraries = importLibrary.getImportedLibraries();
        assertEquals(1, importedLibraries.size());
        assertEquals("foo", importedLibraries.get(0).getLibrarySymbolicName());
        assertEquals("foo", importLibrary.toParseString());
    }
    
    @Test
    public void resetFromParseString() {
        StubHeaderParser parser = new StubHeaderParser();
        
        List<HeaderDeclaration> headers = new ArrayList<HeaderDeclaration>();
        headers.add(new StubHeaderDeclaration("foo"));
        headers.add(new StubHeaderDeclaration("bar"));
        
        ImportLibrary importLibrary = new StandardImportLibrary(parser);
        parser.setImportLibrary(headers);
        
        importLibrary.resetFromParseString("");        
        
        assertEquals(2, importLibrary.getImportedLibraries().size());
        assertEquals("foo,bar", importLibrary.toParseString());
        
        headers.clear();
        headers.add(new StubHeaderDeclaration("bar", "foo"));
        
        importLibrary.resetFromParseString("");
        
        assertEquals(2, importLibrary.getImportedLibraries().size());
        assertEquals("bar,foo", importLibrary.toParseString());
    }
}
