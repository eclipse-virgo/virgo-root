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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.virgo.kernel.model.BundleArtifact;
import org.eclipse.virgo.kernel.model.management.ManageableArtifact;

public abstract class AbstractInstallArtifactCommandFormatter<T extends ManageableArtifact> implements InstallArtifactCommandFormatter<T> {

    protected static final int MAX_LINE_LENGTH = 80;

    private static final String NAME_COLUMN_NAME = "Name";

    private static final int NAME_COLUMN_MIN_WIDTH = NAME_COLUMN_NAME.length();

    private static final String VERSION_COLUMN_NAME = "Version";

    private static final int VERSION_COLUMN_MIN_WIDTH = VERSION_COLUMN_NAME.length();

    private static final String STATE_COLUMN_NAME = "State";

    /**
     * longest state name from {@link BundleArtifact#mapBundleState()}
     */
    private static final int STATE_COLUMN_MIN_WIDTH = 11;

    public List<String> formatList(List<T> artifacts) {
        Collections.sort(artifacts, new ManageableArtifactComparator<T>());

        int maxNameLength = NAME_COLUMN_MIN_WIDTH;
        int maxVersionLength = VERSION_COLUMN_MIN_WIDTH;
        for (T artifact : artifacts) {
            final int nameLength = artifact.getName().length();
            maxNameLength = nameLength > maxNameLength ? nameLength : maxNameLength;
            final int versionLength = artifact.getVersion().length();
            maxVersionLength = versionLength > maxVersionLength ? versionLength : maxVersionLength;
        }

        List<String> lines = new ArrayList<String>();
        int stateLength = MAX_LINE_LENGTH - (2 + maxNameLength + maxVersionLength);
        if (stateLength < STATE_COLUMN_MIN_WIDTH) {
            stateLength = STATE_COLUMN_MIN_WIDTH;
        }
        final String singleLineFormat = String.format("%%-%ds %%-%ds %%%ds", maxNameLength, maxVersionLength, stateLength);
        lines.add(String.format(singleLineFormat, NAME_COLUMN_NAME, VERSION_COLUMN_NAME, STATE_COLUMN_NAME));

        for (T artifact : artifacts) {
            lines.add(String.format(singleLineFormat, artifact.getName(), artifact.getVersion(), artifact.getState()));
        }

        return lines;
    }

    private static class ManageableArtifactComparator<T extends ManageableArtifact> implements Comparator<T> {

        public int compare(T artifact1, T artifact2) {
            int value = artifact1.getName().compareTo(artifact2.getName());
            if (value != 0) {
                return value;
            }
            return artifact1.getVersion().compareTo(artifact2.getVersion());
        }

    }
}
