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

import org.eclipse.virgo.util.osgi.manifest.ImportedPackage;
import org.eclipse.virgo.util.osgi.manifest.Resolution;
import org.eclipse.virgo.util.osgi.manifest.VersionRange;
import org.eclipse.virgo.util.osgi.manifest.internal.StandardImportedPackage;
import org.eclipse.virgo.util.osgi.manifest.parse.HeaderDeclaration;
import org.junit.Test;


public class StandardImportedPackageTests {
    
    private StubHeaderParser parser = new StubHeaderParser();
    
    private ImportedPackage importedPackage = new StandardImportedPackage(this.parser, "foo");
    
    @Test
    public void defaults() {
        assertEquals(Resolution.MANDATORY, importedPackage.getResolution());
        assertTrue(importedPackage.getAttributes().isEmpty());
        assertTrue(importedPackage.getDirectives().isEmpty());
        assertEquals(VersionRange.NATURAL_NUMBER_RANGE, importedPackage.getVersion());
        assertEquals("foo", importedPackage.getPackageName());
    }
    
    @Test
    public void resolutionDirective() {
        importedPackage.setResolution(Resolution.OPTIONAL);
        assertEquals(Resolution.OPTIONAL, importedPackage.getResolution());
        assertEquals("foo;resolution:=\"optional\"", importedPackage.toParseString());
        
        importedPackage.setResolution(Resolution.MANDATORY);
        assertEquals(Resolution.MANDATORY, importedPackage.getResolution());
        assertEquals("foo", importedPackage.toParseString());
        
        importedPackage.setResolution(null);
        assertEquals(Resolution.MANDATORY, importedPackage.getResolution());
        assertEquals("foo", importedPackage.toParseString());
    }
    
    @Test
    public void packageName() {
        importedPackage.setPackageName("bar");
        assertEquals("bar", importedPackage.getPackageName());
        assertEquals("bar", importedPackage.toParseString());
    }
    
    @Test
    public void bundleSymbolicName() {
        importedPackage.setBundleSymbolicName("bsn");
        assertEquals("bsn", importedPackage.getBundleSymbolicName());
    }
    
    @Test
    public void bundleVersion() {
        VersionRange versionRange = new VersionRange("[1.2.3.f,1.3.4.g)");
        importedPackage.setBundleVersion(versionRange);
        assertEquals(versionRange, importedPackage.getBundleVersion());
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void nullSymbolicName() {
        importedPackage.setPackageName(null);
    }
    
    @Test
    public void resetFromParseString() {
        List<HeaderDeclaration> headers = new ArrayList<HeaderDeclaration>();
        headers.add(new StubHeaderDeclaration("bar"));
        this.parser.setImportPackage(headers);
        this.importedPackage.resetFromParseString("");
        assertEquals("bar", this.importedPackage.getPackageName());
    }
    
    @Test 
    public void versionAttribute() {
        VersionRange versionRange = new VersionRange("[1.2.3,2.0)");
        
        importedPackage.setVersion(versionRange);
        assertEquals(versionRange, importedPackage.getVersion());
        assertEquals("foo;version=\"[1.2.3, 2.0.0)\"", importedPackage.toParseString());
        
        importedPackage.setVersion(null);
        assertEquals(VersionRange.NATURAL_NUMBER_RANGE, this.importedPackage.getVersion());
        assertEquals("foo", this.importedPackage.toParseString());
    }
}
