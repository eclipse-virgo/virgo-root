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

package org.eclipse.virgo.kernel.shell.internal.formatting;

import static org.eclipse.virgo.kernel.shell.internal.formatting.TestOutputComparator.assertOutputEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;


import org.eclipse.virgo.kernel.shell.internal.formatting.ServiceCommandFormatter;
import org.eclipse.virgo.kernel.shell.state.QuasiLiveBundle;
import org.eclipse.virgo.kernel.shell.state.QuasiLiveService;
import org.eclipse.virgo.kernel.shell.stubs.StubQuasiLiveBundle;
import org.eclipse.virgo.kernel.shell.stubs.StubQuasiLiveService;
import org.eclipse.virgo.teststubs.osgi.framework.StubBundle;

/**
 * Tests for {@link ServiceCommandFormatter}
 * 
 */
public class ServiceCommandFormatterTests {

    private final ServiceCommandFormatter serviceCommandFormatter = new ServiceCommandFormatter();

    @Test
    public void examine() throws Exception {
        Bundle bundle = new StubBundle(2L, "bundle.symbolic.name", new Version("1.0.1.asdhjgf"), "/some/location");
        QuasiLiveBundle liveBundle = new StubQuasiLiveBundle(2L, bundle);
        StubQuasiLiveService service = new StubQuasiLiveService(1, liveBundle);

        String[] obj1 = new String[] { "This is a string array....", "Second string" };
        service.setProperty("propertyName1", obj1);

        List<String> lines = serviceCommandFormatter.formatExamine(service);
        assertOutputEquals(new File("src/test/resources/org/eclipse/virgo/kernel/shell/internal/formatting/service-examine.txt"), lines);
    }

    @Test
    public void summary() throws Exception {
        Bundle bundle1 = new StubBundle(2L, "bundle.symbolic.name1", new Version("2.0.1.asdhjgf"), "/some/location");
        Bundle bundle2 = new StubBundle(4L, "bundle.symbolic.name2", new Version("4.0.1.asdhjgf"), "/some/location");
        QuasiLiveBundle liveBundle1 = new StubQuasiLiveBundle(2L, bundle1);
        QuasiLiveBundle liveBundle2 = new StubQuasiLiveBundle(4L, bundle2);
        StubQuasiLiveService service1 = new StubQuasiLiveService(1, liveBundle1);
        StubQuasiLiveService service2 = new StubQuasiLiveService(3476, liveBundle2);

        service1.setProperty("objectClass", "object.class.obj.com.com.com.springsource.verylongclassnameinpackage.AgainLongName");

        List<QuasiLiveService> services = new ArrayList<QuasiLiveService>(2);
        services.add(service1);
        services.add(service2);

        List<String> lines = serviceCommandFormatter.formatList(services);
        assertOutputEquals(new File("src/test/resources/org/eclipse/virgo/kernel/shell/internal/formatting/service-list.txt"), lines);
    }

}
