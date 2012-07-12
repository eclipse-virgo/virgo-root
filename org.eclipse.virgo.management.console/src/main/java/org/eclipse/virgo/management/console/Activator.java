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
package org.eclipse.virgo.management.console;

import java.util.Dictionary;
import java.util.Hashtable;

import org.eclipse.virgo.management.console.internal.AdminHttpContext;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.service.url.URLStreamHandlerService;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jolokia.osgi.servlet.JolokiaServlet;

/**
 *
 *	This class is ThreadSafe
 *
 */
public class Activator implements BundleActivator {

	private static final Logger log = LoggerFactory.getLogger(Activator.class);
	
	protected static final String APPLICATION_NAME = "Virgo Admin Console";
	
	private static final String CONTENT_CONTEXT_PATH = "/content";
	
	private static final String RESOURCES_CONTEXT_PATH = "/resources";
	
	private static final String UPLOAD_CONTEXT_PATH = "/upload";
	
	private static final String JOLOKIA_CONTEXT_PATH = "/jolokia";

	protected static String contextPath = null;
	
	private ServiceTracker<HttpService, HttpService> httpServiceTracker;
	
	private ServiceTracker<URLStreamHandlerService, URLStreamHandlerService> urlEncoderServiceTracker;
	
	private transient HttpService registeredHttpService = null;
	
	private transient boolean isRegisteredWithHttpService = false;
	
	private final Object lock = new Object();

	private BundleContext bundleContext;

	@Override
	public void start(BundleContext context) throws Exception {
		this.bundleContext = context;
		
		Activator.contextPath = this.bundleContext.getBundle().getHeaders().get("Web-ContextPath");
		this.httpServiceTracker = new ServiceTracker<HttpService, HttpService>(context, HttpService.class, new HttpServiceTrackerCustomizer(context));
		
		Filter createFilter = context.createFilter("(&(" + Constants.OBJECTCLASS + "=" + URLStreamHandlerService.class.getSimpleName() + ")(url.handler.protocol=webbundle))");
		this.urlEncoderServiceTracker = new ServiceTracker<URLStreamHandlerService, URLStreamHandlerService>(context, createFilter, new UrlEncoderServiceTrackerCustomizer(context));

		this.httpServiceTracker.open();
		this.urlEncoderServiceTracker.open();
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		this.httpServiceTracker.close();
		this.urlEncoderServiceTracker.close();
	}
	
	private void registerWithHttpService(){
		synchronized (this.lock) {
			if(this.registeredHttpService != null){
				try {
					AdminHttpContext adminHttpContext = new AdminHttpContext(this.bundleContext.getBundle());
					Dictionary<String, String> contentServletInitParams = new Hashtable<String, String>();
					contentServletInitParams.put(ContentServlet.CONTENT_SERVLET_PREFIX, "/WEB-INF/layouts");
					contentServletInitParams.put(ContentServlet.CONTENT_SERVLET_SUFFIX, ".html");
					ContentServlet contentServlet = new ContentServlet();
					this.registeredHttpService.registerServlet(Activator.contextPath, 							new IndexServlet(contentServlet), 		null,	adminHttpContext);
					this.registeredHttpService.registerServlet(Activator.contextPath + CONTENT_CONTEXT_PATH, 	contentServlet, contentServletInitParams,		adminHttpContext);
					this.registeredHttpService.registerServlet(Activator.contextPath + RESOURCES_CONTEXT_PATH, 	new ResourceServlet(), 					null,	adminHttpContext);
					this.registeredHttpService.registerServlet(Activator.contextPath + UPLOAD_CONTEXT_PATH, 	new UploadServlet(this.bundleContext),	null,	adminHttpContext);
					this.registeredHttpService.registerServlet(Activator.contextPath + JOLOKIA_CONTEXT_PATH, 	new JolokiaServlet(this.bundleContext),	null, 	null);
					this.isRegisteredWithHttpService = true;
					log.info("Admin console registered to HttpService: " + Activator.contextPath);
				} catch (Exception e) {
					log.error("Failed to register AdminConsole with HttpService", e);
					this.unRegisterWithHttpService();
				} 
			}
		}
	}
	
	private void unRegisterWithHttpService(){
		synchronized (this.lock) {
			if(this.registeredHttpService != null){
				this.doSafeUnregister(Activator.contextPath + Activator.CONTENT_CONTEXT_PATH);
				this.doSafeUnregister(Activator.contextPath + Activator.RESOURCES_CONTEXT_PATH);
				this.doSafeUnregister(Activator.contextPath + Activator.UPLOAD_CONTEXT_PATH);
				this.doSafeUnregister(Activator.contextPath + Activator.JOLOKIA_CONTEXT_PATH);
				this.doSafeUnregister(Activator.contextPath);
			}
			this.isRegisteredWithHttpService = false;
			log.info("Admin console unregistering from HttpService at " + Activator.contextPath);
		}
	}
	
	private void doSafeUnregister(String path){
		try{
			this.registeredHttpService.unregister(path);
		}catch(IllegalArgumentException e){
			log.warn("Failed to unregister '" + path + "' from HttpService");
		}
	}
	
	/**
	 * Tracker event handler for HttpService
	 */
	private class HttpServiceTrackerCustomizer implements ServiceTrackerCustomizer<HttpService, HttpService> {

		private final BundleContext context;

		public HttpServiceTrackerCustomizer(BundleContext context) {
			this.context = context;
		}

		@Override
		public HttpService addingService(ServiceReference<HttpService> reference) {
			HttpService service = this.context.getService(reference);
			if(urlEncoderServiceTracker.isEmpty() && !isRegisteredWithHttpService){
				registeredHttpService = service;
				registerWithHttpService();
			}
			return service;
		}

		@Override
		public void modifiedService(ServiceReference<HttpService> reference, HttpService service) {
			// no-op
		}

		@Override
		public void removedService(ServiceReference<HttpService> reference,	HttpService service) {
			if(registeredHttpService != null && service.equals(registeredHttpService)){
				unRegisterWithHttpService();
				registeredHttpService = null;
			}
		}
		
	}

	/**
	 * Tracker event handler for URLStreamHandlerService
	 */
	private class UrlEncoderServiceTrackerCustomizer implements ServiceTrackerCustomizer<URLStreamHandlerService, URLStreamHandlerService>{

		private final BundleContext context;

		public UrlEncoderServiceTrackerCustomizer(BundleContext context) {
			this.context = context;
		}

		@Override
		public URLStreamHandlerService addingService(ServiceReference<URLStreamHandlerService> reference) {
			if(registeredHttpService != null){
				unRegisterWithHttpService();
			}
			return this.context.getService(reference);
		}

		@Override
		public void modifiedService(ServiceReference<URLStreamHandlerService> reference, URLStreamHandlerService service) {
			// no-op
		}

		@Override
		public void removedService(ServiceReference<URLStreamHandlerService> reference, URLStreamHandlerService service) {
			if(urlEncoderServiceTracker.isEmpty() && !isRegisteredWithHttpService && registeredHttpService != null){
				registerWithHttpService();
			}
		}
		
	}
	
}
