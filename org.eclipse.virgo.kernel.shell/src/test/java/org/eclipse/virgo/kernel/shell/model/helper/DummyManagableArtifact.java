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

package org.eclipse.virgo.kernel.shell.model.helper;

import java.util.HashMap;
import java.util.Map;

import javax.management.ObjectName;

import org.eclipse.virgo.kernel.model.management.ManageableArtifact;

/**
 * 
 */
public final class DummyManagableArtifact implements ManageableArtifact {

    private final String version;

    private final String type;

    private final String name;

    public DummyManagableArtifact(String type, String name, String version) {
        this.type = type;
        this.name = name;
        this.version = version;
    }

    public void start() {
        // do nothing
    }

    public void stop() {
        // do nothing
    }

    public void uninstall() {
        // do nothing
    }

    public boolean refresh() {
        return true;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getVersion() {
        return version;
    }

    public ObjectName[] getDependents() {
        return new ObjectName[0];
    }

    public Map<String, String> getProperties() {
        HashMap<String, String> props = new HashMap<String, String>();
        props.put("user.installed", "true");
        return props;
    }

    public String getState() {
        return "testState";
    }

    public String getRegion() {
        return "testRegion";
    }

}
