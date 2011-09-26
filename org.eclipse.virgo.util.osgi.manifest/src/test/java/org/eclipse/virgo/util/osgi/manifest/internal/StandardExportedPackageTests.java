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

import org.eclipse.virgo.util.osgi.manifest.ExportedPackage;
import org.eclipse.virgo.util.osgi.manifest.internal.StandardExportedPackage;
import org.eclipse.virgo.util.osgi.manifest.parse.HeaderDeclaration;
import org.junit.Test;
import org.osgi.framework.Version;


public class StandardExportedPackageTests {
    
    private StubHeaderParser parser = new StubHeaderParser();

    @Test
    public void defaults() {        
        ExportedPackage exportedPackage = new StandardExportedPackage(this.parser, "");
        assertEquals(new Version("0"), exportedPackage.getVersion());
        assertEquals(0, exportedPackage.getExclude().size());
        assertEquals(0, exportedPackage.getInclude().size());
        assertEquals(0, exportedPackage.getMandatory().size());
        assertEquals(0, exportedPackage.getUses().size());
    }
    
    @Test
    public void version() {        
        ExportedPackage exportedPackage = new StandardExportedPackage(this.parser, "foo");        
        exportedPackage.setVersion(new Version(1, 2, 3));
        assertEquals(new Version(1, 2, 3), exportedPackage.getVersion());
        assertEquals("foo;version=\"1.2.3\"", exportedPackage.toParseString());  
        exportedPackage.setVersion(null);
        assertEquals(new Version(0, 0, 0), exportedPackage.getVersion());
    }
    
    @Test
    public void include() {        
        ExportedPackage exportedPackage = new StandardExportedPackage(this.parser, "foo");
        List<String> include = exportedPackage.getInclude();
        assertEquals(0, include.size());
        include.add("bar");
        assertEquals("foo;include:=\"bar\"", exportedPackage.toParseString());
        include.clear();
        assertEquals("foo", exportedPackage.toParseString());
    }
    
    @Test
    public void exclude() {        
        ExportedPackage exportedPackage = new StandardExportedPackage(this.parser, "foo");
        List<String> exclude = exportedPackage.getExclude();
        assertEquals(0, exclude.size());
        exclude.add("bar");
        assertEquals("foo;exclude:=\"bar\"", exportedPackage.toParseString());
        exclude.clear();
        assertEquals("foo", exportedPackage.toParseString());
    }
    
    @Test
    public void packageName() {        
        ExportedPackage exportedPackage = new StandardExportedPackage(this.parser, "foo");
        assertEquals("foo", exportedPackage.getPackageName());
        assertEquals("foo", exportedPackage.toParseString());
        exportedPackage.setPackageName("bar");
        assertEquals("bar", exportedPackage.getPackageName());
        assertEquals("bar", exportedPackage.toParseString());
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void nullPackageName() {        
        ExportedPackage exportedPackage = new StandardExportedPackage(this.parser, "foo");
        exportedPackage.setPackageName(null);
    }
    
    @Test
    public void resetFromParseString() {        
        ExportedPackage exportedPackage = new StandardExportedPackage(this.parser, "foo");
        List<HeaderDeclaration> exportPackage = new ArrayList<HeaderDeclaration>();
        exportPackage.add(new StubHeaderDeclaration("bar"));
        this.parser.setExportPackage(exportPackage);
        exportedPackage.resetFromParseString("");
        assertEquals("bar", exportedPackage.getPackageName());
    }
}
