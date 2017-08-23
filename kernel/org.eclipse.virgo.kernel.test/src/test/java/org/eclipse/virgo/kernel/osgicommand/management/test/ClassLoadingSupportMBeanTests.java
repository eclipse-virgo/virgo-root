/*******************************************************************************
 * Copyright (c) 2011 SAP AG
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Hristo Iliev, SAP AG - initial contribution
 ******************************************************************************/

package org.eclipse.virgo.kernel.osgicommand.management.test;

import org.eclipse.virgo.kernel.test.AbstractKernelIntegrationTest;
import org.junit.Test;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test for testing Class loading support mBean
 */
public class ClassLoadingSupportMBeanTests extends AbstractKernelIntegrationTest {

    private final MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();

    private final ObjectName objectName;
    {
        try {
            objectName = new ObjectName("org.eclipse.virgo.kernel:type=Classloading");
        } catch (JMException jme) {
            throw new RuntimeException(jme);
        }
    }

    private static final String CLASS_NAME = ClassLoadingSupportMBeanTests.class.getName();
    private static final String PACKAGE_NAME = ClassLoadingSupportMBeanTests.class.getPackage().getName();

    private static final String CLASS_NAME_PATH = CLASS_NAME.replace(".", "/") + ".class";

    private static final String SYSTEM_PACKAGE_NAME = "org.osgi.framework";


    @Test
    @SuppressWarnings("unchecked")
    public void testGetBundlesContainingResource() throws JMException {
        Map<List<String>, List<String>> result = (Map<List<String>, List<String>>) mBeanServer.invoke(objectName, "getBundlesContainingResource",
                                                                                                      new Object[]{CLASS_NAME_PATH},
                                                                                                      new String[]{String.class.getName()});
        assertEquals("Incorrect number of bundles " + result + " contain the test class [" + CLASS_NAME_PATH + "]", 1, result.size());
        assertTrue("Bundles " + result + " do not contain class [" + CLASS_NAME_PATH + "]",
                   containsBundleSymbolicName(result.keySet(), super.context.getBundle().getSymbolicName()));
        assertTrue("Bundle " + super.context.getBundle().getSymbolicName() + " does not contain resource [" + CLASS_NAME_PATH + "]",
                   result.toString().contains(CLASS_NAME_PATH));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGetBundlesLoadingClass() throws JMException {
        Map<List<String>, List<String>> result = (Map<List<String>, List<String>>) mBeanServer.invoke(objectName, "getBundlesLoadingClass",
                                                                                                      new Object[]{CLASS_NAME},
                                                                                                      new String[]{String.class.getName()});
        assertEquals("Incorrect number of bundles " + result + " can load the test class [" + CLASS_NAME + "]", 2, result.size());
        assertTrue("Bundles " + result + " do not contain class [" + CLASS_NAME + "]",
                   containsBundleSymbolicName(result.keySet(), super.context.getBundle().getSymbolicName()));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGetBundlesExportingPackage() throws JMException {
        List<List<String>> result = (List<List<String>>) mBeanServer.invoke(objectName, "getBundlesExportingPackage",
                                                                            new Object[]{SYSTEM_PACKAGE_NAME},
                                                                            new String[]{String.class.getName()});
        assertEquals("No bundle " + result + " exports the test package [" + SYSTEM_PACKAGE_NAME + "]", 1, result.size());
        assertEquals("System bundle [[" + result.get(0).get(0) + "] [" + result.get(0).get(1) + "]] does not contain test package [" + SYSTEM_PACKAGE_NAME + "]",
                     "0", result.get(0).get(0));

        result = (List<List<String>>) mBeanServer.invoke(objectName, "getBundlesExportingPackage",
                                                         new Object[]{PACKAGE_NAME},
                                                         new String[]{String.class.getName()});
        assertEquals("At least one bundle " + result + " exports the test package [" + PACKAGE_NAME + "]", 0, result.size());
    }

    @Test
    public void testTryToLoadClassFromBundleWithId() throws JMException {
        long firstUserRegionBundleId = super.context.getBundles()[0].getBundleId();
        String firstUserRegionBundleSN = super.context.getBundles()[0].getSymbolicName();

        long testBundleId = super.context.getBundle().getBundleId();

        boolean result = (Boolean) mBeanServer.invoke(objectName, "tryToLoadClassFromBundle",
                                                      new Object[]{CLASS_NAME, testBundleId},
                                                      new String[]{String.class.getName(), long.class.getName()});
        assertTrue("This test bundle [" + testBundleId + "] cannot load [" + CLASS_NAME + "]", result);

        result = (Boolean) mBeanServer.invoke(objectName, "tryToLoadClassFromBundle",
                                              new Object[]{CLASS_NAME, firstUserRegionBundleId},
                                              new String[]{String.class.getName(), long.class.getName()});
        assertTrue("Bundle with ID [" + firstUserRegionBundleId + "] and symbolic name [" + firstUserRegionBundleSN +
                   "] can load [" + CLASS_NAME + "]", result);
    }

    private boolean containsBundleSymbolicName(Set<List<String>> set, String bundleSymbolicName) {
        for (List<String> bundleInformation : set) {
            if (bundleInformation.get(1).equals(bundleSymbolicName)) {
                return true;
            }
        }
        return false;
    }
}
