/*******************************************************************************
 * Copyright (c) 2011 SAP AG
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Lazar Kirchev, SAP AG - initial contribution
 ******************************************************************************/

package org.eclipse.virgo.osgi.console.supportability;

import org.eclipse.virgo.osgi.console.common.ConsoleInputStream;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public class ConsoleInputHandlerTests {

    private static final long WAIT_TIME = 2000;

    @Test
    public void testHandler() throws Exception {
        PipedInputStream input = new PipedInputStream();
        PipedOutputStream output = new PipedOutputStream(input);
        ConsoleInputStream in = new ConsoleInputStream();
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ConsoleInputHandler handler = new ConsoleInputHandler(input, in, byteOut, null);
        byte[] expected = new byte[] { 'a', 'b', 'c', 'd', 'e', '\r', '\n' };
        output.write(expected);
        output.flush();
        handler.start();

        try {
            Thread.sleep(WAIT_TIME);
        } catch (Exception e) {
            // do nothing
        }

        byte[] read = new byte[expected.length];
        for (int i = 0; i < expected.length; i++) {
            in.read(read, i, 4);
            Assert.assertEquals("Incorrect char read. Position " + i + ", expected " + expected[i] + ", read " + read[i], expected[i], read[i]);
        }

        output.close();
        input.close();
    }

}
