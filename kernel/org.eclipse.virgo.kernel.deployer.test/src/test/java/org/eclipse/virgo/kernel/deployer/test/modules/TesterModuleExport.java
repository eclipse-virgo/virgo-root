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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.osgi.framework.Version;
/**
 * Export for a {@link TesterModule}
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 * thread-safe
 */
public class TesterModuleExport {
    private static final String HEADER_ENTRY_CLAUSE_SEPARATOR = ";";
    private static final String HEADER_ENTRY_ATTRIBUTE_NAME_SEPARATOR = ",";    
    public static final Version MINIMUM_VERSION = new Version("0");
    private final String pkgName;
    private final Version pkgVersion;
    private final Map<String,String> pkgAttributes;
    private final Set<String> pkgMandatoryAttributes;
    private final Set<String> pkgUses;
    
    public static class  Builder {
        //Required parameters
        private final String pkgName;
        //Optional parameters
        private Version pkgVersion = MINIMUM_VERSION;
        private Map<String,String> pkgAttributes = new HashMap<String,String>();
        private Set<String> pkgMandatoryAttributes = new HashSet<String>();
        private Set<String> pkgUses = new HashSet<String>();

        public Builder(String pkgName) {
            this.pkgName = pkgName;
        }
        public Builder version(String version) {
            this.pkgVersion = new Version(version);
            return this;
        }
        public Builder attribute(String name, String value) {
            this.pkgAttributes.put(name, value);
            return this;
        }
        public Builder mandatoryAttribute(String name, String value) {
            this.pkgAttributes.put(name, value);
            this.pkgMandatoryAttributes.add(name);
            return this;
        }
        public Builder uses(String name) {
            this.pkgUses.add(name);
            return this;
        }
        public TesterModuleExport build() {
            return new TesterModuleExport(this);
        }
    }
    
    private TesterModuleExport(Builder builder) {
        this.pkgName = builder.pkgName;
        this.pkgAttributes = builder.pkgAttributes;
        this.pkgVersion = builder.pkgVersion;
        this.pkgMandatoryAttributes = builder.pkgMandatoryAttributes;
        this.pkgUses = builder.pkgUses;
    }

    public String getName() {
        return this.pkgName;
    }

    public Version getVersion() {
        return this.pkgVersion;
    }

    public Map<String,String> getAttributes() {
        return this.pkgAttributes;
    }
    
    public Set<String> getMandatoryAttributes() {
        return this.pkgMandatoryAttributes;
    }
    
    public String headerEntry() {
        StringBuffer sb = new StringBuffer(this.pkgName);
        if (!this.pkgVersion.equals(MINIMUM_VERSION)) {
            sb.append(HEADER_ENTRY_CLAUSE_SEPARATOR).append(versionClause(this.pkgVersion));
        }        
        if (!this.pkgAttributes.isEmpty()) {
            sb.append(HEADER_ENTRY_CLAUSE_SEPARATOR).append(attributesClause(this.pkgAttributes));
        }
        if (!this.pkgMandatoryAttributes.isEmpty()) {
            sb.append(HEADER_ENTRY_CLAUSE_SEPARATOR).append(mandatoryAttributesClause(this.pkgMandatoryAttributes));
        }
        if (!this.pkgUses.isEmpty()) {
            sb.append(HEADER_ENTRY_CLAUSE_SEPARATOR).append(usesDirectiveClause(this.pkgUses));
        }
        
        return sb.toString();    
    }
    
    private static String versionClause(Version vr) {
        return new StringBuffer("version=\"").append(vr.toString()).append("\"").toString();
    }

    private static String attributesClause(Map<String,String> attrs) {
        boolean firstTime = true;
        StringBuffer sb = new StringBuffer();
        for (Entry<String,String> entry : attrs.entrySet()) {
            
            if (firstTime) firstTime = false;
            else           sb.append(HEADER_ENTRY_CLAUSE_SEPARATOR);
            
            sb.append(entry.getKey()).append("=").append("\"").append(entry.getValue()).append("\"");
        }
        return sb.toString();
    }
    
    private static String mandatoryAttributesClause(Set<String> attrs) {
        boolean firstTime = true;
        StringBuffer sb = new StringBuffer("mandatory:=\"");
        for (String attr : attrs) {
            
            if (firstTime) firstTime = false;
            else           sb.append(HEADER_ENTRY_ATTRIBUTE_NAME_SEPARATOR);
            
            sb.append(attr);
        }
        return sb.append("\"").toString();
    }
    
    private static String usesDirectiveClause(Set<String> uses) {
        boolean firstTime = true;
        StringBuffer sb = new StringBuffer("uses:=\"");
        for (String use : uses) {
            
            if (firstTime) firstTime = false;
            else           sb.append(HEADER_ENTRY_ATTRIBUTE_NAME_SEPARATOR);
            
            sb.append(use);
        }
        return sb.append("\"").toString();
    }
}
