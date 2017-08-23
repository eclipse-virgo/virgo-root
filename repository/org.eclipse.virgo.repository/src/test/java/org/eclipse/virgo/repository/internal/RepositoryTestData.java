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

package org.eclipse.virgo.repository.internal;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.virgo.repository.Attribute;
import org.eclipse.virgo.repository.RepositoryAwareArtifactDescriptor;
import org.eclipse.virgo.repository.builder.ArtifactDescriptorBuilder;
import org.osgi.framework.Version;

public final class RepositoryTestData {

    private final static String TESTING_DIRECTORY = "file:/src/test/resources/";

    public final static String TEST_ARTEFACT_TYPE = "test_type";

    public final static String TEST_REPO_ONE = "foo_one";

    public final static String TEST_REPO_TWO = "foo_two";

    public static final String TEST_ARTEFACT_BRIDGE_NAME_A = "BundleA";

    public static final String TEST_ARTEFACT_BRIDGE_NAME_B = "BundleB";

    public static final String TEST_ARTEFACT_BRIDGE_NAME_C = "BundleC";

    private static final Map<String, Set<String>> TEST_ATTRIBUTE_PARAMETERS_ZEROTWO = new HashMap<String, Set<String>>();

    public static final String TEST_ATTRIBUTE_NAME_ONE = "Attribute Name One";

    public static final String TEST_ATTRIBUTE_VALUE_ONE = "Attribute Value One";

    public static final String TEST_ATTRIBUTE_NAME_TWO = "Attribute Name Two";

    public static final String TEST_ATTRIBUTE_VALUE_TWO = "Attribute Value Two";

    public static final Map<String, Set<String>> TEST_ATTRIBUTE_PARAMETERS_TWO = new HashMap<String, Set<String>>();

    public static final String TEST_ATTRIBUTE_NAME_THREE = "Attribute Name Three";

    public static final String TEST_ATTRIBUTE_VALUE_THREE = "Attribute Value Three";

    public static final Map<String, Set<String>> TEST_ATTRIBUTE_PARAMETERS_THREE = new HashMap<String, Set<String>>();

    private static final String TEST_ATTRIBUTE_PARAMETER_KEY_ONE = "Attribute Paramater Key One";

    private static final String TEST_ATTRIBUTE_PARAMETER_KEY_TWO = "Attribute Paramater Key Two";

    private static final String TEST_ATTRIBUTE_PARAMETER_KEY_THREE = "Attribute Paramater Key Three";

    private static final String TEST_ATTRIBUTE_PARAMETER_KEY_FOUR = "Attribute Paramater Key Four";

    private static final String TEST_ATTRIBUTE_PARAMETER_VALUE_ONE = "Attribute Paramater Value One";

    private static final String TEST_ATTRIBUTE_PARAMETER_VALUE_TWO = "Attribute Paramater Value Two";

    private static final String TEST_ATTRIBUTE_PARAMETER_VALUE_THREE = "Attribute Paramater Value Three";

    private static final String TEST_ATTRIBUTE_PARAMETER_VALUE_FOUR = "Attribute Paramater Value Four";

