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

package org.eclipse.virgo.apps.repository.web;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import org.eclipse.virgo.apps.repository.core.RepositoryIndex;
import org.eclipse.virgo.apps.repository.core.RepositoryManager;

@Controller
class RepositoryController {
    
    private final RepositoryManager repositoryManager;
    
    private static final String INDEX_CONTENT_TYPE = "application/org.eclipse.virgo.repository.Index";
    
    private static final String ARTEFACT_CONTENT_TYPE = "application/octet-stream";
    
    private static final String IF_NONE_MATCH_HEADER_NAME = "If-None-Match";
    
    private static final String ETAG_HEADER_NAME = "Etag";
    
    RepositoryController(RepositoryManager repositoryManager) {
        this.repositoryManager = repositoryManager;
    }
    
    @RequestMapping(method = RequestMethod.GET, value = "/*")
    void getIndex(HttpServletRequest request, HttpServletResponse response) throws IOException {        
        String path = request.getRequestURI();
        String repository = path.substring(path.lastIndexOf('/') + 1);
        
        RepositoryIndex index = this.repositoryManager.getIndex(repository);
        if (index != null) {
            String indexETag = index.getETag();
            
            String eTagHeader = request.getHeader(IF_NONE_MATCH_HEADER_NAME);
            if (eTagHeader != null) {
                String[] eTags = eTagHeader.split(",");
                for (String eTag : eTags) {
                    if (eTag.equals(indexETag)) {
                        response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                        return;
                    }
                }
            }
            
            response.setContentType(INDEX_CONTENT_TYPE);
            response.setContentLength(index.getLength());
            response.addHeader(ETAG_HEADER_NAME, index.getETag());
                                                
            FileCopyUtils.copy(index.getInputStream(), response.getOutputStream());            
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }
    
    @RequestMapping(method = RequestMethod.GET, value="/*/*/*/*")
    void getArtifact(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String requestUri = request.getRequestURI();
        String[] uriComponents = requestUri.split("/");
        
        String repository = uriComponents[uriComponents.length - 4];
        String type = uriComponents[uriComponents.length - 3];
        String name = uriComponents[uriComponents.length - 2];
        String version = uriComponents[uriComponents.length - 1];
            
        InputStream artefact = this.repositoryManager.getArtifact(repository, type, name, version);
        if (artefact != null) {
            response.setContentType(ARTEFACT_CONTENT_TYPE);
            FileCopyUtils.copy(artefact, response.getOutputStream());
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }                      
    }
}
