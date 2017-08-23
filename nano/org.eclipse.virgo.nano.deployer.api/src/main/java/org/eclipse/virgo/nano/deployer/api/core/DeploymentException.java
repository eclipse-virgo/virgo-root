/*******************************************************************************
 * Copyright (c) 2008, 2012 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *   SAP AG - re-factoring
 *******************************************************************************/

package org.eclipse.virgo.nano.deployer.api.core;

/**
 * Signals a checked exception to the caller of the deployer subsystem.
 * <p/>
 * 
 * <strong>Concurrent Semantics</strong><br/>
 * 
 * This class is thread safe.
 * 
 */
public class DeploymentException extends Exception {

    private static final long serialVersionUID = -6809659761040724153L;

    private final boolean diagnosed;

    /**
     * Creates a new <code>DeploymentException</code> with the supplied error message that has not already been
     * diagnosed. Equivalent to calling {@link #DeploymentException(String, boolean) DeploymentException(message,
     * false)}.
     * 
     * @param message The exception's message
     */
    public DeploymentException(String message) {
        this(message, false);

    }

    /**
     * Creates a new <code>DeploymentException</code> with the supplied error message. <code>diagnosed</code> can be
     * used to indicate whether or not this problem has already been diagnosed. If <code>true</code> the caller
     * <strong>must</strong> ensure that the user has already been given sufficient information to diagnose the problem.
     * 
     * @param message The exception's message
     * @param diagnosed <code>true</code> if already diagnosed, otherwise <code>false</code>.
     */
    public DeploymentException(String message, boolean diagnosed) {
        super(message);
        this.diagnosed = diagnosed;
    }

    /**
     * Creates a new <code>DeploymentException</code>, with the supplied error message and cause, that has not already
     * been diagnosed. Equivalent to calling {@link #DeploymentException(String, Throwable, boolean)
     * DeploymentException(message, cause, false)}.
     * 
     * @param message The exception's message.
     * @param cause The exception's cause.
     */
    public DeploymentException(String message, Throwable cause) {
        this(message, cause, false);
    }

    /**
     * Creates a new <code>DeploymentException</code> with the supplied error message and cause. <code>diagnosed</code>
     * can be used to indicate whether or not this problem has already been diagnosed. If <code>true</code> the caller
     * <strong>must</strong> ensure that the user has already been given sufficient information to diagnose the problem.
     * 
     * @param message The exception's message.
     * @param cause The exception's cause.
     * @param diagnosed <code>true</code> if already diagnosed, otherwise <code>false</code>.
     */
    public DeploymentException(String message, Throwable cause, boolean diagnosed) {
        super(message, cause);
        this.diagnosed = diagnosed;
    }

    /**
     * Returns <code>true</code> if this exception has already been diagnosed and no further information relating to
     * this exception needs to be conveyed to the user.
     * 
     * @return <code>true</code> if already diagnosed, otherwise <code>false</code>.
     */
    public boolean isDiagnosed() {
        return diagnosed;
    }
}
