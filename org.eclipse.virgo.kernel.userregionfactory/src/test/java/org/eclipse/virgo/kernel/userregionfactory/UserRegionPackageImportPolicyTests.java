/*******************************************************************************
 * This file is part of the Virgo Web Server.
 *
 * Copyright (c) 2010 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SpringSource, a division of VMware - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.virgo.kernel.userregionfactory;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.virgo.kernel.osgi.region.RegionPackageImportPolicy;
import org.junit.Assert;
import org.junit.Test;
import org.osgi.framework.Version;
import org.osgi.framework.wiring.BundleRevision;

public class UserRegionPackageImportPolicyTests {

    @Test
    public void testNullPackageString() {
        new UserRegionPackageImportPolicy(null);
    }

    @Test
    public void testEmptyPackageString() {
        new UserRegionPackageImportPolicy("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWildcard() {
        new UserRegionPackageImportPolicy("*");
    }

    @Test
    public void testPackageStringWithArbitraryAttribute() {
        UserRegionPackageImportPolicy userRegionPackageImportPolicy = createUserRegionPackageImportPolicy("p;pa=pv,q");
        Assert.assertFalse(userRegionPackageImportPolicy.isImported("p", null, null));
        Map<String, Object> attributes = createAttributes("p");
        attributes.put("pa", "pv");
        Assert.assertTrue(userRegionPackageImportPolicy.isImported("p", attributes, null));

    }

    private UserRegionPackageImportPolicy createUserRegionPackageImportPolicy(String regionImports) {
        UserRegionPackageImportPolicy userRegionPackageImportPolicy = new UserRegionPackageImportPolicy(regionImports);
        return userRegionPackageImportPolicy;
    }

    @Test
    public void testPackageStringWithoutArbitraryAttribute() {
        RegionPackageImportPolicy userRegionPackageImportPolicy = createUserRegionPackageImportPolicy("p,q");
        Map<String, Object> attributes = createAttributes("p");
        attributes.put("pa", "pv");
        Assert.assertTrue(userRegionPackageImportPolicy.isImported("p", attributes, null));

    }

    private Map<String, Object> createAttributes(String packageName) {
        Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put(BundleRevision.PACKAGE_NAMESPACE, packageName);
        return attributes;
    }

    @Test
    public void testPackageStringWithMandatoryAttribute() {
        RegionPackageImportPolicy userRegionPackageImportPolicy = createUserRegionPackageImportPolicy("p;pa=pv");
        Assert.assertFalse(userRegionPackageImportPolicy.isImported("p", null, null));
        Map<String, Object> attributes = createAttributes("p");
        attributes.put("pa", "pv");
        Map<String, String> directives = createMandatoryDirective("pa");
        Assert.assertTrue(userRegionPackageImportPolicy.isImported("p", attributes, directives));
    }

    @Test
    public void testPackageStringWithoutMandatoryAttribute() {
        RegionPackageImportPolicy userRegionPackageImportPolicy = createUserRegionPackageImportPolicy("p");
        Map<String, Object> attributes = createAttributes("p");
        attributes.put("pa", "pv");
        Map<String, String> directives = createMandatoryDirective("pa");
        Assert.assertFalse(userRegionPackageImportPolicy.isImported("p", attributes, directives));
    }

    private Map<String, String> createMandatoryDirective(String attributes) {
        Map<String, String> directives = new HashMap<String, String>();
        directives.put("mandatory", attributes);
        return directives;
    }

    @Test
    public void testPackageStringWithVersion() {
        RegionPackageImportPolicy userRegionPackageImportPolicy = createUserRegionPackageImportPolicy("p;version=2,q");
        Assert.assertFalse(userRegionPackageImportPolicy.isImported("p", null, null));
        Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put("version", new Version("2.0.0"));
        Assert.assertTrue(userRegionPackageImportPolicy.isImported("p", attributes, null));

    }

    @Test
    public void testPackages() {
        RegionPackageImportPolicy userRegionPackageImportPolicy = createUserRegionPackageImportPolicy("p,q");
        Assert.assertTrue(userRegionPackageImportPolicy.isImported("p", null, null));
        Assert.assertTrue(userRegionPackageImportPolicy.isImported("q", null, null));
        Assert.assertFalse(userRegionPackageImportPolicy.isImported("r", null, null));
    }

}
