/*******************************************************************************
 * Copyright (c) 2010 SAP AG
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Lazar Kirchev, SAP AG - initial contribution
 ******************************************************************************/

package org.eclipse.virgo.osgi.console.supportability;

import org.eclipse.virgo.osgi.console.supportability.CommandCompleter;
import org.eclipse.virgo.osgi.console.supportability.Grep;
import org.eclipse.virgo.osgi.console.supportability.HistoryHolder;
import org.eclipse.virgo.osgi.console.common.ConsoleInputStream;
import org.eclipse.virgo.osgi.console.common.KEYS;
import org.eclipse.virgo.osgi.console.common.Scanner;
import org.eclipse.virgo.osgi.console.common.SimpleByteBuffer;
import org.osgi.framework.BundleContext;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * This class performs the processing of the input special characters, and updates respectively what is displayed in the
 * output. It handles escape sequences, delete, backspace, arrows, and provides command history and grep.
 */
public class ConsoleInputScanner extends Scanner {

    private static final byte TAB = 9;

    private boolean isCR = false;

    private boolean replace = false;

    private final HistoryHolder history;

    private final SimpleByteBuffer buffer;

    private CommandCompleter completer;

    public ConsoleInputScanner(ConsoleInputStream toShell, OutputStream toTelnet, BundleContext context) {
        super(toShell, toTelnet);
        history = new HistoryHolder();
        buffer = new SimpleByteBuffer();
        completer = new CommandCompleter(context);
    }

    public void scan(int b) throws IOException {
        b &= 0xFF;
        if (isCR) {
            isCR = false;
            if (b == LF) {
                return;
            }
        }
        if (isEsc) {
            scanEsc(b);
        } else {
            if (b == getBackspace()) {
                backSpace();
            } else if (b == TAB) {
                tab();
            } else if (b == CR) {
                isCR = true;
                processData();
            } else if (b == LF) {
                processData();
            } else if (b == ESC) {
                startEsc();
            } else if (b == getDel()) {
                delete();
            } else {
                if (b >= SPACE && b < MAX_CHAR) {
                    newChar(b);
                }
            }
        }
    }

    private void delete() throws IOException {
        clearLine();
        buffer.delete();
        echoBuff();
        flush();
    }

    private void backSpace() throws IOException {
        clearLine();
        buffer.backSpace();
        echoBuff();
        flush();
    }

    private void tab() throws IOException {
        byte[] cur = buffer.copyCurrentData();
        String currentInput = new String(cur).trim();
        String[] completionCandidates = completer.complete(currentInput);

        if (completionCandidates.length == 1) {
            String suffix = completionCandidates[0].substring(currentInput.length());
            byte[] completion = suffix.getBytes();
            for (byte symbol : completion) {
                buffer.insert(symbol);
                echo(symbol);

            }
            flush();
            return;
        }

        echo(CR);
        echo(LF);
        flush();

        if (completionCandidates.length == 0) {
            buffer.getCurrentData();
            String errorMessage = "No such command";
            for (byte symbol : errorMessage.getBytes()) {
                echo(symbol);
            }
        } else {
            for (String candidate : completionCandidates) {
                for (byte symbol : candidate.getBytes()) {
                    echo(symbol);
                }
                echo(SPACE);
                echo(SPACE);
            }
        }

        echo(CR);
        echo(LF);
        flush();
        echo('o');
        echo('s');
        echo('g');
        echo('i');
        echo('>');
        echo(SPACE);
        echoBuff();
        flush();
    }

    protected void clearLine() throws IOException {
        int size = buffer.getSize();
        int pos = buffer.getPos();
        for (int i = size - pos; i < size; i++) {
            echo(BS);
        }
        for (int i = 0; i < size; i++) {
            echo(SPACE);
        }
        for (int i = 0; i < size; i++) {
            echo(BS);
        }
    }

    protected void echoBuff() throws IOException {
        byte[] data = buffer.copyCurrentData();
        for (byte b : data) {
            echo(b);
        }
        int pos = buffer.getPos();
        for (int i = data.length; i > pos; i--) {
            echo(BS);
        }
    }