    static {
        Set<String> prop_values;

        prop_values = new HashSet<String>();
        prop_values.add(TEST_ATTRIBUTE_PARAMETER_VALUE_TWO);
        TEST_ATTRIBUTE_PARAMETERS_ZEROTWO.put(TEST_ATTRIBUTE_PARAMETER_KEY_TWO, prop_values);
        prop_values = new HashSet<String>();
        prop_values.add(TEST_ATTRIBUTE_PARAMETER_VALUE_THREE);
        TEST_ATTRIBUTE_PARAMETERS_ZEROTWO.put(TEST_ATTRIBUTE_PARAMETER_KEY_THREE, prop_values);
        prop_values = new HashSet<String>();
        prop_values.add(TEST_ATTRIBUTE_PARAMETER_VALUE_FOUR);
        TEST_ATTRIBUTE_PARAMETERS_ZEROTWO.put(TEST_ATTRIBUTE_PARAMETER_KEY_FOUR, prop_values);

        prop_values = new HashSet<String>();
        prop_values.add(TEST_ATTRIBUTE_PARAMETER_VALUE_TWO);
        TEST_ATTRIBUTE_PARAMETERS_TWO.put(TEST_ATTRIBUTE_PARAMETER_KEY_TWO, prop_values);
        prop_values = new HashSet<String>();
        prop_values.add(TEST_ATTRIBUTE_PARAMETER_VALUE_THREE);
        TEST_ATTRIBUTE_PARAMETERS_TWO.put(TEST_ATTRIBUTE_PARAMETER_KEY_THREE, prop_values);
        prop_values = new HashSet<String>();
        prop_values.add(TEST_ATTRIBUTE_PARAMETER_VALUE_FOUR);
        TEST_ATTRIBUTE_PARAMETERS_TWO.put(TEST_ATTRIBUTE_PARAMETER_KEY_FOUR, prop_values);

        prop_values = new HashSet<String>();
        prop_values.add(TEST_ATTRIBUTE_PARAMETER_VALUE_ONE);
        TEST_ATTRIBUTE_PARAMETERS_THREE.put(TEST_ATTRIBUTE_PARAMETER_KEY_ONE, prop_values);
        prop_values = new HashSet<String>();
        prop_values.add(TEST_ATTRIBUTE_PARAMETER_VALUE_TWO);
        TEST_ATTRIBUTE_PARAMETERS_THREE.put(TEST_ATTRIBUTE_PARAMETER_KEY_TWO, prop_values);
    }

    private final static String TEST_NAME_ZERO_TNV = "org.eclipse.virgo.javax.servlet.foo";

    private final static Version TEST_VERSION_ZERO_TNV = new Version("2.4.0.foo");

    private final static String TEST_FILE_ZERO_TNV = "org.eclipse.virgo.javax.servlet-2.4.0.jar";

    private final static URI TEST_URI_ZERO_TNV = URI.create(TESTING_DIRECTORY + TEST_FILE_ZERO_TNV);

    private static final Set<Attribute> TEST_ATTRIBUTE_SET_ZERO_TNV = createAttributeSet(TEST_ARTEFACT_BRIDGE_NAME_C, TEST_NAME_ZERO_TNV,
        TEST_VERSION_ZERO_TNV, TEST_URI_ZERO_TNV);

    public final static RepositoryAwareArtifactDescriptor TEST_ARTEFACT_ZERO_TNV = createDescriptor(TEST_URI_ZERO_TNV, TEST_ARTEFACT_TYPE,
        TEST_NAME_ZERO_TNV, TEST_VERSION_ZERO_TNV, TEST_ATTRIBUTE_SET_ZERO_TNV);

    private final static String TEST_NAME_ZERO_URI = "org.eclipse.virgo.javax.servlet";

    private final static Version TEST_VERSION_ZERO_URI = new Version("2.4.0");

    private final static String TEST_FILE_ZERO_URI = "org.eclipse.virgo.javax.servlet-2.4.0.jar.foo";

    private final static URI TEST_URI_ZERO_URI = URI.create(TESTING_DIRECTORY + TEST_FILE_ZERO_URI);

    private static final Set<Attribute> TEST_ATTRIBUTE_SET_ZERO_URI = createAttributeSet(TEST_ARTEFACT_BRIDGE_NAME_C, TEST_NAME_ZERO_URI,
        TEST_VERSION_ZERO_URI, TEST_URI_ZERO_URI);

    public final static RepositoryAwareArtifactDescriptor TEST_ARTEFACT_ZERO_URI = createDescriptor(TEST_URI_ZERO_URI, TEST_ARTEFACT_TYPE,
        TEST_NAME_ZERO_URI, TEST_VERSION_ZERO_URI, TEST_ATTRIBUTE_SET_ZERO_URI);

    private final static String TEST_NAME_ZERO = "org.eclipse.virgo.javax.servlet";

    private final static Version TEST_VERSION_ZERO = new Version("2.4.0");

