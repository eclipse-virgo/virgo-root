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

package org.eclipse.virgo.kernel.userregionfactory;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.service.packageadmin.ExportedPackage;
import org.osgi.service.packageadmin.PackageAdmin;

import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.virgo.test.stubs.framework.StubBundleContext;

public class PackageImportWildcardExpanderTests {

    private StubBundleContext stubBundleContext;

    private EventLogger mockEventLogger;

    private PackageAdmin mockPackageAdmin;

    private ExportedPackage[] exportedPackages;

    @Before
    public void setUp() {
        this.stubBundleContext = new StubBundleContext();

        this.mockEventLogger = createMock(EventLogger.class);
        replay(this.mockEventLogger);

        this.mockPackageAdmin = createMock(PackageAdmin.class);
        this.exportedPackages = new ExportedPackage[] { createdMockExportedPackage("q"), createdMockExportedPackage("r.a"),
            createdMockExportedPackage("r.b.c") };
        expect(this.mockPackageAdmin.getExportedPackages((Bundle) null)).andReturn(this.exportedPackages);
        replay(this.mockPackageAdmin);
        this.stubBundleContext.registerService(PackageAdmin.class.getName(), this.mockPackageAdmin, null);
    }

    private ExportedPackage createdMockExportedPackage(String packageName) {
        ExportedPackage pkg = createMock(ExportedPackage.class);
        expect(pkg.getName()).andReturn(packageName);
        replay(pkg);
        return pkg;
    }

    @Test
    public void testNoWildcards() {
        String expansion = PackageImportWildcardExpander.expandPackageImportsWildcards("p,r", this.stubBundleContext, mockEventLogger);
        assertEquals("Incorrect expansion", "p,r", expansion);
    }

    @Test
    public void testWildcards() {
        String expansion = PackageImportWildcardExpander.expandPackageImportsWildcards("r.*", this.stubBundleContext, mockEventLogger);
        assertEquals("Incorrect expansion", "r.a,r.b.c", expansion);
    }

}
