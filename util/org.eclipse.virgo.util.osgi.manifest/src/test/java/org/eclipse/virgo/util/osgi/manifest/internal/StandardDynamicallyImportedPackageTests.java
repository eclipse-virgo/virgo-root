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

import org.eclipse.virgo.util.osgi.manifest.DynamicallyImportedPackage;
import org.eclipse.virgo.util.osgi.manifest.VersionRange;
import org.eclipse.virgo.util.osgi.manifest.internal.StandardDynamicallyImportedPackage;
import org.eclipse.virgo.util.osgi.manifest.parse.HeaderDeclaration;
import org.junit.Test;


public class StandardDynamicallyImportedPackageTests {
    private StubHeaderParser parser = new StubHeaderParser();
    
    @Test
    public void defaults() {        
        DynamicallyImportedPackage dynamicallyImportedPackage = new StandardDynamicallyImportedPackage(parser, null);
        assertNull(dynamicallyImportedPackage.getBundleSymbolicName());
        assertEquals(VersionRange.NATURAL_NUMBER_RANGE, dynamicallyImportedPackage.getBundleVersion());
        assertEquals(VersionRange.NATURAL_NUMBER_RANGE, dynamicallyImportedPackage.getVersion());
        assertNull(dynamicallyImportedPackage.toParseString());
    }
    
    @Test
    public void packageName() {     
        DynamicallyImportedPackage dynamicallyImportedPackage = new StandardDynamicallyImportedPackage(parser, "foo");
        assertEquals("foo", dynamicallyImportedPackage.getPackageName());
        assertEquals("foo", dynamicallyImportedPackage.toParseString());
        
        dynamicallyImportedPackage.setPackageName("foo.*");
        assertEquals("foo.*", dynamicallyImportedPackage.getPackageName());
        assertEquals("foo.*", dynamicallyImportedPackage.toParseString());
    }
    
    @Test
    public void bundleVersion() {        
        DynamicallyImportedPackage dynamicallyImportedPackage = new StandardDynamicallyImportedPackage(parser, "foo");        
                
        dynamicallyImportedPackage.setBundleVersion(new VersionRange("[1.0.0,2.0.0)"));
        assertEquals(new VersionRange("[1.0.0,2.0.0)"), dynamicallyImportedPackage.getBundleVersion());
        assertEquals("foo;bundle-version=\"[1.0.0, 2.0.0)\"", dynamicallyImportedPackage.toParseString());
        
        dynamicallyImportedPackage.setBundleVersion(null);
        assertEquals(VersionRange.NATURAL_NUMBER_RANGE, dynamicallyImportedPackage.getBundleVersion());
        assertEquals("foo", dynamicallyImportedPackage.toParseString());
    }
    
    @Test
    public void bundleSymbolicName() {        
        DynamicallyImportedPackage dynamicallyImportedPackage = new StandardDynamicallyImportedPackage(parser, "foo");
                
        dynamicallyImportedPackage.setBundleSymbolicName("bar");
        assertEquals("bar", dynamicallyImportedPackage.getBundleSymbolicName());
        assertEquals("foo;bundle-symbolic-name=\"bar\"", dynamicallyImportedPackage.toParseString());
    }
    
    @Test
    public void version() {        
        DynamicallyImportedPackage dynamicallyImportedPackage = new StandardDynamicallyImportedPackage(parser, "foo");
                
        dynamicallyImportedPackage.setVersion(new VersionRange("[1.0.0,2.0.0)"));
        assertEquals(new VersionRange("[1.0.0,2.0.0)"), dynamicallyImportedPackage.getVersion());
        assertEquals("foo;version=\"[1.0.0, 2.0.0)\"", dynamicallyImportedPackage.toParseString());
        
        dynamicallyImportedPackage.setVersion(null);
        assertEquals(VersionRange.NATURAL_NUMBER_RANGE, dynamicallyImportedPackage.getBundleVersion());
        assertEquals("foo", dynamicallyImportedPackage.toParseString());
    }   
    
    @Test
    public void resetFromParseString() {        
        DynamicallyImportedPackage dynamicallyImportedPackage = new StandardDynamicallyImportedPackage(this.parser, "foo");
        
        List<HeaderDeclaration> dynamicImportPackage = new ArrayList<HeaderDeclaration>();
        dynamicImportPackage.add(new StubHeaderDeclaration("bar"));
        this.parser.setDynamicImportPackage(dynamicImportPackage);
        dynamicallyImportedPackage.resetFromParseString("");
        assertEquals("bar", dynamicallyImportedPackage.getPackageName());
    }
}
