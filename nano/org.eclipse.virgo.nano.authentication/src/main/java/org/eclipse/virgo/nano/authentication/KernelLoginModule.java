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

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;
import java.util.Properties;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

/**
 * An implementation of {@link LoginModule} that reads a properties file for authentication information. The location of
 * the properties file is read from the system property <code>org.eclipse.virgo.kernel.authentication.file</code>. If
 * this property is not set, then instantiation of this {@link LoginModule} will fail.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Not threadsafe
 * 
 */
public final class KernelLoginModule implements LoginModule {

    public static final String FILE_LOCATION = "org.eclipse.virgo.kernel.authentication.file";

    private final CredentialStore credentialStore;

    private volatile Subject subject;

    private volatile CallbackHandler callbackHandler;

    private volatile User user;

    private volatile boolean authenticationResult;

    public KernelLoginModule() {
        this.credentialStore = PropertiesFileCredentialStoreFactory.create(getProperties());
    }

    KernelLoginModule(CredentialStore credentialStore) {
        this.credentialStore = credentialStore;
    }

    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
    }

    public boolean login() throws LoginException {
        // We do not actually care about these prompts but they must be populated
        NameCallback nameCallback = new NameCallback("username");
        PasswordCallback passwordCallback = new PasswordCallback("password", false);

        try {
            this.callbackHandler.handle(new Callback[] { nameCallback, passwordCallback });
        } catch (UnsupportedCallbackException e) {
            throw new FailedLoginException("Unable to get username and password");
        } catch (IOException e) {
            throw new FailedLoginException("Unable to get username and password");
        }

        this.user = this.credentialStore.getUser(nameCallback.getName());
        this.authenticationResult = this.user.authenticate(new String(passwordCallback.getPassword()));

        if (authenticationResult) {
            return true;
        }
        throw new FailedLoginException("Credentials did not match");
    }

    public boolean commit() throws LoginException {
        if (!this.authenticationResult) {
            this.user = null;
            return false;
        }

        this.user.addPrincipals(this.subject);
        return true;
    }

    public boolean abort() throws LoginException {
        this.user = null;
        return true;
    }

    public boolean logout() throws LoginException {
        this.user.removePrincipals(this.subject);
        this.subject = null;
        this.user = null;
        return true;
    }

    private Properties getProperties() {
        String fileLocation = System.getProperty(FILE_LOCATION);
        if (fileLocation == null) {
            throw new IllegalArgumentException(String.format("System property '%s' must be set to use the %s JAAS Login Module", FILE_LOCATION,
                this.getClass().getCanonicalName()));
        }

        try (Reader reader = new InputStreamReader(new FileInputStream(fileLocation), UTF_8)) {
            Properties properties = new Properties();
            properties.load(reader);
            return properties;
        } catch (IOException e) {
            throw new IllegalArgumentException(String.format("Unable to load properties file from '%s'", fileLocation), e);
        }
    }
}
