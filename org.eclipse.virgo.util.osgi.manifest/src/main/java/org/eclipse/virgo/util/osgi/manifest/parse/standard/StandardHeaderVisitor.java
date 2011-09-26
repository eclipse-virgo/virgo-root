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



import java.util.ArrayList;

import java.util.HashMap;

import java.util.List;

import java.util.Map;

import org.eclipse.virgo.util.osgi.manifest.parse.HeaderDeclaration;






/**

 * Standard implementation of the HeaderVisitor. This implementations collects

 * up the names, attributes and directives visited and for each clause creates a

 * HeaderDeclaration. These are then available at the end of the parse through

 * getHeaderDeclaration() and getFirstHeaderDeclaration()

 * 

 * <strong>Concurrent Semantics</strong><br />

 * 

 * Threadsafe.

 * 


 */

public class StandardHeaderVisitor implements HeaderVisitor {



	// The complete list of HeaderDeclarations discovered during the parse

	private List<HeaderDeclaration> headerDeclarations;



	// Collections populated as names/directives/attributes are visited

	private List<String> names;

	private Map<String, String> directives;

	private Map<String, String> attributes;



	public void visitAttribute(String name, String value) {

		attributes.put(name, value);

	}



	public void visitDirective(String name, String value) {

		directives.put(name, value);

	}



	public void visitSymbolicName(String name) {

		names.add(name);

	}



	public void visitUniqueName(String name) {

		names.add(name);

	}



	public void visitWildcardName(String name) {

		names.add(name);

	}



	public void endvisit() {

	}



	/**

	 * Record a new HeaderDeclaration and reset the collections accumulating names, attributes and directives.

	 */

	public void clauseEnded() {

		headerDeclarations.add(new StandardHeaderDeclaration(names, attributes, directives));
		

		names.clear();

		directives.clear();

		attributes.clear();

	}



	/**

	 * @return the first header declaration or null if there are none.

	 */

	public HeaderDeclaration getFirstHeaderDeclaration() {

		if (headerDeclarations.size() == 0) {

			return null;

		} else {

			return headerDeclarations.get(0);

		}

	}



	public List<HeaderDeclaration> getHeaderDeclarations() {

		return headerDeclarations;

	}

	

	/**

	 * Called ahead of parsing a header - allowing state to be reset in a reused visitor

	 */

	public void initialize() {

		headerDeclarations = new ArrayList<HeaderDeclaration>();

		names = new ArrayList<String>();

		directives = new HashMap<String, String>();

		attributes = new HashMap<String, String>();

	}

}

