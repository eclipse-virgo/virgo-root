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

package org.eclipse.virgo.nano.authentication;

import static org.junit.Assert.*;

import org.eclipse.virgo.nano.authentication.CredentialStore;
import org.eclipse.virgo.nano.authentication.PropertiesFileCredentialStoreFactory;
import org.eclipse.virgo.nano.authentication.Role;
import org.eclipse.virgo.nano.authentication.User;
import org.junit.Test;

import javax.security.auth.Subject;
import java.security.Principal;
import java.util.Properties;

public class PropertiesFileCredentialStoreFactoryTests {

    @Test
    public void success() {
        Properties properties = new Properties();
        properties.put("user.admin", "springsource");
        properties.put("role.superuser", "admin");

        CredentialStore credentialStore = PropertiesFileCredentialStoreFactory.create(properties);
        User user = credentialStore.getUser("admin");
        assertNotNull(user);
        assertTrue(user.authenticate("springsource"));

        Subject subject = new Subject();
        user.addPrincipals(subject);
        assertEquals(2, subject.getPrincipals().size());
        assertContainsRole(subject, "superuser");
    }

    @Test
    public void twoUsersOneRole() {
        Properties properties = new Properties();
        properties.put("user.admin1", "springsource");
        properties.put("user.admin2", "springsource");
        properties.put("role.superuser", "admin1");

        CredentialStore credentialStore = PropertiesFileCredentialStoreFactory.create(properties);
        User user1 = credentialStore.getUser("admin1");
        assertNotNull(user1);
        assertTrue(user1.authenticate("springsource"));
        Subject subject1 = new Subject();
        user1.addPrincipals(subject1);
        assertEquals(2, subject1.getPrincipals().size());
        assertContainsRole(subject1, "superuser");

        User user2 = credentialStore.getUser("admin2");
        assertNotNull(user2);
        assertTrue(user2.authenticate("springsource"));
        Subject subject2 = new Subject();
        user2.addPrincipals(subject2);
        assertEquals(1, subject2.getPrincipals().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void roleWithIllegalUser() {
        Properties properties = new Properties();
        properties.put("user.admin", "springsource");
        properties.put("role.superuser", "admin1");

        PropertiesFileCredentialStoreFactory.create(properties);
    }

    private void assertContainsRole(Subject subject, String roleName) {
        boolean foundRole = false;
        for(Principal principal : subject.getPrincipals()) {
            if(principal instanceof Role) {
                if(principal.getName().equals(roleName)) {
                    foundRole = true;
                    break;
                }
            }
        }
        assertTrue(foundRole);
    }

}
