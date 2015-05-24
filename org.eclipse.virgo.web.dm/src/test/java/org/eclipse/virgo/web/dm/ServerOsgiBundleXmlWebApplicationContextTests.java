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

package org.eclipse.virgo.web.dm;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import javax.servlet.ServletContext;

import org.eclipse.gemini.blueprint.context.ConfigurableOsgiBundleApplicationContext;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.springframework.mock.env.MockEnvironment;

public class ServerOsgiBundleXmlWebApplicationContextTests {

    @Test
    public void retrievalOfBundleContextFromServletContext() {
        try (ServerOsgiBundleXmlWebApplicationContext applicationContext = new ServerOsgiBundleXmlWebApplicationContext()) {

            ServletContext servletContext = createMock(ServletContext.class);
            BundleContext bundleContext = createMock(BundleContext.class);
            Bundle bundle = createNiceMock(Bundle.class);
            expect(bundleContext.getBundle()).andReturn(bundle);
            expect(servletContext.getAttribute(ServerOsgiBundleXmlWebApplicationContext.BUNDLE_CONTEXT_ATTRIBUTE)).andReturn(bundleContext);
            expect(bundle.getBundleContext()).andReturn(bundleContext);
            replay(servletContext, bundleContext, bundle);

            applicationContext.setServletContext(servletContext);

            verify(servletContext, bundleContext, bundle);
        }
    }

    @Test
    public void retrievalOfBundleContextFromApplicationContext() {
        BundleContext bundleContext = createNiceMock(BundleContext.class);
        ConfigurableOsgiBundleApplicationContext parent = createMock(ConfigurableOsgiBundleApplicationContext.class);
        Bundle bundle = createNiceMock(Bundle.class);
        expect(bundleContext.getBundle()).andReturn(bundle);
        expect(parent.getBundleContext()).andReturn(bundleContext);
        expect(parent.getEnvironment()).andReturn(new MockEnvironment());
        replay(parent, bundleContext, bundle);
        try (ServerOsgiBundleXmlWebApplicationContext applicationContext = new ServerOsgiBundleXmlWebApplicationContext(parent)) {

            ServletContext servletContext = createMock(ServletContext.class);
            expect(servletContext.getAttribute(ServerOsgiBundleXmlWebApplicationContext.BUNDLE_CONTEXT_ATTRIBUTE)).andReturn(null);

            replay(servletContext);

            applicationContext.setServletContext(servletContext);

            verify(servletContext, parent, bundleContext, bundle);
        }
    }
}