    private final static String TEST_FILE_ZERO = "org.eclipse.virgo.javax.servlet-2.4.0.jar";

    public final static URI TEST_URI_ZERO = URI.create(TESTING_DIRECTORY + TEST_FILE_ZERO);

    private static final Set<Attribute> TEST_ATTRIBUTE_SET_ZERO = createAttributeSet(TEST_ARTEFACT_BRIDGE_NAME_C, TEST_NAME_ZERO, TEST_VERSION_ZERO,
        TEST_URI_ZERO);

    public final static RepositoryAwareArtifactDescriptor TEST_ARTEFACT_ZERO = createDescriptor(TEST_URI_ZERO, TEST_ARTEFACT_TYPE, TEST_NAME_ZERO,
        TEST_VERSION_ZERO, TEST_ATTRIBUTE_SET_ZERO);

    public final static String TEST_NAME_ONE = "org.eclipse.virgo.javax.servlet";

    public final static Version TEST_VERSION_ONE = new Version("2.4.0");

    private final static String TEST_FILE_ONE = "org.eclipse.virgo.javax.servlet-2.4.0.jar";

    public final static URI TEST_URI_ONE = URI.create(TESTING_DIRECTORY + TEST_FILE_ONE);

    private static final Set<Attribute> TEST_ATTRIBUTE_SET_ONE = createAttributeSet(TEST_ARTEFACT_BRIDGE_NAME_C, TEST_NAME_ONE, TEST_VERSION_ONE,
        TEST_URI_ONE);

    public final static RepositoryAwareArtifactDescriptor TEST_ARTEFACT_ONE = createDescriptor(TEST_URI_ONE, TEST_ARTEFACT_TYPE, TEST_NAME_ONE,
        TEST_VERSION_ONE, TEST_ATTRIBUTE_SET_ONE);

    private final static String TEST_NAME_TWO = "org.eclipse.virgo.org.apache.commons.dbcp";

    private final static Version TEST_VERSION_TWO = new Version("1.2.2.osgi");

    private final static String TEST_FILE_TWO = "org.eclipse.virgo.org.apache.commons.dbcp-1.2.2.osgi.jar";

    public final static URI TEST_URI_TWO = URI.create(TESTING_DIRECTORY + TEST_FILE_TWO);

    private static final Set<Attribute> TEST_ATTRIBUTE_SET_TWO = createAttributeSet(TEST_ARTEFACT_BRIDGE_NAME_A, TEST_NAME_TWO, TEST_VERSION_TWO,
        TEST_URI_TWO);

    public final static RepositoryAwareArtifactDescriptor TEST_ARTEFACT_TWO = createDescriptor(TEST_URI_TWO, TEST_ARTEFACT_TYPE, TEST_NAME_TWO,
        TEST_VERSION_TWO, TEST_ATTRIBUTE_SET_TWO);

    private final static String TEST_NAME_THREE = "org.eclipse.virgo.org.apache.commons.logging";

    private final static Version TEST_VERSION_THREE = new Version("1.1.1");

    private final static String TEST_FILE_THREE = "org.eclipse.virgo.org.apache.commons.logging-1.1.1.jar";

    public final static URI TEST_URI_THREE = URI.create(TESTING_DIRECTORY + TEST_FILE_THREE);

    private static final Set<Attribute> TEST_ATTRIBUTE_SET_THREE = createAttributeSet(TEST_ARTEFACT_BRIDGE_NAME_A, TEST_NAME_THREE,
        TEST_VERSION_THREE, TEST_URI_THREE);

    public final static RepositoryAwareArtifactDescriptor TEST_ARTEFACT_THREE = createDescriptor(TEST_URI_THREE, TEST_ARTEFACT_TYPE, TEST_NAME_THREE,
        TEST_VERSION_THREE, TEST_ATTRIBUTE_SET_THREE);

    private final static String TEST_NAME_FOUR = "org.eclipse.virgo.org.apache.commons.pool";

    private final static Version TEST_VERSION_FOUR = new Version("1.3.0");

    private final static String TEST_FILE_FOUR = "org.eclipse.virgo.org.apache.commons.pool-1.3.0.jar";

