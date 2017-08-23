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

package org.eclipse.virgo.medic.log;

import org.aspectj.lang.JoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An aspect that will advise any method with entry and exit trace logging.
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe
 * 
 */
public aspect EntryExitTrace pertypewithin(*) {
	
	private volatile Logger logger;

    pointcut medic() : within(org.eclipse.virgo.medic..*);
    
    pointcut logback() : within(ch.qos.logback..*) || within(org.slf4j.impl..*);
    
    pointcut debugCandidate() : execution(public * *(..)) && !medic() && !logback();
    
    pointcut traceCandidate() : execution(!public * *(..)) && !medic() && !logback();
    
    before() : debugCandidate() {
        getLogger(thisJoinPointStaticPart).debug("{} {}", ">", getSignature(thisJoinPointStaticPart));
    }

    after() returning : debugCandidate()  {
        getLogger(thisJoinPointStaticPart).debug("{} {}", "<", getSignature(thisJoinPointStaticPart));
    }

    after() throwing(Throwable t) : debugCandidate()  {
        Logger logger = getLogger(thisJoinPointStaticPart);
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("< %s", getSignature(thisJoinPointStaticPart)), t);
        }
    }
    
    before() : traceCandidate() {
        getLogger(thisJoinPointStaticPart).trace("{} {}", ">", getSignature(thisJoinPointStaticPart));
    }

    after() returning : traceCandidate()  {
        getLogger(thisJoinPointStaticPart).trace("{} {}", "<", getSignature(thisJoinPointStaticPart));
    }

    after() throwing(Throwable t) : traceCandidate()  {
        Logger logger = getLogger(thisJoinPointStaticPart);
        if (logger.isTraceEnabled()) {
            logger.trace(String.format("< %s", getSignature(thisJoinPointStaticPart)), t);
        }
    }

    private Logger getLogger(JoinPoint.StaticPart sp) {
    	if (this.logger == null) {
    		this.logger = LoggerFactory.getLogger(sp.getSignature().getDeclaringType());
    	}
    	return this.logger;
    }

    private String getSignature(JoinPoint.StaticPart sp) {
        return sp.getSignature().toLongString();
    }
}
