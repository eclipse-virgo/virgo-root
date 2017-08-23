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

package mvctest.web;

import static org.springframework.web.bind.ServletRequestUtils.getRequiredStringParameter;
import static org.springframework.web.bind.ServletRequestUtils.getStringParameter;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.ContextResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;

/*
 Example Links:

 http://localhost:48080/web_module_resource_loading/appCtxGetResourceGetFile?path=/MODULE-INF/WEB-INF/resource.xml
 http://localhost:48080/web_module_resource_loading/appCtxGetResourceGetFile?path=/WEB-INF/resource.xml
 http://localhost:48080/web_module_resource_loading/appCtxGetResourceGetInputStream?path=/MODULE-INF/WEB-INF/resource.xml
 http://localhost:48080/web_module_resource_loading/appCtxGetResourceGetInputStream?path=/WEB-INF/resource.xml
 http://localhost:48080/web_module_resource_loading/appCtxGetResourceGetInputStream?path=classpath:/MODULE-INF/WEB-INF/resource.xml
 http://localhost:48080/web_module_resource_loading/appCtxGetResourceGetInputStream?path=classpath:/WEB-INF/resource.xml
 http://localhost:48080/web_module_resource_loading/appCtxGetResourceGetInputStream?path=classpath:MODULE-INF/WEB-INF/resource.xml
 http://localhost:48080/web_module_resource_loading/appCtxGetResourceGetInputStream?path=classpath:WEB-INF/resource.xml
 http://localhost:48080/web_module_resource_loading/appCtxGetResourceGetInputStream?path=MODULE-INF/WEB-INF/resource.xml
 http://localhost:48080/web_module_resource_loading/appCtxGetResourceGetInputStream?path=osgibundle:/MODULE-INF/WEB-INF/resource.xml
 http://localhost:48080/web_module_resource_loading/appCtxGetResourceGetInputStream?path=osgibundle:/WEB-INF/resource.xml
 http://localhost:48080/web_module_resource_loading/appCtxGetResourceGetInputStream?path=osgibundle:MODULE-INF/WEB-INF/resource.xml
 http://localhost:48080/web_module_resource_loading/appCtxGetResourceGetInputStream?path=osgibundle:WEB-INF/resource.xml
 http://localhost:48080/web_module_resource_loading/appCtxGetResourceGetInputStream?path=WEB-INF/resource.xml
 http://localhost:48080/web_module_resource_loading/appCtxGetResources?path=classpath*%3AMETA-INF%2Fresource-1.xml
 http://localhost:48080/web_module_resource_loading/appCtxGetResourcesLikeSwfFlowDefinitionResourceFactory?basePath=%2FMODULE-INF%2FWEB-INF&pattern=%2F**%2F*-flow.xml
 http://localhost:48080/web_module_resource_loading/appCtxGetResourcesLikeSwfFlowDefinitionResourceFactory?basePath=%2FWEB-INF&pattern=%2F**%2F*-flow.xml
 http://localhost:48080/web_module_resource_loading/appCtxGetResourcesLikeSwfFlowDefinitionResourceFactory?pattern=%2F**%2F*-flow.xml
 http://localhost:48080/web_module_resource_loading/servletContextGetResourceAsStream?path=/WEB-INF/resource.xml

 */

@Controller
public class TestController implements ResourceLoaderAware {

    private ResourceLoader resourceLoader;

    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public TestController() {
        System.out.println("### TestController component scanning...");
    }

    @RequestMapping(value = "/servletContextGetResourceAsStream")
    public void servletContextGetResourceAsStream(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String path = getRequiredStringParameter(request, "path");
        String output = "<html><head><title>Testing path [" + path + "]</title></head><body>";
        InputStream resourceStream = request.getSession().getServletContext().getResourceAsStream(path);
        output += "From ServletContext via path [" + path + "]: first char: [" + (char) resourceStream.read() + "]";
        output += "</body></html>";
        response.getWriter().write(output);
    }

