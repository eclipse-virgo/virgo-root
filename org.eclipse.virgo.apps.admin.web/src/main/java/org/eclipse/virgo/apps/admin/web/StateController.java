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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.virgo.apps.admin.web.internal.DumpListFormatterUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import org.eclipse.virgo.apps.admin.core.BundleHolder;
import org.eclipse.virgo.apps.admin.core.DumpInspectorService;
import org.eclipse.virgo.apps.admin.core.FailedResolutionHolder;
import org.eclipse.virgo.apps.admin.core.PackagesCollection;
import org.eclipse.virgo.apps.admin.core.ServiceHolder;
import org.eclipse.virgo.apps.admin.core.StateHolder;

/**
 * <p>
 * Requests made with either a 'null' or 'Live' value in the state field will get the 
 * live view. Otherwise the requested state dump will be searched for.
 * </p>
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * StateManagerController is thread safe
 *
 */
@Controller
public final class StateController {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(StateController.class);
    
    private static final String BUNDLE_ID_NAME = "id";

    private static final String STATE_NAME = "state";
    
    private static final String FORMATTED_STATE_NAME = "fState";

    private static final String LIVE_STATE_NAME = "Live";

    private static final String BUNDLE_NAME = "name";

    private static final String VERSION_NAME = "version";

    private final StateHolder stateInspectorService;
	
    private final DumpListFormatterUtil dumpListFormatterUtil;
    

	/**
	 * Simple constructor taking an {@link DumpInspectorService} instance to provide any data required to render requests
	 * @param stateInspectorService for data to render requests
	 * @param dumpListFormatterUtil for general list formatting
	 */
    @Autowired
	public StateController(StateHolder stateInspectorService, DumpListFormatterUtil dumpListFormatterUtil) {
		this.stateInspectorService = stateInspectorService;
	    this.dumpListFormatterUtil = dumpListFormatterUtil;
	}

    /**
     * Custom handler for displaying the list of available bundles
     * @param request to limit response
     * @return ModelAndView to render
     * @throws ServletRequestBindingException getting parameters
     */
    @RequestMapping("/state/bundles.htm")
    public ModelAndView bundles(HttpServletRequest request) throws ServletRequestBindingException {
        String newState = ServletRequestUtils.getStringParameter(request, STATE_NAME);
        List<BundleHolder> bundleHolders = this.stateInspectorService.getAllBundles(newState);
        Collections.sort(bundleHolders);
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("bundles", bundleHolders);
        return createStateModelAndView("state-bundles", request, model);       
    }

    /**
     * Custom handler for displaying the list of available services
     * 
     * @param request to limit response
     * @return ModelAndView to render
     * @throws ServletRequestBindingException getting parameters
     */
    @RequestMapping("/state/services.htm")
    public ModelAndView services(HttpServletRequest request) throws ServletRequestBindingException {
        String newState = ServletRequestUtils.getStringParameter(request, STATE_NAME);
        List<ServiceHolder> serviceHolders = this.stateInspectorService.getAllServices(newState);
        Collections.sort(serviceHolders);
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("services", serviceHolders);
        return createStateModelAndView("state-services", request, model);       
    }

	/**
	 * Custom handler for displaying detailed information on a bundle
     * @param request to limit response
     * @return ModelAndView to render
     * @throws ServletRequestBindingException getting parameters
	 */
    @RequestMapping("/state/bundle.htm")
	public ModelAndView bundle(HttpServletRequest request) throws ServletRequestBindingException {
		Long bundleId = ServletRequestUtils.getLongParameter(request, BUNDLE_ID_NAME);
        String name = ServletRequestUtils.getStringParameter(request, BUNDLE_NAME);
        String version = ServletRequestUtils.getStringParameter(request, VERSION_NAME);
        String newState = ServletRequestUtils.getStringParameter(request, STATE_NAME);
		Map<String, Object> model = new HashMap<String, Object>();
		
		BundleHolder bundleHolder = null;
		try {
    		if(bundleId != null){
    		    bundleHolder = this.stateInspectorService.getBundle(newState, bundleId.longValue());
    		} else if(name != null && version != null) {
    		    bundleHolder = this.stateInspectorService.getBundle(newState, name, version);
    		}
		} catch(Exception e) {
		    LOGGER.warn(String.format("Error while retrieving bundle '%d%s'", bundleId, name), e);
		}
		
		if(bundleHolder != null) {
            model.put("title", String.format("Viewing bundle '%s - %s'", bundleHolder.getSymbolicName(), bundleHolder.getVersion()));
            model.put("bundle", bundleHolder);
		} else {
		    model.put("title", "No bundle has been requested");
		}
        
		return createStateModelAndView("state-bundle", request, model);	
	}

