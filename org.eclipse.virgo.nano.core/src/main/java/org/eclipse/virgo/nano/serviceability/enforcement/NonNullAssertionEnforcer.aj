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

package org.eclipse.virgo.nano.serviceability.enforcement;

import org.aspectj.lang.annotation.SuppressAjWarnings;
import org.eclipse.virgo.nano.serviceability.Assert;
import org.eclipse.virgo.nano.serviceability.NonNull;


/**
 * Aspect that enforces that parameters annotated with {@link NonNull} are, in fact, non null.
 * <p/>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe.
 * 
 */
public final aspect NonNullAssertionEnforcer {

    pointcut serviceability() : within(org.eclipse.virgo.nano.serviceability..*);

    pointcut executingOperationWithNonNullFirstParameter(Object obj) :
    	!serviceability() && 
        (execution(* *(@NonNull (*), ..)) || preinitialization(*.new(@NonNull (*), ..))) && args(obj, ..) ;

    pointcut executingOperationWithNonNullSecondParameter(Object obj) :
        !serviceability() && 
        (execution(* *(*, @NonNull (*), ..)) || preinitialization(*.new(*, @NonNull (*), ..))) && args(*, obj, ..);

    pointcut executingOperationWithNonNullThirdParameter(Object obj) :
        !serviceability() && 
        (execution(* *(*, *, @NonNull (*), ..)) || preinitialization(*.new(*, *, @NonNull (*), ..))) && args(*, * , obj, ..);

    pointcut executingOperationWithNonNullFourthParameter(Object obj) :
        !serviceability() && 
        (execution(* *(*, *, *, @NonNull (*), ..)) || preinitialization(*.new(*, *, *, @NonNull (*), ..))) && args(*, *, *, obj, ..);

    pointcut executingOperationWithNonNullFifthParameter(Object obj) :
        !serviceability() && 
        (execution(* *(*, *, *, *, @NonNull (*), ..)) || preinitialization(*.new(*, *, *, *, @NonNull (*), ..))) && args(*, *, *, *, obj, ..);

    pointcut executingOperationWithNonNullSixthParameter(Object obj) :
        !serviceability() && 
        (execution(* *(*, *, *, *, *, @NonNull (*), ..)) || preinitialization(*.new(*, *, *, *, *, @NonNull (*), ..))) && args(*, *, *, *, *, obj, ..);

    before(Object argValue) : executingOperationWithNonNullFirstParameter(argValue) {
        Assert.notNull(argValue, "Argument [1] cannot be null");
    }

    before(Object argValue) : executingOperationWithNonNullSecondParameter(argValue) {
        Assert.notNull(argValue, "Argument [2] cannot be null");
    }

    @SuppressAjWarnings({"adviceDidNotMatch"})
    before(Object argValue) : executingOperationWithNonNullThirdParameter(argValue) {
        Assert.notNull(argValue, "Argument [3] cannot be null");
    }

    @SuppressAjWarnings({"adviceDidNotMatch"})
    before(Object argValue) : executingOperationWithNonNullFourthParameter(argValue) {
        Assert.notNull(argValue, "Argument [4] cannot be null");
    }

    @SuppressAjWarnings({"adviceDidNotMatch"})
    before(Object argValue) : executingOperationWithNonNullFifthParameter(argValue) {
        Assert.notNull(argValue, "Argument [5] cannot be null");
    }

    @SuppressAjWarnings({"adviceDidNotMatch"})
    before(Object argValue) : executingOperationWithNonNullSixthParameter(argValue) {
        Assert.notNull(argValue, "Argument [6] cannot be null");
    }
}
