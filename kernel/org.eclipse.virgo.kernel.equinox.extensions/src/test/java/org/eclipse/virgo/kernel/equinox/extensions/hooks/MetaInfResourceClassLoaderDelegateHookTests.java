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

package org.eclipse.virgo.kernel.equinox.extensions.hooks;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.eclipse.osgi.internal.loader.ModuleClassLoader;
import org.eclipse.virgo.test.stubs.framework.StubBundle;
import org.eclipse.virgo.test.stubs.framework.StubBundleContext;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Version;
import org.osgi.service.packageadmin.ExportedPackage;

/**
 */
@SuppressWarnings("deprecation")
public class MetaInfResourceClassLoaderDelegateHookTests {

    private final ModuleClassLoader classLoader =  createMock(ModuleClassLoader.class);

    private final StubBundle principleBundle = new StubBundle(3L, "principle", new Version(1, 0, 0), "");

    private final StubBundle installedBundleOne = new StubBundle(1L, "one", new Version(1, 0, 0), "");

    private final StubBundle installedBundleTwo = new StubBundle(2L, "two", new Version(1, 0, 0), "");

    private final StubBundleContext principleBundleContext = new StubBundleContext(this.principleBundle);

    private final UnitTestMetaInfResourceClassLoaderDelegateHook hook = new UnitTestMetaInfResourceClassLoaderDelegateHook(
        this.principleBundleContext);

    @Before
    public void initialise() {
        this.principleBundle.setBundleContext(this.principleBundleContext);

        expect(this.classLoader.getBundle()).andReturn(this.principleBundle).anyTimes();
        replay(this.classLoader);

        this.installedBundleOne.setState(Bundle.ACTIVE);
        this.installedBundleTwo.setState(Bundle.RESOLVED);

        this.principleBundleContext.addInstalledBundle(this.installedBundleOne);
        this.principleBundleContext.addInstalledBundle(this.installedBundleTwo);
    }

    @Test
    public void findResourceWithNoExportedPackages() throws FileNotFoundException {
        assertNull(hook.postFindResource("META-INF/the.resource", classLoader));
    }

    @Test
    public void findResourcesWithNoExportedPackages() throws FileNotFoundException {
        assertNull(hook.postFindResources("META-INF/the.resource", classLoader));
    }

    @Test
    public void findResource() throws FileNotFoundException, MalformedURLException {
        ExportedPackage exportedPackage = createMock(ExportedPackage.class);
        expect(exportedPackage.getImportingBundles()).andReturn(new Bundle[] { this.principleBundle });
        replay(exportedPackage);

        hook.exportedPackages.put(this.installedBundleTwo, new ExportedPackage[] { exportedPackage });

        URL resourceUrl = new URL("file:/resource");
        this.installedBundleTwo.addResource("META-INF/the.resource", resourceUrl);

        assertEquals(resourceUrl, this.hook.postFindResource("META-INF/the.resource", this.classLoader));
    }

    @Test
    public void findResources() throws FileNotFoundException, MalformedURLException {
        ExportedPackage exportedPackage = createMock(ExportedPackage.class);
        expect(exportedPackage.getImportingBundles()).andReturn(new Bundle[] { this.principleBundle }).anyTimes();
        replay(exportedPackage);

        hook.exportedPackages.put(this.installedBundleTwo, new ExportedPackage[] { exportedPackage });
        hook.exportedPackages.put(this.installedBundleOne, new ExportedPackage[] { exportedPackage });

        URL resourceUrlOne = new URL("file:/resource/one");
        URL resourceUrlTwo = new URL("file:/resource/two");

        this.installedBundleOne.addResources("META-INF/the.resource", createEnumeration(resourceUrlOne));
        this.installedBundleTwo.addResources("META-INF/the.resource", createEnumeration(resourceUrlTwo));

        Enumeration<?> postFindResources = this.hook.postFindResources("META-INF/the.resource", this.classLoader);
        assertNotNull(postFindResources);

        List<URL> results = new ArrayList<URL>();
        while (postFindResources.hasMoreElements()) {
            results.add((URL) postFindResources.nextElement());
        }

        assertEquals(2, results.size());
        assertTrue(results.contains(resourceUrlOne));
        assertTrue(results.contains(resourceUrlTwo));
    }

    private Enumeration<URL> createEnumeration(URL url) {
        Vector<URL> vector = new Vector<URL>();
        vector.add(url);
        return vector.elements();
    }

    private static final class UnitTestMetaInfResourceClassLoaderDelegateHook extends MetaInfResourceClassLoaderDelegateHook {

        public UnitTestMetaInfResourceClassLoaderDelegateHook(BundleContext systemBundleContext) {
            super(systemBundleContext, null);
        }

        private final Map<Bundle, ExportedPackage[]> exportedPackages = new HashMap<Bundle, ExportedPackage[]>();

        @Override
        protected ExportedPackage[] getExportedPackages(Bundle bundle) {
            return this.exportedPackages.get(bundle);
        }
    }
}
