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

package org.eclipse.virgo.kernel.deployer.app.spring;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Dictionary;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;
import org.springframework.context.ApplicationContext;
import org.eclipse.gemini.blueprint.context.ConfigurableOsgiBundleApplicationContext;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import org.eclipse.virgo.kernel.deployer.app.spring.UserRegionModuleContextAccessor;
import org.eclipse.virgo.kernel.module.ModuleContext;
import org.eclipse.virgo.kernel.module.ModuleContextAccessor;
import org.eclipse.virgo.test.stubs.framework.StubBundle;
import org.eclipse.virgo.test.stubs.framework.StubBundleContext;

/**
 */
public class UserRegionModuleContextAccessorTests {

    private static final String BUNDLE_SYMBOLIC_NAME = "bundle";

    private ModuleContextAccessor moduleContextAccessor;

    private StubBundle stubBundle;

    private StubBundleContext stubBundleContext;

    private ApplicationContext mockBasicApplicationContext;
    
    private interface TestAppCtx extends ConfigurableOsgiBundleApplicationContext, ModuleContext {}
    
    private TestAppCtx mockTestAppCtx;
    
    private static final Filter NEGATIVE_FILTER = new Filter() {
        
        @Override
        public boolean match(ServiceReference<?> reference) {
            return false;
        }
        
        @Override
        public boolean match(Dictionary<String, ?> dictionary) {
            return false;
        }
        
        @Override
        public boolean matchCase(Dictionary<String, ?> dictionary) {
            return false;
        }

        @Override
        public boolean matches(Map<String, ?> map) {
            return false;
        }
        
    };
    
private static final Filter POSITIVE_FILTER = new Filter() {
        
    @Override
    public boolean match(ServiceReference<?> reference) {
        return true;
    }
    
    @Override
    public boolean match(Dictionary<String, ?> dictionary) {
        return true;
    }
    
    @Override
    public boolean matchCase(Dictionary<String, ?> dictionary) {
        return true;
    }

    @Override
    public boolean matches(Map<String, ?> map) {
        return true;
    }
        
    };

    @Before
    public void setUp() throws Exception {
        this.moduleContextAccessor = new UserRegionModuleContextAccessor();

        this.stubBundle = new StubBundle(BUNDLE_SYMBOLIC_NAME, Version.emptyVersion);

        this.stubBundleContext = new StubBundleContext();

        this.stubBundle.setBundleContext(this.stubBundleContext);
        
        this.mockBasicApplicationContext= createMock(ApplicationContext.class);
        
        this.mockTestAppCtx = createMock(TestAppCtx.class);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testUnpublishedApplicationContext() {
        this.stubBundleContext.addFilter("(Bundle-SymbolicName=" + BUNDLE_SYMBOLIC_NAME + ")", NEGATIVE_FILTER);
        assertNull(this.moduleContextAccessor.getModuleContext(this.stubBundle));
    }

    @Test
    public void testUnpublishedApplicationContextWithOtherServicesPresent() {
        this.stubBundleContext.addFilter("(Bundle-SymbolicName=" + BUNDLE_SYMBOLIC_NAME + ")", NEGATIVE_FILTER);
        this.stubBundleContext.registerService(String.class.getName(), "", null);
        assertNull(this.moduleContextAccessor.getModuleContext(this.stubBundle));
    }
    
    @Test
    public void testPublishedApplicationContextOfWrongType() {
        this.stubBundleContext.addFilter("(Bundle-SymbolicName=" + BUNDLE_SYMBOLIC_NAME + ")", POSITIVE_FILTER);
        this.stubBundleContext.registerService(ApplicationContext.class.getName(), this.mockBasicApplicationContext, null);
        assertNull(this.moduleContextAccessor.getModuleContext(this.stubBundle));
    }
    
    @Test
    public void testPublishedApplicationContextOfCorrectType() {
        this.stubBundleContext.addFilter("(Bundle-SymbolicName=" + BUNDLE_SYMBOLIC_NAME + ")", POSITIVE_FILTER);
        this.stubBundleContext.registerService(ApplicationContext.class.getName(), this.mockTestAppCtx, null);
        expect(this.mockTestAppCtx.getBundleContext()).andReturn(this.stubBundleContext);
        replay(this.mockTestAppCtx);
        assertNotNull(this.moduleContextAccessor.getModuleContext(this.stubBundle));
        verify(this.mockTestAppCtx);
    }

}
