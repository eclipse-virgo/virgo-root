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

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Properties;

/**
 * {@link KernelAuthenticationConfiguration} encapsulates reading the kernel user properties from the file with path in
 * the system property named by constant FILE_LOCATION.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread safe.
 * 
 */
final class KernelAuthenticationConfiguration {

    public static final String FILE_LOCATION_PROPERTY = "org.eclipse.virgo.kernel.authentication.file";

    public static final String DEFAULT_USERNAME = "admin";

    public static final String DEFAULT_PASSWORD = "springsource";

    private static final String USER_PREFIX = "user.";

    private static final String ADMIN_ROLE = "role.admin";

    private final String password;

    private final String userName;

    public KernelAuthenticationConfiguration() {
        this(getProperties(FILE_LOCATION_PROPERTY));
    }

    KernelAuthenticationConfiguration(Properties props) {
        String userName = DEFAULT_USERNAME;
        String password = DEFAULT_PASSWORD;

        if (props != null) {
            String adminUserName = props.getProperty(ADMIN_ROLE);
            if (adminUserName != null) {
                String adminPassword = props.getProperty(USER_PREFIX + adminUserName);
                if (adminPassword != null) {
                    userName = adminUserName;
                    password = adminPassword;
                }
            }
        }

        this.userName = userName;
        this.password = password;
    }

    String getPassword() {
        return password;
    }

    String getUserName() {
        return userName;
    }

    private static Properties getProperties(String fileLocationProperty) {
        String fileLocation = System.getProperty(fileLocationProperty);
        if (fileLocation == null) {
            return null;
        }

        try (Reader reader = new InputStreamReader(new FileInputStream(fileLocation), UTF_8)) {
            Properties properties = new Properties();
            properties.load(reader);
            return properties;
        } catch (IOException e) {
            return null;
        }
    }

}
