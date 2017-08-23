/*******************************************************************************
 * Copyright (c) 2008, 2011 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.kernel.deployer.test.util;

import org.osgi.framework.Version;

public class ArtifactLifecycleEvent {

    public ArtifactLifecycleEvent(TestLifecycleEvent lifecycleEvent, String type, String name, Version version) {
        this.lifeCycleEvent = lifecycleEvent;
        this.type = type;
        this.name = name;
        this.version = version;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ArtifactLifecycleEvent) {
            ArtifactLifecycleEvent other = (ArtifactLifecycleEvent) obj;
            return this.lifeCycleEvent.equals(other.lifeCycleEvent) && this.type.equals(other.type) && this.name.equals(other.name)
                && this.version.equals(other.version);
        }
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 17;
        int result = this.lifeCycleEvent.hashCode() + prime
            * (this.name.hashCode() + prime * (this.type.hashCode() + prime * this.version.hashCode()));
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        sb.append(this.lifeCycleEvent).append(", ");
        sb.append(this.type).append(", ");
        sb.append(this.name).append(", ");
        sb.append(this.version).append("]");
        return sb.toString();
    }

    private final TestLifecycleEvent lifeCycleEvent;

    private final String type;

    private final String name;

    private final Version version;
}