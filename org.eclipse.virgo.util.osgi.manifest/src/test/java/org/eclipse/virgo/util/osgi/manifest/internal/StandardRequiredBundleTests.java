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

import java.util.ArrayList;

import org.eclipse.virgo.util.osgi.manifest.RequiredBundle;
import org.eclipse.virgo.util.osgi.manifest.Resolution;
import org.eclipse.virgo.util.osgi.manifest.VersionRange;
import org.eclipse.virgo.util.osgi.manifest.RequiredBundle.Visibility;
import org.eclipse.virgo.util.osgi.manifest.internal.StandardRequiredBundle;
import org.eclipse.virgo.util.osgi.manifest.parse.HeaderDeclaration;
import org.junit.Test;
import org.osgi.framework.Constants;


public class StandardRequiredBundleTests {

    @Test
    public void defaults() {
        RequiredBundle requiredBundle = new StandardRequiredBundle(new StubHeaderParser(), null);
        assertTrue(requiredBundle.getDirectives().isEmpty());
        assertTrue(requiredBundle.getAttributes().isEmpty());
        assertEquals(Resolution.MANDATORY, requiredBundle.getResolution());
        assertEquals(Visibility.PRIVATE, requiredBundle.getVisibility());
        assertNull(requiredBundle.toParseString());
    }
    
    @Test
    public void resetFromParseString() {
        StubHeaderParser parser = new StubHeaderParser();
        ArrayList<HeaderDeclaration> requireBundle = new ArrayList<HeaderDeclaration>();
        requireBundle.add(new StubHeaderDeclaration("x"));
        parser.setRequireBundle(requireBundle);
        RequiredBundle requiredBundle = new StandardRequiredBundle(parser, "");
        requiredBundle.resetFromParseString("foo");
    }

    @Test
    public void symbolicName() {
        RequiredBundle requiredBundle = new StandardRequiredBundle(new StubHeaderParser(), "foo");
        assertEquals("foo", requiredBundle.getBundleSymbolicName());
        assertEquals("foo", requiredBundle.toParseString());
        requiredBundle.setBundleSymbolicName("bar");
        assertEquals("bar", requiredBundle.getBundleSymbolicName());
        assertEquals("bar", requiredBundle.toParseString());
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void setNullSymbolicName() {
        RequiredBundle requiredBundle = new StandardRequiredBundle(new StubHeaderParser(), "");
        requiredBundle.setBundleSymbolicName(null);
    }

    @Test
    public void resolution() {
        RequiredBundle requiredBundle = new StandardRequiredBundle(new StubHeaderParser(), "foo");
        requiredBundle.setResolution(Resolution.OPTIONAL);
        assertEquals(Resolution.OPTIONAL, requiredBundle.getResolution());
        assertEquals("foo;resolution:=\"optional\"", requiredBundle.toParseString());
        requiredBundle.setResolution(Resolution.MANDATORY);
        assertEquals(Resolution.MANDATORY, requiredBundle.getResolution());
        assertEquals("foo", requiredBundle.toParseString());
    }
    
    @Test
    public void setNullResolution() {
        RequiredBundle requiredBundle = new StandardRequiredBundle(new StubHeaderParser(), "foo");
        requiredBundle.setResolution(null);
        assertEquals(Resolution.MANDATORY, requiredBundle.getResolution());
        assertEquals("foo", requiredBundle.toParseString());
    }
    
    @Test
    public void visibility() {
        RequiredBundle requiredBundle = new StandardRequiredBundle(new StubHeaderParser(), "foo");
        requiredBundle.setVisibility(Visibility.REEXPORT);
        assertEquals(Visibility.REEXPORT, requiredBundle.getVisibility());
        assertEquals("foo;visibility:=\"reexport\"", requiredBundle.toParseString());
        requiredBundle.setVisibility(Visibility.PRIVATE);
        assertEquals(Visibility.PRIVATE, requiredBundle.getVisibility());
        assertEquals("foo", requiredBundle.toParseString());
    }
    
    @Test
    public void setNullVisibility() {
        RequiredBundle requiredBundle = new StandardRequiredBundle(new StubHeaderParser(), "foo");
        requiredBundle.setVisibility(null);
        assertEquals(Visibility.PRIVATE, requiredBundle.getVisibility());
        assertEquals("foo", requiredBundle.toParseString());
    }
    
    @Test
    public void bundleVersionAttribute() {
        RequiredBundle requiredBundle = new StandardRequiredBundle(new StubHeaderParser(), "foo");
        requiredBundle.setBundleSymbolicName("foo");
        requiredBundle.getAttributes().put(Constants.BUNDLE_VERSION_ATTRIBUTE, new VersionRange("[1.2.3,2.3.4)").toParseString());
        assertEquals("foo;bundle-version=\"[1.2.3, 2.3.4)\"", requiredBundle.toParseString());
        
        requiredBundle.getAttributes().remove(Constants.BUNDLE_VERSION_ATTRIBUTE);
        assertEquals("foo", requiredBundle.toParseString());
    }
    
    @Test
    public void setBundleVersionAttribute() {
        RequiredBundle requiredBundle = new StandardRequiredBundle(new StubHeaderParser(), "foo");
        requiredBundle.setBundleSymbolicName("foo");      
        
        VersionRange versionRange = new VersionRange("[1.2.3,2.3.4)");
        requiredBundle.setBundleVersion(versionRange);
        assertEquals(versionRange, requiredBundle.getBundleVersion());
        assertEquals("foo;bundle-version=\"[1.2.3, 2.3.4)\"", requiredBundle.toParseString());                
        
        requiredBundle.setBundleVersion(null);
        assertEquals(VersionRange.NATURAL_NUMBER_RANGE, requiredBundle.getBundleVersion());
        assertEquals("foo", requiredBundle.toParseString());
    }
}
