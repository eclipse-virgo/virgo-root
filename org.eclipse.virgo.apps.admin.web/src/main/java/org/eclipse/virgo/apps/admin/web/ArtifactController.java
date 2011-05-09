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

package org.eclipse.virgo.apps.admin.web;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import org.eclipse.virgo.apps.admin.web.internal.DojoTreeFormatter;
import org.eclipse.virgo.kernel.module.ModuleContext;
import org.eclipse.virgo.kernel.module.ModuleContextAccessor;
import org.eclipse.virgo.kernel.shell.model.helper.ArtifactAccessor;
import org.eclipse.virgo.kernel.shell.model.helper.RamAccessorHelper;
import org.eclipse.virgo.apps.admin.core.ArtifactService;
import org.eclipse.virgo.apps.admin.core.BundleHolder;
import org.eclipse.virgo.apps.admin.core.StateHolder;

/**
 * <p>
 * ArtifactController handles all requests that need the RAM to be rendered for the artifact page/view.
 * </p>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * ArtifactController is threadsafe, under the assumption that {@link ArtifactService} is also thread safe.
 * 
 */
@Controller
public final class ArtifactController {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ArtifactController.class);

    private static final String AJAX_JSON_CONTENT_TYPE = "application/json";

    private static final String TYPE = "type";

    private static final String NAME = "name";

    private static final String VERSION = "version";

    private static final String REGION = "region";

    private static final String PARENT = "parent";
    
    private final ArtifactService artifactService;

    private final DojoTreeFormatter dojoTreeJsonFormatter;
    
    private final RamAccessorHelper ramAccessorHelper;

    private final ModuleContextAccessor moduleContextAccessor;

    private final StateHolder stateInspectorService;
    
    private static final String SPRING = "spring-powered";
    
    private static final String BUNDLE = "bundle";

    /**
     * Simple constructor taking an {@link ArtifactService} instance to provide any data required to render requests
     * 
     * @param artifactService data to render requests
     * @param dojoTreeJsonFormatter formatter
     * @param moduleContextAccessor context
     * @param stateInspectorService state
     * @param ramAccessorHelper assistance
     */
    @Autowired
    public ArtifactController(ArtifactService artifactService, DojoTreeFormatter dojoTreeJsonFormatter, ModuleContextAccessor moduleContextAccessor, StateHolder stateInspectorService, RamAccessorHelper ramAccessorHelper) {
        this.artifactService = artifactService;
        this.dojoTreeJsonFormatter = dojoTreeJsonFormatter;
        this.stateInspectorService = stateInspectorService;
        this.ramAccessorHelper = ramAccessorHelper;
        this.moduleContextAccessor = moduleContextAccessor;
        
    }

    /**
     * Custom handler for displaying a list of all installed applications.
     * @param request controlling response
     * @return ModelAndView to render
     */
    @RequestMapping("/artifact/overview.htm")
    public ModelAndView overview(HttpServletRequest request) {
        String msg = request.getParameter("message");
        if (msg == null || "".equals(msg)) {
            return new ModelAndView("artifact-overview");
        }
        return new ModelAndView("artifact-overview").addObject("result", msg);
    }

    /**
     * Custom handler for deploying an application.
     * @param request controlling response
     * @return ModelAndView to render
     */
    @RequestMapping("/artifact/deploy.htm")
    public ModelAndView deploy(HttpServletRequest request) {
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        MultipartFile multipartFile = multipartRequest.getFile("application");
        String msg;
        if (multipartFile == null || multipartFile.isEmpty()) {
            msg = "Error: Please select the artifact you would like to upload.";
        } else {
            msg = this.deployFile(multipartFile);
        }
        return new ModelAndView("artifact-overview").addObject("result", msg);
    }

    private String deployFile(MultipartFile multipartFile) {
        File dest = new File(this.artifactService.getStagingDirectory().getAbsolutePath(), multipartFile.getOriginalFilename());
        try {
            multipartFile.transferTo(dest);
            return this.artifactService.deploy(dest);
        } catch (IOException e) {
            String msg = "An error occurred while transferring an uploaded file to [" + dest.getAbsolutePath()
                + "]. Consult the serviceability output for further details.";
            LOGGER.warn(msg, e);
            return msg;
        }
    }

    /**
     * Custom handler for performing an action upon an artifact.
     * @param request controlling response
     * @return ModelAndView to render
     * @throws ServletRequestBindingException for missing or unparsable parameters
     */
    @RequestMapping("/artifact/do/start")
    public ModelAndView actionStart(HttpServletRequest request) throws ServletRequestBindingException {
        String type = ServletRequestUtils.getRequiredStringParameter(request, TYPE);
        String name = ServletRequestUtils.getRequiredStringParameter(request, NAME);
        String version = ServletRequestUtils.getRequiredStringParameter(request, VERSION);

        String msg = this.ramAccessorHelper.start(type, name, version);
        return new ModelAndView("artifact-overview").addObject("result", msg);
    }

    /**
     * Custom handler for performing an action upon an artifact.
     * @param request controlling response
     * @return ModelAndView to render
     * @throws ServletRequestBindingException for missing or unparsable parameters
     */
    @RequestMapping("/artifact/do/stop")
    public ModelAndView actionStop(HttpServletRequest request) throws ServletRequestBindingException {
        String type = ServletRequestUtils.getRequiredStringParameter(request, TYPE);
        String name = ServletRequestUtils.getRequiredStringParameter(request, NAME);
        String version = ServletRequestUtils.getRequiredStringParameter(request, VERSION);

        String msg = this.ramAccessorHelper.stop(type, name, version);
        return new ModelAndView("artifact-overview").addObject("result", msg);
    }

    /**
     * Custom handler for performing an action upon an artifact.
     * @param request controlling response
     * @return ModelAndView to render
     * @throws ServletRequestBindingException for missing or unparsable parameters
     */
    @RequestMapping("/artifact/do/uninstall")
    public ModelAndView actionUninstall(HttpServletRequest request) throws ServletRequestBindingException {
        String type = ServletRequestUtils.getRequiredStringParameter(request, TYPE);
        String name = ServletRequestUtils.getRequiredStringParameter(request, NAME);
        String version = ServletRequestUtils.getRequiredStringParameter(request, VERSION);

        String msg = this.ramAccessorHelper.uninstall(type, name, version);
        return new ModelAndView("artifact-overview").addObject("result", msg);
    }

    /**
     * Custom handler for performing an action upon an artifact.
     * @param request controlling response
     * @return ModelAndView to render
     * @throws ServletRequestBindingException for missing or unparsable parameters
     */
    @RequestMapping("/artifact/do/refresh")
    public ModelAndView actionRefresh(HttpServletRequest request) throws ServletRequestBindingException {
        String type = ServletRequestUtils.getRequiredStringParameter(request, TYPE);
        String name = ServletRequestUtils.getRequiredStringParameter(request, NAME);
        String version = ServletRequestUtils.getRequiredStringParameter(request, VERSION);

        String msg = this.ramAccessorHelper.refresh(type, name, version);
        return new ModelAndView("artifact-overview").addObject("result", msg);
    }

    /**
     * Write a representation of the RAM.
     * @param request controlling response
     * @param response formatted descriptions
     * @throws IOException writing response
     * @throws ServletRequestBindingException for missing or unparsable parameters
     */
    @RequestMapping("/artifact/data")
    public void data(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletRequestBindingException {
        String type = ServletRequestUtils.getStringParameter(request, TYPE);
        String name = ServletRequestUtils.getStringParameter(request, NAME);
        String version = ServletRequestUtils.getStringParameter(request, VERSION);
        String region = ServletRequestUtils.getStringParameter(request, REGION);
        String parent = ServletRequestUtils.getStringParameter(request, PARENT);

        String responseString;
        if (type == null) {
            responseString = this.dojoTreeJsonFormatter.formatTypes(this.ramAccessorHelper.getTypes());//Top level request
        } else if (type != null && name == null) {
            responseString = this.dojoTreeJsonFormatter.formatArtifactsOfType(parent, this.ramAccessorHelper.getArtifactsOfType(type));//Second level request
        } else if (type != null && name != null && version != null) {
            ArtifactAccessor artifact;
            if(region != null && region.length() != 0){
                System.out.println("Getting with region " + region);
                artifact = this.ramAccessorHelper.getArtifact(type, name, version, region);
            } else {
                System.out.println("Getting with no region");
                artifact = this.ramAccessorHelper.getArtifact(type, name, version);
            }
            this.decorateSpringProperties(artifact);
            responseString = this.dojoTreeJsonFormatter.formatArtifactDetails(parent, artifact);//All other requests
        } else {
            throw new IllegalArgumentException(String.format("Cannot service request with parameters: %s", request.getQueryString()));
        }

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("identifier: 'id',");
        sb.append("label: 'label',");
        sb.append("items: [ ");
        if(responseString != null) {
            sb.append(responseString);
        }
        sb.append("]}");

        response.setContentType(AJAX_JSON_CONTENT_TYPE);
        PrintWriter writer = response.getWriter();
        writer.write(sb.toString());
        writer.flush();
    }

    private void decorateSpringProperties(ArtifactAccessor artifact) {
        if(BUNDLE.equals(artifact.getType())) {
            BundleHolder bundleHolder = this.stateInspectorService.getBundle(null, artifact.getName(), artifact.getVersion(), "org.eclipse.virgo.region.user");
            if(bundleHolder != null) {
                Bundle rawBundle = bundleHolder.getRawBundle();
                if(rawBundle != null) {
                    ModuleContext moduleContext = this.moduleContextAccessor.getModuleContext(rawBundle);
                    if(moduleContext != null) {
                        artifact.getAttributes().put(SPRING, moduleContext.getDisplayName());
                    }
                }
            }
        }
    }

}
