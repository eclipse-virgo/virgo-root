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

package org.eclipse.virgo.kernel.install.artifact.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.virgo.nano.deployer.api.core.DeployerLogEvents;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.nano.deployer.api.core.FatalDeploymentException;
import org.eclipse.virgo.kernel.install.artifact.ScopeServiceRepository;
import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.osgi.framework.Version;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Utility class for parsing Spring config files and populating a {@link StandardScopeServiceRepository}.
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe.
 * 
 */
final class SpringConfigServiceModelScanner {

    private static final String ATTRIBUTE_REF = "ref";

    private static final String ATTRIBUTE_INTERFACE = "interface";

    private static final String ELEMENT_VALUE = "value";

    private static final String ELEMENT_INTERFACES = "interfaces";

    private static final String ATTRIBUTE_VALUE = ELEMENT_VALUE;

    private static final String ATTRIBUTE_KEY = "key";

    private static final String ELEMENT_ENTRY = "entry";

    private static final String ELEMENT_SERVICE_PROPERTIES = "service-properties";

    private static final String SPRING_DM_NAMESPACE = "http://www.springframework.org/schema/osgi";

    private static final String SPRING_BEANS_NAMESPACE = "http://www.springframework.org/schema/beans";

    private static final String ELEMENT_SERVICE = "service";

    private static final String BEAN_NAME_PROPERTY = "org.eclipse.gemini.blueprint.bean.name";

    private final EventLogger eventLogger;

    private final ScopeServiceRepository repository;

    private final DocumentBuilder documentBuilder;

    private final String scopeName;

    public SpringConfigServiceModelScanner(String scopeName, ScopeServiceRepository repository, EventLogger eventLogger) {
        this.scopeName = scopeName;
        this.repository = repository;
        this.eventLogger = eventLogger;
        this.documentBuilder = createDocumentBuilder();
    }

    public void scanConfigFile(String bundleSymbolicName, Version bundleVersion, String configFileName, InputStream stream) throws DeploymentException {
        Document doc = parseConfigFile(bundleSymbolicName, bundleVersion, configFileName, stream);
        doScopeServices(doc.getDocumentElement().getChildNodes());
    }

    private void doScopeServices(NodeList childNodes) {
        for (int x = 0; x < childNodes.getLength(); x++) {
            Node node = childNodes.item(x);
            if (isServiceElement(node)) {
                parseServiceElement((Element) node);
            }
            doScopeServices(node.getChildNodes());
        }
    }

    private void parseServiceElement(Element elem) {
        String[] types = extractInterfaces(elem);
        Dictionary<String, Object> properties = extractServiceProperties(elem);
        this.repository.recordService(this.scopeName, types, properties);
    }

    /**
     * Extracts all the interfaces from the supplied <code>reference</code> or <code>service</code> {@link Element}.
     * 
     * @param e the <code>Element</code> to parse.
     * @return the interfaces.
     */
    private String[] extractInterfaces(Element e) {
        Set<String> exportedInterfaces = new HashSet<String>();
        String iface = StringUtils.trimWhitespace(e.getAttribute(ATTRIBUTE_INTERFACE));
        if (StringUtils.hasText(iface)) {
            exportedInterfaces.add(iface);
        } else {
            NodeList children = e.getChildNodes();
            for (int y = 0; y < children.getLength(); y++) {
                Node child = children.item(y);
                if (child instanceof Element) {
                    if (isInterfacesElement(child)) {
                        Element elem = (Element) child;
                        NodeList intChildren = elem.getChildNodes();
                        for (int i = 0; i < intChildren.getLength(); i++) {
                            Node intChild = intChildren.item(i);
                            if (isValueElement(intChild)) {
                                exportedInterfaces.add(StringUtils.trimWhitespace(intChild.getTextContent()));
                            }
                        }
                    }
                }
            }
        }
        return exportedInterfaces.toArray(new String[exportedInterfaces.size()]);
    }

    private Dictionary<String, Object> extractServiceProperties(Element elem) {
        NodeList servicePropertiesElems = elem.getElementsByTagNameNS(SPRING_DM_NAMESPACE, ELEMENT_SERVICE_PROPERTIES);
        Dictionary<String, Object> p = null;
        if (servicePropertiesElems.getLength() > 0) {
            p = new Hashtable<String, Object>();
            Node item = servicePropertiesElems.item(0);
            readServiceProperties((Element) item, p);
        }
        p = addStandardServiceProperties(elem, p);
        return p;
    }

    private Dictionary<String, Object> addStandardServiceProperties(Element elem, Dictionary<String, Object> p) {
        // The only standard service property in the Spring DM reference manual is "bean name".
        String beanName = StringUtils.trimWhitespace(elem.getAttribute(ATTRIBUTE_REF));
        if (StringUtils.hasText(beanName)) {
            if (p == null) {
                p = new Hashtable<String, Object>();
            }
            p.put(BEAN_NAME_PROPERTY, beanName);
        }
        return p;
    }

    private void readServiceProperties(Element servicePropertiesElement, Dictionary<String, Object> serviceProperties) {
        NodeList childNodes = servicePropertiesElement.getChildNodes();
        for (int y = 0; y < childNodes.getLength(); y++) {
            Node child = childNodes.item(y);
            if (isEntryElement(child)) {
                Element entry = (Element) child;
                serviceProperties.put(entry.getAttribute(ATTRIBUTE_KEY), entry.getAttribute(ATTRIBUTE_VALUE));
            }
        }
    }

    private Document parseConfigFile(String bundleSymbolicName, Version bundleVersion, String configFileName, InputStream stream) throws DeploymentException {
        try {
            return this.documentBuilder.parse(new InputSource(stream));
        } catch (SAXException ex) {
            this.eventLogger.log(DeployerLogEvents.CONFIG_FILE_ERROR, ex, configFileName, bundleSymbolicName, bundleVersion);
            throw new DeploymentException("Error parsing configuration file '" + configFileName + "'.", ex);
        } catch (IOException ex) {
            throw new FatalDeploymentException("Error accessing configuration file '" + configFileName + "'.", ex);
        }
    }

    /**
     * Creates a {@link DocumentBuilder}.
     * 
     * @return the <code>DocumentBuilder</code>.
     */
    private DocumentBuilder createDocumentBuilder() {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            return dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new FatalDeploymentException("Unable to create DocumentBuilder - JAXP parser configuration error.", e);
        }
    }

    private boolean isValueElement(Node node) {
        return SPRING_BEANS_NAMESPACE.equals(node.getNamespaceURI()) && ELEMENT_VALUE.equals(node.getLocalName())
            && node.getNodeType() == Node.ELEMENT_NODE;
    }

    private boolean isInterfacesElement(Node node) {
        return SPRING_DM_NAMESPACE.equals(node.getNamespaceURI()) && ELEMENT_INTERFACES.equals(node.getLocalName())
            && node.getNodeType() == Node.ELEMENT_NODE;
    }

    private boolean isServiceElement(Node node) {
        return SPRING_DM_NAMESPACE.equals(node.getNamespaceURI()) && ELEMENT_SERVICE.equals(node.getLocalName())
            && node.getNodeType() == Node.ELEMENT_NODE;
    }

    private boolean isEntryElement(Node node) {
        return SPRING_BEANS_NAMESPACE.equals(node.getNamespaceURI()) && ELEMENT_ENTRY.equals(node.getLocalName())
            && node.getNodeType() == Node.ELEMENT_NODE;
    }
}
