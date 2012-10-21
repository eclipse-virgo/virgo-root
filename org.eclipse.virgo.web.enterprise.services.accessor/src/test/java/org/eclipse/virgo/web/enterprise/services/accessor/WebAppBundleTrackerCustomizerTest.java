
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
        System.setProperty(WebAppBundleTrackerCustomizer.API_BUNDLES,// NOSONAR
            "test;bundle-version=[1.0,2.0),test1;bundle-version=1.0,test2,test3;1.0,test5;bundle-version=[1.0,1.0]");// NOSONAR
        System.setProperty(WebAppBundleTrackerCustomizer.IMPL_BUNDLES, "test4;bundle-version=[1.0,2.0),test1;bundle-version=1.0,test2,test3;1.0");// NOSONAR

        Dictionary<String, String> dictionary = new Hashtable<String, String>();

        Bundle bundle1 = createMock(Bundle.class);
        expect(bundle1.getHeaders()).andReturn(dictionary).anyTimes();
        expect(bundle1.getSymbolicName()).andReturn("test").andReturn("test").andReturn("test4").andReturn("test4").andReturn("test").times(5).andReturn(
            "test4").times(5);
        expect(bundle1.getVersion()).andReturn(new Version("1.1.1")).times(2).andReturn(new Version("1.0"));

        replay(bundle1);

        WebAppBundleClassLoaderDelegateHook webAppBundleClassLoaderDelegateHook = new WebAppBundleClassLoaderDelegateHook();
        WebAppBundleTrackerCustomizer webAppBundleTrackerCustomizer = createWebAppBundleTrackerCustomizer(webAppBundleClassLoaderDelegateHook);

        dictionary.put(WebAppBundleTrackerCustomizer.HEADER_EXPOSED_CONTENT_TYPE, WebAppBundleTrackerCustomizer.HEADER_EXPOSED_CONTENT_TYPE_API_VALUE);
        webAppBundleTrackerCustomizer.addingBundle(bundle1, null);
        assertTrue(webAppBundleClassLoaderDelegateHook.getApiBundles().contains(bundle1));

        dictionary.put(WebAppBundleTrackerCustomizer.HEADER_EXPOSED_CONTENT_TYPE,
            WebAppBundleTrackerCustomizer.HEADER_EXPOSED_CONTENT_TYPE_IMPL_VALUE);
        webAppBundleTrackerCustomizer.addingBundle(bundle1, null);
        assertTrue(webAppBundleClassLoaderDelegateHook.getApiBundles().contains(bundle1));

        dictionary.remove(WebAppBundleTrackerCustomizer.HEADER_EXPOSED_CONTENT_TYPE);

        dictionary.put(WebAppBundleTrackerCustomizer.HEADER_EXPOSE_ADDITIONAL_API, "test;bundle-version=[1.0,2.0),test1;bundle-version=1.0");
        webAppBundleTrackerCustomizer.addingBundle(bundle1, null);
        assertTrue(webAppBundleClassLoaderDelegateHook.getImplBundles().contains(bundle1));

        dictionary.put(WebAppBundleTrackerCustomizer.HEADER_EXPOSE_ADDITIONAL_API, "test;bundle-version=[1.2,1.5),test1;bundle-version=1.5");
        webAppBundleTrackerCustomizer.addingBundle(bundle1, null);
        assertTrue(webAppBundleClassLoaderDelegateHook.getImplBundles().contains(bundle1));

        assertTrue(webAppBundleTrackerCustomizer.getExposeAdditionalApiBundles().get("test").toString().equals("[1.2.0, 1.5.0)"));
        assertTrue(webAppBundleTrackerCustomizer.getExposeAdditionalApiBundles().get("test1").toString().equals("[1.5.0, oo)"));

        webAppBundleTrackerCustomizer.removedBundle(bundle1, null, null);
        assertTrue(!webAppBundleClassLoaderDelegateHook.getApiBundles().contains(bundle1));
        assertTrue(!webAppBundleClassLoaderDelegateHook.getImplBundles().contains(bundle1));

        verify(bundle1);
    }

    private WebAppBundleTrackerCustomizer createWebAppBundleTrackerCustomizer(WebAppBundleClassLoaderDelegateHook webAppBundleClassLoaderDelegateHook) {
        return new WebAppBundleTrackerCustomizer(webAppBundleClassLoaderDelegateHook);
    }
}
