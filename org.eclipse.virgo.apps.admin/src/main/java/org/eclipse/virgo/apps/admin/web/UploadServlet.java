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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.eclipse.virgo.util.io.PathReference;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 *
 */
public class UploadServlet extends HttpServlet {

	private static final int HTTP_RESPONSE_INTERNAL_SERVER_ERROR = 500;

    private static final String ORG_ECLIPSE_VIRGO_KERNEL_HOME = "org.eclipse.virgo.kernel.home";
	
	private static final long serialVersionUID = 1L;
	
	private static final String STAGING_DIR = "/work/org.eclipse.virgo.apps.admin.web.UploadServlet";
	
	private static final Logger log = LoggerFactory.getLogger(UploadServlet.class);

	private String serverHome;

	public UploadServlet() {
	}
	
	public UploadServlet(String serverHome) {
		this.serverHome = serverHome;
	}

	/**
	 * Do not use this method with the HTTPService.
	 */
    public void init(ServletConfig config) throws ServletException {
    	super.init(config);
		BundleContext bc = (BundleContext) this.getServletContext().getAttribute("osgi-bundlecontext");
		this.serverHome = bc.getProperty(ORG_ECLIPSE_VIRGO_KERNEL_HOME);
    }
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
	    
	    createStagingDirectory();

		FileItemFactory factory = new DiskFileItemFactory();
		ServletFileUpload upload = new ServletFileUpload(factory);
		try {
		    response.setContentType("text/html");
		    PrintWriter writer = response.getWriter();
            writer.append("<ol id=\"uploadLocations\">");
			@SuppressWarnings("unchecked")
			List<FileItem> items = (List<FileItem>) upload.parseRequest(request);
			for (FileItem fileItem : items) {
				if (!fileItem.isFormField()) {
					String name = fileItem.getName();
					if(name != null && name.length() > 0){
						File uploadedFile = new File(String.format("%s%s/%s", this.serverHome, STAGING_DIR, name));
						fileItem.write(uploadedFile);
						log.info(String.format("Uploaded artifact of size (%db) to %s", fileItem.getSize(), uploadedFile.getPath()));
						writer.append("<li>" + uploadedFile.getAbsolutePath() + "</li>");
					}
				}
			}
            writer.append("</ol>");
			writer.close();
		} catch (Exception e) {
		    log.error(e.toString());
			e.printStackTrace();
			response.sendError(HTTP_RESPONSE_INTERNAL_SERVER_ERROR);
		}
	}

    private void createStagingDirectory() {
        PathReference pathReference = new PathReference(new File(String.format("%s%s", this.serverHome, STAGING_DIR)));
        if (!pathReference.exists()) {
            pathReference.createDirectory();
        }
    }

}
