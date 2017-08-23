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

package org.eclipse.virgo.medic.log.impl;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;

import org.eclipse.virgo.medic.impl.config.ConfigurationChangeListener;
import org.eclipse.virgo.medic.impl.config.ConfigurationProvider;
import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.LoggingEvent;

public class LoggingPrintStreamWrapperTests {

    private PrintStream wrapper;

	@Test
    public void test() {
		produceOutput(this.wrapper);

		List<LoggingEvent> loggingEvents = CapturingAppender.getAndResetLoggingEvents();
        assertEquals(22, loggingEvents.size());

        assertEquals("abcdefghij", loggingEvents.get(0).getMessage());
        assertEquals("Three strings", loggingEvents.get(1).getMessage());
        assertEquals("last one on a new line.", loggingEvents.get(2).getMessage());
        assertEquals("3,1416", loggingEvents.get(3).getMessage());
        assertEquals("trueklm", loggingEvents.get(4).getMessage());
        assertEquals("a123.0456.078910toString", loggingEvents.get(5).getMessage());
        assertEquals("abcdThree strings", loggingEvents.get(6).getMessage());
        assertEquals("last one on a new line.", loggingEvents.get(7).getMessage());
        assertEquals("3,1416", loggingEvents.get(8).getMessage());
        assertEquals("false", loggingEvents.get(9).getMessage());
        assertEquals("b", loggingEvents.get(10).getMessage());
        assertEquals("", loggingEvents.get(11).getMessage());
        assertEquals("", loggingEvents.get(12).getMessage());
        assertEquals("abc", loggingEvents.get(13).getMessage());
        assertEquals("de", loggingEvents.get(14).getMessage());
        assertEquals("123.0", loggingEvents.get(15).getMessage());
        assertEquals("456.0", loggingEvents.get(16).getMessage());
        assertEquals("789", loggingEvents.get(17).getMessage());
        assertEquals("101112", loggingEvents.get(18).getMessage());
        assertEquals("toString", loggingEvents.get(19).getMessage());
        assertEquals("A string with a", loggingEvents.get(20).getMessage());
        assertEquals("new line in it.", loggingEvents.get(21).getMessage());
	}

	@Test
	public void testOutputWithinLoggingCode() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(baos);
		PrintStream wrapper = new LoggingPrintStreamWrapper(printStream, getClass().getName(), new ExecutionStackAccessor() {

			public Class<?>[] getExecutionStack() {
				return new Class[] {Logger.class};
			}
		}, new StubConfigurationProvider(), "theProperty");

		produceOutput(wrapper);

		List<LoggingEvent> loggingEvents = CapturingAppender.getAndResetLoggingEvents();
		assertEquals(0, loggingEvents.size());
	}

	private void produceOutput(PrintStream printStream) {
        printStream.append('a');
        printStream.append("bcd");
        printStream.append("abcdefghij", 4, 10);
        printStream.println();
        printStream.format("%s %s%n%s%n", "Three", "strings", "last one on a new line.");
        printStream.format(Locale.FRANCE, "%.4f%n", Math.PI);
        printStream.print(true);
        printStream.print('k');
        printStream.print(new char[] {'l', 'm', '\r', 'a'});
        printStream.print(123d);
        printStream.print(456f);
        printStream.print(7);
        printStream.print(8910l);
        printStream.print(new Object() {@Override public String toString() { return "toString";}});
        printStream.append('\n');
        printStream.print("abcd");
        printStream.printf("%s %s%n%s%n", "Three", "strings", "last one on a new line.");
        printStream.printf(Locale.FRANCE, "%.4f%n", Math.PI);
        printStream.println(false);
        printStream.println('b');
        printStream.println('\n');
        printStream.println(new char[] {'a', 'b', 'c', '\n', 'd', 'e'});
        printStream.println(123d);
        printStream.println(456f);
        printStream.println(789);
        printStream.println(101112l);
        printStream.println(new Object() {@Override public String toString() { return "toString";}});
        printStream.println("A string with a\nnew line in it.");
	}

    @Test
    public void testByteArrayHandling() {
        String string = "Some text to be turned into bytes.";
        String stringWithNewLine = string + "\n";
        byte[] stringBytes = stringWithNewLine.getBytes(UTF_8);

        wrapper.write(stringBytes, 0, stringBytes.length);

        List<LoggingEvent> loggingEvents = CapturingAppender.getAndResetLoggingEvents();
        assertEquals(1, loggingEvents.size());

        assertEquals("Some text to be turned into bytes.", loggingEvents.get(0).getMessage());
    }

    @Test
    public void testSingleByteHandling() {
        String string = "Some text to be turned into bytes.";
        byte[] stringBytes = string.getBytes(UTF_8);

        for (byte b: stringBytes) {
            wrapper.write(b);
        }
        wrapper.println();

        List<LoggingEvent> loggingEvents = CapturingAppender.getAndResetLoggingEvents();
        assertEquals(1, loggingEvents.size());

        assertEquals("Some text to be turned into bytes.", loggingEvents.get(0).getMessage());
    }

    @Test
    public void testPrintNullString(){

        String imNull = null;

        wrapper.println(imNull);
        wrapper.print(imNull);
        wrapper.println();

        List<LoggingEvent> loggingEvents = CapturingAppender.getAndResetLoggingEvents();
        assertEquals(2, loggingEvents.size());

        assertEquals("null", loggingEvents.get(0).getMessage());
        assertEquals("null", loggingEvents.get(1).getMessage());
    }

	@Before
    public void createWrapper() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(baos);
        this.wrapper = new LoggingPrintStreamWrapper(printStream, getClass().getName(), new SecurityManagerExecutionStackAccessor(), new StubConfigurationProvider(), "theProperty");
	}

	private final static class StubConfigurationProvider implements ConfigurationProvider {

		private final Hashtable<String, Object> configuration;

		private StubConfigurationProvider() {
			this.configuration = new Hashtable<String, Object>();
			this.configuration.put("theProperty", "true");
		}

		public Dictionary<String, Object> getConfiguration() {
			return this.configuration;
		}

        public void addChangeListener(ConfigurationChangeListener listener) {
            throw new UnsupportedOperationException();
        }

        public boolean removeChangeListener(ConfigurationChangeListener listener) {
            throw new UnsupportedOperationException();
        }

	}
}
