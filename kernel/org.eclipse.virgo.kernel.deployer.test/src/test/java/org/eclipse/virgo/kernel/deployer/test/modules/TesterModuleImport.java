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
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.osgi.service.resolver.VersionRange;


/**
 * Import for a {@link TesterModule}
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 * thread-safe
 */
public class TesterModuleImport {

    private static final String HEADER_ENTRY_CLAUSE_SEPARATOR = ";";
    private final String pkgName;
    private final VersionRange pkgVersionRange;
    private final Map<String,String> pkgAttributes;
    
    
    public static class  Builder {
        //Required parameters
        private final String pkgName;
        //Optional parameters
        private VersionRange pkgVersionRange = VersionRange.emptyRange;
        private Map<String,String> pkgAttributes = new HashMap<String,String>(); 
        
        public Builder(String pkgName) {
            this.pkgName = pkgName;
        }
        public Builder versionRange(String versionRangeString) {
            this.pkgVersionRange = new VersionRange(versionRangeString);
            return this;
        }
        public Builder attribute(String name, String value) {
            this.pkgAttributes.put(name, value);
            return this;
        }
        
        public TesterModuleImport build() {
            return new TesterModuleImport(this);
        }
    }
    
    private TesterModuleImport(Builder builder) {
        this.pkgName = builder.pkgName;
        this.pkgAttributes = builder.pkgAttributes;
        this.pkgVersionRange = builder.pkgVersionRange;
    }

    public String getName() {
        return this.pkgName;
    }

    public VersionRange getVersionRange() {
        return this.pkgVersionRange;
    }

    public Map<String,String> getAttributes() {
        return this.pkgAttributes;
    }
    
    public String headerEntry() {
        StringBuffer sb = new StringBuffer(this.pkgName);
        if (!this.pkgVersionRange.equals(VersionRange.emptyRange)) {
            sb.append(HEADER_ENTRY_CLAUSE_SEPARATOR).append(versionClause(this.pkgVersionRange));
        }        
        if (!this.pkgAttributes.isEmpty()) {
            sb.append(HEADER_ENTRY_CLAUSE_SEPARATOR).append(attributesClause(this.pkgAttributes));
        }
        return sb.toString();    
    }
    
    private static String versionClause(VersionRange vr) {
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
}
