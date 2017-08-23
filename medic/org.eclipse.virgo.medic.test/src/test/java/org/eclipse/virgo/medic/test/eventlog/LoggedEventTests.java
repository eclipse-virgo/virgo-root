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

package org.eclipse.virgo.medic.test.eventlog;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import org.eclipse.virgo.medic.eventlog.Level;
import org.junit.Test;


public class LoggedEventTests {
	
	@Test
	public void print() throws UnsupportedEncodingException {
		StringWriter printedWriter = new StringWriter();
		PrintStream printStream = new PrintStream(new WriterOutputStream(printedWriter), true, UTF_8.name());
		new LoggedEvent("code", Level.WARNING, "a", 5, true).print(printStream);

		String printed = printedWriter.toString();
		assertEquals("Code: 'code' Level: 'WARNING' Inserts: {'a', '5', 'true'}", printed);
	}
	
	@Test
	public void printWithOneInsert() throws UnsupportedEncodingException {
		StringWriter printedWriter = new StringWriter();
        PrintStream printStream = new PrintStream(new WriterOutputStream(printedWriter), true, UTF_8.name());
		
		new LoggedEvent("code", Level.WARNING, 5).print(printStream);
		
		String printed = printedWriter.toString();
		assertEquals("Code: 'code' Level: 'WARNING' Inserts: {'5'}", printed);
	}
	
	@Test
	public void printWithNoInserts() throws UnsupportedEncodingException {
		StringWriter printedWriter = new StringWriter();
        PrintStream printStream = new PrintStream(new WriterOutputStream(printedWriter), true, UTF_8.name());
		
		new LoggedEvent("code", Level.WARNING).print(printStream);
		
		String printed = printedWriter.toString();
		assertEquals("Code: 'code' Level: 'WARNING'", printed);
	}
	
	private static final class WriterOutputStream extends OutputStream {
		
		private final Writer writer;

		private WriterOutputStream(Writer writer) {
			this.writer = writer;
		}
		
		@Override
		public void write(int b) throws IOException {
			writer.write(b);
		}		
	}
}
