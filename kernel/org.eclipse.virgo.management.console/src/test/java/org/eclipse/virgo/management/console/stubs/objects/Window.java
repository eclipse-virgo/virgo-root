/*******************************************************************************
 * Copyright (c) 2008, 2011 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *******************************************************************************/
package org.eclipse.virgo.management.console.stubs.objects;

import org.mozilla.javascript.Function;

/**
 * 
 *
 */
public class Window {

	public Window() {

	}
	
	public void addEvent(String name, Function function){
		
	}

	public void log(String msg){
		System.out.println(msg);
	}
	
}