    protected void newChar(int b) throws IOException {
        if (buffer.getPos() < buffer.getSize()) {
            if (replace) {
                buffer.replace(b);
            } else {
                buffer.insert(b);
            }
            clearLine();
            echoBuff();
            flush();
        } else {
            if (replace) {
                buffer.replace(b);
            } else {
                buffer.insert(b);
            }
        }
    }

    private void processData() throws IOException {
        buffer.add(CR);
        buffer.add(LF);
        echo(CR);
        echo(LF);
        flush();
        byte[] curr = buffer.getCurrentData();
        history.add(curr);

        int index = 0;
        boolean isGrep = false;
        for (; index < curr.length; index++) {
            if (curr[index] == '|') {
                isGrep = true;
                break;
            }
        }

        if (isGrep) {
            byte[] grepExpression = new byte[curr.length - index - 1];
            System.arraycopy(curr, index + 1, grepExpression, 0, grepExpression.length);
            Grep grep = new Grep(grepExpression, toTelnet);
            grep.start();
            byte[] array = Arrays.copyOf(curr, index + 2);
            array[index] = CR;
            array[index + 1] = LF;
            toShell.add(array);
        } else {
            toShell.add(curr);
        }
    }

    public void resetHistory() {
        history.reset();
    }

    protected void scanEsc(final int b) throws IOException {
        esc += (char) b;
        KEYS key = checkEscape(esc);
        if (key == KEYS.UNFINISHED) {
            return;
        }
        if (key == KEYS.UNKNOWN) {
            isEsc = false;
            scan(b);
            return;
        }
        isEsc = false;
        switch (key) {
            case UP:
                processUpArrow();
                break;
            case DOWN:
                processDownArrow();
                break;
            case RIGHT:
                processRightArrow();
                break;
            case LEFT:
                processLeftArrow();
                break;
            case HOME:
                processHome();
                break;
            case END:
                processEnd();
                break;
            case PGUP:
                processPgUp();
                break;
            case PGDN:
                processPgDn();
                break;
            case INS:
                processIns();
                break;
            case DEL:
                delete();
                break;
            default: // CENTER
                break;
        }
    }

    private static final byte[] INVERSE_ON = { ESC, '[', '7', 'm' };

    private static final byte[] INVERSE_OFF = { ESC, '[', '2', '7', 'm' };

    private void echo(byte[] data) throws IOException {
        for (byte b : data) {
            echo(b);
        }
    }

    private void processIns() throws IOException {
        replace = !replace;
        int b = buffer.getCurrentChar();
        echo(INVERSE_ON);
        echo(replace ? 'R' : 'I');
        flush();
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            // do not care $JL-EXC$
        }
        echo(INVERSE_OFF);
        echo(BS);
        echo(b == -1 ? SPACE : b);
        echo(BS);
        flush();
    }

    private void processPgDn() throws IOException {
        byte[] last = history.last();
        if (last != null) {
            clearLine();
            buffer.set(last);
            echoBuff();
            flush();
        }
    }

    private void processPgUp() throws IOException {
        byte[] first = history.first();
        if (first != null) {
            clearLine();
            buffer.set(first);
            echoBuff();
            flush();
        }
    }

    private void processHome() throws IOException {
        int pos = buffer.resetPos();
        if (pos > 0) {
            for (int i = 0; i < pos; i++) {
                echo(BS);
            }
            flush();
        }
    }

    private void processEnd() throws IOException {
        int b;
        while ((b = buffer.goRight()) != -1) {
            echo(b);
        }
        flush();
    }

    private void processLeftArrow() throws IOException {
        if (buffer.goLeft()) {
            echo(BS);
            flush();
        }
    }

    private void processRightArrow() throws IOException {
        int b = buffer.goRight();
        if (b != -1) {
            echo(b);
            flush();
        }
    }

    private void processDownArrow() throws IOException {
        byte[] next = history.next();
        if (next != null) {
            clearLine();
            buffer.set(next);
            echoBuff();
            flush();
        }
    }

    private void processUpArrow() throws IOException {
        clearLine();
        byte[] prev = history.prev();
        buffer.set(prev);
        echoBuff();
        flush();
    }
}
