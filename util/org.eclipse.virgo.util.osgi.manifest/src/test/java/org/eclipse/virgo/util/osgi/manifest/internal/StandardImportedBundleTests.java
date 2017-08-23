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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.virgo.util.osgi.manifest.ImportedBundle;
import org.eclipse.virgo.util.osgi.manifest.Resolution;
import org.eclipse.virgo.util.osgi.manifest.Sharing;
import org.eclipse.virgo.util.osgi.manifest.VersionRange;
import org.eclipse.virgo.util.osgi.manifest.internal.StandardImportedBundle;
import org.eclipse.virgo.util.osgi.manifest.parse.HeaderDeclaration;
import org.junit.Test;


public class StandardImportedBundleTests {
    
    private StubHeaderParser parser = new StubHeaderParser();
    
    private ImportedBundle importedBundle = new StandardImportedBundle(this.parser, "foo");
    
    @Test
    public void defaults() {
        assertFalse(importedBundle.isApplicationImportScope());
        assertEquals(Resolution.MANDATORY, importedBundle.getResolution());
        assertTrue(importedBundle.getAttributes().isEmpty());
        assertTrue(importedBundle.getDirectives().isEmpty());
        assertEquals(Sharing.AUTOMATIC, importedBundle.getSharing());
        assertEquals(VersionRange.NATURAL_NUMBER_RANGE, importedBundle.getVersion());
        assertEquals("foo", importedBundle.getBundleSymbolicName());
    }
    
    @Test
    public void resolutionDirective() {
        importedBundle.setResolution(Resolution.OPTIONAL);
        assertEquals(Resolution.OPTIONAL, importedBundle.getResolution());
        assertEquals("foo;resolution:=\"optional\"", importedBundle.toParseString());
        
        importedBundle.setResolution(Resolution.MANDATORY);
        assertEquals(Resolution.MANDATORY, importedBundle.getResolution());
        assertEquals("foo", importedBundle.toParseString());
        
        importedBundle.setResolution(null);
        assertEquals(Resolution.MANDATORY, importedBundle.getResolution());
        assertEquals("foo", importedBundle.toParseString());
    }
    
    @Test
    public void sharingDirective() {
        importedBundle.setSharing(Sharing.CLONE);
        assertEquals(Sharing.CLONE, importedBundle.getSharing());
        assertEquals("foo;sharing:=\"clone\"", importedBundle.toParseString());
        
        importedBundle.setSharing(Sharing.AUTOMATIC);
        assertEquals(Sharing.AUTOMATIC, importedBundle.getSharing());
        assertEquals("foo", importedBundle.toParseString());
        
        importedBundle.setSharing(Sharing.SHARE);
        assertEquals(Sharing.SHARE, importedBundle.getSharing());
        assertEquals("foo;sharing:=\"share\"", importedBundle.toParseString());         
        
        importedBundle.setSharing(null);
        assertEquals(Sharing.AUTOMATIC, importedBundle.getSharing());
        assertEquals("foo", importedBundle.toParseString());
    }
    
    @Test
    public void importScope() {
        importedBundle.setApplicationImportScope(true);
        assertTrue(importedBundle.isApplicationImportScope());
        assertEquals("foo;import-scope:=\"application\"", importedBundle.toParseString());
        
        importedBundle.setApplicationImportScope(false);
        assertFalse(importedBundle.isApplicationImportScope());
        assertEquals("foo", importedBundle.toParseString());
    }
    
    @Test
    public void bundleSymbolicName() {
        importedBundle.setBundleSymbolicName("bar");
        assertEquals("bar", importedBundle.getBundleSymbolicName());
        assertEquals("bar", importedBundle.toParseString());
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void nullSymbolicName() {
        importedBundle.setBundleSymbolicName(null);
    }
    
    @Test
    public void resetFromParseString() {
        List<HeaderDeclaration> headers = new ArrayList<HeaderDeclaration>();
        headers.add(new StubHeaderDeclaration("bar"));
        this.parser.setImportBundle(headers);
        this.importedBundle.resetFromParseString("");
        assertEquals("bar", this.importedBundle.getBundleSymbolicName());
    }
    
    @Test 
    public void versionAttribute() {
        VersionRange versionRange = new VersionRange("[1.2.3,2.0)");
        
        importedBundle.setVersion(versionRange);
        assertEquals(versionRange, importedBundle.getVersion());
        assertEquals("foo;version=\"[1.2.3, 2.0.0)\"", importedBundle.toParseString());
        
        importedBundle.setVersion(null);
        assertEquals(VersionRange.NATURAL_NUMBER_RANGE, this.importedBundle.getVersion());
        assertEquals("foo", this.importedBundle.toParseString());
    }
}
