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
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.virgo.util.osgi.manifest.ImportedLibrary;
import org.eclipse.virgo.util.osgi.manifest.Resolution;
import org.eclipse.virgo.util.osgi.manifest.Sharing;
import org.eclipse.virgo.util.osgi.manifest.VersionRange;
import org.eclipse.virgo.util.osgi.manifest.internal.StandardImportedLibrary;
import org.eclipse.virgo.util.osgi.manifest.parse.HeaderDeclaration;
import org.junit.Test;


public class StandardImportedLibraryTests {
    
private StubHeaderParser parser = new StubHeaderParser();
    
    private ImportedLibrary importedLibrary = new StandardImportedLibrary(this.parser, "foo");
    
    @Test
    public void defaults() {        
        assertEquals(Resolution.MANDATORY, importedLibrary.getResolution());
        assertTrue(importedLibrary.getAttributes().isEmpty());
        assertTrue(importedLibrary.getDirectives().isEmpty());
        assertEquals(Sharing.AUTOMATIC, importedLibrary.getSharing());
        assertEquals(VersionRange.NATURAL_NUMBER_RANGE, importedLibrary.getVersion());
        assertEquals("foo", importedLibrary.getLibrarySymbolicName());
    }
    
    @Test
    public void resolutionDirective() {
        importedLibrary.setResolution(Resolution.OPTIONAL);
        assertEquals(Resolution.OPTIONAL, importedLibrary.getResolution());
        assertEquals("foo;resolution:=\"optional\"", importedLibrary.toParseString());
        
        importedLibrary.setResolution(Resolution.MANDATORY);
        assertEquals(Resolution.MANDATORY, importedLibrary.getResolution());
        assertEquals("foo", importedLibrary.toParseString());
        
        importedLibrary.setResolution(null);
        assertEquals(Resolution.MANDATORY, importedLibrary.getResolution());
        assertEquals("foo", importedLibrary.toParseString());
    }
    
    @Test
    public void sharingDirective() {
        importedLibrary.setSharing(Sharing.CLONE);
        assertEquals(Sharing.CLONE, importedLibrary.getSharing());
        assertEquals("foo;sharing:=\"clone\"", importedLibrary.toParseString());
        
        importedLibrary.setSharing(Sharing.AUTOMATIC);
        assertEquals(Sharing.AUTOMATIC, importedLibrary.getSharing());
        assertEquals("foo", importedLibrary.toParseString());
        
        importedLibrary.setSharing(Sharing.SHARE);
        assertEquals(Sharing.SHARE, importedLibrary.getSharing());
        assertEquals("foo;sharing:=\"share\"", importedLibrary.toParseString());         
        
        importedLibrary.setSharing(null);
        assertEquals(Sharing.AUTOMATIC, importedLibrary.getSharing());
        assertEquals("foo", importedLibrary.toParseString());
    }
    
    @Test
    public void librarySymbolicName() {
        importedLibrary.setLibrarySymbolicName("bar");
        assertEquals("bar", importedLibrary.getLibrarySymbolicName());
        assertEquals("bar", importedLibrary.toParseString());
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void nullSymbolicName() {
        importedLibrary.setLibrarySymbolicName(null);
    }
    
    @Test
    public void resetFromParseString() {
        List<HeaderDeclaration> headers = new ArrayList<HeaderDeclaration>();
        headers.add(new StubHeaderDeclaration("bar"));
        this.parser.setImportLibrary(headers);
        this.importedLibrary.resetFromParseString("");
        assertEquals("bar", this.importedLibrary.getLibrarySymbolicName());
    }
    
    @Test 
    public void versionAttribute() {
        VersionRange versionRange = new VersionRange("[1.2.3,2.0)");
        
        importedLibrary.setVersion(versionRange);
        assertEquals(versionRange, importedLibrary.getVersion());
        assertEquals("foo;version=\"[1.2.3, 2.0.0)\"", importedLibrary.toParseString());
        
        importedLibrary.setVersion(null);
        assertEquals(VersionRange.NATURAL_NUMBER_RANGE, this.importedLibrary.getVersion());
        assertEquals("foo", this.importedLibrary.toParseString());
    }
}
