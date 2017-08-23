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

package org.eclipse.virgo.test.launcher;

import static org.junit.Assert.assertEquals;

import java.util.Properties;

import org.eclipse.virgo.test.launcher.PropertyPlaceholderResolver;
import org.junit.Test;


public class PropertyPlaceholderResolverTests {

    private final PropertyPlaceholderResolver resolver = new PropertyPlaceholderResolver();
    
    @Test
    public void testSimpleResolve() {
        Properties p = new Properties();
        p.setProperty("foo", "bar");
        p.setProperty("other", "Hello ${foo}");
        
        p = resolver.resolve(p);
        
        assertEquals("Hello bar", p.getProperty("other"));
    }
    
    @Test
    public void testDoubleResolve() {
        Properties p = new Properties();
        p.setProperty("foo", "bar");
        p.setProperty("bar", "baz");
        p.setProperty("other", "Hello ${foo} and ${bar}");
        
        p = resolver.resolve(p);
        
        assertEquals("Hello bar and baz", p.getProperty("other"));
    }
    
    @Test
    public void testChainResolve() {
        Properties p = new Properties();
        p.setProperty("config.dir", "${server.home}/config");
        p.setProperty("server.home", "/opt/dms");
        p.setProperty("repo.config", "${config.dir}/repo.config");
        
        p = resolver.resolve(p);
        
        assertEquals("/opt/dms/config", p.getProperty("config.dir"));
        assertEquals("/opt/dms/config/repo.config", p.getProperty("repo.config"));
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testCircularResolve() {
        Properties p = new Properties();
        p.setProperty("foo", "${bar}");
        p.setProperty("bar", "${foo}");
        
        p = resolver.resolve(p);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testTightCircularResolve() {
        Properties p = new Properties();
        p.setProperty("foo", "${foo}");
        
        p = resolver.resolve(p);
    }
    
    @Test
    public void testBackslashInValuesResolve() {
        Properties p = new Properties();
        p.setProperty("foo", "a\\b\\d\\");
        p.setProperty("bar", "[${foo}]");
        p.setProperty("vartochange", "here is ${foo} and ${bar} aha!");

        p = resolver.resolve(p);

        assertEquals("Backslashes not substituted properly", "here is a\\b\\d\\ and [a\\b\\d\\] aha!", p.getProperty("vartochange"));
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testLongerCircularResolve() {
        Properties p = new Properties();
        p.setProperty("foo", "${bar}");
        p.setProperty("bar", "${baz}");
        p.setProperty("baz", "${foo}");
        
        p = resolver.resolve(p);
    }
}
