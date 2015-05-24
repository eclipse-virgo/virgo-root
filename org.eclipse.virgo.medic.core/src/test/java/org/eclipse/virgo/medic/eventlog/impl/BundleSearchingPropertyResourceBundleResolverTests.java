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

package org.eclipse.virgo.medic.eventlog.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.PropertyResourceBundle;
import java.util.Vector;

import org.junit.Test;

import org.eclipse.virgo.medic.eventlog.impl.BundleSearchingPropertyResourceBundleResolver;
import org.eclipse.virgo.medic.eventlog.impl.PropertyResourceBundleResolver;
import org.eclipse.virgo.test.stubs.framework.FindEntriesDelegate;
import org.eclipse.virgo.test.stubs.framework.StubBundle;

public class BundleSearchingPropertyResourceBundleResolverTests {

    private final PropertyResourceBundleResolver resourceBundleResolver = new BundleSearchingPropertyResourceBundleResolver();

    @Test
    public void entryNotFound() {
        StubBundle bundle = new StubBundle().setFindEntriesDelegate(new FindEntriesDelegate() {

            public Enumeration<URL> findEntries(String path, String pattern, boolean recurse) {
                return null;
            }
        });
        List<PropertyResourceBundle> resourceBundles = this.resourceBundleResolver.getResourceBundles(bundle, "foo.properties");
        assertNotNull(resourceBundles);
        assertEquals(0, resourceBundles.size());
    }

    @Test
    public void nonExistentEntry() {
        StubBundle bundle = new StubBundle().setFindEntriesDelegate(new FindEntriesDelegate() {

            public Enumeration<URL> findEntries(String path, String pattern, boolean recurse) {
                try {
                    Vector<URL> urls = new Vector<URL>();
                    urls.add(new File("does/not/exist").toURI().toURL());
                    return urls.elements();
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        List<PropertyResourceBundle> resourceBundles = this.resourceBundleResolver.getResourceBundles(bundle, "foo.properties");
        assertNotNull(resourceBundles);
        assertEquals(0, resourceBundles.size());
    }

    @Test
    public void existingEntry() {
        StubBundle bundle = new StubBundle().setFindEntriesDelegate(new FindEntriesDelegate() {

            public Enumeration<URL> findEntries(String path, String pattern, boolean recurse) {
                try {
                    Vector<URL> urls = new Vector<URL>();
                    urls.add(new File("src/test/resources/messages.properties").toURI().toURL());
                    return urls.elements();
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        List<PropertyResourceBundle> resourceBundles = this.resourceBundleResolver.getResourceBundles(bundle, "foo.properties");
        assertNotNull(resourceBundles);
        assertEquals(1, resourceBundles.size());
        assertEquals("Bar", resourceBundles.get(0).getString("ABC123"));
    }
}
