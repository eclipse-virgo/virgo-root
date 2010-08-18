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

import org.eclipse.virgo.util.osgi.manifest.RequireBundle;
import org.eclipse.virgo.util.osgi.manifest.RequiredBundle;
import org.eclipse.virgo.util.osgi.manifest.internal.StandardRequireBundle;
import org.eclipse.virgo.util.osgi.manifest.parse.HeaderDeclaration;
import org.junit.Ignore;
import org.junit.Test;


public class StandardRequireBundleTests {

    @Test
    public void defaults() {
        assertEquals(0, new StandardRequireBundle(new StubHeaderParser()).getRequiredBundles().size());
    }

    @Test
    public void requiredBundleAddition() {
        StubHeaderParser parser = new StubHeaderParser();
        List<HeaderDeclaration> headers = new ArrayList<HeaderDeclaration>();
        headers.add(new StubHeaderDeclaration());

        parser.setRequireBundle(headers);
        RequireBundle requireBundle = new StandardRequireBundle(parser);
        assertEquals(0, requireBundle.getRequiredBundles().size());

        RequiredBundle newRequiredBundle = requireBundle.addRequiredBundle("bar");
        List<RequiredBundle> requiredBundles = requireBundle.getRequiredBundles();
        assertEquals(1, requiredBundles.size());
        assertEquals("bar", requiredBundles.get(0).getBundleSymbolicName());
        assertEquals(newRequiredBundle, requiredBundles.get(0));
    }

    @Test
    public void requiredBundleListAddition() {
        StubHeaderParser parser = new StubHeaderParser();
        List<HeaderDeclaration> headers = new ArrayList<HeaderDeclaration>();
        headers.add(new StubHeaderDeclaration());

        parser.setRequireBundle(headers);
        RequireBundle requireBundle = new StandardRequireBundle(parser);

        RequiredBundle newRequiredBundle = requireBundle.addRequiredBundle("foo");
        List<RequiredBundle> requiredBundles = requireBundle.getRequiredBundles();
        requiredBundles.clear();
        assertEquals(0, requiredBundles.size());
        requiredBundles.add(newRequiredBundle);
        assertEquals(1, requiredBundles.size());
        assertEquals("foo", requiredBundles.get(0).getBundleSymbolicName());

        {
            StubHeaderParser parser2 = new StubHeaderParser();
            List<HeaderDeclaration> headers2 = new ArrayList<HeaderDeclaration>();
            headers2.add(new StubHeaderDeclaration());

            parser2.setRequireBundle(headers2);
            requiredBundles.add(new StandardRequireBundle(parser2).addRequiredBundle("bar"));
            assertEquals(2, requiredBundles.size());
            assertEquals("foo", requiredBundles.get(0).getBundleSymbolicName());
            assertEquals("bar", requiredBundles.get(1).getBundleSymbolicName());
        }
    }
    
    @Test
    public void parse() {
        StubHeaderParser parser = new StubHeaderParser();
        List<HeaderDeclaration> headers = new ArrayList<HeaderDeclaration>();
        headers.add(new StubHeaderDeclaration("foo"));

        parser.setRequireBundle(headers);
        RequireBundle requireBundle = new StandardRequireBundle(parser);
        requireBundle.resetFromParseString("bar");
        List<RequiredBundle> requiredBundles = requireBundle.getRequiredBundles();
        assertEquals(1, requiredBundles.size());
        assertEquals("foo", requiredBundles.get(0).getBundleSymbolicName());
    }

    @Test
    @Ignore("[DMS-2887]")
    public void requiredBundleDuplicateListAddition() {
        StubHeaderParser parser = new StubHeaderParser();
        List<HeaderDeclaration> headers = new ArrayList<HeaderDeclaration>();
        headers.add(new StubHeaderDeclaration());

        parser.setRequireBundle(headers);
        RequireBundle requireBundle = new StandardRequireBundle(parser);

        RequiredBundle newRequiredBundle = requireBundle.addRequiredBundle("foo");
        List<RequiredBundle> requiredBundles = requireBundle.getRequiredBundles();
        requiredBundles.add(newRequiredBundle);
        assertEquals(1, requiredBundles.size()); // or expect an exception
    }
}
