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

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import static java.lang.String.format;

/**
 * A factory for a {@link CredentialStore} that reads a {@link Properties} object for authentication information.  The keys of
 * this {@link Properties} object indicate user name and role information.  A user name and password are identified with
 * a <code>user.</code> prefix and a role to user mapping is represented with a <code>role.</code> prefix.
 * <p />
 *
 * <pre>
 * user.admin=springsource
 * role.superuser=admin
 * </pre>
 *
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe
 * 
 */
final class PropertiesFileCredentialStoreFactory {

    private static final String USER_PREFIX = "user.";

    private static final String ROLE_PREFIX = "role.";

    static CredentialStore create(Properties properties) {
        Map<String, User> credentials = getUsers(properties);
        assignRoles(properties, credentials);
        return new ImmutableCredentialStore(credentials);
    }

    private static Map<String, User> getUsers(Properties properties) {
        Map<String, User> users = new HashMap<String, User>();

        for(String key : properties.stringPropertyNames()) {
            if (key.startsWith(USER_PREFIX)) {
                String username = key.substring(USER_PREFIX.length()).trim();
                String password = properties.getProperty(key);
                users.put(username, new User(username, password));
            }
        }

        return users;
    }

    private static void assignRoles(Properties properties, Map<String, User> credentials) {
        for(String key : properties.stringPropertyNames()) {
            if (key.startsWith(ROLE_PREFIX)) {
                String rolename = key.substring(ROLE_PREFIX.length()).trim();
                Role role = new Role(rolename);
                assignRole(role, (String) properties.get(key), credentials);
            }
        }
    }

    private static void assignRole(Role role, String usernames, Map<String, User> credentials) {
        for(String username : usernames.split(",")) {
            User user = credentials.get(username);
            if (user != null) {
                user.addRole(role);
            } else {
                throw new IllegalArgumentException(format("Could not assign role '%s' to user '%s', as user does not exist", role.getName(),
                    username));
            }
        }
    }

    private static class ImmutableCredentialStore implements CredentialStore {

        private final Map<String, User> credentials;

        public ImmutableCredentialStore(Map<String, User> credentials) {
            this.credentials = credentials;
        }

        public User getUser(String name) {
            return this.credentials.get(name);
        }

    }
}