    @RequestMapping(value = "/appCtxGetResourceGetFile")
    public void appCtxGetResourceGetFile(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String path = getRequiredStringParameter(request, "path");
        String output = "<html><head><title>Testing path [" + path + "]</title></head><body>";
        Resource resource = this.resourceLoader.getResource(path);
        File file = resource.getFile();
        output += "From ApplicationContext/ResourceLoader's getResource().getFile() for [" + path + "]: file exists: " + file.exists()
            + "; canonical path: " + file.getCanonicalPath();
        output += "</body></html>";
        response.getWriter().write(output);
    }

    @RequestMapping(value = "/appCtxGetResourceGetInputStream")
    public void appCtxGetResourceGetInputStream(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String path = getRequiredStringParameter(request, "path");
        String output = "<html><head><title>Testing path [" + path + "]</title></head><body>";
        Resource resource = this.resourceLoader.getResource(path);
        InputStream resourceStream = resource.getInputStream();
        output += "From ApplicationContext/ResourceLoader's getResource().getInputStream() for [" + path + "]: first char: ["
            + (char) resourceStream.read() + "]";
        output += "</body></html>";
        response.getWriter().write(output);
    }

    @RequestMapping(value = "/appCtxGetResources")
    public void appCtxGetResources(HttpServletRequest request, HttpServletResponse response) throws Exception {

        final String path = getRequiredStringParameter(request, "path");
        final String title = "From ApplicationContext/ResourceLoader's getResources()";
        StringBuilder builder = new StringBuilder();
        builder.append("<html>\n<head>\n<title>").append(title).append("</title>\n</head>\n<body>\n");
        builder.append("<h1>").append(title).append("</h1>\n");
        builder.append("path: ").append(path).append("<br />\n");
        builder.append("<hr />\n");

        if (this.resourceLoader instanceof ResourcePatternResolver) {
            ResourcePatternResolver resourcePatternResolver = (ResourcePatternResolver) this.resourceLoader;
            Resource[] resources = resourcePatternResolver.getResources(path);
            for (Resource resource : resources) {
                builder.append("<h3>Resource: ").append(resource.getURL()).append("</h3>\n");
                InputStream inputStream = null;
                try {
                    inputStream = resource.getInputStream();
                    builder.append("<p>").append(FileCopyUtils.copyToString(new BufferedReader(new InputStreamReader(inputStream)))).append("</p>\n");
                } finally {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                }
                builder.append("<hr />\n");
            }
        }

        builder.append("</body>\n</html>\n");
        response.getWriter().write(builder.toString());
    }

    // -----------------------------------------------------------------------------------------------------------------
    // --- From SWF: FlowDefinitionResourceFactory
    // -----------------------------------------------------------------------------------------------------------------

    private static final String CLASSPATH_SCHEME = "classpath:";

    private static final String CLASSPATH_STAR_SCHEME = "classpath*:";

    private static final String SLASH = "/";

    @RequestMapping(value = "/appCtxGetResourcesLikeSwfFlowDefinitionResourceFactory")
    public void appCtxGetResourcesLikeSwfFlowDefinitionResourceFactory(HttpServletRequest request, HttpServletResponse response) throws Exception {

        final String pattern = getRequiredStringParameter(request, "pattern");
        final String basePath = getStringParameter(request, "basePath", null);
        final String title = "From ApplicationContext/ResourceLoader's getResources() like SWF's FlowDefinitionResourceFactory";
        StringBuilder builder = new StringBuilder();
        builder.append("<html>\n<head>\n<title>").append(title).append("</title>\n</head>\n<body>\n");
        builder.append("<h1>").append(title).append("</h1>\n");
        builder.append("base path: ").append(basePath).append("<br />\n");
        builder.append("pattern: ").append(pattern).append("<br />\n");
        builder.append("<hr />\n");

        if (this.resourceLoader instanceof ResourcePatternResolver) {
            ResourcePatternResolver resolver = (ResourcePatternResolver) this.resourceLoader;
            Resource[] resources;
            if (basePath == null) {
                resources = resolver.getResources(pattern);
            } else {
                if (basePath.endsWith(SLASH) || pattern.startsWith(SLASH)) {
                    resources = resolver.getResources(basePath + pattern);
                } else {
                    resources = resolver.getResources(basePath + SLASH + pattern);
                }
            }

            for (Resource resource : resources) {
                builder.append("<h3>Resource: ").append(resource.getURL()).append("</h3>\n");
                if (resource instanceof ContextResource) {
                    ContextResource contextResource = (ContextResource) resource;
                    builder.append("<p>PathWithinContext: ").append(contextResource.getPathWithinContext()).append("</p>\n");
                } else {
                    builder.append("<p>Resource is not a ContextResource but rather a [").append(resource.getClass().getName()).append("].</p>\n");
                }
                builder.append("<p>Flow ID: ").append(getFlowId(basePath, resource)).append("</p>\n");
                builder.append("<hr />\n");
            }
        }

        builder.append("</body>\n</html>\n");
        response.getWriter().write(builder.toString());
    }

