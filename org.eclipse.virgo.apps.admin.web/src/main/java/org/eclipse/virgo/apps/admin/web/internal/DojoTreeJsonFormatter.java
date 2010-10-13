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

package org.eclipse.virgo.apps.admin.web.internal;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.springframework.stereotype.Component;

import org.eclipse.virgo.kernel.shell.model.helper.ArtifactAccessor;
import org.eclipse.virgo.kernel.shell.model.helper.ArtifactAccessorPointer;

/**
 * <p>
 * DojoTreeJsonFormatter takes in rich objects and formats them in to json for use by the artifacts dojo tree
 * </p>
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * DojoTreeJsonFormatter is thread safe
 *
 */
@Component
final class DojoTreeJsonFormatter implements DojoTreeFormatter {

    private static final String SCOPED = "scoped";

    private static final String ATOMIC = "atomic";

    private static final String STATE = "State";

    private static final String SPRING = "spring-powered";

    private static final String SCOPED_ATOMIC = "scoped-atomic";

    private static final String BUNDLE_TYPE = "bundle";

    private static final String CONFIG_TYPE = "configuration";

    private static final String BUNDLE_LINK = "/admin/web/state/bundle.htm?name=%s&version=%s";

    private static final String CONFIG_LINK = "/admin/web/config/overview.htm#%s";
    
    /** 
     * {@inheritDoc}
     */
    public String formatTypes(final List<String> types) {
        StringBuilder sb = new StringBuilder();
        if(types != null) {
            Collections.sort(types);
            if (types.size() > 0) {
                for (String type : types) {
                    sb.append("{");
                    sb.append("id: '").append(type).append("',");
                    sb.append("label: '").append(type).append("s',");
                    sb.append("type: '").append(type).append("',");
                    sb.append("tooltip: 'all user installed ").append(type).append("s',");
                    sb.append("children: []");
                    sb.append("},");
                }
                sb.deleteCharAt(sb.length() - 1);
            }
        }
        return sb.toString();
    }

    /** 
     * {@inheritDoc}
     */
    public String formatArtifactsOfType(final String parent, final List<ArtifactAccessorPointer> artifacts) {
        StringBuilder sb = new StringBuilder();
        if(parent != null && artifacts != null) {
            Collections.sort(artifacts);
            
            if (artifacts.size() > 0) {
                for (ArtifactAccessorPointer artifact : artifacts) {
                    FormattingData fd = new FormattingData(sb, parent, artifact.getType(), artifact.getName(), artifact.getVersion());
                    renderComplexChild(fd, artifact.getState());
                }
                sb.deleteCharAt(sb.length() - 1);
            }
        }
        return sb.toString();
    }

    /** 
     * {@inheritDoc}
     */
    public String formatArtifactDetails(final String parent, final ArtifactAccessor artifact) {
        StringBuilder sb = new StringBuilder();
        if(parent != null && artifact != null) {
            FormattingData fd = new FormattingData(sb, parent, artifact.getType(), artifact.getName(), artifact.getVersion());
            
            if(BUNDLE_TYPE.equals(fd.type)) {
                renderLinkChild(fd, String.format("View this %s artifact", fd.type), String.format(BUNDLE_LINK, fd.name, fd.version));
            } else if(CONFIG_TYPE.equals(fd.type)) {
                renderLinkChild(fd, String.format("View this %s artifact", fd.type), String.format(CONFIG_LINK, fd.name));
            }
            
            //Attributes
            Map<String, Object> attributes = artifact.getAttributes();
            processScopedAtomicAttributes(attributes);
            String key, value;
            Set<Entry<String, Object>> attributesEntrySet = attributes.entrySet();
            if(attributesEntrySet.size() > 0) {
                for (Entry<String, Object> attribute : attributesEntrySet) {
                    key  = attribute.getKey();
                    value = attribute.getValue().toString();
                    if(!"false".equals(value)) {
                        if(SPRING.equalsIgnoreCase(key) || SCOPED.equalsIgnoreCase(key) || ATOMIC.equalsIgnoreCase(key) || SCOPED_ATOMIC.equalsIgnoreCase(key)) {
                            renderCustomIconChild(fd, key, key);
                        } else if("true".equalsIgnoreCase(value)) {
                            renderSimpleChild(fd, key);
                        } else if (STATE.equalsIgnoreCase(key)) {
                            renderCustomIconChild(fd, value, value);
                        } else {
                            renderSimpleChild(fd, String.format("%s: %s", key, value));
                        }
                    }
                    
                }
                sb.deleteCharAt(sb.length() - 1);
            }
            //Properties
            Set<Entry<String, String>> propertiesEntrySet = artifact.getProperties().entrySet();
            if(propertiesEntrySet.size() > 0) {
                sb.append(",");
                for (Entry<String, String> attribute : propertiesEntrySet) {
                    key  = attribute.getKey();
                    value = attribute.getValue();
                    if ("org.eclipse.virgo.web.contextPath".equalsIgnoreCase(key)) {
                        renderLinkChild(fd, String.format("%s: %s", key, value), value);
                    } else if("true".equalsIgnoreCase(value)) {
                        renderSimpleChild(fd, key);
                    } else {
                        renderSimpleChild(fd, String.format("%s: %s", key, value));
                    }
                }
                sb.deleteCharAt(sb.length() - 1);
            }
            
            //Dependents
            Set<ArtifactAccessorPointer> dependents = artifact.getDependents();
            if(dependents.size() > 0) {
                sb.append(",");
                for (ArtifactAccessorPointer dependent : dependents) {
                    fd = new FormattingData(sb, parent, dependent.getType(), dependent.getName(), dependent.getVersion());
                    renderComplexChild(fd, dependent.getState());
                }
                sb.deleteCharAt(sb.length() - 1);
            }
        }
        return sb.toString();
    }
    
