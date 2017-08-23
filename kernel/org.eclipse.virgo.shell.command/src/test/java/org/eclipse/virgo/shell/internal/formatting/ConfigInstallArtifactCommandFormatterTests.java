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
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.management.ObjectName;

import org.junit.Test;

import org.eclipse.virgo.kernel.model.management.ManageableArtifact;
import org.eclipse.virgo.shell.internal.formatting.ConfigInstallArtifactCommandFormatter;
import org.eclipse.virgo.test.stubs.service.cm.StubConfiguration;
import org.eclipse.virgo.test.stubs.service.cm.StubConfigurationAdmin;

public class ConfigInstallArtifactCommandFormatterTests {

    private final StubConfigurationAdmin configurationAdmin = new StubConfigurationAdmin();

    private final ConfigInstallArtifactCommandFormatter formatter = new ConfigInstallArtifactCommandFormatter(configurationAdmin);

    @Test
    public void examine() throws IOException {
        StubConfiguration configuration = this.configurationAdmin.createConfiguration("testPid");
        configuration.setBundleLocation("/a/location");
        configuration.addProperty("key1", new Object[] { "value11111111111111111111111111111111111111111111111111111111111111111111111111111111a",
            "value1b" });
        configuration.addProperty("key2", "value2");

        List<String> lines = this.formatter.formatExamine(new StubManageableArtifact());
        assertOutputEquals(new File("src/test/resources/org/eclipse/virgo/kernel/shell/internal/formatting/config-examine.txt"), lines);
    }

    private static class StubManageableArtifact implements ManageableArtifact {

        public ObjectName[] getDependents() {
            throw new UnsupportedOperationException();
        }

        public String getName() {
            return "testPid";
        }

        public Map<String, String> getProperties() {
            throw new UnsupportedOperationException();
        }

        public String getState() {
            throw new UnsupportedOperationException();
        }

        public String getRegion() {
            throw new UnsupportedOperationException();
        }

        public String getType() {
            throw new UnsupportedOperationException();
        }

        public String getVersion() {
            throw new UnsupportedOperationException();
        }

        public void start() {
            throw new UnsupportedOperationException();
        }

        public void stop() {
            throw new UnsupportedOperationException();
        }

        public void uninstall() {
            throw new UnsupportedOperationException();
        }

        public boolean refresh() {
            throw new UnsupportedOperationException();
        }

    }

}
