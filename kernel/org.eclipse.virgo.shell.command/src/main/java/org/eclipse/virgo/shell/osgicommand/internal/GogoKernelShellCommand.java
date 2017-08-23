/*******************************************************************************
 * Copyright (c) 2011 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.shell.osgicommand.internal;

import java.io.IOException;

import org.eclipse.virgo.shell.CommandExecutor;
import org.apache.felix.service.command.Descriptor;

/**
 * {@link GogoKernelShellCommand} binds the vsh commands to the Gogo shell.
 * <p />
 * Thread safe.
 */
public final class GogoKernelShellCommand {

    /*
     * The following operations must be listed in
     * org.eclipse.virgo.kernel.osgicommand.Activator#KERNEL_SHELL_SUBCOMMANDS.
     */
    private static final String BUNDLE_OP = "bundle";

    private static final String CONFIG_OP = "config";

    private static final String PACKAGE_OP = "package";

    private static final String PAR_OP = "par";

    private static final String PLAN_OP = "plan";

    private static final String SERVICE_OP = "service";

    private static final String INSTALL_OP = "install";

    private static final String SHUTDOWN_OP = "shutdown";

    private static final String NULL_STRING = "";

    private static final String SPACE = " ";

    private final CommandExecutor commandExecutor;

    public GogoKernelShellCommand(CommandExecutor commandExecutor) {
        this.commandExecutor = commandExecutor;
    }

    /*
     * Bundle commands
     */

    @Descriptor("list bundle artifacts")
    public void bundle(@Descriptor("operation (list)") String op) {
        doBundle(op, NULL_STRING);
    }

    @Descriptor("examine or manage a bundle artifact")
    public void bundle(@Descriptor("operation (examine|diag|headers|start|stop|refresh|uninstall)") String op,
        @Descriptor("bundle symbolic name") String bsn, @Descriptor("bundle version") String bv) {
        doBundle(op, bsn + SPACE + bv);
    }

    @Descriptor("examine or manage a bundle artifact")
    public void bundle(@Descriptor("operation (examine|diag|headers|start|stop|refresh|uninstall)") String op,
        @Descriptor("  bundle id") long bundleId) {
        doBundle(op, String.valueOf(bundleId));
    }

    public void doBundle(String op, String argList) {
        doOp(BUNDLE_OP, op, argList);

    }

    /*
     * Configuration commands
     */

    @Descriptor("list configuration artifacts")
    public void config(@Descriptor("operation (list)") String op) {
        doConfig(op, NULL_STRING);
    }

    @Descriptor("examine or manage a configuration artifact")
    public void config(@Descriptor("operation (examine|start|stop|refresh|uninstall)") String op, @Descriptor("configuration name") String configName) {
        doConfig(op, configName);
    }

    @Descriptor("examine or manage a configuration artifact")
    public void config(@Descriptor("operation (examine|start|stop|refresh|uninstall)") String op,
        @Descriptor("configuration name") String configName, @Descriptor("configuration version") String configVersion) {
        doConfig(op, configName + SPACE + configVersion);
    }

    public void doConfig(String op, String argList) {
        doOp(CONFIG_OP, op, argList);

    }

    /*
     * Package commands - renamed to "packages" to avoid clash with Java keyword
     */

    @Descriptor("list exported packages")
    public void packages(@Descriptor("operation (list)") String op) {
        doPackage(op, NULL_STRING);
    }

    @Descriptor("examine an exported package")
    public void packages(@Descriptor("operation (examine)") String op, @Descriptor("package name") String packageName,
        @Descriptor("package version") String packageVersion) {
        doPackage(op, packageName + SPACE + packageVersion);
    }

    public void doPackage(String op, String argList) {
        doOp(PACKAGE_OP, op, argList);

    }

    /*
     * Par commands
     */

    @Descriptor("list PAR artifacts")
    public void par(@Descriptor("operation (list)") String op) {
        doPar(op, NULL_STRING);
    }

    @Descriptor("examine or manage a PAR artifact")
    public void par(@Descriptor("operation (examine|start|stop|refresh|uninstall)") String op, @Descriptor("PAR name") String parName,
        @Descriptor("PAR version") String parVersion) {
        doPar(op, parName + SPACE + parVersion);
    }

    public void doPar(String op, String argList) {
        doOp(PAR_OP, op, argList);

    }

    /*
     * Plan commands
     */

    @Descriptor("list plan artifacts")
    public void plan(@Descriptor("operation (list)") String op) {
        doPlan(op, NULL_STRING);
    }

    @Descriptor("examine or manage a plan artifact")
    public void plan(@Descriptor("operation (examine|start|stop|refresh|uninstall)") String op, @Descriptor("plan name") String planName,
        @Descriptor("plan version") String planVersion) {
        doPlan(op, planName + SPACE + planVersion);
    }

    public void doPlan(String op, String argList) {
        doOp(PLAN_OP, op, argList);

    }

    /*
     * Service commands
     */

    @Descriptor("list all services in the service registry")
    public void service(@Descriptor("operation (list)") String op) {
        doService(op, NULL_STRING);
    }

    @Descriptor("examine a service in the service registry")
    public void service(@Descriptor("operation (examine)") String op, @Descriptor("  service id") long serviceId) {
        doService(op, String.valueOf(serviceId));
    }

    public void doService(String op, String argList) {
        doOp(SERVICE_OP, op, argList);

    }

    /*
     * Install command
     */

    @Descriptor("install (deploy) an artifact")
    public void install(@Descriptor("artifact URI") String configName) {
        doOp(INSTALL_OP, configName);
    }

    /*
     * Shutdown command
     */

    @Descriptor("shut down the kernel")
    public void shutdown() {
        doOp(SHUTDOWN_OP, "");
    }

    /*
     * Helper methods
     */

    private void doOp(String mainOp, String subOp, String argList) {
        String args = concatArgs(mainOp, subOp, argList);
        try {
            boolean continueCommands = this.commandExecutor.execute(args, new GogoLinePrinter());
            if (!continueCommands) {
                System.out.println("vsh: command '" + args + "' requested exit");
            }
        } catch (IOException e) {
            System.out.println("vsh: command '" + args + "' threw an exception...");
            e.printStackTrace(System.out);
        }
    }

    private void doOp(String op, String argList) {
        String args = concatArgs(op, argList);
        try {
            boolean continueCommands = this.commandExecutor.execute(args, new GogoLinePrinter());
            if (!continueCommands) {
                System.out.println("vsh: command '" + args + "' requested exit");
            }
        } catch (IOException e) {
            System.out.println("vsh: command '" + args + "' threw an exception...");
            e.printStackTrace(System.out);
        }
    }

    private String concatArgs(String cmd, String... args) {
        StringBuffer result = new StringBuffer(cmd).append(SPACE);
        for (String arg : args) {
            result.append(arg).append(SPACE);
        }
        return result.toString();
    }

}
