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

import org.eclipse.virgo.util.osgi.manifest.ImportBundle;
import org.eclipse.virgo.util.osgi.manifest.ImportedBundle;
import org.eclipse.virgo.util.osgi.manifest.internal.StandardImportBundle;
import org.eclipse.virgo.util.osgi.manifest.parse.HeaderDeclaration;
import org.junit.Test;


public class StandardImportBundleTests {
    @Test
    public void bundleAddition() {
        StubHeaderParser parser = new StubHeaderParser();
        
        ImportBundle importBundle = new StandardImportBundle(parser);
        assertEquals(0, importBundle.getImportedBundles().size());
        assertNull(importBundle.toParseString());
        
        importBundle.addImportedBundle("foo");
        List<ImportedBundle> importedBundles = importBundle.getImportedBundles();
        assertEquals(1, importedBundles.size());
        assertEquals("foo", importedBundles.get(0).getBundleSymbolicName());
        assertEquals("foo", importBundle.toParseString());
    }
    
    @Test
    public void resetFromParseString() {
        StubHeaderParser parser = new StubHeaderParser();
        
        List<HeaderDeclaration> headers = new ArrayList<HeaderDeclaration>();
        headers.add(new StubHeaderDeclaration("foo"));
        headers.add(new StubHeaderDeclaration("bar"));
        
        ImportBundle importBundle = new StandardImportBundle(parser);
        parser.setImportBundle(headers);
        
        importBundle.resetFromParseString("");
        
        assertEquals(2, importBundle.getImportedBundles().size());
        
        assertEquals("foo,bar", importBundle.toParseString());
        
        headers.clear();
        headers.add(new StubHeaderDeclaration("foo", "bar"));
        
        importBundle.resetFromParseString("");
        
        assertEquals(2, importBundle.getImportedBundles().size());
        
        assertEquals("foo,bar", importBundle.toParseString());
    }
}