    public final static URI TEST_URI_FOUR = URI.create(TESTING_DIRECTORY + TEST_FILE_FOUR);

    private static final Set<Attribute> TEST_ATTRIBUTE_SET_FOUR = createAttributeSet(TEST_ARTEFACT_BRIDGE_NAME_A, TEST_NAME_FOUR, TEST_VERSION_FOUR,
        TEST_URI_FOUR);

    public final static RepositoryAwareArtifactDescriptor TEST_ARTEFACT_FOUR = createDescriptor(TEST_URI_FOUR, TEST_ARTEFACT_TYPE, TEST_NAME_FOUR,
        TEST_VERSION_FOUR, TEST_ATTRIBUTE_SET_FOUR);

    private final static String TEST_NAME_FIVE = "org.eclipse.virgo.org.apache.log4j";

    private final static Version TEST_VERSION_FIVE = new Version("1.2.15");

    private final static String TEST_FILE_FIVE = "org.eclipse.virgo.org.apache.log4j-1.2.15.jar";

    public final static URI TEST_URI_FIVE = URI.create(TESTING_DIRECTORY + TEST_FILE_FIVE);

    private static final Set<Attribute> TEST_ATTRIBUTE_SET_FIVE = createAttributeSet(TEST_ARTEFACT_BRIDGE_NAME_A, TEST_NAME_FIVE, TEST_VERSION_FIVE,
        TEST_URI_FIVE);

    public final static RepositoryAwareArtifactDescriptor TEST_ARTEFACT_FIVE = createDescriptor(TEST_URI_FIVE, TEST_ARTEFACT_TYPE, TEST_NAME_FIVE,
        TEST_VERSION_FIVE, TEST_ATTRIBUTE_SET_FIVE);

    private final static String TEST_NAME_SIX = "org.eclipse.virgo.org.hsqldb";

    private final static Version TEST_VERSION_SIX = new Version("1.8.0.9");

    private final static String TEST_FILE_SIX = "org.eclipse.virgo.org.hsqldb-1.8.0.9.jar";

    public final static URI TEST_URI_SIX = URI.create(TESTING_DIRECTORY + TEST_FILE_SIX);

    private static final Set<Attribute> TEST_ATTRIBUTE_SET_SIX = createAttributeSet(TEST_ARTEFACT_BRIDGE_NAME_C, TEST_NAME_SIX, TEST_VERSION_SIX,
        TEST_URI_SIX);

    public final static RepositoryAwareArtifactDescriptor TEST_ARTEFACT_SIX = createDescriptor(TEST_URI_SIX, TEST_ARTEFACT_TYPE, TEST_NAME_SIX,
        TEST_VERSION_SIX, TEST_ATTRIBUTE_SET_SIX);

    private final static String TEST_NAME_SEVEN = "org.springframework.aop";

    private final static Version TEST_VERSION_SEVEN = new Version("2.5.4.A");

    private final static String TEST_FILE_SEVEN = "org.springframework.aop-2.5.4.A.jar";

    public final static URI TEST_URI_SEVEN = URI.create(TESTING_DIRECTORY + TEST_FILE_SEVEN);

    private static final Set<Attribute> TEST_ATTRIBUTE_SET_SEVEN = createAttributeSet(TEST_ARTEFACT_BRIDGE_NAME_B, TEST_NAME_SEVEN,
        TEST_VERSION_SEVEN, TEST_URI_SEVEN);

    public final static RepositoryAwareArtifactDescriptor TEST_ARTEFACT_SEVEN = createDescriptor(TEST_URI_SEVEN, TEST_ARTEFACT_TYPE, TEST_NAME_SEVEN,
        TEST_VERSION_SEVEN, TEST_ATTRIBUTE_SET_SEVEN);

    private final static String TEST_NAME_EIGHT = "org.springframework.beans";

    private final static Version TEST_VERSION_EIGHT = new Version("2.5.4.A");

    private final static String TEST_FILE_EIGHT = "org.springframework.beans-2.5.4.A.jar";

