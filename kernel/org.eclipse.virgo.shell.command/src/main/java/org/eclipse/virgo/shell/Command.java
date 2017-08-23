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

package org.eclipse.virgo.shell;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <code>Command</code> is used to annotate shell commands. Applying the annotation to a class will identify it as a
 * provider of shell commands and allows the base command name to be specified. Applying the annotation to a method will
 * identify it as a shell command and allows the command's name to be specified. For example:
 * 
 * <pre>
 *{@link Command @Command}("do")
 * public class MyCommands {
 *     
 *    {@link Command @Command}("something")
 *     public String performCommand() {
 *         return "hello";
 *     }
 * }
 * <p />
 * </pre>
 * 
 * This class will result in a top-level command called do that provides a sub-command named something, i.e.
 * <code>do something</code> would be a valid command in the shell.
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.TYPE, ElementType.METHOD })
@Inherited
public @interface Command {

    String value();
}
