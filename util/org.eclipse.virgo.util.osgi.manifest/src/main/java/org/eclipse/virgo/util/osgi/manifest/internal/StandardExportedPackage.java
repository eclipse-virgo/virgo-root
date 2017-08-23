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

import org.eclipse.virgo.util.osgi.manifest.ExportedPackage;
import org.eclipse.virgo.util.osgi.manifest.parse.HeaderDeclaration;
import org.eclipse.virgo.util.osgi.manifest.parse.HeaderParser;
import org.osgi.framework.Constants;
import org.osgi.framework.Version;


/**
 * <strong>Concurrent Semantics</strong><br />
 * Not thread-safe.
 */
public class StandardExportedPackage extends BaseCompoundHeaderEntry implements ExportedPackage {    
    
    StandardExportedPackage(HeaderParser parser, String name) {
        super(parser, name);
    }
    
    @Override
    HeaderDeclaration parse(HeaderParser parser, String parseString) {
        List<HeaderDeclaration> header = parser.parsePackageHeader(parseString, Constants.EXPORT_PACKAGE);
        return header.get(0);
    }

    /** 
     * {@inheritDoc}
     */
    public List<String> getExclude() {
        return HeaderUtils.toList(Constants.EXCLUDE_DIRECTIVE, getDirectives()); 
    }

    /** 
     * {@inheritDoc}
     */
    public List<String> getInclude() {
        return HeaderUtils.toList(Constants.INCLUDE_DIRECTIVE, getDirectives());        
    }

    /** 
     * {@inheritDoc}
     */
    public List<String> getMandatory() {
        return HeaderUtils.toList(Constants.MANDATORY_DIRECTIVE, getDirectives());
    }

    /** 
     * {@inheritDoc}
     */
    public String getPackageName() {
        return this.name;
    }

    /** 
     * {@inheritDoc}
     */
    public List<String> getUses() {
        return HeaderUtils.toList(Constants.USES_DIRECTIVE, getDirectives());
    }

    /** 
     * {@inheritDoc}
     */
    public Version getVersion() {
        String value = getAttributes().get(Constants.VERSION_ATTRIBUTE);
        if (value != null) {
            return new Version(value);
        } else {
            return Version.emptyVersion;
        }
    }

    /** 
     * {@inheritDoc}
     */
    public void setVersion(Version version) {
        if (version != null) {
            getAttributes().put(Constants.VERSION_ATTRIBUTE, version.toString());
        } else {
            getAttributes().remove(Constants.VERSION_ATTRIBUTE);
        }
    }

    /** 
     * {@inheritDoc}
     */
    public void setPackageName(String packageName) {
        if (packageName == null) {
            throw new IllegalArgumentException("packageName must not be null");
        }
        this.name = packageName;        
    }
}
