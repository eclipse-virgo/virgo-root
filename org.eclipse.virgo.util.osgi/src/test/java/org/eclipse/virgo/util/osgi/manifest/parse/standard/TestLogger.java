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



///CLOVER:OFF

/**

 * Simple test logger for use in the other tests.

 * 


 */

public class TestLogger implements ParserLogger {



	public String[] errorReports() {

		return null;

	}



	public void messageProcessed(@SuppressWarnings("unused") String eventCode, @SuppressWarnings("unused") String message) {

	}



	public void outputErrorMsg(Exception re, String item) {

	}



	public void outputInfoMsg(@SuppressWarnings("unused") Exception re, @SuppressWarnings("unused") String item) {

	}



	public void outputWarnMsg(@SuppressWarnings("unused") Exception re, @SuppressWarnings("unused") String item) {

	}



	public void resetErrorReport() {

	}



}

