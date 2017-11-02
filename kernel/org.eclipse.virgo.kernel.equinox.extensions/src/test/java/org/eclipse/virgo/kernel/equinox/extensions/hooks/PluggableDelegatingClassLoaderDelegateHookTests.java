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
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;
import static org.eclipse.virgo.kernel.equinox.extensions.hooks.PluggableDelegatingClassLoaderDelegateHook.getInstance;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.lang.reflect.Method;
import java.net.URL;

import org.eclipse.osgi.internal.hookregistry.ClassLoaderHook;
import org.eclipse.osgi.internal.loader.ModuleClassLoader;
import org.junit.Test;

/**
 * Tests for {@link PluggableDelegatingClassLoaderDelegateHook}
 */
public class PluggableDelegatingClassLoaderDelegateHookTests {

    private final ClassLoaderHook hook1 = createMock(ClassLoaderHook.class);

    private final ClassLoaderHook hook2 = createMock(ClassLoaderHook.class);

    private final PluggableDelegatingClassLoaderDelegateHook delegatingHook = getInstance();

    private final ModuleClassLoader classLoader = createMock(ModuleClassLoader.class);

    @Test
    public void preFindClass() throws Exception {
        performTest(ClassLoaderHook.class.getMethod("preFindClass", String.class, ModuleClassLoader.class), String.class);
    }

    @Test
    public void postFindClass() throws Exception {
        performTest(ClassLoaderHook.class.getMethod("postFindClass", String.class, ModuleClassLoader.class), String.class);
    }

    @Test
    public void preFindResource() throws Exception {
        performTest(ClassLoaderHook.class.getMethod("preFindResource", String.class, ModuleClassLoader.class), new URL("file:foo"));
    }

//    @Test
//    public void postFindResource() throws Exception {
//        performTest(ClassLoaderHook.class.getMethod("postFindResource", String.class, BundleClassLoader.class, BundleData.class),
//            new URL("file:foo"));
//    }
//
//    @Test
//    public void preFindLibrary() throws Exception {
//        performTest(ClassLoaderHook.class.getMethod("preFindLibrary", String.class, BundleClassLoader.class, BundleData.class), "library");
//    }
//
//    @Test
//    public void postFindLibrary() throws Exception {
//        performTest(ClassLoaderHook.class.getMethod("postFindLibrary", String.class, BundleClassLoader.class, BundleData.class), "library");
//    }
//
//    @Test
//    public void preFindResources() throws Exception {
//        performTest(ClassLoaderHook.class.getMethod("preFindResources", String.class, BundleClassLoader.class, BundleData.class),
//            new Vector<URL>().elements());
//    }
//
//    @Test
//    public void postFindResources() throws Exception {
//        performTest(ClassLoaderHook.class.getMethod("postFindResources", String.class, BundleClassLoader.class, BundleData.class),
//            new Vector<URL>().elements());
//    }

    private void performTest(Method method, Object mockResult) throws Exception {

        Object result = method.invoke(this.delegatingHook, "foo", this.classLoader);
        assertNull(result);

        delegatingHook.addDelegate(hook1);
        delegatingHook.addDelegate(hook2);

        expect(method.invoke(hook1, "foo", this.classLoader)).andReturn(null);
        expect(method.invoke(hook2, "foo", this.classLoader)).andReturn(null);

        replay(hook1, hook2);

        result = method.invoke(this.delegatingHook, "foo", this.classLoader);
        assertNull(result);

        verify(hook1, hook2);

        reset(hook1, hook2);

        expect(method.invoke(hook1, "foo", this.classLoader)).andReturn(mockResult);

        replay(hook1, hook2);

        result = method.invoke(this.delegatingHook, "foo", this.classLoader);
        assertEquals(mockResult, result);

        delegatingHook.removeDelegate(hook1);
        delegatingHook.removeDelegate(hook2);

        result = method.invoke(this.delegatingHook, "foo", this.classLoader);
        assertNull(result);

        verify(hook1, hook2);
    }
}
