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

package org.eclipse.virgo.kernel.userregion.internal.quasi;

import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.ResolverError;
import org.eclipse.osgi.service.resolver.State;

/**
 * 
 */
public interface ResolutionFailureDetective {

    String generateFailureDescription(State state, BundleDescription bundleDescription, ResolverErrorsHolder resolverErrors);

    public static class ResolverErrorsHolder {

        private ResolverError[] resolverErrors = null;

        void setResolverErrors(ResolverError[] resolverErrors) {
            this.resolverErrors = resolverErrors;
        }

        public ResolverError[] getResolverErrors() {
            // Prevent findbugs complaining by returning a copy of this object's internal state.
            ResolverError[] copy = new ResolverError[this.resolverErrors.length];
            System.arraycopy(this.resolverErrors, 0, copy, 0, this.resolverErrors.length);
            return copy;
        }
    }
}
