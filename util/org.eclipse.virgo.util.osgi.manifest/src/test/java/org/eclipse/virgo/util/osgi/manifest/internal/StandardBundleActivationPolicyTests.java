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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.virgo.util.osgi.manifest.BundleActivationPolicy;
import org.eclipse.virgo.util.osgi.manifest.internal.StandardBundleActivationPolicy;
import org.junit.Test;
import org.osgi.framework.Constants;


public class StandardBundleActivationPolicyTests {
    
    private StubHeaderParser parser = new StubHeaderParser();
    private BundleActivationPolicy bundleActivationPolicy = new StandardBundleActivationPolicy(parser);
    
    @Test
    public void defaults() {
        assertEquals(BundleActivationPolicy.Policy.EAGER, this.bundleActivationPolicy.getActivationPolicy());
        assertTrue(this.bundleActivationPolicy.getInclude().isEmpty());
        assertTrue(this.bundleActivationPolicy.getExclude().isEmpty());
        assertNull(this.bundleActivationPolicy.toParseString());
    }
    
    @Test
    public void lazy() {              
        this.parser.setBundleActivationPolicy(new StubHeaderDeclaration("lazy"));
        this.bundleActivationPolicy.resetFromParseString("");        
        assertEquals(BundleActivationPolicy.Policy.LAZY, this.bundleActivationPolicy.getActivationPolicy());
        assertTrue(this.bundleActivationPolicy.getInclude().isEmpty());
        assertTrue(this.bundleActivationPolicy.getExclude().isEmpty());
        assertEquals("lazy", this.bundleActivationPolicy.toParseString());
    }
    
    @Test
    public void includeDirective() {
        Map<String, String> directives = new HashMap<String, String>();
        directives.put(Constants.INCLUDE_DIRECTIVE, "a,b,c");
        this.parser.setBundleActivationPolicy(new StubHeaderDeclaration(new HashMap<String, String>(), directives, "lazy"));
        this.bundleActivationPolicy.resetFromParseString("");
        assertEquals(BundleActivationPolicy.Policy.LAZY, this.bundleActivationPolicy.getActivationPolicy());
        assertTrue(this.bundleActivationPolicy.getExclude().isEmpty());
        List<String> include = this.bundleActivationPolicy.getInclude(); 
        assertEquals(3, include.size());
        assertEquals("a", include.get(0));
        assertEquals("b", include.get(1));
        assertEquals("c", include.get(2));
        assertEquals("lazy;include:=\"a,b,c\"", this.bundleActivationPolicy.toParseString());
    }
    
    @Test
    public void excludeDirective() {
        Map<String, String> directives = new HashMap<String, String>();
        directives.put(Constants.EXCLUDE_DIRECTIVE, "a,b,c");
        this.parser.setBundleActivationPolicy(new StubHeaderDeclaration(new HashMap<String, String>(), directives, "lazy"));
        this.bundleActivationPolicy.resetFromParseString("");
        assertEquals(BundleActivationPolicy.Policy.LAZY, this.bundleActivationPolicy.getActivationPolicy());
        assertTrue(this.bundleActivationPolicy.getInclude().isEmpty());
        List<String> exclude = this.bundleActivationPolicy.getExclude(); 
        assertEquals(3, exclude.size());
        assertEquals("a", exclude.get(0));
        assertEquals("b", exclude.get(1));
        assertEquals("c", exclude.get(2));
        assertEquals("lazy;exclude:=\"a,b,c\"", this.bundleActivationPolicy.toParseString());
    }
    
    @Test
    public void setActivationPolicy() {
        this.bundleActivationPolicy.setActivationPolicy(BundleActivationPolicy.Policy.EAGER);
        assertNull(this.bundleActivationPolicy.toParseString());
        this.bundleActivationPolicy.setActivationPolicy(BundleActivationPolicy.Policy.LAZY);
        assertEquals("lazy", this.bundleActivationPolicy.toParseString());
    }
}