    public final static URI TEST_URI_EIGHT = URI.create(TESTING_DIRECTORY + TEST_FILE_EIGHT);

    private static final Set<Attribute> TEST_ATTRIBUTE_SET_EIGHT = createAttributeSet(TEST_ARTEFACT_BRIDGE_NAME_B, TEST_NAME_EIGHT,
        TEST_VERSION_EIGHT, TEST_URI_EIGHT);

    public final static RepositoryAwareArtifactDescriptor TEST_ARTEFACT_EIGHT = createDescriptor(TEST_URI_EIGHT, TEST_ARTEFACT_TYPE, TEST_NAME_EIGHT,
        TEST_VERSION_EIGHT, TEST_ATTRIBUTE_SET_EIGHT);

    private final static String TEST_NAME_NINE = "org.springframework.context";

    private final static Version TEST_VERSION_NINE = new Version("2.5.4.A");

    private final static String TEST_FILE_NINE = "org.springframework.context-2.5.4.A.jar";

    public final static URI TEST_URI_NINE = URI.create(TESTING_DIRECTORY + TEST_FILE_NINE);

    private static final Set<Attribute> TEST_ATTRIBUTE_SET_NINE = createAttributeSet(TEST_ARTEFACT_BRIDGE_NAME_B, TEST_NAME_NINE, TEST_VERSION_NINE,
        TEST_URI_NINE);

    public final static RepositoryAwareArtifactDescriptor TEST_ARTEFACT_NINE = createDescriptor(TEST_URI_NINE, TEST_ARTEFACT_TYPE, TEST_NAME_NINE,
        TEST_VERSION_NINE, TEST_ATTRIBUTE_SET_NINE);

    private final static String TEST_NAME_TEN = "org.springframework.core";

    private final static Version TEST_VERSION_TEN = new Version("2.5.4.A");

    private final static String TEST_FILE_TEN = "org.springframework.core-2.5.4.A.jar";

    public final static URI TEST_URI_TEN = URI.create(TESTING_DIRECTORY + TEST_FILE_TEN);

    private static final Set<Attribute> TEST_ATTRIBUTE_SET_TEN = createAttributeSet(TEST_ARTEFACT_BRIDGE_NAME_B, TEST_NAME_TEN, TEST_VERSION_TEN,
        TEST_URI_TEN);

    public final static RepositoryAwareArtifactDescriptor TEST_ARTEFACT_TEN = createDescriptor(TEST_URI_TEN, TEST_ARTEFACT_TYPE, TEST_NAME_TEN,
        TEST_VERSION_TEN, TEST_ATTRIBUTE_SET_TEN);

    private final static String TEST_NAME_ELEVEN = "org.springframework.jdbc";

    private final static Version TEST_VERSION_ELEVEN = new Version("2.5.4.A");

    private final static String TEST_FILE_ELEVEN = "org.springframework.jdbc-2.5.4.A.jar";

    public final static URI TEST_URI_ELEVEN = URI.create(TESTING_DIRECTORY + TEST_FILE_ELEVEN);

    private static final Set<Attribute> TEST_ATTRIBUTE_SET_ELEVEN = createAttributeSet(TEST_ARTEFACT_BRIDGE_NAME_B, TEST_NAME_ELEVEN,
        TEST_VERSION_ELEVEN, TEST_URI_ELEVEN);

    public final static RepositoryAwareArtifactDescriptor TEST_ARTEFACT_ELEVEN = createDescriptor(TEST_URI_ELEVEN, TEST_ARTEFACT_TYPE,
        TEST_NAME_ELEVEN, TEST_VERSION_ELEVEN, TEST_ATTRIBUTE_SET_ELEVEN);

    private final static String TEST_NAME_TWELVE = "org.springframework.orm";

    private final static Version TEST_VERSION_TWELVE = new Version("2.5.4.A");

    private final static String TEST_FILE_TWELVE = "org.springframework.orm-2.5.4.A.jar";

    public final static URI TEST_URI_TWELVE = URI.create(TESTING_DIRECTORY + TEST_FILE_TWELVE);

