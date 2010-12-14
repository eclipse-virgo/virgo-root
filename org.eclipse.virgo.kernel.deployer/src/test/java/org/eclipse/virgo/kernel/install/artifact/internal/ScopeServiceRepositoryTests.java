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

package org.eclipse.virgo.kernel.install.artifact.internal;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.Dictionary;
import java.util.Hashtable;

import org.eclipse.virgo.kernel.install.artifact.ScopeServiceRepository;
import org.junit.Test;


/**
 */
public class ScopeServiceRepositoryTests {

    private static final String TEST_SCOPE = "test";

    @Test
    public void testServiceWithSingleTypeNoFilter() throws Exception {
        ScopeServiceRepository repository = new StandardScopeServiceRepository();
        repository.recordService(TEST_SCOPE, new String[]{String.class.getName()}, null);
        assertTrue(repository.scopeHasMatchingService(TEST_SCOPE, String.class.getName(), null));
        assertFalse(repository.scopeHasMatchingService(TEST_SCOPE, Integer.class.getName(), null));
    }
    
    @Test
    public void testServiceWithSingleTypeFiltered() throws Exception {
        ScopeServiceRepository repository = new StandardScopeServiceRepository();
        Dictionary<String, Object> p = new Hashtable<String, Object>();
        p.put("foo", "bar");
        repository.recordService(TEST_SCOPE, new String[]{String.class.getName()}, p);
        assertTrue(repository.scopeHasMatchingService(TEST_SCOPE, String.class.getName(), "(foo=bar)"));
        assertFalse(repository.scopeHasMatchingService(TEST_SCOPE, Integer.class.getName(), "(foo=bar)"));
    }
    
    @Test
    public void testServiceWithMultiTypeNoFilter() throws Exception {
        ScopeServiceRepository repository = new StandardScopeServiceRepository();
        repository.recordService(TEST_SCOPE, new String[]{String.class.getName(), Serializable.class.getName()}, null);
        assertTrue(repository.scopeHasMatchingService(TEST_SCOPE, String.class.getName(), null));
        assertTrue(repository.scopeHasMatchingService(TEST_SCOPE, Serializable.class.getName(), null));
        assertFalse(repository.scopeHasMatchingService(TEST_SCOPE, Integer.class.getName(), null));
    }
    
    @Test
    public void testServiceWithMultiTypeFilter() throws Exception {
        ScopeServiceRepository repository = new StandardScopeServiceRepository();
        Dictionary<String, Object> p = new Hashtable<String, Object>();
        p.put("foo", "bar");
        repository.recordService(TEST_SCOPE, new String[]{String.class.getName(), Serializable.class.getName()}, p);
        assertTrue(repository.scopeHasMatchingService(TEST_SCOPE, String.class.getName(), "(foo=bar)"));
        assertTrue(repository.scopeHasMatchingService(TEST_SCOPE, Serializable.class.getName(), "(foo=bar)"));
        assertFalse(repository.scopeHasMatchingService(TEST_SCOPE, Integer.class.getName(), "(foo=bar)"));
    }
    
    @Test
    public void testClearScope() throws Exception {
        ScopeServiceRepository repository = new StandardScopeServiceRepository();
        repository.recordService(TEST_SCOPE, new String[]{String.class.getName()}, null);
        assertTrue(repository.scopeHasMatchingService(TEST_SCOPE, String.class.getName(), null));
        repository.clearScope(TEST_SCOPE);
        assertFalse(repository.scopeHasMatchingService(TEST_SCOPE, String.class.getName(), null));
    }
    
    @Test
    public void testNullNameWithObjectClassFilter() throws Exception {
        ScopeServiceRepository repository = new StandardScopeServiceRepository();
        repository.recordService(TEST_SCOPE, new String[]{String.class.getName()}, null);
        assertTrue(repository.scopeHasMatchingService(TEST_SCOPE, null, "(objectClass=java.lang.String)"));
    }
}
