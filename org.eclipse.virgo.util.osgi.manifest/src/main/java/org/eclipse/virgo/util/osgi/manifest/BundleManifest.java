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

package org.eclipse.virgo.util.osgi.manifest;

import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.Dictionary;
import java.util.List;

import org.osgi.framework.Version;

/**
 * Represents an OSGi bundle's manifest.
 * 
 * <p/>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations of this interface need not be thread safe.
 * 
 */
public interface BundleManifest {

    /**
     * The <code>Import-Bundle</code> bundle manifest header.
     */
    public static final String IMPORT_BUNDLE = "Import-Bundle";

    /**
     * The <code>Import-Library</code> bundle manifest header.
     */
    public static final String IMPORT_LIBRARY = "Import-Library";

    /**
     * The <code>Module-Scope</code> bundle manifest header.
     */
    public static final String MODULE_SCOPE = "Module-Scope";

    /**
     * The <code>Module-Type</code> bundle manifest header.
     */
    public static final String MODULE_TYPE = "Module-Type";

    /**
     * Returns the <code>Bundle-ActivationPolicy</code> header, never <code>null</code>.
     * 
     * @return the <code>Bundle-ActivationPolicy</code> header.
     */
    BundleActivationPolicy getBundleActivationPolicy();

    /**
     * Returns a <code>List</code> containing an item for each entry in the comma-separated
     * <code>Bundle-Classpath</code> header. Returns an empty list if the manifest does not contain a
     * <code>Bundle-Classpath</code> header.
     * 
     * @return a <code>List</code> of the entries in the <code>Bundle-Classpath</code> header.
     */
    List<String> getBundleClasspath();

    /**
     * Returns the value of the <code>Bundle-Description</code> header, or <code>null</code> if no description is
     * specified.
     * 
     * @return the value of the <code>Bundle-Description</code> header.
     */
    String getBundleDescription();

    /**
     * Sets the value of the <code>Bundle-Description</code> header.
     * 
     * @param bundleDescription The bundle's description
     */
    void setBundleDescription(String bundleDescription);

    /**
     * Returns the <code>Bundle-ManifestVersion</code> header, or <code>1</code> if no manifest version is specified.
     * 
     * @return the value of the <code>Bundle-ManifestVersion</code> header.
     */
    int getBundleManifestVersion();

    /**
     * Sets the value of the <code>Bundle-ManifestVersion</code> header.
     * 
     * @param bundleManifestVersion The bundle's bundle manifest version
     * 
     */
    void setBundleManifestVersion(int bundleManifestVersion);

    /**
     * Returns the <code>Bundle-Name</code> header, or <code>null</code> if no name is specified.
     * 
     * @return the value of the <code>Bundle-Name</code> header.
     */
    String getBundleName();

    /**
     * Sets the value of the <code>Bundle-Name</code> header.
     * 
     * @param bundleName The bundle's name
     */
    void setBundleName(String bundleName);

    /**
     * Returns the <code>Bundle-SymbolicName</code> header, never <code>null</code>.
     * 
     * @return the <code>Bundle-SymbolicName</code> header.
     */
    BundleSymbolicName getBundleSymbolicName();

    /**
     * Returns the value of the <code>Bundle-UpdateLocation</code> header, or <code>null</code> if no update location is
     * specified.
     * 
     * @return the value of the <code>Bundle-UpdateLocation</code> header.
     */
    URL getBundleUpdateLocation();

    /**
     * Sets the value of the <code>Bundle-UpdateLocation</code> header.
     * 
     * @param bundleUpdateLocation The bundle's update location
     */
    void setBundleUpdateLocation(URL bundleUpdateLocation);

    /**
     * Returns the <code>DynamicImport-Package</code> header, never <code>null</code>.
     * 
     * @return the <code>DynamicImport-Package</code> header.
     */
    DynamicImportPackage getDynamicImportPackage();

    /**
     * Returns the <code>Export-Package</code> header, never <code>null</code>.
     * 
     * @return the <code>Export-Package</code> header.
     */
    ExportPackage getExportPackage();

    /**
     * Returns the <code>Fragment-Host</code> header, never <code>null</code>.
     * 
     * @return the <code>Fragment-Host</code> header.
     */
    FragmentHost getFragmentHost();

    /**
     * Returns the <code>Import-Bundle</code> header, never <code>null</code>.
     * 
     * @return the <code>Import-Bundle</code> header.
     */
    ImportBundle getImportBundle();

    /**
     * Returns the <code>Import-Library</code> header, never <code>null</code>.
     * 
     * @return the <code>Import-Library</code> header.
     */
    ImportLibrary getImportLibrary();

    /**
     * Returns the <code>Import-Package</code> header, never <code>null</code>.
     * 
     * @return the <code>Import-Package</code> header.
     */
    ImportPackage getImportPackage();

    /**
     * Returns the value of the <code>Module-Scope</code> header, or <code>null</code> if no module scope is specified.
     * 
     * @return the value of the <code>Module-Scope</code> header.
     */
    String getModuleScope();

    /**
     * Sets the value of the <code>Module-Scope</code> header.
     * 
     * @param moduleScope The bundle's module scope
     */
    void setModuleScope(String moduleScope);

    /**
     * Returns the value of the <code>Module-Type</code> header, or <code>null</code> if no module type is specified.
     * 
     * @return the value of the <code>Module-Type</code> header.
     */
    String getModuleType();

    /**
     * Sets the value of the <code>Module-Type</code> header.
     * 
     * @param moduleType The bundle's module type
     */
    void setModuleType(String moduleType);

    /**
     * Returns the <code>Require-Bundle</code> header, never <code>null</code>.
     * 
     * @return the <code>Require-Bundle</code> header.
     */
    RequireBundle getRequireBundle();

    /**
     * Returns the value of the <code>Bundle-Version</code> header, or <code>null</code> if no version is specified.
     * 
     * @return the bundle's version
     */
    Version getBundleVersion();

    /**
     * Set the value of the <code>Bundle-Version</code> header.
     * 
     * @param bundleVersion bundle's version
     */
    void setBundleVersion(Version bundleVersion);

    /**
     * Returns the value of the header identified by the supplied name.
     * 
     * @param name the name of the header
     * @return the value of the header
     */
    String getHeader(String name);

    /**
     * Sets the header with the supplied name to have the supplied value.
     * 
     * @param name The name of the header
     * @param value The value of the header
     */
    void setHeader(String name, String value);

    /**
     * Returns a snapshot of this manifest as a {@link Dictionary}
     * 
     * @return This manifest in <code>Dictionary</code> form.
     */
    Dictionary<String, String> toDictionary();
    
    /**
     * Writes the contents of this manifest to the supplied writer
     * @param writer the writer to which the manifest is written
     * @throws IOException if a problem occurs when writing out the manifest
     */
    void write(Writer writer) throws IOException;
}
