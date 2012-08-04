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

import org.eclipse.virgo.osgi.console.common.ConsoleOutputStream;

import java.io.*;
import java.util.ArrayList;

/**
 * This class implements grep. Since in Equinox 3.6 there is not piping support, grep cannot be implemented as a shell
 * command. That is why it is implemented as part of the command line editing features. The socket output stream inside
 * ConsoleOutputStream is substituted with a PipedOutputStream, so that what the command writes to the output stream
 * does not go to the console, but is read through a PipedInputStream and is filtered for the searched expression. After
 * all output of the command is read and filtered, the socket output stream inside ConsoleOutputStream is restored and
 * the lines of the command output, which match the grep expression, are written to it.
 */
public class Grep extends Thread {

    private String expression;

    private ConsoleOutputStream out;

    private PipedInputStream input;

    private PipedOutputStream output;

    private BufferedReader reader;

    private ArrayList<String> filteredOutput;

    private static int LENGTH = 4;

    public Grep(byte[] expression, OutputStream out) {
        String expr = (new String(expression)).trim();
        int index = expr.indexOf("grep");
        this.expression = expr.substring(index + LENGTH).trim();
        this.out = (ConsoleOutputStream) out;
        input = new PipedInputStream();
        try {
            output = new PipedOutputStream(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.out.setOutput(output);
        filteredOutput = new ArrayList<String>();
    }

    public void run() {
        reader = new BufferedReader(new InputStreamReader(input));
        boolean hasMore = true;
        try {
            while (hasMore) {
                String line = getLine();
                hasMore = line != null;
                if (hasMore) {
                    // last line containing the osgi prompt should be output although it does not contain the grep
                    // expression
                    if (line.contains(expression) || line.contains("osgi>")) {
                        filteredOutput.add(line);
                    }

                    if (line.contains("osgi>")) {
                        hasMore = false;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        out.setOutput(null);
        PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(out)));
        for (int i = 0; i < filteredOutput.size(); i++) {
            if (i == filteredOutput.size() - 1 && filteredOutput.get(i).contains("osgi>")) {
                writer.print(filteredOutput.get(i));
                writer.print(" ");
            } else {
                writer.println(filteredOutput.get(i));
            }
            writer.flush();
        }
    }

    private String getLine() throws IOException {
        StringBuilder line = new StringBuilder();
        boolean quit = false;
        while (!quit) {
            int c = reader.read();
            if (c < 0) {
                quit = true;
            } else {
                switch (c) {
                    case '\r':
                        break;
                    case '\n':
                        return line.toString();
                    default:
                        line.append((char) c);
                        if (line.toString().contains("osgi>")) {
                            return line.toString();
                        }
                        break;
                }
            }
        }

        return null;
    }
}
