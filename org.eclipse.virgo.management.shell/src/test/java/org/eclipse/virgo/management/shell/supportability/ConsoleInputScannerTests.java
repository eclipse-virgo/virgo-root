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

import org.eclipse.osgi.framework.console.CommandProvider;
import org.eclipse.virgo.osgi.console.common.ConsoleInputStream;
import org.eclipse.virgo.osgi.console.common.ConsoleOutputStream;
import org.eclipse.virgo.osgi.console.common.KEYS;
import org.eclipse.virgo.osgi.console.telnet.ANSITerminalTypeMappings;
import org.eclipse.virgo.osgi.console.telnet.SCOTerminalTypeMappings;
import org.eclipse.virgo.osgi.console.telnet.TerminalTypeMappings;
import org.eclipse.virgo.osgi.console.telnet.VT100TerminalTypeMappings;
import org.eclipse.virgo.osgi.console.telnet.VT220TerminalTypeMappings;
import org.eclipse.virgo.osgi.console.telnet.VT320TerminalTypeMappings;
import org.eclipse.virgo.teststubs.osgi.framework.StubBundleContext;
import org.eclipse.virgo.teststubs.osgi.support.ObjectClassFilter;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class ConsoleInputScannerTests {

    private static int BS;

    private static final int LF = 10;

    private static final int CR = 13;

    private static final int ESC = 27;

    private static int DELL;

    @Test
    public void test() throws Exception {
        Set<TerminalTypeMappings> supportedEscapeSequences = new HashSet<TerminalTypeMappings>();
        supportedEscapeSequences.add(new ANSITerminalTypeMappings());
        supportedEscapeSequences.add(new VT100TerminalTypeMappings());
        supportedEscapeSequences.add(new VT220TerminalTypeMappings());
        supportedEscapeSequences.add(new VT320TerminalTypeMappings());
        supportedEscapeSequences.add(new SCOTerminalTypeMappings());

        for (TerminalTypeMappings ttMappings : supportedEscapeSequences) {
            Map<String, KEYS> escapesToKey = ttMappings.getEscapesToKey();
            Map<KEYS, byte[]> keysToEscapes = new HashMap<KEYS, byte[]>();
            for (Entry<String, KEYS> entry : escapesToKey.entrySet()) {
                keysToEscapes.put(entry.getValue(), entry.getKey().getBytes());
            }

            BS = ttMappings.getBackspace();
            DELL = ttMappings.getDel();

            testScan(ttMappings, keysToEscapes);
        }
    }

    private void testScan(TerminalTypeMappings mappings, Map<KEYS, byte[]> keysToEscapes) throws Exception {
        ConsoleInputStream in = new ConsoleInputStream();
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ConsoleOutputStream out = new ConsoleOutputStream(byteOut);
        StubBundleContext context = new StubBundleContext();
        TestCommandProvider commandProvider = new TestCommandProvider();
        context.registerService(CommandProvider.class.getName(), commandProvider, null);
        context.addFilter(new ObjectClassFilter(CommandProvider.class.getName()));
        ConsoleInputScanner scanner = new ConsoleInputScanner(in, out, context);
        scanner.setBackspace(mappings.getBackspace());
        scanner.setCurrentEscapesToKey(mappings.getEscapesToKey());
        scanner.setDel(mappings.getDel());
        scanner.setEscapes(mappings.getEscapes());

        byte[] line1 = new byte[] { 'a', 'b', 'c', 'd', 'e' };
        byte[] line2 = new byte[] { 't', 'e', 's', 't' };
        byte[] line3 = new byte[] { 'l', 'a', 's', 't' };

        addLine(scanner, line1);
        checkInpusStream(in, line1);

        addLine(scanner, line2);
        checkInpusStream(in, line2);

        addLine(scanner, line3);
        checkInpusStream(in, line3);

        add(scanner, keysToEscapes.get(KEYS.UP));
        add(scanner, keysToEscapes.get(KEYS.UP));
        String res = byteOut.toString();
        Assert.assertTrue("Error processing up arrow; expected test, actual " + res.substring(res.length() - 4), res.endsWith("test"));

        add(scanner, keysToEscapes.get(KEYS.DOWN));
        res = byteOut.toString();
        Assert.assertTrue("Error processing down arrow; expected last, actual " + res.substring(res.length() - 4), res.endsWith("last"));

        add(scanner, keysToEscapes.get(KEYS.PGUP));
        res = byteOut.toString();
        Assert.assertTrue("Error processing PageUp; expected abcde, actual " + res.substring(res.length() - 4), res.endsWith("abcde"));

        add(scanner, keysToEscapes.get(KEYS.PGDN));
        res = byteOut.toString();
        Assert.assertTrue("Error processing PageDown; expected last, actual " + res.substring(res.length() - 4), res.endsWith("last"));

        if (BS > 0) {
            scanner.scan(BS);
            res = byteOut.toString();
            Assert.assertTrue("Error processing backspace; expected las, actual " + res.substring(res.length() - 3), res.endsWith("las"));
            scanner.scan('t');
        }

        if (DELL > 0) {
            add(scanner, keysToEscapes.get(KEYS.LEFT));
            scanner.scan(DELL);
            res = byteOut.toString();
            Assert.assertTrue("Error processing del; expected las, actual " + res.substring(res.length() - 3), res.endsWith("las"));
            scanner.scan('t');
        }

        add(scanner, keysToEscapes.get(KEYS.LEFT));
        add(scanner, keysToEscapes.get(KEYS.LEFT));
        add(scanner, keysToEscapes.get(KEYS.RIGHT));
        if (DELL > 0) {
            scanner.scan(DELL);
        } else {
            add(scanner, keysToEscapes.get(KEYS.DEL));
        }
        res = byteOut.toString();
        Assert.assertTrue("Error processing arrows; expected las, actual " + res.substring(res.length() - 3), res.endsWith("las"));
        scanner.scan('t');

        if (keysToEscapes.get(KEYS.DEL) != null) {
            add(scanner, keysToEscapes.get(KEYS.LEFT));
            add(scanner, keysToEscapes.get(KEYS.DEL));
            res = byteOut.toString();
            Assert.assertTrue("Error processing delete; expected las, actual " + res.substring(res.length() - 3), res.endsWith("las"));
            scanner.scan('t');
        }

        add(scanner, keysToEscapes.get(KEYS.HOME));
        if (DELL > 0) {
            scanner.scan(DELL);
        } else {
            add(scanner, keysToEscapes.get(KEYS.DEL));
        }
        res = byteOut.toString();
        res = res.substring(res.length() - 6, res.length() - 3);
        Assert.assertTrue("Error processing Home; expected ast, actual " + res, res.equals("ast"));
        scanner.scan('l');

        add(scanner, keysToEscapes.get(KEYS.END));
        add(scanner, keysToEscapes.get(KEYS.LEFT));
        if (DELL > 0) {
            scanner.scan(DELL);
        } else {
            add(scanner, keysToEscapes.get(KEYS.DEL));
        }
        res = byteOut.toString();
        Assert.assertTrue("Error processing End; expected las, actual " + res.substring(res.length() - 3), res.endsWith("las"));
        scanner.scan('t');

        add(scanner, keysToEscapes.get(KEYS.LEFT));
        add(scanner, keysToEscapes.get(KEYS.INS));
        scanner.scan('a');
        res = byteOut.toString();
        Assert.assertTrue("Error processing Ins; expected las, actual " + res.substring(res.length() - 4), res.endsWith("lasa"));

        scanner.scan(CR);
        scanner.scan(LF);
        scanner.scan('t');
        scanner.scan('e');
        scanner.scan(9);
        res = byteOut.toString();
        Assert.assertTrue("Expected completion suggestions are not contained in the output", res.contains("test  testMethod"));
    }

    private static void addLine(ConsoleInputScanner scanner, byte[] line) throws Exception {
        for (byte b : line) {
            try {
                scanner.scan(b);
            } catch (Exception e) {
                System.out.println("Error scanning symbol " + b);
                throw new Exception("Error scanning symbol" + b);
            }
        }

        try {
            scanner.scan(CR);
        } catch (Exception e) {
            System.out.println("Error scanning symbol " + CR);
            throw new Exception("Error scanning symbol " + CR);
        }

        try {
            scanner.scan(LF);
        } catch (Exception e) {
            System.out.println("Error scanning symbol " + LF);
            throw new Exception("Error scanning symbol " + LF);
        }
    }

    private void add(ConsoleInputScanner scanner, byte[] sequence) throws Exception {
        scanner.scan(ESC);
        for (byte b : sequence) {
            scanner.scan(b);
        }
    }

    private void checkInpusStream(ConsoleInputStream in, byte[] expected) throws Exception {
        // the actual number of bytes in the stream is two more than the bytes in the array, because of the CR and LF
        // symbols, added after the array
        byte[] read = new byte[expected.length + 2];
        for (int i = 0; i < expected.length; i++) {
            in.read(read, i, 1);
            Assert.assertEquals("Incorrect char read. Position " + i + ", expected " + expected[i] + ", read " + read[i], expected[i], read[i]);
        }
        in.read(read, expected.length, 1);
        in.read(read, expected.length + 1, 1);
    }

    class TestCommandProvider implements CommandProvider {

        @Override
        public String getHelp() {
            // TODO Auto-generated method stub
            return null;
        }

        public void _test() {

        }

        public void _testMethod() {

        }

        public void _dummy() {

        }

        public void _fake() {

        }

    }
}
