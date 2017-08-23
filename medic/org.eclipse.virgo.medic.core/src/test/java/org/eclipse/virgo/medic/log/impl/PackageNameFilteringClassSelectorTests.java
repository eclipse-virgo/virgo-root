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

package org.eclipse.virgo.medic.log.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Arrays;

import org.eclipse.virgo.medic.log.impl.PackageNameFilteringClassSelector;
import org.junit.Test;

public class PackageNameFilteringClassSelectorTests {

    @Test
    public void noFiltering() {
        PackageNameFilteringClassSelector classSelector = new PackageNameFilteringClassSelector(Arrays.asList(""));
        Class<?> selected = classSelector.select(new Class<?>[] { getClass() });
        assertEquals(getClass(), selected);
    }

    @Test
    public void filterToEmpty() {
        PackageNameFilteringClassSelector classSelector = new PackageNameFilteringClassSelector(Arrays.asList(getClass().getPackage().getName()));
        Class<?> selected = classSelector.select(new Class<?>[] { getClass() });
        assertNull(selected);
    }

    @Test
    public void filtered() {
        PackageNameFilteringClassSelector classSelector = new PackageNameFilteringClassSelector(Arrays.asList(getClass().getPackage().getName()));
        Class<?> selected = classSelector.select(new Class<?>[] { getClass(), java.util.ArrayList.class });
        assertEquals(java.util.ArrayList.class, selected);
    }
}
