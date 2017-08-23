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

package org.eclipse.virgo.nano.serviceability.dump.internal;

import static org.easymock.EasyMock.createNiceMock;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.osgi.service.resolver.State;
import org.eclipse.virgo.util.io.PathReference;
import org.eclipse.virgo.util.io.ZipUtils;
import org.junit.Test;

/**
 */
public class ResolutionStateDumperTests {

    @Test
    public void createDump() throws Exception {

        State state = createNiceMock(State.class);
        StubStateWriter writer = new StubStateWriter();

        byte[] bytes = new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
        writer.addBytes(state, bytes);

        ResolutionStateDumper dumper = new ResolutionStateDumper(new StubSystemStateAccessor(state), writer);

        File outputFile = new File("./build/dump.zip");
        if (outputFile.exists()) {
            assertTrue(outputFile.delete());
        }

        dumper.dump(outputFile);

        assertTrue(outputFile.exists());

        PathReference unzipLocation = new PathReference("build/dump");
        if (unzipLocation.exists()) {
            assertTrue(unzipLocation.delete(true));
        }

        ZipUtils.unzipTo(new PathReference(outputFile), unzipLocation);
        File stateFile = new File("build/dump/state/state");

        assertTrue(stateFile.exists());
        assertEquals(10, stateFile.length());

        byte[] actualBytes = new byte[10];

        try (InputStream in = new FileInputStream(stateFile)) {
            in.read(actualBytes);
        }

        assertArrayEquals(bytes, actualBytes);
    }

    private static final class StubSystemStateAccessor implements SystemStateAccessor {

        private final State systemState;

        private StubSystemStateAccessor(State systemState) {
            this.systemState = systemState;
        }

        public State getSystemState() {
            return this.systemState;
        }
    }

    private static final class StubStateWriter implements StateWriter {

        private final Map<State, byte[]> stateBytes = new HashMap<State, byte[]>();

        private void addBytes(State state, byte[] bytes) {
            this.stateBytes.put(state, bytes);
        }

        public void writeState(State state, File outputDir) throws IOException {
            byte[] bytes = this.stateBytes.get(state);
            if (bytes == null) {
                throw new IOException();
            } else {
                FileOutputStream fos = null;

                try {
                    fos = new FileOutputStream(new File(outputDir, "state"));
                    fos.write(bytes);
                } finally {
                    if (fos != null) {
                        fos.close();
                    }
                }
            }
        }
    }
}
