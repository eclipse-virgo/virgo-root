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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.virgo.util.osgi.manifest.BundleSymbolicName;
import org.eclipse.virgo.util.osgi.manifest.BundleSymbolicName.FragmentAttachment;
import org.eclipse.virgo.util.osgi.manifest.internal.StandardBundleSymbolicName;
import org.junit.Test;
import org.osgi.framework.Constants;


public class StandardBundleSymbolicNameTests {
    
    private StubHeaderParser parser = new StubHeaderParser();
    private BundleSymbolicName bundleSymbolicName = new StandardBundleSymbolicName(parser);
    
    @Test
    public void defaults() {
        assertTrue(this.bundleSymbolicName.getDirectives().isEmpty());
        assertTrue(this.bundleSymbolicName.getAttributes().isEmpty());
        assertEquals(FragmentAttachment.ALWAYS, this.bundleSymbolicName.getFragmentAttachment());
        assertFalse(this.bundleSymbolicName.isSingleton());
        assertNull(this.bundleSymbolicName.toParseString());
    }
    
    @Test
    public void symbolicName() {
        this.parser.setBundleSymbolicName(new StubHeaderDeclaration("foo"));
        this.bundleSymbolicName.resetFromParseString("");
        
        assertEquals("foo", this.bundleSymbolicName.getSymbolicName());
        assertEquals("foo", this.bundleSymbolicName.toParseString());
    }
    
    @Test
    public void singletonDirective() {
        Map<String, String> directives = new HashMap<String, String>();
        
        directives.put(Constants.SINGLETON_DIRECTIVE, "true");        
        this.parser.setBundleSymbolicName(new StubHeaderDeclaration(new HashMap<String, String>(), directives, "foo"));
        this.bundleSymbolicName.resetFromParseString("");        
        assertTrue(this.bundleSymbolicName.isSingleton());
        assertEquals("foo;singleton:=\"true\"", this.bundleSymbolicName.toParseString());
        
        directives.put(Constants.SINGLETON_DIRECTIVE, "false");
        this.parser.setBundleActivationPolicy(new StubHeaderDeclaration(new HashMap<String, String>(), directives, "foo"));
        this.bundleSymbolicName.resetFromParseString("");        
        assertFalse(this.bundleSymbolicName.isSingleton());
        assertEquals("foo;singleton:=\"false\"", this.bundleSymbolicName.toParseString());
    }
    
    @Test
    public void fragmentAttachmentDirective() {
        Map<String, String> directives = new HashMap<String, String>();
        
        directives.put(Constants.FRAGMENT_ATTACHMENT_DIRECTIVE, Constants.FRAGMENT_ATTACHMENT_NEVER);        
        this.parser.setBundleSymbolicName(new StubHeaderDeclaration(new HashMap<String, String>(), directives, "foo"));
        this.bundleSymbolicName.resetFromParseString("");        
        assertEquals(this.bundleSymbolicName.getFragmentAttachment(), FragmentAttachment.NEVER);
        assertEquals("foo;fragment-attachment:=\"never\"", this.bundleSymbolicName.toParseString());
        
        directives.put(Constants.FRAGMENT_ATTACHMENT_DIRECTIVE, Constants.FRAGMENT_ATTACHMENT_RESOLVETIME);
        this.parser.setBundleSymbolicName(new StubHeaderDeclaration(new HashMap<String, String>(), directives, "foo"));
        this.bundleSymbolicName.resetFromParseString("");        
        assertEquals(this.bundleSymbolicName.getFragmentAttachment(), FragmentAttachment.RESOLVE_TIME);
        assertEquals("foo;fragment-attachment:=\"resolve-time\"", this.bundleSymbolicName.toParseString());
        
        directives.put(Constants.FRAGMENT_ATTACHMENT_DIRECTIVE, Constants.FRAGMENT_ATTACHMENT_ALWAYS);
        this.parser.setBundleSymbolicName(new StubHeaderDeclaration(new HashMap<String, String>(), directives, "foo"));
        this.bundleSymbolicName.resetFromParseString("");        
        assertEquals(this.bundleSymbolicName.getFragmentAttachment(), FragmentAttachment.ALWAYS);
        assertEquals("foo;fragment-attachment:=\"always\"", this.bundleSymbolicName.toParseString());
    }
    
    @Test
    public void setSingleton() {
        this.bundleSymbolicName.setSingleton(true);
        assertTrue(this.bundleSymbolicName.isSingleton());        
        
        this.bundleSymbolicName.setSingleton(false);
        assertFalse(this.bundleSymbolicName.isSingleton());
    }
    
    @Test
    public void setFragmentAttachment() {
        this.bundleSymbolicName.setFragmentAttachment(FragmentAttachment.NEVER);
        assertEquals(FragmentAttachment.NEVER, this.bundleSymbolicName.getFragmentAttachment());
        
        this.bundleSymbolicName.setFragmentAttachment(FragmentAttachment.RESOLVE_TIME);
        assertEquals(FragmentAttachment.RESOLVE_TIME, this.bundleSymbolicName.getFragmentAttachment());
        
        this.bundleSymbolicName.setFragmentAttachment(FragmentAttachment.ALWAYS);
        assertEquals(FragmentAttachment.ALWAYS, this.bundleSymbolicName.getFragmentAttachment());
    }
    
    @Test
    public void setSymbolicName() {
        this.bundleSymbolicName.setSymbolicName("foo");
        assertEquals("foo", this.bundleSymbolicName.toParseString());
    }
}
