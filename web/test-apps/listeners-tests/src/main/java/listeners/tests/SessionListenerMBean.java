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

package listeners.tests;

import java.util.Map;

public interface SessionListenerMBean {

	int getSessionCount();
	
	String getSessionId();
	
	Map<String, Object> getAddedSessionAttribute();
	
	Map<String,Object> getRemovedSessionAttribute();
	
	Map<String,Object> getReplacedSessionAttribute();

	void awaitNextDecrement() throws InterruptedException;
	
	void invalidate();
}