    /**
     * Obtains the flow id from the flow resource. By default, the flow id becomes the portion of the path between the
     * basePath and the filename. If no directory structure is available then the filename without the extension is
     * used. Subclasses may override.
     * <p>
     * For example, '${basePath}/booking.xml' becomes 'booking' and '${basePath}/hotels/booking/booking.xml' becomes
     * 'hotels/booking'
     * 
     * @param flowResource the flow resource
     * @return the flow id
     */
    protected String getFlowId(String basePath, Resource flowResource) {
        if (basePath == null) {
            return getFlowIdFromFileName(flowResource);
        }
        String filePath;
        if (flowResource instanceof ClassPathResource) {
            filePath = ((ClassPathResource) flowResource).getPath();
            // remove classpath scheme
            if (basePath.startsWith(CLASSPATH_SCHEME)) {
                basePath = basePath.substring(CLASSPATH_SCHEME.length());
            } else if (basePath.startsWith(CLASSPATH_STAR_SCHEME)) {
                basePath = basePath.substring(CLASSPATH_STAR_SCHEME.length());
            }
        } else if (flowResource instanceof ContextResource) {
            filePath = ((ContextResource) flowResource).getPathWithinContext();
        } else {
            // default to the filename
            return getFlowIdFromFileName(flowResource);
        }
        // TODO can this logic be simplified?
        int beginIndex = 0;
        int endIndex = filePath.length();
        if (filePath.startsWith(SLASH) || !basePath.startsWith(SLASH)) {
            if (filePath.startsWith(basePath)) {
                beginIndex = basePath.length();
            }
        } else {
            if (filePath.startsWith(SLASH + basePath)) {
                beginIndex = basePath.length() + 1;
            }
        }
        // ignore a leading slash
        if (filePath.startsWith(SLASH, beginIndex)) {
            beginIndex++;
        }
        if (filePath.lastIndexOf(SLASH) >= beginIndex) {
            // ignore the filename
            endIndex = filePath.lastIndexOf(SLASH);
        } else {
            // there is no path info, default to the filename
            return getFlowIdFromFileName(flowResource);
        }
        return filePath.substring(beginIndex, endIndex);
    }

    private String getFlowIdFromFileName(Resource flowResource) {
        return StringUtils.stripFilenameExtension(flowResource.getFilename());
    }

    // -----------------------------------------------------------------------------------------------------------------
    // --- BundleContextAware
    // -----------------------------------------------------------------------------------------------------------------

    /*
     * @SuppressWarnings("unused") private BundleContext bundleContext;
     * 
     * public void setBundleContext(BundleContext bundleContext) { this.bundleContext = bundleContext; }
     */

    // String location = this.bundleContext.getBundle().getLocation();
    // if (location.startsWith("file:")) {
    // location = location.substring(5);
    // }
    // File f = new File(location, "/WEB-INF/resource.xml" );
    //
    // System.err.println("*****: new File " + f + " exists? " + f.exists());
    // System.err.println("*****: URL " + file);
    // System.err.println("*****: File " + new File(file.toURI()));
    // return "From ApplicationContext: URL: " + file;
    // -----------------------------------------------------------------------------------------------------------------
}
