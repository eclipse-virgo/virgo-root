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

package org.eclipse.virgo.kernel.userregion.internal.equinox;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;

import org.eclipse.osgi.service.resolver.ResolverError;
import org.eclipse.osgi.service.resolver.State;
import org.eclipse.virgo.kernel.userregion.internal.equinox.UsesAnalyser.AnalysedUsesConflict;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.Version;

/**
 */
public class UsesAnalyserTests extends AbstractOsgiFrameworkLaunchingTests {

    @Override
    protected String getRepositoryConfigDirectory() {
        return new File("src/test/resources/config/UsesAnalyserTests").getAbsolutePath();
    }

    @Test
    public void testDependentConstraints() throws Exception {
        Bundle p = install("dependent/bundles/p");
        install("dependent/bundles/q");
        install("dependent/bundles/r1");
        install("dependent/bundles/r2");
        install("dependent/bundles/s1");
        install("dependent/bundles/s2");

        try {
            p.start();
        } catch (BundleException ex) {
        }

        State systemState = this.platformAdmin.getState();

        UsesAnalyser analyser = new UsesAnalyser();

        ResolverError[] resolverErrors = analyser.getUsesResolverErrors(systemState, systemState.getBundle(p.getBundleId()));
        assertNotNull("No uses errors found for bundle '" + p + "'.", resolverErrors);

        AnalysedUsesConflict[] usesConflicts = analyser.getUsesConflicts(systemState, resolverErrors[0]);
        assertNotNull("No conflicts found for bundle '" + p + "'.", usesConflicts);
        printUsesConflicts(usesConflicts);

        assertEquals("No, or more than one conflict discovered.", 1, usesConflicts.length);

        assertEquals("q", usesConflicts[0].getUsesRootPackage().getName());

        assertEquals("r", usesConflicts[0].getPackage().getName());
        assertEquals(new Version("1.1.0"), usesConflicts[0].getPackage().getVersion());

        assertEquals("r", usesConflicts[0].getConflictingPackage().getName());
        assertEquals(new Version("1.0.0"), usesConflicts[0].getConflictingPackage().getVersion());
    }

    @Test
    public void testInstallOrder() throws Exception {
        install("install/bundles/s1");
        install("install/bundles/s2");
        Bundle q = install("install/bundles/q");
        install("install/bundles/r1");
        q.start();

        Bundle p = install("install/bundles/p");
        install("install/bundles/r2");
        try {
            p.start();
        } catch (BundleException ex) {
        }

        State systemState = this.platformAdmin.getState();
        UsesAnalyser analyser = new UsesAnalyser();

        ResolverError[] resolverErrors = analyser.getUsesResolverErrors(systemState, systemState.getBundle(p.getBundleId()));
        assertNotNull("No uses errors found for bundle '" + p + "'.", resolverErrors);

        AnalysedUsesConflict[] usesConflicts = analyser.getUsesConflicts(systemState, resolverErrors[0]);

        assertNotNull("No conflicts found for bundle '" + p + "'.", usesConflicts);
        printUsesConflicts(usesConflicts);

        assertEquals("No, or more than one conflict discovered.", 1, usesConflicts.length);

        assertEquals("q", usesConflicts[0].getUsesRootPackage().getName());

        assertEquals("r", usesConflicts[0].getPackage().getName());
        assertEquals(new Version("1.1.0"), usesConflicts[0].getPackage().getVersion());

        assertEquals("r", usesConflicts[0].getConflictingPackage().getName());
        assertEquals(new Version("1.0.0"), usesConflicts[0].getConflictingPackage().getVersion());

    }

    @Test
    public void transitiveUsesConstraint() throws Exception {
        install("transitiveconstraint/tmD.jar");
        install("transitiveconstraint/tmC.jar");
        install("transitiveconstraint/tmB.jar");
        Bundle a = install("transitiveconstraint/tmA.jar");
        try {
            a.start();
        } catch (BundleException ignored) {
        }

        State systemState = this.platformAdmin.getState();
        UsesAnalyser analyser = new UsesAnalyser();

        ResolverError[] resolverErrors = analyser.getUsesResolverErrors(systemState, systemState.getBundle(a.getBundleId()));
        assertNotNull("No uses errors found for bundle '" + a + "'.", resolverErrors);

        AnalysedUsesConflict[] usesConflicts = analyser.getUsesConflicts(systemState, resolverErrors[0]);

        assertNotNull("No conflicts found for bundle '" + a + "'.", usesConflicts);
        printUsesConflicts(usesConflicts);

        assertEquals("No, or more than one conflict discovered.", 1, usesConflicts.length);

        assertEquals("p", usesConflicts[0].getUsesRootPackage().getName());

        assertEquals("r", usesConflicts[0].getPackage().getName());
        assertEquals(new Version("1.0.0"), usesConflicts[0].getPackage().getVersion());

        assertEquals("r", usesConflicts[0].getConflictingPackage().getName());
        assertEquals(new Version("0.0.0"), usesConflicts[0].getConflictingPackage().getVersion());
    }

    private static final void printUsesConflicts(AnalysedUsesConflict[] usesConflicts) {
        int count = 0;
        for (AnalysedUsesConflict a : usesConflicts) {
            System.out.println("AnalysedUsesConflict element " + (count++));
            for (String s : a.getConflictStatement()) {
                System.out.println("  " + s);
            }
        }
    }

    private Bundle install(String subPath) throws BundleException {
        String fullPath = "src/test/resources/uat/" + subPath;
        String location = "reference:file:/" + new File(fullPath).getAbsolutePath();
        return this.framework.getBundleContext().installBundle(location);
    }
}
