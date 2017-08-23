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

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.util.Locale;

import org.eclipse.virgo.medic.log.impl.StandardDelegatingPrintStream;
import org.junit.Before;
import org.junit.Test;

/**
 */
public class StandardDelegatingPrintStreamTests {
    
    private final WriterOutputStream writerOutputStream = new WriterOutputStream();    
            
    private final StandardDelegatingPrintStream delegatingPrintStream = new StandardDelegatingPrintStream(new PrintStream(writerOutputStream));
    
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    
    @Before
    public void setLocale() {
        Locale.setDefault(Locale.ENGLISH);
    }
    
    @Test
    public void appendChar() {
        delegatingPrintStream.append('a');
        assertEquals("a", this.writerOutputStream.getOutput());
    }
    
    @Test
    public void append() {
        delegatingPrintStream.append("a");
        assertEquals("a", this.writerOutputStream.getOutput());
    }
    
    @Test
    public void appendSegment() {
        delegatingPrintStream.append("abc", 1, 2);
        assertEquals("b", this.writerOutputStream.getOutput());
    }
    
    @Test
    public void format() {
        delegatingPrintStream.format("%s", "apple");
        assertEquals("apple", this.writerOutputStream.getOutput());
    }
    
    @Test
    public void formatWithLocale() {
        delegatingPrintStream.format(Locale.FRENCH, "%f", 3.1417d);
        assertEquals("3,141700", this.writerOutputStream.getOutput());
    }
    
    @Test
    public void printBoolean() {
        delegatingPrintStream.print("true");
        assertEquals("true", this.writerOutputStream.getOutput());
    }
    
    @Test
    public void printChar() {
        delegatingPrintStream.print('a');
        assertEquals("a", this.writerOutputStream.getOutput());
    }
    
    @Test
    public void printCharArray() {
        delegatingPrintStream.print(new char[] {'a', 'b', 'c'});
        assertEquals("abc", this.writerOutputStream.getOutput());
    }
    
    @Test
    public void printDouble() {
        delegatingPrintStream.print(3.1d);
        assertEquals("3.1", this.writerOutputStream.getOutput());
    }
    
    @Test
    public void printFloat() {
        delegatingPrintStream.print(3.1f);
        assertEquals("3.1", this.writerOutputStream.getOutput());
    }
    
    @Test
    public void printInt() {
        delegatingPrintStream.print(3);
        assertEquals("3", this.writerOutputStream.getOutput());
    }
    
    @Test
    public void printLong() {
        delegatingPrintStream.print(12345678901234567L);
        assertEquals("12345678901234567", this.writerOutputStream.getOutput());
    }
    
    @Test
    public void printObject() {
        delegatingPrintStream.print(new Integer(345));
        assertEquals("345", this.writerOutputStream.getOutput());
    }
    
    @Test
    public void printString() {
        delegatingPrintStream.print("hello");
        assertEquals("hello", this.writerOutputStream.getOutput());
    }
    
    @Test
    public void printf() {
        delegatingPrintStream.printf("%s", "alpha");
        assertEquals("alpha", this.writerOutputStream.getOutput());
    }
    
    @Test
    public void printfWithLocale() {
        delegatingPrintStream.printf(Locale.FRENCH, "%f", 3.1417d);
        assertEquals("3,141700", this.writerOutputStream.getOutput());
    }
    
    @Test
    public void println() {
        delegatingPrintStream.println();
        assertEquals(LINE_SEPARATOR, this.writerOutputStream.getOutput());
    }
    
    @Test
    public void printlnBoolean() {
        delegatingPrintStream.println(true);
        assertEquals("true" + LINE_SEPARATOR, this.writerOutputStream.getOutput());
    }
    
    @Test
    public void printlnChar() {
        delegatingPrintStream.println('a');
        assertEquals("a" + LINE_SEPARATOR, this.writerOutputStream.getOutput());
    }
    
    @Test
    public void printlnCharArray() {
        delegatingPrintStream.println(new char[] {'a', 'b', 'c'});
        assertEquals("abc" + LINE_SEPARATOR, this.writerOutputStream.getOutput());
    }
    
    @Test
    public void printlnDouble() {
        delegatingPrintStream.println(3.1d);
        assertEquals("3.1" + LINE_SEPARATOR, this.writerOutputStream.getOutput());
    }
    
    @Test
    public void printlnFloat() {
        delegatingPrintStream.println(3.1f);
        assertEquals("3.1" + LINE_SEPARATOR, this.writerOutputStream.getOutput());
    }
    
    @Test
    public void printlnInt() {
        delegatingPrintStream.println(3);
        assertEquals("3" + LINE_SEPARATOR, this.writerOutputStream.getOutput());
    }
    
    @Test
    public void printlnLong() {
        delegatingPrintStream.println(12345678901234567L);
        assertEquals("12345678901234567" + LINE_SEPARATOR, this.writerOutputStream.getOutput());
    }
    
    @Test
    public void printlnObject() {
        delegatingPrintStream.println(new Integer(345));
        assertEquals("345" + LINE_SEPARATOR, this.writerOutputStream.getOutput());
    }
    
    @Test
    public void printlnString() {
        delegatingPrintStream.println("hello");
        assertEquals("hello" + LINE_SEPARATOR, this.writerOutputStream.getOutput());
    }
    
    @Test
    public void write() {
        delegatingPrintStream.write('a');
        assertEquals("a", this.writerOutputStream.getOutput());
    }
    
    @Test
    public void writeByteArray() throws IOException {
        delegatingPrintStream.write(new byte[] {'a', 'b', 'c'});
        assertEquals("abc", this.writerOutputStream.getOutput());
    }
    
    @Test
    public void writeByteArraySegment() {
        delegatingPrintStream.write(new byte[] {'a', 'b', 'c'}, 1, 2);
        assertEquals("bc", this.writerOutputStream.getOutput());
    }
    
    @Test
    public void setDelegate() {
        delegatingPrintStream.print("hello");
        assertEquals("hello", this.writerOutputStream.getOutput());
        
        delegatingPrintStream.setDelegate(null);
        delegatingPrintStream.print("hello");
        assertEquals("hello", this.writerOutputStream.getOutput());
        
        delegatingPrintStream.setDelegate(new PrintStream(this.writerOutputStream));
        delegatingPrintStream.print("hello");
        assertEquals("hellohello", this.writerOutputStream.getOutput());
    }
    
    private static final class WriterOutputStream extends OutputStream {        
        private final StringWriter writer = new StringWriter();

        @Override
        public void write(int b) throws IOException {
            writer.write(b);
        }        
        
        public String getOutput() {
            return this.writer.toString();
        }
    }
}

