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

package org.eclipse.virgo.test.stubs.service.cm.aspects;

import org.osgi.service.cm.Configuration;

import org.eclipse.virgo.test.stubs.service.cm.StubConfiguration;

/**
 * Ensures that a configuration has not been deleted before method execution
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe
 * 
 */

public final aspect DeletedConfiguration {

    /**
     * Ensures that a {@link Configuration} has not been deleted before allowing method invocation
     * 
     * @param configuration The {@link Configuration} to check
     * @throws IllegalStateException if the {@link Configuration} has been deleted
     */
    before(StubConfiguration configuration) : 
                this(configuration) &&
                within(org.eclipse.virgo.test.stubs.service.cm.StubConfiguration) &&
                execution(* org.osgi.service.cm.Configuration.*(..)) &&
                !execution(* org.eclipse.virgo.test.stubs.service.cm.StubConfiguration.equals(Object)) &&
                !execution(int org.eclipse.virgo.test.stubs.service.cm.StubConfiguration.hashCode()) &&
                !execution(java.lang.String org.eclipse.virgo.test.stubs.service.cm.StubConfiguration.toString()) {
        if (configuration.getDeleted()) {
            throw new IllegalStateException("This configuration has been deleted");
        }
    }

}
