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

package org.eclipse.virgo.kernel.userregion.internal.equinox;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.virgo.kernel.userregion.internal.equinox.EquinoxBootDelegationHelper;
import org.junit.Test;


/**
 */
public class EquinoxBootDelegationHelperTests {

    @Test public void isBootDelegated() {
        EquinoxBootDelegationHelper helper = new EquinoxBootDelegationHelper("a.b.c.*, d.e.f");

        assertFalse(helper.isBootDelegated("g.h.MyClass"));
        assertTrue(helper.isBootDelegated("d.e.f.MyClass"));
        assertFalse(helper.isBootDelegated("d.e.f.g.MyClass"));
        assertTrue(helper.isBootDelegated("a.b.c.d.MyClass"));

        // I believe there's a bug in Equinox which we need to mirror. The boot delegation is
        // specified as (e.g.) org.eclipse.virgo.server.osgi.* which should mean that all classes that
        // reside in a subpackage of org.eclipse.virgo.server.osgi are boot delegated but those
        // that reside directly in org.eclipse.virgo.server.osgi are not boot delegated. However,
        // Equinox treats org.eclipse.virgo.server.osgi.* as meaning that everything in
        // org.eclipse.virgo.server.osgi and its subpackages is boot delegated.
        assertTrue(helper.isBootDelegated("a.b.c.MyClass"));
        assertFalse(helper.isBootDelegated("d.e.MyClass"));
        assertFalse(helper.isBootDelegated("a.b.MyClass"));
    }

    @Test public void testDelegationOfAllPackages() {
        EquinoxBootDelegationHelper helper = new EquinoxBootDelegationHelper("a.b.c.*, *");
        assertTrue(helper.isBootDelegated("d.e.f.MyClass"));
    }
}
