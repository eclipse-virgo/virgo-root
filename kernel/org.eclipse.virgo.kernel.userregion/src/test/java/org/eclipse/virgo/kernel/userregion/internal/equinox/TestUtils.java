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

package org.eclipse.virgo.kernel.userregion.internal.equinox;

import java.io.File;

/**
 */
public class TestUtils {

    public static boolean deleteRecursively(File root) {
        if (root.exists()) {
            if (root.isDirectory()) {
                File[] children = root.listFiles();
                if (children == null) {
					throw new IllegalStateException("Failed to list files in '" + root + "'.");
				}
                for (File file : children) {
                    deleteRecursively(file);
                }
            }
            return root.delete();
        }
        return false;
    }
}
