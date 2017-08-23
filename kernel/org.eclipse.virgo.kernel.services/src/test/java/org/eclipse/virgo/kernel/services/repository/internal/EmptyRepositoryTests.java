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

package org.eclipse.virgo.kernel.services.repository.internal;

import org.eclipse.virgo.kernel.services.repository.internal.EmptyRepository;
import org.junit.Test;
import static org.junit.Assert.*;


/**
 */
public class EmptyRepositoryTests {

    @Test
    public void testContract() {
        EmptyRepository repo = new EmptyRepository();
        assertNotNull(repo.createQuery(null, null));
        assertNotNull(repo.createQuery(null, null, null));
        assertNotNull(repo.getName());
        assertNull(repo.get(null, null, null));
        assertNotNull(repo.createQuery(null, null).run());
    }
}
