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
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.osgi.internal.hookregistry.ClassLoaderHook;
import org.eclipse.virgo.kernel.equinox.extensions.hooks.PluggableDelegatingClassLoaderDelegateHook;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.SynchronousBundleListener;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.framework.wiring.BundleWire;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.service.component.ComponentContext;

public class WebAppBundleClassloaderCustomizerTest {

    @Test
    public void testExtendClassLoaderChain() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        ComponentContext ctx = createMock(ComponentContext.class);

        BundleContext bundleContext = createMock(BundleContext.class);

        Bundle apiBundle = createMock(Bundle.class);
        Bundle implBundle = createMock(Bundle.class);
        Bundle webBundle = createMock(Bundle.class);
        BundleRevision bundleRevision = createMock(BundleRevision.class);
        BundleWiring bundleWiring = createMock(BundleWiring.class);

        Dictionary<String, String> apiHeaders = new Hashtable<String, String>();
        apiHeaders.put(WebAppBundleTrackerCustomizer.HEADER_EXPOSED_CONTENT_TYPE, WebAppBundleTrackerCustomizer.HEADER_EXPOSED_CONTENT_TYPE_API_VALUE);
        Dictionary<String, String> implHeaders = new Hashtable<String, String>();
        implHeaders.put(WebAppBundleTrackerCustomizer.HEADER_EXPOSED_CONTENT_TYPE,
            WebAppBundleTrackerCustomizer.HEADER_EXPOSED_CONTENT_TYPE_IMPL_VALUE);

        expect(apiBundle.getHeaders()).andReturn(apiHeaders).anyTimes();
        expect(implBundle.getHeaders()).andReturn(implHeaders).anyTimes();
        expect(apiBundle.getState()).andReturn(Bundle.ACTIVE);
        expect(implBundle.getState()).andReturn(Bundle.ACTIVE);
        expect(apiBundle.getSymbolicName()).andReturn("api").anyTimes();
        expect(implBundle.getSymbolicName()).andReturn("impl").anyTimes();
        expect(ctx.getBundleContext()).andReturn(bundleContext);
        expect(bundleContext.getBundles()).andReturn(new Bundle[] { apiBundle, implBundle });

        bundleContext.addBundleListener(isA(SynchronousBundleListener.class));
        expectLastCall();
        bundleContext.removeBundleListener(isA(SynchronousBundleListener.class));
        expectLastCall();

        expect(webBundle.adapt(BundleRevision.class)).andReturn(bundleRevision);
        expect(bundleRevision.getWiring()).andReturn(bundleWiring);
        expect(bundleWiring.getRequiredWires(BundleRevision.PACKAGE_NAMESPACE)).andReturn(new ArrayList<BundleWire>());
        expect(bundleWiring.getRequiredWires(BundleRevision.BUNDLE_NAMESPACE)).andReturn(new ArrayList<BundleWire>());

        replay(ctx, bundleContext, apiBundle, implBundle, webBundle, bundleRevision, bundleWiring);

        Field field = PluggableDelegatingClassLoaderDelegateHook.getInstance().getClass().getDeclaredField("delegates");
        field.setAccessible(true);

        WebAppBundleClassloaderCustomizer webAppBundleClassloaderCustomizer = createWebAppBundleClassloaderCustomizer();

        Field field2 = webAppBundleClassloaderCustomizer.getWebAppBundleClassLoaderDelegateHook().getClass().getDeclaredField("webAppBundles");
        field2.setAccessible(true);

        webAppBundleClassloaderCustomizer.activate(ctx);

        assertTrue(webAppBundleClassloaderCustomizer.getWebAppBundleClassLoaderDelegateHook().getApiBundles().contains(apiBundle));
        assertTrue(webAppBundleClassloaderCustomizer.getWebAppBundleClassLoaderDelegateHook().getImplBundles().contains(implBundle));
        assertTrue(((List<ClassLoaderHook>) field.get(PluggableDelegatingClassLoaderDelegateHook.getInstance())).contains(webAppBundleClassloaderCustomizer.getWebAppBundleClassLoaderDelegateHook()));

        assertTrue(webAppBundleClassloaderCustomizer.extendClassLoaderChain(webBundle).length == 0);
        assertTrue(webAppBundleClassloaderCustomizer.getWebAppBundleTrackerCustomizer().getExposeAdditionalApiBundles().isEmpty());
        assertTrue(((Map<Bundle, Set<String>>) field2.get(webAppBundleClassloaderCustomizer.getWebAppBundleClassLoaderDelegateHook())).containsKey(webBundle));

        webAppBundleClassloaderCustomizer.deactivate(ctx);

        assertTrue(webAppBundleClassloaderCustomizer.getWebAppBundleClassLoaderDelegateHook().getApiBundles().isEmpty());
        assertTrue(webAppBundleClassloaderCustomizer.getWebAppBundleClassLoaderDelegateHook().getImplBundles().isEmpty());
        assertTrue(((List<ClassLoaderHook>) field.get(PluggableDelegatingClassLoaderDelegateHook.getInstance())).isEmpty());

        verify(ctx, bundleContext, apiBundle, implBundle, webBundle, bundleRevision, bundleWiring);
    }

    private WebAppBundleClassloaderCustomizer createWebAppBundleClassloaderCustomizer() {
        return new WebAppBundleClassloaderCustomizer();
    }

}
