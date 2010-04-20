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

package org.eclipse.virgo.medic.eventlog.impl;

import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.eclipse.virgo.medic.eventlog.impl.StandardLocaleResolver;
import org.junit.Test;

public class StandardLocaleResolverTests {

    @Test
    public void getLocale() {
        assertEquals(Locale.getDefault(), new StandardLocaleResolver().getLocale());
    }
}
