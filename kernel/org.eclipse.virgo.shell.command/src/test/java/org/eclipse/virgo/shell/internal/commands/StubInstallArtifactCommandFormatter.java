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

package org.eclipse.virgo.shell.internal.commands;

import java.util.Arrays;
import java.util.List;

import org.eclipse.virgo.kernel.model.management.ManageableCompositeArtifact;
import org.eclipse.virgo.shell.internal.formatting.InstallArtifactCommandFormatter;

final class StubInstallArtifactCommandFormatter implements InstallArtifactCommandFormatter<ManageableCompositeArtifact> {

    public List<String> formatExamine(ManageableCompositeArtifact artifact) {
        return Arrays.asList("");
    }

    public List<String> formatList(List<ManageableCompositeArtifact> artifacts) {
        return Arrays.asList("");
    }

}
