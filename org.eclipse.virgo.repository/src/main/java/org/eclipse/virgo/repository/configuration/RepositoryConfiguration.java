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

package org.eclipse.virgo.repository.configuration;

import org.eclipse.virgo.repository.Repository;
import org.eclipse.virgo.repository.RepositoryFactory;

/**
 * <code>RepositoryConfiguration</code> provides the information that is used by a {@link RepositoryFactory} to create a
 * {@link Repository}.
 * 
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * Thread-safe, and concrete subclass <strong>must</strong> be thread-safe.
 * 
 * 
 * @see Repository
 * @see RepositoryFactory
 */
public abstract class RepositoryConfiguration {

    private final String name;

    private final String mBeanDomain;

    protected RepositoryConfiguration(String name, String mBeanDomain) {
        this.name = name;
        this.mBeanDomain = mBeanDomain;
        validate();
    }

    /**
     * The name of the repository
     * 
     * @return the repository's name
     */
    public String getName() {
        return this.name;
    }

    /**
     * The domain for management beans to be registered under
     * 
     * @return the mBean domain name
     */
    public String getMBeanDomain() {
        return this.mBeanDomain;
    }

    /**
     * Check that name is valid
     */
    private final void validate() {
        char[] chars = this.name.toCharArray();
        for (char ch : chars) {
            if ('A' <= ch && ch <= 'Z') {
                continue;
            }
            if ('a' <= ch && ch <= 'z') {
                continue;
            }
            if ('0' <= ch && ch <= '9') {
                continue;
            }
            if (ch == '_' || ch == '-') {
                continue;
            }
            throw new IllegalArgumentException("invalid repository name '" + this.name + "'");
        }
    }
}
