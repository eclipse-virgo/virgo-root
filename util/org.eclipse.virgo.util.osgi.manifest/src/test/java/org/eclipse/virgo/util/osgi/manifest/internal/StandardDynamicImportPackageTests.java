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

package org.eclipse.virgo.util.osgi.manifest.internal;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.virgo.util.osgi.manifest.DynamicImportPackage;
import org.eclipse.virgo.util.osgi.manifest.DynamicallyImportedPackage;
import org.eclipse.virgo.util.osgi.manifest.internal.StandardDynamicImportPackage;
import org.eclipse.virgo.util.osgi.manifest.parse.HeaderDeclaration;
import org.junit.Test;


public class StandardDynamicImportPackageTests {

    @Test
    public void packageAddition() {
        StubHeaderParser parser = new StubHeaderParser();

        DynamicImportPackage dynamicImportPackage = new StandardDynamicImportPackage(parser);
        assertEquals(0, dynamicImportPackage.getDynamicallyImportedPackages().size());

        dynamicImportPackage.addDynamicallyImportedPackage("foo.*");
        List<DynamicallyImportedPackage> dynamicallyImportedPackages = dynamicImportPackage.getDynamicallyImportedPackages();
        assertEquals(1, dynamicallyImportedPackages.size());
        assertEquals("foo.*", dynamicallyImportedPackages.get(0).getPackageName());
    }

    @Test
    public void resetFromParseString() {
        StubHeaderParser parser = new StubHeaderParser();

        List<HeaderDeclaration> headers = new ArrayList<HeaderDeclaration>();
        headers.add(new StubHeaderDeclaration("foo"));
        headers.add(new StubHeaderDeclaration("bar"));

        DynamicImportPackage dynamicImportPackage = new StandardDynamicImportPackage(parser);
        parser.setDynamicImportPackage(headers);

        dynamicImportPackage.resetFromParseString("");

        assertEquals(2, dynamicImportPackage.getDynamicallyImportedPackages().size());

        headers.clear();
        headers.add(new StubHeaderDeclaration("foo", "bar"));

        dynamicImportPackage.resetFromParseString("");

        assertEquals(2, dynamicImportPackage.getDynamicallyImportedPackages().size());
    }
}
