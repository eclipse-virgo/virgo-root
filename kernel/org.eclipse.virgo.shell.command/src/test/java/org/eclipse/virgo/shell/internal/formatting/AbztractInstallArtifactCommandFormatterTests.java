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

import static org.eclipse.virgo.shell.internal.formatting.TestOutputComparator.assertOutputEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import org.eclipse.virgo.kernel.model.management.ManageableArtifact;
import org.eclipse.virgo.shell.internal.formatting.AbstractInstallArtifactCommandFormatter;

public class AbztractInstallArtifactCommandFormatterTests {

    private final StubInstallArtifactCommandFormatter formatter = new StubInstallArtifactCommandFormatter();

    @Test
    public void list() {
        List<ManageableArtifact> artifacts = new ArrayList<ManageableArtifact>();
        artifacts.add(new StubManageableCompositeArtifact());
        List<String> lines = this.formatter.formatList(artifacts);
        assertOutputEquals(new File("src/test/resources/org/eclipse/virgo/kernel/shell/internal/formatting/abstract-list.txt"), lines);
    }

    private static class StubInstallArtifactCommandFormatter extends AbstractInstallArtifactCommandFormatter<ManageableArtifact> {

        public List<String> formatExamine(ManageableArtifact artifact) {
            throw new UnsupportedOperationException();
        }

    }
}