    private static final Set<Attribute> TEST_ATTRIBUTE_SET_TWELEVE = createAttributeSet(TEST_ARTEFACT_BRIDGE_NAME_B, TEST_NAME_TWELVE,
        TEST_VERSION_TWELVE, TEST_URI_TWELVE);

    public final static RepositoryAwareArtifactDescriptor TEST_ARTEFACT_TWELVE = createDescriptor(TEST_URI_TWELVE, TEST_ARTEFACT_TYPE,
        TEST_NAME_TWELVE, TEST_VERSION_TWELVE, TEST_ATTRIBUTE_SET_TWELEVE);

    private final static String TEST_NAME_THIRTEEN = "org.springframework.transaction";

    private final static Version TEST_VERSION_THIRTEEN = new Version("2.5.4.A");

    private final static String TEST_FILE_THIRTEEN = "org.springframework.transaction-2.5.4.A.jar";

    public final static URI TEST_URI_THIRTEEN = URI.create(TESTING_DIRECTORY + TEST_FILE_THIRTEEN);

    private static final Set<Attribute> TEST_ATTRIBUTE_SET_THIRTEEN = createAttributeSet(TEST_ARTEFACT_BRIDGE_NAME_C, TEST_NAME_THIRTEEN,
        TEST_VERSION_THIRTEEN, TEST_URI_THIRTEEN, new StandardAttribute(TEST_ATTRIBUTE_NAME_ONE, TEST_ATTRIBUTE_VALUE_ONE));

    public final static RepositoryAwareArtifactDescriptor TEST_ARTEFACT_THIRTEEN = createDescriptor(TEST_URI_THIRTEEN, TEST_ARTEFACT_TYPE,
        TEST_NAME_THIRTEEN, TEST_VERSION_THIRTEEN, TEST_ATTRIBUTE_SET_THIRTEEN);

    private final static String TEST_NAME_FOURTEEN = "org.springframework.web";

    private final static Version TEST_VERSION_FOURTEEN = new Version("2.5.4.A");

    private final static String TEST_FILE_FOURTEEN = "org.springframework.web-2.5.4.A.jar";

    public final static URI TEST_URI_FOURTEEN = URI.create(TESTING_DIRECTORY + TEST_FILE_FOURTEEN);

    private static final Set<Attribute> TEST_ATTRIBUTE_SET_FOURTEEN = createAttributeSet(TEST_ARTEFACT_BRIDGE_NAME_C, TEST_NAME_FOURTEEN,
        TEST_VERSION_FOURTEEN, TEST_URI_FOURTEEN, new StandardAttribute(TEST_ATTRIBUTE_NAME_TWO, TEST_ATTRIBUTE_VALUE_TWO,
            TEST_ATTRIBUTE_PARAMETERS_TWO));

    public final static RepositoryAwareArtifactDescriptor TEST_ARTEFACT_FOURTEEN = createDescriptor(TEST_URI_FOURTEEN, TEST_ARTEFACT_TYPE,
        TEST_NAME_FOURTEEN, TEST_VERSION_FOURTEEN, TEST_ATTRIBUTE_SET_FOURTEEN);

    public final static String TEST_NAME_FIFTEEN = "org.springframework.web.servlet";

    private final static Version TEST_VERSION_FIFTEEN = new Version("2.5.4.A");

    private final static String TEST_FILE_FIFTEEN = "org.springframework.web.servlet-2.5.4.A.jar";

    public final static URI TEST_URI_FIFTEEN = URI.create(TESTING_DIRECTORY + TEST_FILE_FIFTEEN);

    private static final Set<Attribute> TEST_ATTRIBUTE_SET_FIFTEEN = createAttributeSet(TEST_ARTEFACT_BRIDGE_NAME_C, TEST_NAME_FIFTEEN,
        TEST_VERSION_FIFTEEN, TEST_URI_FIFTEEN, new StandardAttribute(TEST_ATTRIBUTE_NAME_ONE, TEST_ATTRIBUTE_VALUE_ONE), new StandardAttribute(
            TEST_ATTRIBUTE_NAME_TWO, TEST_ATTRIBUTE_VALUE_TWO, TEST_ATTRIBUTE_PARAMETERS_TWO), new StandardAttribute(TEST_ATTRIBUTE_NAME_THREE,
            TEST_ATTRIBUTE_VALUE_THREE, TEST_ATTRIBUTE_PARAMETERS_THREE));

