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



import java.io.PrintStream;

import java.util.Collections;

import java.util.List;

import org.eclipse.virgo.util.osgi.manifest.parse.HeaderDeclaration;






/**

 * Simple debug visitor that can be plugged into the parser to observe the

 * visiting order. Plugging it in is as easy as using the StandardHeaderParser

 * constructor that takes a visitor:

 * <code>new StandardHeaderParser(new DebugVisitor())</code>. By default the

 * logged output will go to System.out - but a difference PrintStream can be

 * specified through the DebugVisitor constructor.

 * 

 * <strong>Concurrent Semantics</strong><br />

 * 

 * Threadsafe.

 * 


 */

public class DebugVisitor implements HeaderVisitor {



	private PrintStream printStream;



	public DebugVisitor() {

		printStream = System.out;

	}



	public DebugVisitor(PrintStream ps) {

		this.printStream = ps;

	}



	public void visitAttribute(String name, String value) {

		printStream.println("visitAttribute(" + name + "=" + value + ")");

	}



	public void visitDirective(String name, String value) {

		printStream.println("visitDirective(" + name + ":=" + value + ")");

	}



	public void visitSymbolicName(String symbolicName) {

		printStream.println("visitSymbolicName(" + symbolicName + ")");

	}



	public void visitUniqueName(String uniqueName) {

		printStream.println("visitUniqueName(" + uniqueName + ")");

	}



	public void visitWildcardName(String name) {

		printStream.println("visitWildcardName(" + name + ")");

	}



	public void endvisit() {

		printStream.println("endVisit()");

	}



	public void clauseEnded() {

		printStream.println("clauseEnded()");

	}



	public List<HeaderDeclaration> getHeaderDeclarations() {

		printStream.println("getHeaderDeclarations()");

		return Collections.emptyList();

	}



	public HeaderDeclaration getFirstHeaderDeclaration() {

		printStream.println("getFirstHeaderDeclaration()");

		return null;

	}

	

	public void initialize() {

		

	}

}

