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

import org.eclipse.virgo.apps.repository.core.HostedRepositoryInfo;
import org.eclipse.virgo.apps.repository.core.internal.StandardHostedRepositoryInfo;
import org.junit.Test;


/**
 * Tests for {@link StandardHostedRepositoryInfo}
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * Test-safe
 * 
 */
public class StandardHostedRepositoryInfoTests {

    @Test
    public void testCreateStandardHostedRepositoryInfo() {
        String uriPrefix = "uriPrefix";
        String localName = "localName";
        HostedRepositoryInfo hri = new StandardHostedRepositoryInfo(uriPrefix, localName);
        assertEquals("uriPrefix not on info mBean", uriPrefix, hri.getUriPrefix());
        assertEquals("localRepositoryName not on info mBean", localName, hri.getLocalRepositoryName());
    }
}