    public final static RepositoryAwareArtifactDescriptor TEST_ARTEFACT_FIFTEEN = createDescriptor(TEST_URI_FIFTEEN, TEST_ARTEFACT_TYPE,
        TEST_NAME_FIFTEEN, TEST_VERSION_FIFTEEN, TEST_ATTRIBUTE_SET_FIFTEEN);

    private final static String TEST_NAME_SIXTEEN = "org.eclipse.virgo.org.aopalliance";

    private final static Version TEST_VERSION_SIXTEEN = new Version("1.0.0");

    private final static String TEST_FILE_SIXTEEN = "org.eclipse.virgo.org.aopalliance-1.0.0.jar";

    public final static URI TEST_URI_SIXTEEN = URI.create(TESTING_DIRECTORY + TEST_FILE_SIXTEEN);

    private static final Set<Attribute> TEST_ATTRIBUTE_SET_SIXTEEN = createAttributeSet(TEST_ARTEFACT_BRIDGE_NAME_A, TEST_NAME_SIXTEEN,
        TEST_VERSION_SIXTEEN, TEST_URI_SIXTEEN, new StandardAttribute(TEST_ATTRIBUTE_NAME_ONE, TEST_ATTRIBUTE_VALUE_ONE), new StandardAttribute(
            TEST_ATTRIBUTE_NAME_THREE, TEST_ATTRIBUTE_VALUE_THREE, TEST_ATTRIBUTE_PARAMETERS_THREE));

    public final static RepositoryAwareArtifactDescriptor TEST_ARTEFACT_SIXTEEN = createDescriptor(TEST_URI_SIXTEEN, TEST_ARTEFACT_TYPE,
        TEST_NAME_SIXTEEN, TEST_VERSION_SIXTEEN, TEST_ATTRIBUTE_SET_SIXTEEN);

    public final static String ARTEFACT_ATTRIBUTE_TYPE = "type";

    private final static String ARTEFACT_ATTRIBUTE_NAME = "name";

    private final static String ARTEFACT_ATTRIBUTE_VERSION = "String";

    private final static String ARTEFACT_ATTRIBUTE_URI = "uri";

    public final static Attribute TEST_QUERY_FILTER_TYPE_A = new StandardAttribute(ARTEFACT_ATTRIBUTE_TYPE, TEST_ARTEFACT_BRIDGE_NAME_A);

    public final static Attribute TEST_QUERY_FILTER_TYPE_B = new StandardAttribute(ARTEFACT_ATTRIBUTE_TYPE, TEST_ARTEFACT_BRIDGE_NAME_B);

    public final static Attribute TEST_QUERY_FILTER_TYPE_C = new StandardAttribute(ARTEFACT_ATTRIBUTE_TYPE, TEST_ARTEFACT_BRIDGE_NAME_C);

    // private final static Attribute TEST_QUERY_FILTER_NAME_ONE = new StandardAttribute(ARTEFACT_ATTRIBUTE_NAME,
    // TEST_NAME_ONE);
    // private final static Attribute TEST_QUERY_FILTER_NAME_TWO = new StandardAttribute(ARTEFACT_ATTRIBUTE_NAME,
    // TEST_NAME_TWO);
    public final static Attribute TEST_QUERY_FILTER_NAME_TWELVE = new StandardAttribute(ARTEFACT_ATTRIBUTE_NAME, TEST_NAME_TWELVE);

    public final static Attribute TEST_QUERY_FILTER_NAME_THIRTEEN = new StandardAttribute(ARTEFACT_ATTRIBUTE_NAME, TEST_NAME_THIRTEEN);

