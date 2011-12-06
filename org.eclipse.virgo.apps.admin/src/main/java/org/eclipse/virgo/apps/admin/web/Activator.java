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
package org.eclipse.virgo.apps.admin.web;

import java.lang.management.ManagementFactory;
import java.util.Dictionary;
import java.util.Hashtable;

import javax.management.MBeanServer;

import org.eclipse.virgo.apps.admin.web.internal.AdminHttpContext;
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

//import org.jolokia.osgi.servlet.JolokiaServlet;

/**
 *
 *	This class is ThreadSafe
 *
 */
public class Activator implements BundleActivator {

	private static final Logger log = LoggerFactory.getLogger(Activator.class);

	private static final String ORG_ECLIPSE_VIRGO_KERNEL_HOME = "org.eclipse.virgo.kernel.home";
	
	protected static final String APPLICATION_NAME = "Virgo Admin Console";
	
	private static final String contentContextPath = "/content";
	
	private static final String resourcesContextPath = "/resources";
	
	private static final String uploadContextPath = "/upload";
	
	//private static final String contextPathJolokia = "/jolokia";

	protected static String contextPath = null;
	
	private ServiceTracker<HttpService, HttpService> httpServiceTracker;
	
	private ServiceTracker<URLStreamHandlerService, URLStreamHandlerService> urlEncoderServiceTracker;
	
	private transient HttpService registeredHttpService = null;
	
	private transient boolean isRegister = false;
	
	private final Object lock = new Object();

	private BundleContext context;

	@Override
	public void start(BundleContext context) throws Exception {
		this.context = context;
		
		MBeanServer platformMBeanServer = ManagementFactory.getPlatformMBeanServer();
		this.context.registerService(MBeanServer.class, platformMBeanServer, null);
		
		Activator.contextPath = this.context.getBundle().getHeaders().get("Web-ContextPath");
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
					Dictionary<String, String> initParams = new Hashtable<String, String>();
					initParams.put(ContentServlet.CONTENT_SERVLET_PREFIX, "/WEB-INF/layouts");
					initParams.put(ContentServlet.CONTENT_SERVLET_SUFFIX, ".html");
					AdminHttpContext adminHttpContext = new AdminHttpContext(this.context.getBundle());
					ContentServlet contentServlet = new ContentServlet();
					this.registeredHttpService.registerServlet(Activator.contextPath, new IndexServlet(contentServlet), null, adminHttpContext);
					this.registeredHttpService.registerServlet(Activator.contextPath + Activator.contentContextPath, contentServlet, initParams, adminHttpContext);
					this.registeredHttpService.registerServlet(Activator.contextPath + Activator.resourcesContextPath, new ResourceServlet(), null, adminHttpContext);
					this.registeredHttpService.registerServlet(Activator.contextPath + Activator.uploadContextPath, new UploadServlet(this.context.getProperty(ORG_ECLIPSE_VIRGO_KERNEL_HOME)), null, adminHttpContext);
//					this.registeredHttpService.registerServlet(this.contextPathJolokia, new JolokiaServlet(), null, null);
					this.isRegister = true;
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
				this.doSafeUnregister(Activator.contextPath + Activator.contentContextPath);
				this.doSafeUnregister(Activator.contextPath + Activator.resourcesContextPath);
				this.doSafeUnregister(Activator.contextPath + Activator.uploadContextPath);
				this.doSafeUnregister(Activator.contextPath);
				//this.registeredHttpService.unregister(this.contextPathJolokia);
			}
			this.isRegister = false;
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
			if(urlEncoderServiceTracker.isEmpty() && !isRegister){
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
			if(urlEncoderServiceTracker.isEmpty() && !isRegister && registeredHttpService != null){
				registerWithHttpService();
			}
		}
		
	}
	
}
