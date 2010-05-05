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

import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_ARTEFACT_EIGHT;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_ARTEFACT_ELEVEN;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_ARTEFACT_FIFTEEN;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_ARTEFACT_FIVE;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_ARTEFACT_FOUR;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_ARTEFACT_FOURTEEN;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_ARTEFACT_NINE;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_ARTEFACT_ONE;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_ARTEFACT_SEVEN;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_ARTEFACT_SIX;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_ARTEFACT_SIXTEEN;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_ARTEFACT_TEN;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_ARTEFACT_THIRTEEN;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_ARTEFACT_THREE;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_ARTEFACT_TWELVE;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_ARTEFACT_TWO;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_URI_EIGHT;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_URI_ELEVEN;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_URI_FIFTEEN;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_URI_FIVE;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_URI_FOUR;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_URI_FOURTEEN;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_URI_NINE;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_URI_ONE;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_URI_SEVEN;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_URI_SIX;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_URI_SIXTEEN;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_URI_TEN;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_URI_THIRTEEN;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_URI_THREE;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_URI_TWELVE;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_URI_TWO;

import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.virgo.repository.ArtifactBridge;
import org.eclipse.virgo.repository.ArtifactDescriptor;
import org.eclipse.virgo.repository.RepositoryAwareArtifactDescriptor;


/**
 * <p>
 * A Stub impl of Artefact Bridge, is pre-configured to return values from the test data set.
 * </p>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is Threadsafe
 * 
 */
final public class StubArtifactBridge implements ArtifactBridge {

    private final Map<String, RepositoryAwareArtifactDescriptor> testArtefacts = new HashMap<String, RepositoryAwareArtifactDescriptor>();

    public StubArtifactBridge() {

        testArtefacts.put(translate(TEST_URI_ONE), TEST_ARTEFACT_ONE);
        testArtefacts.put(translate(TEST_URI_TWO), TEST_ARTEFACT_TWO);
        testArtefacts.put(translate(TEST_URI_THREE), TEST_ARTEFACT_THREE);
        testArtefacts.put(translate(TEST_URI_FOUR), TEST_ARTEFACT_FOUR);
        testArtefacts.put(translate(TEST_URI_FIVE), TEST_ARTEFACT_FIVE);
        testArtefacts.put(translate(TEST_URI_SIX), TEST_ARTEFACT_SIX);
        testArtefacts.put(translate(TEST_URI_SEVEN), TEST_ARTEFACT_SEVEN);
        testArtefacts.put(translate(TEST_URI_EIGHT), TEST_ARTEFACT_EIGHT);
        testArtefacts.put(translate(TEST_URI_NINE), TEST_ARTEFACT_NINE);
        testArtefacts.put(translate(TEST_URI_TEN), TEST_ARTEFACT_TEN);
        testArtefacts.put(translate(TEST_URI_ELEVEN), TEST_ARTEFACT_ELEVEN);
        testArtefacts.put(translate(TEST_URI_TWELVE), TEST_ARTEFACT_TWELVE);
        testArtefacts.put(translate(TEST_URI_THIRTEEN), TEST_ARTEFACT_THIRTEEN);
        testArtefacts.put(translate(TEST_URI_FOURTEEN), TEST_ARTEFACT_FOURTEEN);
        testArtefacts.put(translate(TEST_URI_FIFTEEN), TEST_ARTEFACT_FIFTEEN);
        testArtefacts.put(translate(TEST_URI_SIXTEEN), TEST_ARTEFACT_SIXTEEN);

    }

    /**
     * {@inheritDoc}
     */
    public ArtifactDescriptor generateArtifactDescriptor(File artifact) {
        return this.testArtefacts.get(artifact.getName());
    }

    private String translate(URI uri) {
        return new File(uri).getName();
    }

}
