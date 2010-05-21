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

package org.eclipse.virgo.apps.admin.web.internal;

import java.util.HashMap;
import java.util.Map;

import javax.management.ObjectName;

/**
 * 
 */
public class DummyManagableArtifact implements DummyManagableArtifactMBean {

    private final String version;

    private final String type;

    private final String name;

    public DummyManagableArtifact(String type, String name, String version) {
        this.type = type;
        this.name = name;
        this.version = version;
    }

    /** 
     * {@inheritDoc}
     */
    public boolean refresh() {
        return false;
    }

    /** 
     * {@inheritDoc}
     */
    public String getName() {
        return name;
    }

    /** 
     * {@inheritDoc}
     */
    public String getType() {
        return type;
    }

    /** 
     * {@inheritDoc}
     */
    public String getVersion() {
        return version;
    }

    /** 
     * {@inheritDoc}
     */
    public String getState() {
        return "testState";
    }

    /** 
     * {@inheritDoc}
     */
    public ObjectName[] getDependents() {
        return new ObjectName[0];
    }

    /** 
     * {@inheritDoc}
     */
    public Map<String, String> getProperties() {
        HashMap<String, String> props = new HashMap<String, String>();
        props.put("user.installed", "true");
        return props;
    }

}
