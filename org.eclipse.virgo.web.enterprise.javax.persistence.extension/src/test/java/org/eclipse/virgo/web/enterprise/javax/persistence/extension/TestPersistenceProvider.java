/*******************************************************************************
 * Copyright (c) 2013 SAP AG
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   SAP AG - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.web.enterprise.javax.persistence.extension;

import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.ProviderUtil;

public class TestPersistenceProvider implements PersistenceProvider {

	@Override
	public EntityManagerFactory createContainerEntityManagerFactory(
			PersistenceUnitInfo arg0, @SuppressWarnings("rawtypes") Map arg1) {
		return null;
	}

	@Override
	public EntityManagerFactory createEntityManagerFactory(String arg0, @SuppressWarnings("rawtypes") Map arg1) {
		return null;
	}

	@Override
	public ProviderUtil getProviderUtil() {
		return null;
	}

	@Override
	public boolean generateSchema(String arg0, Map arg1) {
		return false;
	}

	@Override
	public void generateSchema(PersistenceUnitInfo arg0, Map arg1) {
	}
}
