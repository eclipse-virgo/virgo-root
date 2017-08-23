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

package org.eclipse.virgo.shell.internal.formatting;

import static org.eclipse.virgo.shell.internal.formatting.TestOutputComparator.assertOutputEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.virgo.shell.internal.formatting.ServiceCommandFormatter;
import org.eclipse.virgo.shell.internal.util.ServiceHolder;
import org.eclipse.virgo.shell.stubs.StubQuasiFramework;
import org.eclipse.virgo.test.stubs.framework.StubBundle;
import org.eclipse.virgo.test.stubs.framework.StubBundleContext;
import org.eclipse.virgo.test.stubs.framework.StubServiceReference;
import org.eclipse.virgo.test.stubs.framework.StubServiceRegistration;
import org.junit.Test;
import org.osgi.framework.Constants;
import org.osgi.framework.Version;

/**
 * Tests for {@link ServiceCommandFormatter}
 * 
 */
public class ServiceCommandFormatterTests {

    private final ServiceCommandFormatter serviceCommandFormatter = new ServiceCommandFormatter();

    @Test
    public void examine() throws Exception {
    	StubBundle bundle = new StubBundle(2L, "bundle.symbolic.name", new Version("1.0.1.asdhjgf"), "/some/location");
        StubQuasiFramework stubQuasiFramework = new StubQuasiFramework(bundle);

        StubServiceRegistration<Object> stubServiceRegistration = new StubServiceRegistration<Object>((StubBundleContext) bundle.getBundleContext(), new String[]{"bundle.symbolic.name"});
		StubServiceReference<Object> serviceReference = new StubServiceReference<Object>(stubServiceRegistration);
		serviceReference.setBundle(bundle);
        ServiceHolder service = new ServiceHolder(stubQuasiFramework, serviceReference);

        String[] obj1 = new String[] { "This is a string array....", "Second string" };
        Dictionary<String, Object> dic = new Hashtable<String, Object>();
        dic.put("propertyName1", obj1);
        stubServiceRegistration.setProperties(dic);

        List<String> lines = serviceCommandFormatter.formatExamine(service);
        assertOutputEquals(new File("src/test/resources/org/eclipse/virgo/kernel/shell/internal/formatting/service-examine.txt"), lines);
    }

    @Test
    public void summary() throws Exception {
    	StubBundle bundle1 = new StubBundle(2L, "bundle.symbolic.name1", new Version("2.0.1.asdhjgf"), "/some/location");
    	StubBundle bundle2 = new StubBundle(4L, "bundle.symbolic.name2", new Version("4.0.1.asdhjgf"), "/some/location");
        StubQuasiFramework stubQuasiFramework = new StubQuasiFramework(bundle1, bundle2);

        StubServiceRegistration<Object> stubServiceRegistration1 = new StubServiceRegistration<Object>((StubBundleContext) bundle1.getBundleContext(), new String[]{"bundle.symbolic.name1"});
		StubServiceReference<Object> serviceReference1 = new StubServiceReference<Object>(stubServiceRegistration1);
		serviceReference1.setBundle(bundle1);
        ServiceHolder service1 = new ServiceHolder(stubQuasiFramework, serviceReference1);

        StubServiceRegistration<Object> stubServiceRegistration2 = new StubServiceRegistration<Object>((StubBundleContext) bundle2.getBundleContext(), new String[]{"bundle.symbolic.name2"});
		StubServiceReference<Object> serviceReference2 = new StubServiceReference<Object>(stubServiceRegistration2);
		serviceReference2.setBundle(bundle2);
        ServiceHolder service2 = new ServiceHolder(stubQuasiFramework, serviceReference2);
        
        Dictionary<String, Object> dic = new Hashtable<String, Object>();
        dic.put(Constants.OBJECTCLASS, "object.class.obj.com.com.com.springsource.verylongclassnameinpackage.AgainLongName");
        stubServiceRegistration1.setProperties(dic);

        List<ServiceHolder> services = new ArrayList<ServiceHolder>(2);
        services.add(service1);
        services.add(service2);

        List<String> lines = serviceCommandFormatter.formatList(services);
        assertOutputEquals(new File("src/test/resources/org/eclipse/virgo/kernel/shell/internal/formatting/service-list.txt"), lines);
    }

}
