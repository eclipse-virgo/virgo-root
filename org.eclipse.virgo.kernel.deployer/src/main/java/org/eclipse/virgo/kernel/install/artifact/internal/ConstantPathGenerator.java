/*******************************************************************************
 * Copyright (c) 2012 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.kernel.install.artifact.internal;

import org.eclipse.virgo.util.io.PathReference;

final class ConstantPathGenerator extends AbstractPathGenerator implements PathGenerator {

    private final PathReference basePathReference;

    private final PathReference previousPathReference;

    public ConstantPathGenerator(PathReference basePathReference) {
        super(basePathReference);
        this.basePathReference = basePathReference;
        this.previousPathReference = new PathReference(String.format("%s-past", this.basePathReference.getAbsolutePath()));
        
        this.basePathReference.getParent().createDirectory();
        this.basePathReference.delete(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void next() {
        if (this.basePathReference.exists()) {
            this.previousPathReference.delete(true);
            this.basePathReference.moveTo(this.previousPathReference);
        }
        super.next();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void previous() {
        super.previous();
        if (this.previousPathReference.exists()) {
            this.previousPathReference.moveTo(this.basePathReference);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PathReference getCurrentPath() {
        return this.basePathReference;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PathReference getPreviousPath() {
        return this.previousPathReference;
    }

}
