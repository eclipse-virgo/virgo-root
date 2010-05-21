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

package org.eclipse.virgo.apps.splash;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * <p>
 * ServerVersionController handles requests for the splash page. It simply obtains 
 * the version of the dmserver it is running on by reading the version file under 
 * the lib directory of the server install.
 * </p>
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * ServerVersionController is thread-safe
 *
 */
public final class ServerVersionController extends AbstractController {

	private final String version;
	
    public ServerVersionController(String pathToVersionFile) {
        this.version = readServerVersion(pathToVersionFile);
    }
	
	private final String readServerVersion(String path){
	    String readVersion;
			File versionFile = new File(path);
			Properties versions = new Properties();
			InputStream stream = null;
			try {
				stream = new FileInputStream(versionFile);
				versions.load(stream);
				readVersion = versions.getProperty("virgo.server.version");
				stream.close();
			} catch (IOException e) {
				readVersion = "";
				try {
					if(stream != null){
						stream.close();
					}
				} catch (IOException e1) {
					// no-op
				}
		}
		return readVersion;
	}

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
		return new ModelAndView("splash").addObject("version", this.version);
	}
	
}