	/**
	 * Custom handler for displaying the list of available dumps
     * @param request to limit response
     * @return ModelAndView to render
     * @throws ServletRequestBindingException getting parameters
	 */
    @RequestMapping("/state/packages.htm")
	public ModelAndView packages(HttpServletRequest request) throws ServletRequestBindingException {
        String packageName = ServletRequestUtils.getStringParameter(request, "name");	 
        String newState = ServletRequestUtils.getStringParameter(request, STATE_NAME);
		Map<String, Object> model = new HashMap<String, Object>();
		if(packageName != null){
		    PackagesCollection packages = this.stateInspectorService.getPackages(newState, packageName);
		    model.put("importers", packages.getImported());
		    model.put("exporters", packages.getExported());
    		model.put("title", String.format("Viewing package '%s'", packages.getPackageName()));
		} else {
            model.put("title", "No package name has been provided");
		}
		return createStateModelAndView("state-packages", request, model);	
	}

    /**
     * Custom handler for displaying the possible resolution issues with the requested bundle
     * @param request to limit response
     * @return ModelAndView to render
     * @throws ServletRequestBindingException getting parameters
     */
    @RequestMapping("/state/resolve.htm")
    public ModelAndView resolve(HttpServletRequest request) throws ServletRequestBindingException {
        Long bundleId = ServletRequestUtils.getLongParameter(request, BUNDLE_ID_NAME);
        String newState = ServletRequestUtils.getStringParameter(request, STATE_NAME);
        Map<String, Object> model = new HashMap<String, Object>();
        if(bundleId != null){
            List<FailedResolutionHolder> failedResolutions = this.stateInspectorService.getResolverReport(newState, bundleId);
            if(failedResolutions.size() == 0){
                model.put("title", String.format("No resolution faliures found for bundle '%s'", bundleId));
            } else {
                FailedResolutionHolder quasiResolutionFailure = failedResolutions.get(0);
                model.put("title", String.format("State resolver report for '%s - %s'", quasiResolutionFailure.getUnresolvedBundle().getSymbolicName(), quasiResolutionFailure.getUnresolvedBundle().getVersion()));
                model.put("failure", failedResolutions);
            }
        } else {
            model.put("title", "No bundle has been requested");
        }
        return createStateModelAndView("state-resolve", request, model);  
    }
    
    /**
     * Custom handler for displaying the search results of the given search term
     * @param request to limit response
     * @return ModelAndView to render
     * @throws ServletRequestBindingException getting parameters
     */
    @RequestMapping("/state/search.htm")
    public ModelAndView search(HttpServletRequest request) throws ServletRequestBindingException {
        String term = ServletRequestUtils.getStringParameter(request, "term");
        String newState = ServletRequestUtils.getStringParameter(request, STATE_NAME);
        Map<String, Object> model = new HashMap<String, Object>();
        if(term != null){        
            List<BundleHolder> matchingBundles = this.stateInspectorService.search(newState, term);
            model.put("title", String.format("Search results for '%s'", term));
            model.put("bundles", matchingBundles);
        } else {
            model.put("title", "No search term given");
        }
        return createStateModelAndView("state-search", request, model);  
    }


    /**
     * Add any common model items and return a {@link ModelAndView}
     * 
     * @throws ServletRequestBindingException 
     */
	private ModelAndView createStateModelAndView(String viewName, HttpServletRequest request, Map<String, Object> model) throws ServletRequestBindingException {
	    Map<String, String> dumps = this.dumpListFormatterUtil.getAvaliableDumps();
	    model.put("stateSources", dumps);
        String newState = ServletRequestUtils.getStringParameter(request, STATE_NAME); // Don't want to unformat it so that it reads correct on the next call
        if(newState == null || LIVE_STATE_NAME.equals(newState)){
            model.put(STATE_NAME, LIVE_STATE_NAME);
            model.put(FORMATTED_STATE_NAME, LIVE_STATE_NAME);
        } else {
            model.put(STATE_NAME, newState);      
            model.put(FORMATTED_STATE_NAME, dumps.get(newState));
        }
		return new ModelAndView(viewName, model);
	}

}
