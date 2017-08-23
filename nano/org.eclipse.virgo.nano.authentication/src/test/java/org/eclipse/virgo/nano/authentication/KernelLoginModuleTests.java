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

import org.eclipse.virgo.nano.authentication.CredentialStore;
import org.eclipse.virgo.nano.authentication.KernelLoginModule;
import org.eclipse.virgo.nano.authentication.User;
import org.junit.Test;
import static org.junit.Assert.assertFalse;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.callback.*;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class KernelLoginModuleTests {

    private final StubCredentialStore credentialStore = new StubCredentialStore();

    private final Subject subject = new Subject();

    private final KernelLoginModule loginModule = new KernelLoginModule(credentialStore);

    @Test
    public void loginSuccess() throws LoginException {
        this.credentialStore.setUser(new User("username", "password"));
        this.loginModule.initialize(this.subject, new StubCallbackHandler(), null, null);
        assertTrue(this.loginModule.login());
    }

    @Test(expected = FailedLoginException.class)
    public void loginFailure() throws LoginException {
        this.credentialStore.setUser(new User("username", "different-password"));
        this.loginModule.initialize(this.subject, new StubCallbackHandler(), null, null);
        this.loginModule.login();
    }

    @Test
    public void loginSuccessCommit() throws LoginException {
        this.credentialStore.setUser(new User("username", "password"));
        this.loginModule.initialize(this.subject, new StubCallbackHandler(), null, null);
        assertTrue(this.loginModule.login());

        assertTrue(this.loginModule.commit());
        assertTrue(this.subject.getPrincipals().size() != 0);
    }

    @Test
    public void loginFailureCommit() throws LoginException {
        this.credentialStore.setUser(new User("username", "different-password"));
        this.loginModule.initialize(this.subject, new StubCallbackHandler(), null, null);
        try {
            this.loginModule.login();
        } catch (LoginException e) {
        }

        assertFalse(this.loginModule.commit());
        assertTrue(this.subject.getPrincipals().size() == 0);
    }

    @Test
    public void abort() throws LoginException {
        assertTrue(this.loginModule.abort());
    }

    @Test
    public void logout() throws LoginException {
        this.credentialStore.setUser(new User("username", "password"));
        this.loginModule.initialize(this.subject, new StubCallbackHandler(), null, null);
        assertTrue(this.loginModule.login());

        assertTrue(this.loginModule.commit());
        assertTrue(this.subject.getPrincipals().size() != 0);

        assertTrue(this.loginModule.logout());
        assertTrue(this.subject.getPrincipals().size() == 0);
    }

    @Test
    public void getProperties() {
        System.setProperty(KernelLoginModule.FILE_LOCATION, "src/test/resources/user.properties");
        new KernelLoginModule();
        System.clearProperty(KernelLoginModule.FILE_LOCATION);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getPropertiesNoSystemProperty() {
        new KernelLoginModule();
    }

    private static class StubCredentialStore implements CredentialStore {

        private volatile User user;

        public void setUser(User user) {
            this.user = user;
        }

        public User getUser(String name) {
            return this.user;
        }
    }

    private static class StubCallbackHandler implements CallbackHandler {

        private final String username;

        private final String password;

        public StubCallbackHandler() {
            this("username", "password");
        }

        public StubCallbackHandler(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
            for(Callback callback : callbacks) {
                if(callback instanceof NameCallback) {
                    ((NameCallback) callback).setName(this.username);
                } else if (callback instanceof PasswordCallback) {
                    ((PasswordCallback) callback).setPassword(this.password.toCharArray());
                }
            }
        }
    }

}
