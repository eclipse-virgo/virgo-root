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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertTrue;

import java.util.Dictionary;
import java.util.Hashtable;

import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;

public class WebAppBundleTrackerCustomizerTest {

    @Test
    public void testAddingBundleRemovedBundle() {
        System.setProperty(WebAppBundleTrackerCustomizer.API_BUNDLES,
            "test;bundle-version=[1.0,2.0),test1;bundle-version=1.0,test2,test3;1.0,test5;bundle-version=[1.0,1.0]");
        System.setProperty(WebAppBundleTrackerCustomizer.IMPL_BUNDLES, "test4;bundle-version=[1.0,2.0),test1;bundle-version=1.0,test2,test3;1.0");

        Dictionary<String, String> dictionary1 = new Hashtable<String, String>();
        Dictionary<String, String> dictionary2 = new Hashtable<String, String>();

        Bundle bundle1 = createMock(Bundle.class);
        expect(bundle1.getHeaders()).andReturn(dictionary1).anyTimes();
        expect(bundle1.getSymbolicName()).andReturn("test").times(9);
        expect(bundle1.getVersion()).andReturn(new Version("1.1.1"));
        replay(bundle1);
        
        Bundle bundle2 = createMock(Bundle.class);
        expect(bundle2.getHeaders()).andReturn(dictionary2).anyTimes();
        expect(bundle2.getSymbolicName()).andReturn("test4").times(11);
        expect(bundle2.getVersion()).andReturn(new Version("1.0"));
        replay(bundle2);

        WebAppBundleClassLoaderDelegateHook webAppBundleClassLoaderDelegateHook = new WebAppBundleClassLoaderDelegateHook();
        WebAppBundleTrackerCustomizer webAppBundleTrackerCustomizer = createWebAppBundleTrackerCustomizer(webAppBundleClassLoaderDelegateHook);

        dictionary1.put(WebAppBundleTrackerCustomizer.HEADER_EXPOSED_CONTENT_TYPE, WebAppBundleTrackerCustomizer.HEADER_EXPOSED_CONTENT_TYPE_API_VALUE);
        webAppBundleTrackerCustomizer.addingBundle(bundle1, null);
        assertTrue(webAppBundleClassLoaderDelegateHook.getApiBundles().contains(bundle1));

        dictionary2.put(WebAppBundleTrackerCustomizer.HEADER_EXPOSED_CONTENT_TYPE,
            WebAppBundleTrackerCustomizer.HEADER_EXPOSED_CONTENT_TYPE_IMPL_VALUE);
        webAppBundleTrackerCustomizer.addingBundle(bundle2, null);
        assertTrue(webAppBundleClassLoaderDelegateHook.getImplBundles().contains(bundle2));

        dictionary1.remove(WebAppBundleTrackerCustomizer.HEADER_EXPOSED_CONTENT_TYPE);
        dictionary2.remove(WebAppBundleTrackerCustomizer.HEADER_EXPOSED_CONTENT_TYPE);

        dictionary1.put(WebAppBundleTrackerCustomizer.HEADER_EXPOSE_ADDITIONAL_API, "test;bundle-version=[1.0,2.0),test1;bundle-version=1.0");
        webAppBundleTrackerCustomizer.addingBundle(bundle1, null);
        assertTrue(webAppBundleClassLoaderDelegateHook.getApiBundles().contains(bundle1));

        dictionary2.put(WebAppBundleTrackerCustomizer.HEADER_EXPOSE_ADDITIONAL_API, "test;bundle-version=[1.2,1.5),test1;bundle-version=1.5");
        webAppBundleTrackerCustomizer.addingBundle(bundle2, null);
        assertTrue(webAppBundleClassLoaderDelegateHook.getImplBundles().contains(bundle2));

        assertTrue(webAppBundleTrackerCustomizer.getExposeAdditionalApiBundles().get("test").toString().equals("[1.2.0, 1.5.0)"));
        assertTrue(webAppBundleTrackerCustomizer.getExposeAdditionalApiBundles().get("test1").toString().equals("[1.5.0, oo)"));

        webAppBundleTrackerCustomizer.removedBundle(bundle1, null, null);
        webAppBundleTrackerCustomizer.removedBundle(bundle2, null, null);
        assertTrue(!webAppBundleClassLoaderDelegateHook.getApiBundles().contains(bundle1));
        assertTrue(!webAppBundleClassLoaderDelegateHook.getImplBundles().contains(bundle2));

        verify(bundle1);
        verify(bundle2);
    }

    private WebAppBundleTrackerCustomizer createWebAppBundleTrackerCustomizer(WebAppBundleClassLoaderDelegateHook webAppBundleClassLoaderDelegateHook) {
        return new WebAppBundleTrackerCustomizer(webAppBundleClassLoaderDelegateHook);
    }
}
