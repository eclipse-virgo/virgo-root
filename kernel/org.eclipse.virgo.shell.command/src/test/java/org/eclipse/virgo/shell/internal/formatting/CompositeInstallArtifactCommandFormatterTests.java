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
import java.util.List;

import org.eclipse.virgo.shell.internal.formatting.CompositeInstallArtifactCommandFormatter;
import org.junit.Test;

public class CompositeInstallArtifactCommandFormatterTests {

    private final CompositeInstallArtifactCommandFormatter formatter = new CompositeInstallArtifactCommandFormatter();

    @Test
    public void examine() {
        List<String> lines = this.formatter.formatExamine(new StubManageableCompositeArtifact());
        assertOutputEquals(new File("src/test/resources/org/eclipse/virgo/kernel/shell/internal/formatting/composite-examine.txt"), lines);
    }
}
