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

import org.eclipse.virgo.kernel.model.management.ManageableCompositeArtifact;
import org.eclipse.virgo.kernel.model.management.RuntimeArtifactModelObjectNameCreator;
import org.eclipse.virgo.shell.Command;
import org.eclipse.virgo.shell.internal.formatting.CompositeInstallArtifactCommandFormatter;

@Command("par")
final class ParCommands extends AbstractInstallArtifactBasedCommands<ManageableCompositeArtifact> {

    private static final String TYPE = "par";

    public ParCommands(RuntimeArtifactModelObjectNameCreator objectNameCreator) {
        super(TYPE, objectNameCreator, new CompositeInstallArtifactCommandFormatter(), ManageableCompositeArtifact.class, null);
    }
}
