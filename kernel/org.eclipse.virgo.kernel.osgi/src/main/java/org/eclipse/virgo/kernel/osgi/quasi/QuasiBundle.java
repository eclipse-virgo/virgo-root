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

package org.eclipse.virgo.kernel.osgi.quasi;

import java.io.File;
import java.util.List;

import org.eclipse.equinox.region.Region;
import org.eclipse.virgo.kernel.artifact.plan.PlanDescriptor.Provisioning;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.Version;

/**
 * {@link QuasiBundle} is a representation of a bundle in a {@link QuasiFramework}.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations of this class must be thread safe.
 * 
 */
public interface QuasiBundle {

    /**
     * Returns this {@link QuasiBundle}'s symbolic name. If this <code>QuasiBundle</code> does not have a symbolic name,
     * returns <code>null</code>.
     * 
     * @return a bundle symbolic name String or <code>null</code> if this <code>QuasiBundle</code> does not have a
     *         symbolic name
     */
    String getSymbolicName();

    /**
     * Returns this {@link QuasiBundle}'s bundle version.
     * 
     * @return a {@link Version}, which is never <code>null</code>
     */
    Version getVersion();

    /**
     * Returns whether this {@link QuasiBundle} was resolved in its {@link QuasiFramework}.
     * 
     * @return <code>true</code> if and only if this <code>QuasiBundle</code> is resolved
     */
    boolean isResolved();

    /**
     * Uninstalls this {@link QuasiBundle} from its {@link QuasiFramework}. This does not modify any committed
     * {@link Bundle} in the OSGi framework.
     * @throws BundleException 
     */
    void uninstall() throws BundleException;

    /**
     * If this {@link QuasiBundle} has been successfully committed, returns the corresponding {@link Bundle} in the OSGi
     * framework. If this {@link QuasiBundle} has not been successfully committed, returns <code>null</code>.
     * 
     * @return a <code>Bundle</code> or <code>null</code>
     */
    Bundle getBundle();

    /**
     * Returns the numeric id of this QuasiBundle. Typically a QuasiBundle will only have a numeric id if it represents
     * a bundle that is present in a QuasiFramework as the framework assigns the ids. -1 is returned if the id is not
     * known.
     * 
     * @return the numeric id of this QuasiBundle
     */
    long getBundleId();

    /**
     * Returns the location of the file or directory that the bundle that this QuasiBundle represents was installed from.
     * 
     * @return the location of the file or directory the bundle was installed from
     */
    String getBundleLocation();
    
    /**
     * Return the Region containing this bundle
     * 
     * @return The region this bundle belongs to
     */
    Region getRegion();
    
    /**
     * Returns all fragments known to this QuasiBundle (regardless resolution status).
     * 
     * @return an array of QuasiBundle containing all known fragments
     */
    List<QuasiBundle> getFragments();

    /**
     * Returns the potential hosts for this QuasiBundle. null is returned if this QuasiBundle is not a fragment.
     * 
     * @return the host QuasiBundles for this QuasiBundle or null.
     */
    List<QuasiBundle> getHosts();

    /**
     * Returns an array of QuasiExportPackage defined by the Export-Package clauses. All QuasiExportPackage are returned
     * even if they have not been selected by the resolver as an exporter of the package.
     * 
     * @return an array of QuasiExportPackage
     */
    List<QuasiExportPackage> getExportPackages();

    /**
     * Returns an array of {@link QuasiImportPackage} defined by the Import-Package clause of this QuasiBundle.
     * 
     * @return an array of QuasiImportPackage
     */
    List<QuasiImportPackage> getImportPackages();

    /**
     * Returns an array of {@link QuasiRequiredBundle} defined by the Require-Bundle clause of this QuasiBundle.
     * 
     * @return an array of QuasiRequiredBundle
     */
    List<QuasiRequiredBundle> getRequiredBundles();

    /**
     * A utility method that returns all QuasiBundle which depend on this bundle. A bundle depends on another bundle if
     * it requires the bundle, imports a package which is exported by that bundle, is a fragment to the bundle or is the
     * host of the bundle.
     * 
     * @return all QuasiBundle(s) which depend on this bundle.
     */
    List<QuasiBundle> getDependents();

    /**
     * Returns the actual filesystem location of this QuasiBundle
     * 
     * @return The file that is represented by this QuasiBundle
     */
    File getBundleFile();

    /**
     * Sets the provisioning behaviour required for this bundle.
     * 
     * @param provisioning the required provisioning behaviour, either Provisioning.AUTO or Provisioning.DISABLED
     */
    void setProvisioning(Provisioning provisioning);

    /**
     * Returns the provisioning behaviour required for this bundle.
     * 
     * @return Provisioning.AUTO or Provisioning.DISABLED
     */
    Provisioning getProvisioning();

}
