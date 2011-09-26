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

package org.eclipse.virgo.util.osgi.manifest.parse.standard;



import org.eclipse.virgo.util.osgi.manifest.parse.ParserLogger;



/**

 * Simple test logger for use in the other tests.

 * 


 */

public class TestLogger implements ParserLogger {



	public String[] errorReports() {

		return null;

	}



	public void messageProcessed(String eventCode, String message) {

	}



	public void outputErrorMsg(Exception re, String item) {

	}



	public void outputInfoMsg(Exception re, String item) {

	}



	public void outputWarnMsg(Exception re, String item) {

	}



	public void resetErrorReport() {

	}



}

