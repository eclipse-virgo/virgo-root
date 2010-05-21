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

import java.util.Map;

import javax.management.ObjectName;

public interface DummyManagableArtifactMBean {

    public abstract boolean refresh();

    public abstract String getName();

    public abstract String getType();

    public abstract String getVersion();

    public abstract String getState();

    public abstract ObjectName[] getDependents();

    public abstract Map<String, String> getProperties();

}
