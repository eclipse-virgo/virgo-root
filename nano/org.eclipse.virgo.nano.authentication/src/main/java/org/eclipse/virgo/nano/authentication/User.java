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

import javax.security.auth.Subject;
import java.security.Principal;
import java.util.HashSet;
import java.util.Set;

/**
 * An implementation of {@link Principal} that represents a user
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe
 * 
 */
public final class User implements Principal {

    private final String name;

    private final String credential;

    private final Set<Role> roles = new HashSet<Role>();

    private final Object roleMonitor = new Object();

    public User(String name, String password) {
        this.name = name;
        this.credential = password;
    }

    public String getName() {
        return this.name;
    }

    /**
     * Checks that a candidate credential is authentic with respect to this {@link User}
     * 
     * @param candidateCredential The candidate credential to check
     * @return <code>true</code> if candidate credential matches, <code>false</code> otherwise
     */
    public boolean authenticate(String candidateCredential) {
        return this.credential.equals(candidateCredential);
    }

    /**
     * Adds all of the {@link Principal}s represented by this {@link User}
     * 
     * @param subject The {@link Subject} to configure
     */
    public void addPrincipals(Subject subject) {
        Set<Principal> principals = subject.getPrincipals();
        synchronized (this.roleMonitor) {
            principals.add(this);
            principals.addAll(this.roles);
        }
    }

    /**
     * Removes all of the {@link Principal}s represented by this {@link User}
     *
     * @param subject The {@link Subject} to configure}
     */
    public void removePrincipals(Subject subject) {
        Set<Principal> principals = subject.getPrincipals();
        synchronized (this.roleMonitor) {
            principals.remove(this);
            principals.removeAll(this.roles);
        }
    }

    /**
     * Adds a {@link Role} to this {@link User}
     *
     * @param role The {@link Role} this user has
     * @return <tt>true</tt> if this {@link User} did not already contain the specified {@link Role}
     */
    public boolean addRole(Role role) {
        synchronized (this.roleMonitor) {
            return this.roles.add(role);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((credential == null) ? 0 : credential.hashCode());
        result = prime * result + ((roles == null) ? 0 : roles.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof User)) {
            return false;
        }
        User other = (User) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (credential == null) {
            if (other.credential != null) {
                return false;
            }
        } else if (!credential.equals(other.credential)) {
            return false;
        }
        if (roles == null) {
            if (other.roles != null) {
                return false;
            }
        } else if (!roles.equals(other.roles)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
