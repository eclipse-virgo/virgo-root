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

import java.util.List;

import org.eclipse.virgo.util.osgi.manifest.parse.HeaderDeclaration;
import org.eclipse.virgo.util.osgi.manifest.parse.HeaderParser;
import org.osgi.framework.Constants;


public class StubHeaderParser implements HeaderParser {
    
    private HeaderDeclaration bundleActivationPolicy;
    
    private HeaderDeclaration bundleSymbolicName;
    
    private List<HeaderDeclaration> dynamicImportPackage;
    
    private HeaderDeclaration fragmentHost;
    
    private List<HeaderDeclaration> importBundle;
    
    private List<HeaderDeclaration> importLibrary;
    
    private List<HeaderDeclaration> importPackage;
    
    private HeaderDeclaration librarySymbolicName;
    
    private List<HeaderDeclaration> requireBundle;

    private List<HeaderDeclaration> exportPackage;

    public HeaderDeclaration parseBundleActivationPolicy(String header) {
        return this.bundleActivationPolicy;
    }

    public HeaderDeclaration parseBundleSymbolicName(String header) {
        return this.bundleSymbolicName;
    }

    public List<HeaderDeclaration> parseDynamicImportPackageHeader(String header) {
        return this.dynamicImportPackage;
    }

    public HeaderDeclaration parseFragmentHostHeader(String header) {
        return this.fragmentHost;
    }

    public List<HeaderDeclaration> parseHeader(String header) {
        return null;
    }

    public List<HeaderDeclaration> parseImportBundleHeader(String header) {
        return this.importBundle;
    }

    public List<HeaderDeclaration> parseImportLibraryHeader(String header) {
        return this.importLibrary;
    }

    public HeaderDeclaration parseLibrarySymbolicName(String header) {
        return this.librarySymbolicName;
    }

    public List<HeaderDeclaration> parsePackageHeader(String header, String headerType) {
        if (headerType.equals(Constants.IMPORT_PACKAGE)) {
            return this.importPackage;
        } else if (headerType.equals(Constants.EXPORT_PACKAGE)) {
            return this.exportPackage;
        }
        return null;
    }

    public List<HeaderDeclaration> parseRequireBundleHeader(String header) {
        return this.requireBundle;
    }

    public List<HeaderDeclaration> parseWebFilterMappingsHeader(String header) {
        return null;
    }

    
    void setBundleActivationPolicy(HeaderDeclaration bundleActivationPolicy) {
        this.bundleActivationPolicy = bundleActivationPolicy;
    }

    
    void setBundleSymbolicName(HeaderDeclaration bundleSymbolicName) {
        this.bundleSymbolicName = bundleSymbolicName;
    }

    
    void setDynamicImportPackage(List<HeaderDeclaration> dynamicImportPackage) {
        this.dynamicImportPackage = dynamicImportPackage;
    }

    
    void setFragmentHost(HeaderDeclaration fragmentHost) {
        this.fragmentHost = fragmentHost;
    }

    
    void setImportBundle(List<HeaderDeclaration> importBundle) {
        this.importBundle = importBundle;
    }

    
    void setImportLibrary(List<HeaderDeclaration> importLibrary) {
        this.importLibrary = importLibrary;
    }

    
    void setImportPackage(List<HeaderDeclaration> importPackage) {
        this.importPackage = importPackage;
    }

    
    void setLibrarySymbolicName(HeaderDeclaration librarySymbolicName) {
        this.librarySymbolicName = librarySymbolicName;
    }

    
    void setRequireBundle(List<HeaderDeclaration> requireBundle) {
        this.requireBundle = requireBundle;
    }

    
    void setExportPackage(List<HeaderDeclaration> exportPackage) {
        this.exportPackage = exportPackage;
    }
}
