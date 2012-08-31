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

package org.eclipse.virgo.shell.internal.formatting;

import java.util.ArrayList;
import java.util.List;

import javax.management.ObjectName;

import org.eclipse.virgo.kernel.model.management.ManageableCompositeArtifact;

public final class CompositeInstallArtifactCommandFormatter extends AbstractInstallArtifactCommandFormatter<ManageableCompositeArtifact> {

    private static final String CHILD_FORMAT = "    %s %s %s";

    public List<String> formatExamine(ManageableCompositeArtifact artifact) {
        List<String> lines = new ArrayList<String>();

        lines.add(String.format("State:  %s", artifact.getState()));
        lines.add(String.format("Scoped: %s", artifact.isScoped()));
        lines.add(String.format("Atomic: %s", artifact.isAtomic()));
        lines.add("");
        lines.add("Children:");

        for (ObjectName child : artifact.getDependents()) {
            lines.add(String.format(CHILD_FORMAT, child.getKeyProperty("artifact-type"), child.getKeyProperty("name"),
                child.getKeyProperty("version")));
        }

        return lines;
    }
}
