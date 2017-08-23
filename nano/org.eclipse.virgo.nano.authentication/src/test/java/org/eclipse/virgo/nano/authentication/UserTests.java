/* Copyright (c) 2010 Olivier Girardot
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Olivier Girardot - initial contribution
 */

package org.eclipse.virgo.nano.authentication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.security.Principal;

import org.junit.Test;

/**
 * This class is for testing {@link User} class, 
 * an implementation of the {@link Principal} interface.
 */
public class UserTests {
    private static final String TEST_NAME = "john";
    private static final String ALT_TEST_NAME = "jane";
    private static final String TEST_PASSWORD = "#znuvdpo";
    private static final String ALT_TEST_PASSWORD = "#anjbjvdpo";
    private static final Role ADMIN_ROLE = new Role("Administrator");
    private static final Role TESTER_ROLE = new Role("Tester");
    
    @Test
    public void testGetName() {
        assertEquals(TEST_NAME, new User(TEST_NAME, TEST_PASSWORD).getName());
    }
    
    @Test
    public void testEqualsWithNull() {
        assertFalse(new User(TEST_NAME, TEST_PASSWORD).equals(null));
    }
    
    @Test
    public void testEqualsWithNullFields() {
        assertTrue(new User(null, null).equals(new User(null, null)));
    }
    
    @Test
    public void testEqualsWithAlmostNullFields() {
        assertFalse(new User(null, null).equals(new User(TEST_NAME, TEST_PASSWORD)));
        assertFalse(new User(null, null).equals(new User(TEST_NAME, null)));
        assertFalse(new User(null, null).equals(new User(null, TEST_PASSWORD)));
    }
    
    @Test
    public void testEqualsWithSameReference() {
        User testUser = new User(TEST_NAME, TEST_PASSWORD);
        assertTrue(testUser.equals(testUser));
    }
    
    @Test
    public void testEqualsWithObject() {
        User testUser = new User(TEST_NAME, TEST_PASSWORD);
        assertFalse(testUser.equals(new Object()));
    }
    
    @Test
    public void testEqualsWithSameNameDifferentCredentials() {
        User refUser = new User(TEST_NAME, TEST_PASSWORD);
        User candidateUser = new User(TEST_NAME, ALT_TEST_PASSWORD);
        assertFalse(refUser.equals(candidateUser));
    }
    
    @Test
    public void testEqualsWithDifferentRoles() {
        User refUser = new User(TEST_NAME, TEST_PASSWORD);
        refUser.addRole(ADMIN_ROLE);
        User candidateUser = new User(TEST_NAME, TEST_PASSWORD);
        candidateUser.addRole(TESTER_ROLE);
        assertFalse(refUser.equals(candidateUser));
    }
    
    @Test
    public void testEqualsWithSameCredentialDifferentNames() {
        User refUser = new User(TEST_NAME, TEST_PASSWORD);
        User candidateUser = new User(ALT_TEST_NAME, TEST_PASSWORD);
        assertFalse(refUser.equals(candidateUser));
    }
    
    @Test
    public void testEqualsWithSameRoles() {
        User refUser = new User(TEST_NAME, TEST_PASSWORD);
        refUser.addRole(ADMIN_ROLE);
        User candidateUser = new User(ALT_TEST_NAME, ALT_TEST_PASSWORD);
        candidateUser.addRole(ADMIN_ROLE);
        assertFalse(refUser.equals(candidateUser));
    }
    
    @Test
    public void testEqualsWithFullyDifferentUser() {
        User refUser = new User(TEST_NAME, TEST_PASSWORD);
        refUser.addRole(ADMIN_ROLE);
        User candidateUser = new User(ALT_TEST_NAME, ALT_TEST_PASSWORD);
        candidateUser.addRole(TESTER_ROLE);
        assertFalse(refUser.equals(candidateUser));
    }
    
    @Test
    public void testEqualsWithMeaningfullEquality() {
        User refUser = new User(TEST_NAME, TEST_PASSWORD);
        refUser.addRole(ADMIN_ROLE);
        User candidateUser = new User(TEST_NAME, TEST_PASSWORD);
        candidateUser.addRole(ADMIN_ROLE);
        assertTrue(refUser.equals(candidateUser));
    }
    
    @Test
    public void testHashCodeForSameObjects() {
        User refUser = new User(TEST_NAME, TEST_PASSWORD);
        refUser.addRole(ADMIN_ROLE);
        assertEquals(refUser.hashCode(), refUser.hashCode());
    }
    
    @Test
    public void testHashCodeForDifferentEqualObjects() {
        User refUser = new User(TEST_NAME, TEST_PASSWORD);
        refUser.addRole(ADMIN_ROLE);
        User candidateUser = new User(TEST_NAME, TEST_PASSWORD);
        candidateUser.addRole(ADMIN_ROLE);
        assertEquals(refUser.hashCode(), candidateUser.hashCode());
    }
    
    @Test
    public void testHashCodeForDifferentObjects() {
        User refUser = new User(TEST_NAME, TEST_PASSWORD);
        refUser.addRole(ADMIN_ROLE);
        User candidateUser = new User(ALT_TEST_NAME, ALT_TEST_PASSWORD);
        candidateUser.addRole(TESTER_ROLE);
        assertFalse(refUser.hashCode() == candidateUser.hashCode());
    }
    
    @Test
    public void testHashCodeForSameNullObjects() {
        User refUser = new User(null, null);
        refUser.addRole(null);
        User candidateUser = new User(null, null);
        candidateUser.addRole(null);
        assertTrue(refUser.hashCode() == candidateUser.hashCode());
    }
    
    @Test
    public void testToStringWithNulls() {
        User refUser = new User(null, null);
        assertNull(refUser.toString());
    }
    
    @Test
    public void testToString() {
        User refUser = new User(TEST_NAME, TEST_PASSWORD);
        assertEquals(TEST_NAME, refUser.toString());
    }
    
    @Test
    public void testAuthenticate() {
        User refUser = new User(TEST_NAME, TEST_PASSWORD);
        assertTrue(refUser.authenticate(TEST_PASSWORD));
        assertFalse(refUser.authenticate(ALT_TEST_PASSWORD));
    }
}