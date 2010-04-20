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

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Locale;

import org.eclipse.virgo.medic.log.DelegatingPrintStream;


/**
 * Standard implementation of {@link DelegatingPrintStream}.
 * 
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread-safe
 * 
 */
public final class StandardDelegatingPrintStream extends PrintStream implements DelegatingPrintStream {

    private static final PrintStream NO_OP_PRINT_STREAM = new PrintStream(new NoOpOutputStream());

    private volatile PrintStream delegate;

    /**
     * Creates a new <code>StandardDelegatingPrintStream</code> that will delegate to the given <code>delegate</code>.
     * 
     * @param delegate The delegate
     */
    public StandardDelegatingPrintStream(PrintStream delegate) {
        super(new NoOpOutputStream());
        setDelegate(delegate);
    }

    public void setDelegate(PrintStream printStream) {
        if (printStream == null) {
            this.delegate = NO_OP_PRINT_STREAM;
        } else {
            this.delegate = printStream;
        }
    }

    public PrintStream append(char c) {
        return delegate.append(c);
    }

    public PrintStream append(CharSequence csq, int start, int end) {
        return delegate.append(csq, start, end);
    }

    public PrintStream append(CharSequence csq) {
        return delegate.append(csq);
    }

    public boolean checkError() {
        return delegate.checkError();
    }

    public void close() {
        delegate.close();
    }

    public boolean equals(Object obj) {
        return delegate.equals(obj);
    }

    public void flush() {
        delegate.flush();
    }

    public PrintStream format(Locale l, String format, Object... args) {
        return delegate.format(l, format, args);
    }

    public PrintStream format(String format, Object... args) {
        return delegate.format(format, args);
    }

    public int hashCode() {
        return delegate.hashCode();
    }

    public void print(boolean b) {
        delegate.print(b);
    }

    public void print(char c) {
        delegate.print(c);
    }

    public void print(char[] s) {
        delegate.print(s);
    }

    public void print(double d) {
        delegate.print(d);
    }

    public void print(float f) {
        delegate.print(f);
    }

    public void print(int i) {
        delegate.print(i);
    }

    public void print(long l) {
        delegate.print(l);
    }

    public void print(Object obj) {
        delegate.print(obj);
    }

    public void print(String s) {
        delegate.print(s);
    }

    public PrintStream printf(Locale l, String format, Object... args) {
        return delegate.printf(l, format, args);
    }

    public PrintStream printf(String format, Object... args) {
        return delegate.printf(format, args);
    }

    public void println() {
        delegate.println();
    }

    public void println(boolean x) {
        delegate.println(x);
    }

    public void println(char x) {
        delegate.println(x);
    }

    public void println(char[] x) {
        delegate.println(x);
    }

    public void println(double x) {
        delegate.println(x);
    }

    public void println(float x) {
        delegate.println(x);
    }

    public void println(int x) {
        delegate.println(x);
    }

    public void println(long x) {
        delegate.println(x);
    }

    public void println(Object x) {
        delegate.println(x);
    }

    public void println(String x) {
        delegate.println(x);
    }

    public String toString() {
        return delegate.toString();
    }

    public void write(byte[] buf, int off, int len) {
        delegate.write(buf, off, len);
    }

    public void write(byte[] b) throws IOException {
        delegate.write(b);
    }

    public void write(int b) {
        delegate.write(b);
    }

    private static final class NoOpOutputStream extends OutputStream {

        public void write(int b) throws IOException {
        }
    }
}
