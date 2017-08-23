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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.naming.NamingException;

import org.apache.naming.ContextBindings;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.core.ThreadContextListener;

public class VirgoThreadContextListener implements ThreadContextListener {

	private static final String REDIRECTING_OPENEJB_CONTEXT = "RedirectingOpenEJBContext";
	protected Method method;

	public VirgoThreadContextListener() {
		ContextBindings.bindContext(REDIRECTING_OPENEJB_CONTEXT, new RedirectingOpenEJBContext());
		try {
			//getThreadName is package protected
			method = ContextBindings.class.getDeclaredMethod("getThreadName");
			method.setAccessible(true);
		} catch (NoSuchMethodException e) {
			System.err.println("Expected ContextBinding to have the method getThreadName()");
		}
	}

	public void contextEntered(ThreadContext oldContext, ThreadContext newContext) {
		// save the old context if possible
		try {
			OldContextHolder data = new OldContextHolder(getThreadName());
			newContext.set(OldContextHolder.class, data);
		} catch (NamingException ignored) {
		}
		// set the new context
		try {
			ContextBindings.bindThread(REDIRECTING_OPENEJB_CONTEXT, null);
		} catch (NamingException e) {
			ContextBindings.unbindContext(REDIRECTING_OPENEJB_CONTEXT, null);
			throw new IllegalArgumentException("Unable to bind OpenEJB enc");
		}
	}

	public void contextExited(ThreadContext exitedContext, ThreadContext reenteredContext) {
		// unbind the new context
		ContextBindings.unbindThread(REDIRECTING_OPENEJB_CONTEXT, null);

		// attempt to restore the old context
		OldContextHolder data = exitedContext.get(OldContextHolder.class);
		if (data != null && data.oldContextName != null) {
			try {
				ContextBindings.bindThread(data.oldContextName, null);
			} catch (NamingException e) {
				System.err.println("Exception in method contextExited");
				e.printStackTrace();
			}
		}
	}

	private Object getThreadName() throws NamingException {
		try {
			Object threadName = method.invoke(null);
			return threadName;

		} catch (InvocationTargetException e) {
			// if it's a naming exception, it should be treated by the caller
			if (e.getCause() != null && e.getCause() instanceof NamingException) {
				throw (NamingException) e.getCause();
			}

			System.err.println("Exception in method getThreadName");
			e.printStackTrace();
			return null;

		} catch (Exception e) {
			System.err.println("Exception in method getThreadName");
			e.printStackTrace();
			return null;
		}
	}

	// Internal stuff to hold old context name
	private static class OldContextHolder {
		private Object oldContextName;

		public OldContextHolder(Object oldContextName) {
			this.oldContextName = oldContextName;
		}
	}
}
