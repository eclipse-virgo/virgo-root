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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.virgo.util.osgi.manifest.internal.BaseParameterised;
import org.eclipse.virgo.util.osgi.manifest.parse.HeaderDeclaration;
import org.eclipse.virgo.util.osgi.manifest.parse.HeaderParser;
import org.junit.Test;


public class BaseParameterisedTests {
    
    private static class TestParameterised extends BaseParameterised {
        
        private final Map<String, String> attrs;
        private final Map<String, String> dirs;
        private final String name;

        TestParameterised(HeaderParser parser, String name, Map<String, String> attrs, Map<String, String> dirs) {
            super(parser);
            this.attrs = attrs;
            this.dirs = dirs;
            this.name = name;
        }

        @Override
        HeaderDeclaration parse(HeaderParser parser, String parseString) {
            StubHeaderDeclaration stubHeaderDeclaration = new StubHeaderDeclaration(this.name);
            stubHeaderDeclaration.getAttributes().putAll(this.attrs);
            stubHeaderDeclaration.getDirectives().putAll(this.dirs);
            return stubHeaderDeclaration;
        }
        
    }
    
    @Test
    public void constructor() throws Exception {   
        new TestParameterised(new StubHeaderParser(), "foo", null, null);
    }
    
    @Test
    public void resetFromParseString() throws Exception {
        // a=b;c:=d;e=f;g:=h
        Map<String, String> attrs = new HashMap<String, String>();
        attrs.put("a", "b");
        attrs.put("e", "f");
        Map<String, String> dirs = new HashMap<String, String>();
        dirs.put("c", "d");
        dirs.put("g", "h");
        TestParameterised testParameterised = new TestParameterised(new StubHeaderParser(), "foo", attrs, dirs);
        testParameterised.resetFromParseString("");
        
        TestParameterised testParameterised2 = new TestParameterised(new StubHeaderParser(), "foo", attrs, dirs);
        testParameterised2.resetFromParseString("");
        
        assertEquals(testParameterised, testParameterised2);
        assertEquals(testParameterised.hashCode(), testParameterised2.hashCode());
    }
    
    @Test
    public void equality() {
        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put("a", "b");
        
        Map<String, String> directives = new HashMap<String, String>();
        directives.put("c", "d");
        
        TestParameterised a = new TestParameterised(new StubHeaderParser(), "foo", attributes, directives);
        a.resetFromParseString(null);
        
        assertFalse(a.equals(null));
        assertFalse(a.equals(new Object()));
        assertTrue(a.equals(a));
        
        TestParameterised b = new TestParameterised(new StubHeaderParser(), "foo", attributes, directives);
        b.resetFromParseString(null);
        
        assertTrue(a.equals(b));
        assertTrue(b.equals(a));
        assertEquals(a.hashCode(), b.hashCode());
        
        TestParameterised c = new TestParameterised(new StubHeaderParser(), "bar", attributes, directives);
        c.resetFromParseString(null);
        
        assertFalse(a.equals(c));
        assertFalse(c.equals(a));
        
        TestParameterised d = new TestParameterised(new StubHeaderParser(), "bar", new HashMap<String, String>(), directives);
        d.resetFromParseString(null);
        
        assertFalse(c.equals(d));
        assertFalse(d.equals(c));
        
        TestParameterised e = new TestParameterised(new StubHeaderParser(), "bar", attributes, new HashMap<String, String>());
        e.resetFromParseString(null);
        
        assertFalse(c.equals(e));
        assertFalse(e.equals(c));
        
        TestParameterised f = new TestParameterised(new StubHeaderParser(), null, attributes, directives);
        f.resetFromParseString(null);
        
        assertFalse(a.equals(f));
        assertFalse(f.equals(a));
        
        TestParameterised g = new TestParameterised(new StubHeaderParser(), null, attributes, directives);
        g.resetFromParseString(null);
        
        assertTrue(f.equals(g));
        assertTrue(g.equals(f));
        assertEquals(f.hashCode(), g.hashCode());
    }
}
