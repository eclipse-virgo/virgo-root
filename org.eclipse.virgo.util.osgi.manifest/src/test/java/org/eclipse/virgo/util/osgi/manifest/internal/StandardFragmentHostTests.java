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
import static org.junit.Assert.assertTrue;

import org.eclipse.virgo.util.osgi.manifest.FragmentHost;
import org.eclipse.virgo.util.osgi.manifest.VersionRange;
import org.eclipse.virgo.util.osgi.manifest.FragmentHost.Extension;
import org.eclipse.virgo.util.osgi.manifest.internal.StandardFragmentHost;
import org.junit.Test;
import org.osgi.framework.Constants;


public class StandardFragmentHostTests {
    
    private final StubHeaderParser parser = new StubHeaderParser();
    
    private final FragmentHost fragmentHost = new StandardFragmentHost(this.parser);
    
    @Test
    public void defaults() {
        assertNull(fragmentHost.getExtension());
        assertTrue(fragmentHost.getAttributes().isEmpty());
        assertTrue(fragmentHost.getDirectives().isEmpty());
    }
    
    @Test
    public void extensionDirective() {
        this.fragmentHost.setBundleSymbolicName("foo");
        
        this.fragmentHost.getDirectives().put(Constants.EXTENSION_DIRECTIVE, Constants.EXTENSION_FRAMEWORK);
        assertEquals("foo;extension:=\"framework\"", this.fragmentHost.toParseString());
        
        this.fragmentHost.getDirectives().put(Constants.EXTENSION_DIRECTIVE, Constants.EXTENSION_BOOTCLASSPATH);
        assertEquals("foo;extension:=\"bootclasspath\"", this.fragmentHost.toParseString());
        
        this.fragmentHost.getDirectives().remove(Constants.EXTENSION_DIRECTIVE);
        assertEquals("foo", this.fragmentHost.toParseString());
    }        
    
    @Test
    public void setExtension() {
        this.fragmentHost.setBundleSymbolicName("foo");
        
        this.fragmentHost.setExtension(Extension.FRAMEWORK);
        assertEquals(Extension.FRAMEWORK, this.fragmentHost.getExtension());
        assertEquals("foo;extension:=\"framework\"", this.fragmentHost.toParseString());
        
        this.fragmentHost.setExtension(Extension.BOOTCLASSPATH);
        assertEquals(Extension.BOOTCLASSPATH, this.fragmentHost.getExtension());
        assertEquals("foo;extension:=\"bootclasspath\"", this.fragmentHost.toParseString());
        
        this.fragmentHost.setExtension(null);
        assertNull(this.fragmentHost.getExtension());
        assertEquals("foo", this.fragmentHost.toParseString());
    }
    
    @Test
    public void bundleVersionAttribute() {
        this.fragmentHost.setBundleSymbolicName("foo");
        this.fragmentHost.getAttributes().put(Constants.BUNDLE_VERSION_ATTRIBUTE, new VersionRange("[1.2.3,2.3.4)").toParseString());
        assertEquals("foo;bundle-version=\"[1.2.3, 2.3.4)\"", this.fragmentHost.toParseString());
        
        this.fragmentHost.getAttributes().remove(Constants.BUNDLE_VERSION_ATTRIBUTE);
        assertEquals("foo", this.fragmentHost.toParseString());
    }
    
    @Test
    public void setBundleVersionAttribute() {
        this.fragmentHost.setBundleSymbolicName("foo");      
        
        VersionRange versionRange = new VersionRange("[1.2.3,2.3.4)");
        this.fragmentHost.setBundleVersion(versionRange);
        assertEquals(versionRange, this.fragmentHost.getBundleVersion());
        assertEquals("foo;bundle-version=\"[1.2.3, 2.3.4)\"", this.fragmentHost.toParseString());                
        
        this.fragmentHost.setBundleVersion(null);
        assertEquals(VersionRange.NATURAL_NUMBER_RANGE, this.fragmentHost.getBundleVersion());
        assertEquals("foo", this.fragmentHost.toParseString());
    }
    
    @Test
    public void bundleSymbolicName() {
        this.fragmentHost.setBundleSymbolicName("foo");
        assertEquals("foo", this.fragmentHost.getBundleSymbolicName());
        assertEquals("foo", this.fragmentHost.toParseString());
        
        this.fragmentHost.setBundleSymbolicName(null);
        assertNull(this.fragmentHost.getBundleSymbolicName());
        assertNull(this.fragmentHost.toParseString());
    }
    
    @Test
    public void resetFromParseString() {
        StubHeaderDeclaration header = new StubHeaderDeclaration("bar");
        this.parser.setFragmentHost(header);
        this.fragmentHost.resetFromParseString("");
        assertEquals("bar", this.fragmentHost.getBundleSymbolicName());
        assertEquals("bar", this.fragmentHost.toParseString());
    }
}
