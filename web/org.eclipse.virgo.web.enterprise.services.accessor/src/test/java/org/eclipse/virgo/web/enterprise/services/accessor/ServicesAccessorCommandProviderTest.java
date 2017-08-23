/*******************************************************************************
 * Copyright (c) 2012 SAP AG
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   SAP AG - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.web.enterprise.services.accessor;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;

public class ServicesAccessorCommandProviderTest {

    @Test
    public void testListApi() {
        testListBundles("api");
    }

    @Test
    public void testListImpl() {
        testListBundles("impl");
    }

    private void testListBundles(String type) {
        Bundle bundle1 = prepareBundleMock(1, type + "1", "1.0.0", false, null, null);
        Bundle bundle2 = prepareBundleMock(2, type + "2", "2.0.0", false, null, null);
        Bundle bundle3 = prepareBundleMock(3, type + "3", "3.0.0", false, null, null);

        EasyMock.replay(bundle1, bundle2, bundle3);

        WebAppBundleClassloaderCustomizer classloaderCustomizer = new WebAppBundleClassloaderCustomizer();
        WebAppBundleClassLoaderDelegateHook hook = classloaderCustomizer.getWebAppBundleClassLoaderDelegateHook();
        Bundle[] bundles = new Bundle[] { bundle1, bundle2, bundle3 };
        if ("api".equals(type)) {
            for (Bundle bundle : bundles) {
                hook.addApiBundle(bundle);
            }
        } else if ("impl".equals(type)) {
            for (Bundle bundle : bundles) {
                hook.addImplBundle(bundle);
            }
        }

        TestSystemOut ci = new TestSystemOut();
        System.setOut(ci);
        
        ServicesAccessorCommandProvider cmdProvider = new ServicesAccessorCommandProvider();
        cmdProvider.bindCustomizer(classloaderCustomizer);
        cmdProvider.list_exposed_content("-" + type);

        List<Object> output = ci.getOutput();
        Assert.assertEquals(12, output.size());

        Assert.assertEquals("Wrong bundle id of " + type + "1", Long.valueOf(1), output.get(3));
        Assert.assertEquals("Wrong name/version of " + type + "1", type + "1_1.0.0", output.get(5));
        Assert.assertEquals("Wrong bundle id of " + type + "2", Long.valueOf(2), output.get(6));
        Assert.assertEquals("Wrong name/version of " + type + "2", type + "2_2.0.0", output.get(8));
        Assert.assertEquals("Wrong bundle id of " + type + "3", Long.valueOf(3), output.get(9));
        Assert.assertEquals("Wrong name/version of " + type + "3", type + "3_3.0.0", output.get(11));
    }

    @Test
    public void testClashes() {
        Bundle bundle1 = prepareBundleMock(1, "api", "1.0.0", true, null, null);
        Bundle bundle2 = prepareBundleMock(2, "api", "2.0.0", true, null, null);
        Bundle bundle3 = prepareBundleMock(3, "api", "3.0.0", true, null, null);
        Bundle bundle = prepareBundleMock(4, "bundle", "1.0.0", true, "Expose-AdditionalAPI", "api;bundle-version=3.0.0");

        EasyMock.replay(bundle1, bundle2, bundle3, bundle);

        WebAppBundleClassloaderCustomizer classloaderCustomizer = new WebAppBundleClassloaderCustomizer();
        WebAppBundleTrackerCustomizer trackerCustomizer = classloaderCustomizer.getWebAppBundleTrackerCustomizer();
        Bundle[] bundles = new Bundle[] { bundle, bundle1, bundle2, bundle3 };
        for (Bundle bun : bundles) {
            trackerCustomizer.addingBundle(bun, null);
        }
        trackerCustomizer.processAdditionalAPIBundles(bundles);

        TestSystemOut ci = new TestSystemOut();
        System.setOut(ci);
        
        ServicesAccessorCommandProvider cmdProvider = new ServicesAccessorCommandProvider();
        cmdProvider.bindCustomizer(classloaderCustomizer);
        cmdProvider.list_exposed_content("-clash");

        List<Object> output = ci.getOutput();
        Assert.assertEquals(7, output.size());
        Assert.assertEquals("Clashing APIs:", output.get(0));
        Assert.assertEquals("Clashing bundles for BSN api:", output.get(1));
        Assert.assertEquals("api_1.0.0", output.get(2));
        Assert.assertEquals("api_2.0.0", output.get(3));
        Assert.assertEquals("api_3.0.0", output.get(4));
        Assert.assertEquals("Chosen bundle: api_3.0.0", output.get(5));
        Assert.assertEquals("Clashing Implementations:", output.get(6));
    }

    private Bundle prepareBundleMock(long bundleId, String bsn, String version, boolean isHeader, String headerName, String headerValue) {
        Bundle bundle = EasyMock.createMock(Bundle.class);
        if (isHeader) {
            Dictionary<String, String> header = new Hashtable<String, String>();
            if (headerName != null && headerValue != null) {
                header.put(headerName, headerValue);
            }
            EasyMock.expect(bundle.getHeaders()).andReturn(header).anyTimes();
        }
        EasyMock.expect(bundle.getBundleId()).andReturn(bundleId);
        EasyMock.expect(bundle.getSymbolicName()).andReturn(bsn).anyTimes();
        EasyMock.expect(bundle.getVersion()).andReturn(new Version(version)).anyTimes();
        return bundle;
    }
}