    // private final static Attribute TEST_QUERY_FILTER_NAME_FOURTEEN = new StandardAttribute(ARTEFACT_ATTRIBUTE_NAME,
    // TEST_NAME_FOURTEEN);

    public final static Attribute TEST_QUERY_FILTER_VERSION_254A = new StandardAttribute(ARTEFACT_ATTRIBUTE_VERSION, new String("2.5.4.A"));

    // private final static Attribute TEST_QUERY_FILTER_VERSION_240 = new StandardAttribute(ARTEFACT_ATTRIBUTE_VERSION,
    // new String("2.4.0"));
    public final static Attribute TEST_QUERY_FILTER_VERSION_100 = new StandardAttribute(ARTEFACT_ATTRIBUTE_VERSION, new String("1.0.0"));

    // private final static Attribute TEST_QUERY_FILTER_URI_TWO = new StandardAttribute(ARTEFACT_ATTRIBUTE_URI,
    // TEST_URI_TWO.toString());
    public final static Attribute TEST_QUERY_FILTER_URI_THREE = new StandardAttribute(ARTEFACT_ATTRIBUTE_URI, TEST_URI_THREE.toString());

    // private final static Attribute TEST_QUERY_FILTER_URI_FOURTEEN = new StandardAttribute(ARTEFACT_ATTRIBUTE_URI,
    // TEST_URI_FOURTEEN.toString());
    // private final static Attribute TEST_QUERY_FILTER_URI_FIFTEEN = new StandardAttribute(ARTEFACT_ATTRIBUTE_URI,
    // TEST_URI_FIFTEEN.toString());

    public final static Attribute TEST_QUERY_FILTER_NOTHING = new StandardAttribute(ARTEFACT_ATTRIBUTE_NAME, "I don't exist");

    public final static Attribute TEST_QUERY_FILTER_ATTRIBUTE_ONE = new StandardAttribute(TEST_ATTRIBUTE_NAME_ONE, TEST_ATTRIBUTE_VALUE_ONE);

    public final static Attribute TEST_QUERY_FILTER_ATTRIBUTE_TWO = new StandardAttribute(TEST_ATTRIBUTE_NAME_TWO, TEST_ATTRIBUTE_VALUE_TWO,
        TEST_ATTRIBUTE_PARAMETERS_TWO);

    public final static Attribute TEST_QUERY_FILTER_ATTRIBUTE_THREE = new StandardAttribute(TEST_ATTRIBUTE_NAME_THREE, TEST_ATTRIBUTE_VALUE_THREE,
        TEST_ATTRIBUTE_PARAMETERS_THREE);

    private RepositoryTestData() {
    }

    static RepositoryAwareArtifactDescriptor createDescriptor(String type, String name, Version version, Set<Attribute> attributes) {
        return createDescriptor(URI.create(TESTING_DIRECTORY + name + "." + type), type, name, version, attributes);
    }

    private static RepositoryAwareArtifactDescriptor createDescriptor(URI uri, String type, String name, Version version, Set<Attribute> attributes) {
        ArtifactDescriptorBuilder builder = new ArtifactDescriptorBuilder().setUri(uri).setType(type).setName(name).setVersion(version);
        for (Attribute attribute : attributes) {
            builder.addAttribute(attribute);
        }
        return new DelegatingRepositoryAwareArtifactDescriptor(builder.build(), null, new IdentityUriMapper());
    }

    private static Set<Attribute> createAttributeSet(String type, String name, Version version, URI uri, StandardAttribute... extras) {
        Set<Attribute> attributes = new HashSet<Attribute>();
        attributes.add(new StandardAttribute(ARTEFACT_ATTRIBUTE_TYPE, type));
        attributes.add(new StandardAttribute(ARTEFACT_ATTRIBUTE_NAME, name));
        attributes.add(new StandardAttribute(ARTEFACT_ATTRIBUTE_VERSION, version.toString()));
        attributes.add(new StandardAttribute(ARTEFACT_ATTRIBUTE_URI, uri.toString()));
        for (StandardAttribute extra : extras) {
            attributes.add(extra);
        }
        return attributes;
    }

}
