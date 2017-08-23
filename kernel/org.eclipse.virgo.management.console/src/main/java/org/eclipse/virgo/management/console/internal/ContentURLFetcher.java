/*
 * Copyright 2004-2008 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eclipse.virgo.management.console.internal;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author cgfrost
 *
 */
public class ContentURLFetcher {

	private static final Logger log = LoggerFactory.getLogger(ContentURLFetcher.class);

	private Set<String> protectedPaths = new HashSet<String>();
	{
		protectedPaths.add("/?WEB-INF/.*");
		protectedPaths.add(".*css");
		protectedPaths.add(".*gif");
		protectedPaths.add(".*ico");
		protectedPaths.add(".*jpeg");
		protectedPaths.add(".*jpg");
		protectedPaths.add(".*js");
		protectedPaths.add(".*png");
	}
	
	private String prefix = "";
	
	private String suffix = "";

	private final ServletContext context;
	
	/**
	 * 
	 * @param suffix
	 * @param prefix
	 */
	public ContentURLFetcher(ServletContext context, String prefix, String suffix) {
		this.context = context;
		this.prefix = prefix != null ?  prefix : "";
		this.suffix = suffix != null ?  suffix : "";
	}

	public URL getRequestedContentURL(String rawRequestPath) throws MalformedURLException {
		if (!isAllowed(rawRequestPath)) {
			if (log.isWarnEnabled()) {
				log.warn("An attempt to access protected content at " + rawRequestPath + " was disallowed.");
			}
			return null;
		}
		String localResourcePath = String.format("%s%s%s", this.prefix, rawRequestPath, this.suffix);	
		URL resource = this.context.getResource(localResourcePath);
		if (resource == null) {
			if (log.isDebugEnabled()) {
				log.debug("Content not found: " + localResourcePath);
			}
		}
		return resource;
	}

	private boolean isAllowed(String resourcePath) {
		for(String protectedPath: protectedPaths){
			if(resourcePath.matches(protectedPath)){
				return false;
			}
		}
		return true;
	}
	
}