    private void processScopedAtomicAttributes(Map<String, Object> attributes) {
        boolean containsAtomic = attributes.containsKey(ATOMIC);
        boolean containsScoped = attributes.containsKey(SCOPED);

        if(containsScoped && containsAtomic) {
            Object scopedObject = attributes.get(SCOPED);
            boolean scoped = Boolean.parseBoolean(scopedObject.toString());

            Object atomicObject = attributes.get(ATOMIC);
            boolean atomic = Boolean.parseBoolean(atomicObject.toString());
            
            if(scoped && atomic) {
                attributes.remove(SCOPED);
                attributes.remove(ATOMIC);
                attributes.put(SCOPED_ATOMIC, "true");
            } 
        }
    }

    private void renderComplexChild(FormattingData fd, String state) {
        StringBuilder sb = fd.stringBuilder;
        sb.append("{");
        sb.append("id: '").append(fd.parentKey).append(fd.type).append(fd.name).append(fd.version).append("',");
        sb.append("label: '").append(fd.name).append("-").append(fd.version).append("',");
        sb.append("type: '").append(fd.type).append("',");
        sb.append("name: '").append(fd.name).append("',");
        sb.append("version: '").append(fd.version).append("',");
        sb.append("state: '").append(state).append("',");
        sb.append("tooltip: '").append(fd.type).append(" artifact',");
        sb.append("children: []");
        sb.append("},");
    }

    private void renderSimpleChild(FormattingData fd, String label) {
        StringBuilder sb = fd.stringBuilder;
        sb.append("{");
        sb.append("id: '").append(fd.parentKey).append(fd.type).append(fd.name).append(fd.version).append(label).append("',");
        sb.append("label: '").append(label).append("'");
        sb.append("},");
    }

    private void renderCustomIconChild(FormattingData fd, String label, String iconClass) {
        StringBuilder sb = fd.stringBuilder;
        sb.append("{");
        sb.append("id: '").append(fd.parentKey).append(fd.type).append(fd.name).append(fd.version).append(label).append("',");
        sb.append("label: '").append(label).append("',");
        sb.append("icon: '").append(iconClass).append("'");
        sb.append("},");
    }

    private void renderLinkChild(FormattingData fd, String linkText, String link) {
        StringBuilder sb = fd.stringBuilder;
        sb.append("{");
        sb.append("id: '").append(fd.parentKey).append(fd.type).append(fd.name).append(fd.version).append(linkText).append("',");
        sb.append("label: '").append(linkText).append("',");
        sb.append("link: '").append(link).append("'");
        sb.append("},");
    }
    
    /**
     * 
     * <p>
     * FormattingData is a simple internal data holder as the same set of types are used all over the place
     * </p>
     *
     * FormattingData is thread safe
     *
     */
    private static class FormattingData {
        
        final StringBuilder stringBuilder;
        final String parentKey;
        final String type;
        final String name;
        final String version;

        public FormattingData(StringBuilder stringBuilder, String parentKey, String type, String name, String version) {
            this.stringBuilder = stringBuilder;
            this.parentKey = parentKey;
            this.type = type;
            this.name = name;
            this.version = version;
        }

    }
    
}
