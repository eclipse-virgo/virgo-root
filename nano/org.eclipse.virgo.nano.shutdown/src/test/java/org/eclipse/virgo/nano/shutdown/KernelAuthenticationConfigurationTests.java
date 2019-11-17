/*******************************************************************************
 * This file is part of the Virgo Web Server.
 *
 * Copyright (c) 2010 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SpringSource, a division of VMware - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.virgo.nano.shutdown;

import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class KernelAuthenticationConfigurationTests {

    @Test
    public void testSystemProperty() {
        try {
            System.setProperty(KernelAuthenticationConfiguration.FILE_LOCATION_PROPERTY, "src/test/resources/test.users.properties");
            KernelAuthenticationConfiguration kac = new KernelAuthenticationConfiguration();

            assertEquals("testuser", kac.getUserName());
            assertEquals("testpw", kac.getPassword());
        } finally {
            System.clearProperty(KernelAuthenticationConfiguration.FILE_LOCATION_PROPERTY);
        }
    }

    @Test
    public void testBadFile() {
        try {
            System.setProperty(KernelAuthenticationConfiguration.FILE_LOCATION_PROPERTY, "src/test/resources/users.nosuch.properties");
            KernelAuthenticationConfiguration kac = new KernelAuthenticationConfiguration();
            assertDefaults(kac);
        } finally {
            System.clearProperty(KernelAuthenticationConfiguration.FILE_LOCATION_PROPERTY);
        }
    }

    private void assertDefaults(KernelAuthenticationConfiguration kac) {
        assertEquals(KernelAuthenticationConfiguration.DEFAULT_USERNAME, kac.getUserName());
        assertEquals(KernelAuthenticationConfiguration.DEFAULT_PASSWORD, kac.getPassword());
    }

    @Test
    public void testMissingProperty() {
        KernelAuthenticationConfiguration kac = new KernelAuthenticationConfiguration();
        assertDefaults(kac);
    }
    
    @Test
    public void testMissingProperties() {
        KernelAuthenticationConfiguration kac = new KernelAuthenticationConfiguration(null);
        assertDefaults(kac);
    }

    @Test
    public void testValidProperties() {
        Properties props = new Properties();
        props.put("user.u", "p");
        props.put("role.admin", "u");
        KernelAuthenticationConfiguration kac = new KernelAuthenticationConfiguration(props);
        assertEquals("u", kac.getUserName());
        assertEquals("p", kac.getPassword());
    }
    
    @Test
    public void testMissingRole() {
        Properties props = new Properties();
        props.put("user.u", "p");
        KernelAuthenticationConfiguration kac = new KernelAuthenticationConfiguration(props);
        assertDefaults(kac);
    }

    @Test
    public void testMissingAdmin() {
        Properties props = new Properties();
        props.put("user.u", "p");
        props.put("role.admin", "v");
        KernelAuthenticationConfiguration kac = new KernelAuthenticationConfiguration(props);
        assertDefaults(kac);
    }

    @Test
    public void testEmptyUsername() {
        Properties props = new Properties();
        props.put("user.", "pw");
        props.put("role.admin", "");
        KernelAuthenticationConfiguration kac = new KernelAuthenticationConfiguration(props);
        assertEquals("", kac.getUserName());
        assertEquals("pw", kac.getPassword());

    }
    
    @Test
    public void testEmptyPassword() {
        Properties props = new Properties();
        props.put("user.u", "");
        props.put("role.admin", "u");
        KernelAuthenticationConfiguration kac = new KernelAuthenticationConfiguration(props);
        assertEquals("u", kac.getUserName());
        assertEquals("", kac.getPassword());
    }
    
    @Test
    public void testNonStringKey() {
        Properties props = new Properties();
        props.put(new Object(), "");
        props.put("user." + KernelAuthenticationConfiguration.DEFAULT_USERNAME, KernelAuthenticationConfiguration.DEFAULT_PASSWORD);
        props.put("role.admin", KernelAuthenticationConfiguration.DEFAULT_USERNAME);
        KernelAuthenticationConfiguration kac = new KernelAuthenticationConfiguration(props);
        assertDefaults(kac);
    }
    
    @Test
    public void testNonStringValue() {
        Properties props = new Properties();
        props.put("user.u", new Object());
        KernelAuthenticationConfiguration kac = new KernelAuthenticationConfiguration(props);
        assertDefaults(kac);
    }

    
    
}
