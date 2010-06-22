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

    pointcut util() : within(org.eclipse.virgo.util..*);
    
    pointcut repository() : within(org.eclipse.virgo.repository..*);
    
    pointcut logback() : within(ch.qos.logback..*) || within(org.slf4j.impl..*);
    
    pointcut setter() : execution(* set*(..));
    
    pointcut getter() : execution(* get*(..));
    
    pointcut infoCandidate() : execution(public * *(..)) && !setter() && !getter() && !medic() && !util() && !repository() && !logback();
    
    pointcut debugCandidate() : execution(!public !private * *(..)) && !setter() && !getter() && !medic() && !util() && !repository() && !logback();
    
    pointcut traceCandidate() : execution(private * *(..)) && !setter() && !getter() && !medic() && !util() && !repository() && !logback();
    
    before() : infoCandidate() {
        getLogger(thisJoinPointStaticPart).info("{} {}", ">", getSignature(thisJoinPointStaticPart));
    }

    after() returning : infoCandidate()  {
        getLogger(thisJoinPointStaticPart).info("{} {}", "<", getSignature(thisJoinPointStaticPart));
    }

    after() throwing(Throwable t) : infoCandidate()  {
        Logger logger = getLogger(thisJoinPointStaticPart);
        if (logger.isInfoEnabled()) {
            logger.info(String.format("< %s", getSignature(thisJoinPointStaticPart)), t);
        }
    }

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
