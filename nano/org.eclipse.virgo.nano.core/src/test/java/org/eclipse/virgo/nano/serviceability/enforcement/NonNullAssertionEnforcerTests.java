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

package org.eclipse.virgo.nano.serviceability.enforcement;

import internal.AssertingService;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.eclipse.virgo.nano.serviceability.Assert;
import org.eclipse.virgo.medic.dump.DumpGenerator;

public class NonNullAssertionEnforcerTests {

    @Before
    public void injectDumpGenerator() {
//        DumpCoordinator.aspectOf().setDumpGenerator(new StubDumpGenerator());
    }

    @Test(expected = Assert.FatalAssertionException.class)
    public void firstMethodArg() {
        AssertingService service = new AssertingService();
        service.test(null);
    }

    @Test(expected = Assert.FatalAssertionException.class)
    public void secondMethodArg() {
        AssertingService service = new AssertingService();
        service.test("foo", null);
    }

    @Test(expected = Assert.FatalAssertionException.class)
    public void thirdMethodArg() {
        AssertingService service = new AssertingService();
        service.test("foo", 1, null);
    }

    @Test(expected = Assert.FatalAssertionException.class)
    public void firstConstructorArg() {
        new AssertingService(null);
    }

    @Test(expected = Assert.FatalAssertionException.class)
    public void secondConstructorArg() {
        new AssertingService("foo", null);
    }

    @Test(expected = Assert.FatalAssertionException.class)
    public void thirdConstructorArg() {
        new AssertingService("foo", 1, null);
    }

    private static class StubDumpGenerator implements DumpGenerator {

        public void generateDump(String cause, Throwable... throwables) {
        }

        public void generateDump(String cause, Map<String, Object> context, Throwable... throwables) {
        }

    }
}
