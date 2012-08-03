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

import java.io.ByteArrayOutputStream;

import org.eclipse.virgo.osgi.console.common.ConsoleOutputStream;
import org.junit.Assert;
import org.junit.Test;

public class GrepTests {

    private static final int CR = 13;

    private static final int LF = 10;

    @Test
    public void testGrep() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ConsoleOutputStream consoleOut = new ConsoleOutputStream(output);
        byte[] expression = "test".getBytes();
        Grep grep = new Grep(expression, consoleOut);
        grep.start();
        byte[] line1 = new byte[] { 't', 'e', 's', 't' };
        byte[] line2 = new byte[] { 'a', 'b', 'c', 'd' };
        byte[] line3 = new byte[] { 't', 'e', 's', 't', 'e', 'r' };
        byte[] line4 = new byte[] { 'k', 'l', 'm', 'n' };
        byte[] line5 = new byte[] { 't', 'e', 's', 't', 'i', 'n', 'g' };
        byte[] line6 = new byte[] { 'o', 's', 'g', 'i', '>' };

        addLine(consoleOut, line1);
        addLine(consoleOut, line2);
        addLine(consoleOut, line3);
        addLine(consoleOut, line4);
        addLine(consoleOut, line5);

        for (byte b : line6) {
            consoleOut.write(b);
        }
        consoleOut.flush();

        grep.join();

        byte[] expectedResult = new byte[] { 't', 'e', 's', 't', CR, LF, 't', 'e', 's', 't', 'e', 'r', CR, LF, 't', 'e', 's', 't', 'i', 'n', 'g', CR,
            LF, 'o', 's', 'g', 'i', '>', ' ' };
        byte[] result = output.toByteArray();

        Assert.assertNotNull("Bytes not written; result null", result);
        Assert.assertFalse("Bytes not written; result empty", result.length == 0);
        Assert.assertTrue("Bytes written to output differ in number from expected", result.length == expectedResult.length);

        for (int i = 0; i < result.length; i++) {
            Assert.assertEquals("Wrong char read. Position " + i + ", expected " + expectedResult[i] + ", read " + result[i], expectedResult[i],
                result[i]);
        }
    }

    private static void addLine(ConsoleOutputStream out, byte[] line) throws Exception {
        for (byte b : line) {
            try {
                out.write(b);
            } catch (Exception e) {
                System.out.println("Error writing symbol " + b);
                throw new Exception("Error writing symbol" + b);
            }
        }

        try {
            out.write(CR);
        } catch (Exception e) {
            System.out.println("Error writing symbol " + CR);
            throw new Exception("Error writing symbol " + CR);
        }

        try {
            out.write(LF);
        } catch (Exception e) {
            System.out.println("Error writing symbol " + LF);
            throw new Exception("Error writing symbol " + LF);
        }

        out.flush();
    }
}
