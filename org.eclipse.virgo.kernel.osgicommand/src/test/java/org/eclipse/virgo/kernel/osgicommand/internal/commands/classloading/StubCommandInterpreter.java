/*******************************************************************************
 * Copyright (c) 2010 SAP AG
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Hristo Iliev, SAP AG - initial contribution
 ******************************************************************************/
package org.eclipse.virgo.kernel.osgicommand.internal.commands.classloading;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.osgi.framework.Bundle;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Dictionary;

/**
 * Stub class for mocking {@link CommandInterpreter}. Needed for testing Equinox shell commands
 */
public class StubCommandInterpreter implements CommandInterpreter {

    String[] arguments;
    int argumentNumber = 0;

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintStream output = new PrintStream(baos);

    public String[] getArguments() {
        return arguments;
    }

    public void setArguments(String[] arguments) {
        this.arguments = arguments;
    }

    public int getArgumentNumber() {
        return argumentNumber;
    }

    public void setArgumentNumber(int argumentNumber) {
        this.argumentNumber = argumentNumber;
    }

    public String getOutput() {
        try {
            output.flush();
            return new String(baos.toByteArray());
        } finally {
            output.close();
        }
    }

    public String nextArgument() {
        if (argumentNumber >= arguments.length) {
            return null;
        }
        return arguments[argumentNumber++];
    }

    public Object execute(String s) {
        return null;
    }

    public void print(Object o) {
        output.print(o);
    }

    public void println() {
        output.println();
    }

    public void println(Object o) {
        output.println(o);
    }

    public void printStackTrace(Throwable throwable) {
        throwable.printStackTrace(output);
    }

    @SuppressWarnings("rawtypes")
    public void printDictionary(Dictionary dictionary, String s) {
    }

    public void printBundleResource(Bundle bundle, String s) {
    }
}
