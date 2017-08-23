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



import java.util.List;

import org.eclipse.virgo.util.osgi.manifest.parse.HeaderDeclaration;






/**

 * A basic visitor that delegates to some array of other visitors. This visitor

 * is used when multiple are plugged into the parser - to ensure the standard

 * visitor still runs, in addition to the user specified visitors.

 * 

 * <strong>Concurrent Semantics</strong><br />

 * 

 * Threadsafe.

 * 


 */

public class MultiplexingVisitor implements HeaderVisitor {



	private static final HeaderVisitor[] NONE = new HeaderVisitor[] {};

	private HeaderVisitor[] visitors = NONE;



	public MultiplexingVisitor(HeaderVisitor... visitors) {

		this.visitors = visitors;

	}



	public void endvisit() {

		for (int i = 0; i < visitors.length; i++) {

			visitors[i].endvisit();

		}

	}



	public List<HeaderDeclaration> getHeaderDeclarations() {

		return visitors[0].getHeaderDeclarations();

	}



	public void visitAttribute(String name, String value) {

		for (int i = 0; i < visitors.length; i++) {

			visitors[i].visitAttribute(name, value);

		}

	}



	public void visitDirective(String name, String value) {

		for (int i = 0; i < visitors.length; i++) {

			visitors[i].visitDirective(name, value);

		}

	}



	public void visitSymbolicName(String name) {

		for (int i = 0; i < visitors.length; i++) {

			visitors[i].visitSymbolicName(name);

		}

	}



	public void visitWildcardName(String name) {

		for (int i = 0; i < visitors.length; i++) {

			visitors[i].visitWildcardName(name);

		}

	}



	public void visitUniqueName(String name) {

		for (int i = 0; i < visitors.length; i++) {

			visitors[i].visitUniqueName(name);

		}

	}



	public void clauseEnded() {

		for (int i = 0; i < visitors.length; i++) {

			visitors[i].clauseEnded();

		}

	}



	public HeaderDeclaration getFirstHeaderDeclaration() {

		return null;

	}

	

	public void initialize() {

		for (int i = 0; i < visitors.length; i++) {

			visitors[i].initialize();

		}

	}



}

