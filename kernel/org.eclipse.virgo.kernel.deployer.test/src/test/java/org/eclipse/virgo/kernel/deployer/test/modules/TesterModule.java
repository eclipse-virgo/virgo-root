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

package org.eclipse.virgo.kernel.deployer.test.modules;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.osgi.framework.Version;


/**
 * A module for testing deployment/resolution problems.
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 * thread-safe
 *
 */
public class TesterModule {
    private final String moduleName;
    private final Version moduleVersion;
    public static final Version MINIMUM_VERSION = new Version("0");

    private final Set<TesterModuleImport> moduleImports;
    private final Set<TesterModuleExport> moduleExports;
    
    public static class Builder {
        private final String moduleName;
        private Version moduleVersion = MINIMUM_VERSION;
        private Set<TesterModuleImport> moduleImports = new HashSet<TesterModuleImport>();
        private Set<TesterModuleExport> moduleExports = new HashSet<TesterModuleExport>();

        public Builder(String name) {
            this.moduleName = name;
        }
        
        public Builder version(String version) {
            this.moduleVersion = new Version(version);
            return this;
        }
        public Builder addImport(TesterModuleImport imp) {
            this.moduleImports.add(imp);
            return this;
        }
        public Builder addExport(TesterModuleExport exp) {
            this.moduleExports.add(exp);
            return this;
        }
        
        public TesterModule build() {
            return new TesterModule(this);
        }
    }
    
    private TesterModule(Builder builder) {
        this.moduleName = builder.moduleName;
        this.moduleVersion = builder.moduleVersion;
        this.moduleImports = builder.moduleImports;
        this.moduleExports = builder.moduleExports;
    }
    
    public String getName() {
        return this.moduleName;
    }
    public Version getVersion() {
        return this.moduleVersion;
    }
    public Set<TesterModuleImport> getImports() {
        return this.moduleImports;
    }
    public Set<TesterModuleExport> getExports() {
        return this.moduleExports;
    }

    public String getImportsEntry() {
        return importsList(this.moduleImports);
    }

    public String getExportsEntry() {
        return exportsList(this.moduleExports);
    }
    
    public List<String> getAllHeaders() {
        List<String> hdrs = new ArrayList<String>();
        hdrs.add("Manifest-Version: 1.0");
        hdrs.add("Bundle-ManifestVersion: 2");
        hdrs.add("Bundle-Name: TesterModule-" + this.moduleName);
        hdrs.add("Bundle-SymbolicName: " + this.moduleName);
        if (!this.moduleVersion.equals(MINIMUM_VERSION)) {
            hdrs.add("Bundle-Version: " + this.moduleVersion);
        }
        if (!this.moduleExports.isEmpty()) {
            hdrs.add("Export-Package: " + exportsList(this.moduleExports));
        }
        if (!this.moduleImports.isEmpty()) {
            hdrs.add("Import-Package: " + importsList(this.moduleImports));
        }
        
        return hdrs;
    }
    
    private static String importsList(Set<TesterModuleImport> imports) {
        boolean firstTime = true;
        StringBuffer sb = new StringBuffer();
        for (TesterModuleImport imp : imports) {
            if (firstTime) firstTime = false;
            else           sb.append(",");
            sb.append(imp.headerEntry());
        }
        return sb.toString();
    }
    private static String exportsList(Set<TesterModuleExport> exports) {
        boolean firstTime = true;
        StringBuffer sb = new StringBuffer();
        for (TesterModuleExport exp : exports) {
            if (firstTime) firstTime = false;
            else           sb.append(",");
            sb.append(exp.headerEntry());
        }
        return sb.toString();
    }
}
