/*******************************************************************************
 * Copyright (c) 2012 SAP AG
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   SAP AG - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.web.enterprise.openejb.initialiser;

import java.util.Hashtable;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import org.apache.openejb.core.ThreadContext;

public class RedirectingOpenEJBContext implements Context {

	@Override
	public Object addToEnvironment(String arg0, Object arg1) throws NamingException {
		return getThreadContext().addToEnvironment(arg0, arg1);
	}

	@Override
	public void bind(Name arg0, Object arg1) throws NamingException {
		getThreadContext().bind(arg0, arg1);
	}

	@Override
	public void bind(String arg0, Object arg1) throws NamingException {
		getThreadContext().bind(arg0, arg1);
	}

	@Override
	public void close() throws NamingException {
		getThreadContext().close();
	}

	@Override
	public Name composeName(Name arg0, Name arg1) throws NamingException {
		return getThreadContext().composeName(arg0, arg1);
	}

	@Override
	public String composeName(String arg0, String arg1) throws NamingException {
		return getThreadContext().composeName(arg0, arg1);
	}

	@Override
	public Context createSubcontext(Name arg0) throws NamingException {
		return getThreadContext().createSubcontext(arg0);
	}

	@Override
	public Context createSubcontext(String arg0) throws NamingException {
		return getThreadContext().createSubcontext(arg0);
	}

	@Override
	public void destroySubcontext(Name arg0) throws NamingException {
		getThreadContext().destroySubcontext(arg0);
	}

	@Override
	public void destroySubcontext(String arg0) throws NamingException {
		getThreadContext().destroySubcontext(arg0);
	}

	@Override
	public Hashtable<?, ?> getEnvironment() throws NamingException {
		return getThreadContext().getEnvironment();
	}

	@Override
	public String getNameInNamespace() throws NamingException {
		return getThreadContext().getNameInNamespace();
	}

	@Override
	public NameParser getNameParser(Name arg0) throws NamingException {
		return getThreadContext().getNameParser(arg0);
	}

	@Override
	public NameParser getNameParser(String arg0) throws NamingException {
		return getThreadContext().getNameParser(arg0);
	}

	@Override
	public NamingEnumeration<NameClassPair> list(Name arg0) throws NamingException {
		return getThreadContext().list(arg0);
	}

	@Override
	public NamingEnumeration<NameClassPair> list(String arg0) throws NamingException {
		return getThreadContext().list(arg0);
	}

	@Override
	public NamingEnumeration<Binding> listBindings(Name arg0) throws NamingException {
		return getThreadContext().listBindings(arg0);
	}

	@Override
	public NamingEnumeration<Binding> listBindings(String arg0) throws NamingException {
		return getThreadContext().listBindings(arg0);
	}

	@Override
	public Object lookup(Name arg0) throws NamingException {
		return getThreadContext().lookup(arg0);
	}

	@Override
	public Object lookup(String arg0) throws NamingException {
		return getThreadContext().lookup(arg0);
	}

	@Override
	public Object lookupLink(Name arg0) throws NamingException {
		return getThreadContext().lookupLink(arg0);
	}

	@Override
	public Object lookupLink(String arg0) throws NamingException {
		return getThreadContext().lookupLink(arg0);
	}

	@Override
	public void rebind(Name arg0, Object arg1) throws NamingException {
		getThreadContext().rebind(arg0, arg1);
	}

	@Override
	public void rebind(String arg0, Object arg1) throws NamingException {
		getThreadContext().rebind(arg0, arg1);
	}

	@Override
	public Object removeFromEnvironment(String arg0) throws NamingException {
		return getThreadContext().removeFromEnvironment(arg0);
	}

	@Override
	public void rename(Name arg0, Name arg1) throws NamingException {
		getThreadContext().rename(arg0, arg1);
	}

	@Override
	public void rename(String arg0, String arg1) throws NamingException {
		getThreadContext().rename(arg0, arg1);
	}

	@Override
	public void unbind(Name arg0) throws NamingException {
		getThreadContext().unbind(arg0);
	}

	@Override
	public void unbind(String arg0) throws NamingException {
		getThreadContext().unbind(arg0);
	}
	
    private Context getThreadContext() throws NamingException {
        ThreadContext threadContext = ThreadContext.getThreadContext();
        Context context = threadContext.getBeanContext().getJndiContext();
        return context;
    }
}
