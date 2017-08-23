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

    @Override
    public PrintStream append(char c) {
        return delegate.append(c);
    }

    @Override
    public PrintStream append(CharSequence csq, int start, int end) {
        return delegate.append(csq, start, end);
    }

    @Override
    public PrintStream append(CharSequence csq) {
        return delegate.append(csq);
    }

    @Override
    public boolean checkError() {
        return delegate.checkError();
    }

    @Override
    public void close() {
        delegate.close();
    }

    @Override
    public boolean equals(Object obj) {
        return delegate.equals(obj);
    }

    @Override
    public void flush() {
        delegate.flush();
    }

    @Override
    public PrintStream format(Locale l, String format, Object... args) {
        return delegate.format(l, format, args);
    }

    @Override
    public PrintStream format(String format, Object... args) {
        return delegate.format(format, args);
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public void print(boolean b) {
        delegate.print(b);
    }

    @Override
    public void print(char c) {
        delegate.print(c);
    }

    @Override
    public void print(char[] s) {
        delegate.print(s);
    }

    @Override
    public void print(double d) {
        delegate.print(d);
    }

    @Override
    public void print(float f) {
        delegate.print(f);
    }

    @Override
    public void print(int i) {
        delegate.print(i);
    }

    @Override
    public void print(long l) {
        delegate.print(l);
    }

    @Override
    public void print(Object obj) {
        delegate.print(obj);
    }

    @Override
    public void print(String s) {
        delegate.print(s);
    }

    @Override
    public PrintStream printf(Locale l, String format, Object... args) {
        return delegate.printf(l, format, args);
    }

    @Override
    public PrintStream printf(String format, Object... args) {
        return delegate.printf(format, args);
    }

    @Override
    public void println() {
        delegate.println();
    }

    @Override
    public void println(boolean x) {
        delegate.println(x);
    }

    @Override
    public void println(char x) {
        delegate.println(x);
    }

    @Override
    public void println(char[] x) {
        delegate.println(x);
    }

    @Override
    public void println(double x) {
        delegate.println(x);
    }

    @Override
    public void println(float x) {
        delegate.println(x);
    }

    @Override
    public void println(int x) {
        delegate.println(x);
    }

    @Override
    public void println(long x) {
        delegate.println(x);
    }

    @Override
    public void println(Object x) {
        delegate.println(x);
    }

    @Override
    public void println(String x) {
        delegate.println(x);
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

    @Override
    public void write(byte[] buf, int off, int len) {
        delegate.write(buf, off, len);
    }

    @Override
    public void write(byte[] b) throws IOException {
        delegate.write(b);
    }

    @Override
    public void write(int b) {
        delegate.write(b);
    }

    private static final class NoOpOutputStream extends OutputStream {

        @Override
        public void write(int b) throws IOException {
        }
    }
}
