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

package org.eclipse.virgo.kernel.deployer.core.internal;


import org.eclipse.virgo.kernel.deployer.core.DeploymentException;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.internal.AbstractInstallArtifact;
import org.eclipse.virgo.util.common.Tree;
import org.eclipse.virgo.util.common.Tree.ExceptionThrowingTreeVisitor;

/**
 * Helper methods for manipulating {@link Tree Tree&lt;InstallArtifact&gt;} instances.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is thread safe.
 * 
 */
public class TreeUtils {

    /**
     * Adds the given child tree as a subtree of the given parent tree and fixes up the subtree artifacts to refer to
     * the copies.
     * 
     * @param parent to get new child
     * @param child to copy into parent
     * @throws DeploymentException not really
     */
    public static void addChild(Tree<InstallArtifact> parent, Tree<InstallArtifact> child) throws DeploymentException {
        Tree<InstallArtifact> childCopy = parent.addChild(child);
        setTreeReferences(childCopy);
    }

    /**
     * The given tree was deeply copied. This method sets the references in each install artifact to its
     * corresponding copied tree.
     */
    private static void setTreeReferences(Tree<InstallArtifact> tree) throws DeploymentException {
        tree.visit(new ExceptionThrowingTreeVisitor<InstallArtifact, DeploymentException>() {
            public boolean visit(Tree<InstallArtifact> tree) throws DeploymentException {
                InstallArtifact value = tree.getValue();
                if (value instanceof AbstractInstallArtifact) {
                    ((AbstractInstallArtifact)value).setTree(tree);
                }
                return true;
            }
        });
    }

}
