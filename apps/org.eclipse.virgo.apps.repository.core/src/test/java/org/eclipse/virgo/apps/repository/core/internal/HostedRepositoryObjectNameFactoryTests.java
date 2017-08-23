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

package org.eclipse.virgo.apps.repository.core.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;

import org.eclipse.virgo.apps.repository.core.internal.HostedRepositoryObjectNameFactory;
import org.junit.Test;



/**
 * Unit tests for {@link HostedRepositoryObjectNameFactory}
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 * Test-safe
 *
 */
public class HostedRepositoryObjectNameFactoryTests {

    private static final String TEST_BEAN_MGT_DOMAIN = "com.dom";
    
    private final HostedRepositoryObjectNameFactory objectNameFactory = new HostedRepositoryObjectNameFactory(TEST_BEAN_MGT_DOMAIN);

    @Test
    public void testCreateValidObjectName() throws Exception {
        ObjectName objectName = objectNameFactory.createObjectName("repo-name-one");
        assertEquals("Objectname incorrect", TEST_BEAN_MGT_DOMAIN + ":name=repo-name-one,type=HostedRepository", objectName.getCanonicalName());
    }
    
    @Test(expected=MalformedObjectNameException.class)
    public void testCreateInvalidObjectName() throws Exception {
        ObjectName objectName = objectNameFactory.createObjectName("a: :b");
        String oName = objectName.getCanonicalName();
        assertFalse("Objectname '" + oName + "' should not have been generated", oName.startsWith(TEST_BEAN_MGT_DOMAIN + ":") && oName.endsWith("type=HostedRepository"));
    }
}
