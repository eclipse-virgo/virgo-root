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

 * HeaderDeclarations are constructed through a visitor pattern. As the parser

 * processes data it calls methods on the visitor interface. The standard

 * implementation is {@link StandardHeaderVisitor} but other visitors can be

 * plugged in. For example, the {@link DebugVisitor} produces diagnostics about

 * the visiting process. A validation visitor could be plugged in to verify

 * package names or attribute names and values.

 * 

 * <strong>Concurrent Semantics</strong><br />

 * 

 * Threadsafe.

 * 


 */

public interface HeaderVisitor {



	/**

	 * Visit a new directive (parsed from input of the form name:=value)
	 * @param name 
	 * @param value 

	 */

	void visitDirective(String name, String value);



	/**

	 * Visit a new attribute (parsed from input of the form name=value)
	 * @param name 
	 * @param value 

	 */

	void visitAttribute(String name, String value);



	/**

	 * @return a list of all HeaderDeclarations if this visitor has been collecting the information. Empty list if nothing has been

	 *         collected

	 */

	List<HeaderDeclaration> getHeaderDeclarations();



	/**

	 * @return the first (and probably only) HeaderDeclaration, otherwise null

	 */

	HeaderDeclaration getFirstHeaderDeclaration();



	/**

	 * Called when a clause ends. Clauses are comma separated: header=clause (',' clause)*. It is a sign that a new clause is about

	 * to start.

	 */

	void clauseEnded();



	/**

	 * Called to visit a symbolic name. These are of the form: "symbolic-name :: = token('.'token)*". So basically a sequence of dot

	 * separated tokens.
	 * @param name 

	 */

	void visitSymbolicName(String name);



	/**

	 * Called to visit a unique name. These are of the form: "unique-name :: = identifier('.'identifier)*". So basically a sequence

	 * of dot separated identifiers..
	 * @param name 

	 */

	void visitUniqueName(String name);



	/**

	 * Called to visit a wildcard name. These are similar to a unique name but wildcards are permitted. These are valid wildcarded

	 * names: '*' or 'com.foo.*' or 'com.foo.goo'.
	 * @param name 

	 */

	void visitWildcardName(String name);



	/**

	 * Called when the end of the input has been reached.

	 */

	void endvisit();



	/**

	 * Called ahead of parsing a new header

	 */

	void initialize();



}

