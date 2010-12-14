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

package org.eclipse.virgo.kernel.shell.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Dictionary;
import java.util.Hashtable;

import org.eclipse.virgo.teststubs.osgi.framework.StubBundleContext;
import org.eclipse.virgo.teststubs.osgi.support.TrueFilter;
import org.junit.Test;


/**
 */
public class ServiceRegistryCommandProviderResolverTests {
    
    private final StubBundleContext bundleContext = new StubBundleContext();
    
    private final CommandProviderResolver commandProviderResolver = new ServiceRegistryCommandProviderResolver(this.bundleContext);
    
    @Test
    public void noCommandProvider() {
        this.bundleContext.addFilter(new TrueFilter("(osgi.command.function=*)"));
        assertNull(this.commandProviderResolver.getCommandProvider("command"));
    }
    
    @Test
    public void commandProvider() {
        this.bundleContext.addFilter(new TrueFilter("(osgi.command.function=*)"));
        Dictionary<String, String> properties = new Hashtable<String, String>();
        properties.put("osgi.command.function", "command");
        
        Object commandProvider = new Object();
        
        this.bundleContext.registerService("Foo", commandProvider, properties);
        assertEquals(commandProvider, this.commandProviderResolver.getCommandProvider("command"));
    }
    
    @Test
    public void noCommandProviderAsCommandFunctionDoesNotMatch() {
        this.bundleContext.addFilter(new TrueFilter("(osgi.command.function=*)"));
        Dictionary<String, String> properties = new Hashtable<String, String>();
        properties.put("osgi.command.function", "foo");
        
        Object commandProvider = new Object();
        
        this.bundleContext.registerService("Foo", commandProvider, properties);
        assertNull(this.commandProviderResolver.getCommandProvider("command"));
    }
}
