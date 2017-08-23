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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNull;

import java.security.Principal;

import org.junit.Test;

/**
 * This class is for testing {@link Role} class, 
 * an implementation of the {@link Principal} interface.
 */
public class RoleTests {

    private static final String ADMINISTRATOR_ROLE = "Administrator";
    private static final String TESTER_ROLE = "Tester";

    @Test
    public void testEqualsWithNull() {
        assertFalse(new Role(ADMINISTRATOR_ROLE).equals(null));
    }
    
    @Test
    public void testEqualsWithSameReference() {
        Role adminRole = new Role(ADMINISTRATOR_ROLE);
        assertTrue(adminRole.equals(adminRole));
    }
    
    @Test
    public void testHashCodeWithSameNullParameters() {
        Role nullRole = new Role(null);
        assertEquals((new Role(null)).hashCode(), nullRole.hashCode());
    }
    
    @Test
    public void testHashCodeWithSameNonNullParameters() {
        Role adminRole = new Role(ADMINISTRATOR_ROLE);
        assertEquals((new Role(ADMINISTRATOR_ROLE)).hashCode(), adminRole.hashCode());
    }
    
    @Test
    public void testEqualsWithSameNullParameters() {
        assertTrue(new Role(null).equals(new Role(null)));
    }
    
    @Test
    public void testNotEqualsWithDifferentParameters() {
        assertFalse(new Role(null).equals(new Role(ADMINISTRATOR_ROLE)));
    }
    
    @Test
    public void testMeaningFullEquals() {
        Role adminRole = new Role(ADMINISTRATOR_ROLE);
        Role secAdminRole = new Role(ADMINISTRATOR_ROLE);
        assertTrue(adminRole.equals(secAdminRole));
    }
    
    @Test
    public void testEqualsWithDifferentParameters() {
        Role adminRole = new Role(ADMINISTRATOR_ROLE);
        Role secAdminRole = new Role(TESTER_ROLE);
        assertFalse(adminRole.equals(secAdminRole));
    }
    
    @Test
    public void testEqualsWithWrongType() {
        assertFalse(new Role(null).equals(new Object()));
    }
    
    @Test
    public void testToStringWithNull() {
        assertNull(new Role(null).toString());
    }
}
