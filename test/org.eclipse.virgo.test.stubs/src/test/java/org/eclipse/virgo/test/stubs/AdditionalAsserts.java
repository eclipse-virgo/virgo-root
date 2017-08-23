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

package org.eclipse.virgo.test.stubs;

import static org.junit.Assert.assertTrue;

public final class AdditionalAsserts {

    public static void assertContains(String substring, String string) {
        assertTrue(String.format("String '%s' did not contain substring '%s'", string, substring), string.contains(substring));
    }
}
