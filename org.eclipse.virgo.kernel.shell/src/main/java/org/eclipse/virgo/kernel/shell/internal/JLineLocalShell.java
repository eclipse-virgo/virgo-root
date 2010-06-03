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

package org.eclipse.virgo.kernel.shell.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jline.ConsoleReader;

import org.eclipse.virgo.kernel.shell.CommandExecutor;
import org.eclipse.virgo.kernel.shell.LinePrinter;
import org.eclipse.virgo.kernel.shell.internal.completers.CommandCompleterRegistry;
import org.eclipse.virgo.kernel.shell.internal.completers.CommandRegistryBackedJLineCompletor;
import org.eclipse.virgo.kernel.shell.internal.completers.DelegatingJLineCompletor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * SpringShellWrapper is a wrapper around a JLine {@link ConsoleReader} that knows how to talk to 
 * the dm Server and handle shutdown from the client.
 * </p>
 * 
 * <strong>Concurrent Semantics</strong><br />
 *
 * SpringShellWrapper is thread safe
 *
 */
final class JLineLocalShell implements Runnable, LocalShell, CommandExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(JLineLocalShell.class);

    private static final String PROMPT = ":> ";

    private static final String EXIT = "exit";

    private static final String EXIT_MSG = "Goodbye.";

	private static final String CR_LF = "\r\n";

    private final CommandProcessor commandProcessor;

    private final InputStream in;

    private final PrintStream out;

    private final Set<ExitCallback> callbacks = new HashSet<ExitCallback>();

    private final CommandRegistry commandRegistry;

    private final CommandCompleterRegistry completerRegistry;

    public JLineLocalShell(CommandRegistry commandRegistry, CommandCompleterRegistry completerRegistry, CommandProcessor commandProcessor,
        InputStream in, PrintStream out) {
        this.commandRegistry = commandRegistry;
        this.completerRegistry = completerRegistry;
        this.commandProcessor = commandProcessor;
        this.in = in;
        this.out = out;
    }

    /**
     * {@inheritDoc}
     */
    public void addExitCallback(ExitCallback exitCallback) {
        if (exitCallback != null) {
            this.callbacks.add(exitCallback);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void run() {
        ConsoleReader console;
        try {
            console = new ConsoleReader(this.in, new LineSeparatorEnforcingWriter(new PrintWriter((this.out))));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        LOGGER.info("Launching a new shell in thread %s", Thread.currentThread().getName());

        console.setUseHistory(true);
        console.setUsePagination(true);
        console.setDefaultPrompt(PROMPT);

        console.addCompletor(new CommandRegistryBackedJLineCompletor(this.commandRegistry));
        console.addCompletor(new DelegatingJLineCompletor(this.completerRegistry));
        
        LinePrinter consoleLinePrinter = new ConsoleLinePrinter(console);

        CommandSession commandSession = this.commandProcessor.createSession();
        
        try {
            this.printHeader(console);
            
            while (new SessionCommandExecutor(commandSession).execute(console.readLine(), consoleLinePrinter)) { }
         
            printToScreen(console, Arrays.asList(EXIT_MSG));
            informCallbacksOfExit();
            
        } catch (IOException e) {
            this.out.println("Error occurred while writing to the shell. Please restart the shell bundle.");
            throw new IllegalStateException("The Shell was unable to recover from an IO error", e);
        }
    }

    private void informCallbacksOfExit() {
        for (ExitCallback exitCallback : this.callbacks) {
            exitCallback.onExit();
        }
    }

    /**
     * Each line in this header must be kept under 80 characters in order to avoid line wrapping problems on some
     * consoles.
     * 
     * Generated with the help of http://www.network-science.de/ascii/ using the 'slant' font.
     * 
     * @param console
     * @throws IOException
     */
    private void printHeader(ConsoleReader console) throws IOException {
        List<String> lines = new ArrayList<String>();
        lines.add("");
		lines.add("    _    ___");
		lines.add("   | |  / (_)________ _____");
		lines.add("   | | / / / ___/ __ `/ __ \\");
		lines.add("   | |/ / / /  / /_/ / /_/ /");
		lines.add("   |___/_/_/   \\__, /\\____/");
		lines.add("              /____/");
        lines.add("");
        lines.add("Type 'help' to see the available commands.");
        printToScreen(console, lines);
    }

    private void printToScreen(ConsoleReader console, List<String> lines) throws IOException {
        console.printString(String.format(CR_LF));
        for (String line : lines) {
            console.printString(String.format("%s%s", line, CR_LF));
        }
        console.printString(String.format(CR_LF));
    }
    
    private static final class LineSeparatorEnforcingWriter extends Writer {
        
        private final Writer delegate;
        
        LineSeparatorEnforcingWriter(Writer delegate) {
            this.delegate = delegate;
        }

        /** 
         * {@inheritDoc}
         */
        @Override
        public void close() throws IOException {
            this.delegate.close();
        }

        /** 
         * {@inheritDoc}
         */
        @Override
        public void flush() throws IOException {
            this.delegate.flush();
        }

        /** 
         * {@inheritDoc}
         */
        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
            for (int i = off; i < len; i++) {
                char c = cbuf[i];
                if (c == '\n') {
                    this.delegate.write('\r');
                }
                this.delegate.write(c);
            }
        }
    }

    public boolean execute(String commandLine, LinePrinter linePrinter) throws IOException {
        CommandSession commandSession = this.commandProcessor.createSession();
        CommandExecutor sessionCommandExecutor = new SessionCommandExecutor(commandSession);
        return sessionCommandExecutor.execute(commandLine, linePrinter);
    }

    private static void printList(LinePrinter linePrinter, List<String> executionResult) throws IOException {
        linePrinter.println("");
        for (String line : executionResult) {
            linePrinter.println(line);
        }
    }

    final static class ConsoleLinePrinter implements LinePrinter {
        
        private final ConsoleReader consoleReader;
        
        ConsoleLinePrinter(ConsoleReader consoleReader) {
            this.consoleReader = consoleReader;
        }

        public LinePrinter println(String line) throws IOException {
            this.consoleReader.printString(String.format("%s%s", line, CR_LF));
            return this;
        }
        
    }

    final static class SessionCommandExecutor implements CommandExecutor {
        
        private final CommandSession commandSession;
        
        SessionCommandExecutor(CommandSession commandSession) {
            this.commandSession = commandSession;
        }

        public boolean execute(String commandLine, LinePrinter linePrinter) throws IOException {
            if (commandLine!=null) {
                commandLine = commandLine.trim();
                if (commandLine.length()>0) {
                    if (EXIT.equalsIgnoreCase(commandLine)) {
                        return false;
                    } else {
                        try {
                            List<String> executionResult = this.commandSession.execute(commandLine);
                            if (executionResult == null) {
                                linePrinter.println(String.format("Null result returned for '%s'", commandLine));
                            } else {
                                printList(linePrinter, executionResult);
                            }
                        } catch (Exception e) {
                            linePrinter.println(String.format("%s while executing command '%s': '%s'", e.getClass().getName(), commandLine,
                                e.getMessage()));
                        }
                    }
                }
            }
            return true;
        }
        
    }
}
