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

package org.eclipse.virgo.test.tools;

import java.io.File;
import java.io.IOException;

import org.eclipse.virgo.util.io.FileCopyUtils;

public class AbstractSmokeTests {

    private String srcDir = "src/smokeTest/resources";

    private File bundlesDir = null;

    private File setupBundleResourcesDir() {
        if (bundlesDir == null) {
            File testExpanded = new File("./" + srcDir);
            bundlesDir = new File(testExpanded, "bundles");
        }
        return bundlesDir;
    }

    public void deployTestBundles(String flavor, String bundleName) {
        setupBundleResourcesDir();
        try {
            FileCopyUtils.copy(new File(bundlesDir, bundleName), new File(ServerUtils.getPickupDir(flavor), bundleName));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to deploy '" + bundleName + "'.");
        }
    }

    public void undeployTestBundles(String flavor, String bundleName) {
        setupBundleResourcesDir();
        File file = new File(ServerUtils.getPickupDir(flavor), bundleName);
        if (file.exists()) {
            file.delete();
        }
    }

}
