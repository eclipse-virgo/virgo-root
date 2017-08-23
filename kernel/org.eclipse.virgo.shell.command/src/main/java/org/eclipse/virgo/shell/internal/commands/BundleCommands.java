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

package org.eclipse.virgo.shell.internal.commands;

import java.util.Arrays;
import java.util.List;

import org.eclipse.equinox.region.RegionDigraph;
import org.eclipse.virgo.kernel.model.management.ManageableArtifact;
import org.eclipse.virgo.kernel.model.management.RuntimeArtifactModelObjectNameCreator;
import org.eclipse.virgo.kernel.module.ModuleContextAccessor;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiBundle;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiResolutionFailure;
import org.eclipse.virgo.shell.Command;
import org.eclipse.virgo.shell.internal.formatting.BundleInstallArtifactCommandFormatter;
import org.eclipse.virgo.shell.internal.util.QuasiBundleUtil;
import org.eclipse.virgo.shell.internal.util.QuasiServiceUtil;
import org.osgi.framework.Version;

/**
 * <p>
 * BundleCommands provides implementations of all the supported commands that can be 
 * performed on a bundle or bundles. In some cases it can fall back to generic behaviour 
 * in it super class {@link AbstractInstallArtifactBasedCommands}.
 * </p>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * BundleCommands is threadsafe
 * 
 */
@Command("bundle")
final class BundleCommands extends AbstractInstallArtifactBasedCommands<ManageableArtifact> {

    private static final String NO_BUNDLE_FOR_BUNDLE_ID = "No bundle with id '%s' was found";
    
    private static final String TYPE = "bundle";

    private final BundleInstallArtifactCommandFormatter formatter;

    private final QuasiBundleUtil quasiBundleUtil;

    public BundleCommands(RuntimeArtifactModelObjectNameCreator objectNameCreator, QuasiBundleUtil quasiBundleUtil, QuasiServiceUtil quasiServiceUtil, ModuleContextAccessor moduleContextAccessor, RegionDigraph regionDigraph) {
        super(TYPE, objectNameCreator, new BundleInstallArtifactCommandFormatter(regionDigraph, quasiBundleUtil, quasiServiceUtil, moduleContextAccessor), ManageableArtifact.class, regionDigraph);
        this.quasiBundleUtil = quasiBundleUtil;
        this.formatter = new BundleInstallArtifactCommandFormatter(regionDigraph, quasiBundleUtil, quasiServiceUtil, moduleContextAccessor);
    }

    @Command("examine")
    public List<String> examine(long id) {
        QuasiBundle bundle = this.quasiBundleUtil.getBundle(id);
        if (bundle != null) {
            return examine(bundle.getSymbolicName(), bundle.getVersion().toString(), bundle.getRegion().getName());
        } else {
            return Arrays.asList(String.format(NO_BUNDLE_FOR_BUNDLE_ID, id));
        }
    }

    @Command("start")
    public List<String> start(long id) {
        QuasiBundle bundle = this.quasiBundleUtil.getBundle(id);
        if (bundle != null) {
            return start(bundle.getSymbolicName(), bundle.getVersion().toString(), bundle.getRegion().getName());
        } else {
            return Arrays.asList(String.format(NO_BUNDLE_FOR_BUNDLE_ID, id));
        }
    }

    @Command("stop")
    public List<String> stop(long id) {
        QuasiBundle bundle = this.quasiBundleUtil.getBundle(id);
        if (bundle != null) {
            return stop(bundle.getSymbolicName(), bundle.getVersion().toString(), bundle.getRegion().getName());
        } else {
            return Arrays.asList(String.format(NO_BUNDLE_FOR_BUNDLE_ID, id));
        }
    }

    @Command("refresh")
    public List<String> refresh(long id) {
        QuasiBundle bundle = this.quasiBundleUtil.getBundle(id);
        if (bundle != null) {
            return refresh(bundle.getSymbolicName(), bundle.getVersion().toString(), bundle.getRegion().getName());
        } else {
            return Arrays.asList(String.format(NO_BUNDLE_FOR_BUNDLE_ID, id));
        }
    }

    @Command("uninstall")
    public List<String> uninstall(long id) {
        QuasiBundle bundle = this.quasiBundleUtil.getBundle(id);
        if (bundle != null) {
            return uninstall(bundle.getSymbolicName(), bundle.getVersion().toString(), bundle.getRegion().getName());
        } else {
            return Arrays.asList(String.format(NO_BUNDLE_FOR_BUNDLE_ID, id));
        }
    }

    @Command("diag")
    public List<String> diag(long id) {
        QuasiBundle bundle = this.quasiBundleUtil.getBundle(id);
        if (bundle != null) {
            return diag(bundle.getSymbolicName(), bundle.getVersion().toString(), bundle.getRegion().getName());
        } else {
            return Arrays.asList(String.format(NO_BUNDLE_FOR_BUNDLE_ID, id));
        }
    }

    @Command("diag")
    public List<String> diag(String name, String version, String region) {
        QuasiBundle bundle = getBundle(name, version, region);
        if (bundle != null) {
            List<QuasiResolutionFailure> resolverReport = this.quasiBundleUtil.getResolverReport(bundle.getBundleId());
            return this.formatter.formatDiag(bundle, resolverReport);
        } else {
            return getDoesNotExistMessage(TYPE, name, version, region);
        }
    }

    @Command("headers")
    public List<String> headers(long id) {
        QuasiBundle bundle = this.quasiBundleUtil.getBundle(id);
        if (bundle != null) {
            return headers(bundle.getSymbolicName(), bundle.getVersion().toString(), bundle.getRegion().getName());
        } else {
            return Arrays.asList(String.format(NO_BUNDLE_FOR_BUNDLE_ID, id));
        }
    }

    @Command("headers")
    public List<String> headers(String name, String version, String region) {
        return this.formatter.formatHeaders(getBundle(name, version, region));
    }

    private QuasiBundle getBundle(String name, String version, String region) {
        Version v = new Version(version);
        List<QuasiBundle> bundles = this.quasiBundleUtil.getAllBundles();
        for (QuasiBundle bundle : bundles) {
            if (bundle.getSymbolicName().equals(name) && bundle.getVersion().equals(v) && bundle.getRegion().equals(region)) {
                return bundle;
            }
        }
        return null;
    }

}
