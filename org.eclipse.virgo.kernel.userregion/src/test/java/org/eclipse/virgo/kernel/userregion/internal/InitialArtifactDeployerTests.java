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

package org.eclipse.virgo.kernel.userregion.internal;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;


import org.eclipse.virgo.nano.core.Shutdown;
import org.eclipse.virgo.nano.deployer.api.core.ApplicationDeployer;
import org.eclipse.virgo.nano.deployer.api.core.DeployUriNormaliser;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentOptions;
import org.eclipse.virgo.kernel.userregion.internal.InitialArtifactDeployer;
import org.eclipse.virgo.kernel.userregion.internal.KernelStartedAwaiter;
import org.eclipse.virgo.medic.test.eventlog.MockEventLogger;

public class InitialArtifactDeployerTests {
	
	private static final Map<String, ?> NULL_PROPERTIES = (Map<String, ?>)null;

    private ApplicationDeployer deployer = createMock(ApplicationDeployer.class);
	
	private KernelStartedAwaiter startedAwaiter = new KernelStartedAwaiter();
	
	private StubEventAdmin eventAdmin = new StubEventAdmin();
	
	private MockEventLogger eventLogger = new MockEventLogger();
	
	private Shutdown shutdown = createMock(Shutdown.class);
	
	@Test
	public void basicEventingWithNoArtifactDeployment() throws InterruptedException {
		replay(this.deployer);
		
		InitialArtifactDeployer initialArtifactDeployer = new InitialArtifactDeployer(this.startedAwaiter, this.deployer, "", "", new StubDeployUriNormaliser(), this.eventAdmin, this.eventLogger, this.shutdown);
		this.startedAwaiter.handleEvent(new Event("org/eclipse/virgo/kernel/STARTED", NULL_PROPERTIES));
		
		initialArtifactDeployer.deployArtifacts();
		
		Event eventSystemArtifactsDeployed = new Event("org/eclipse/virgo/kernel/userregion/systemartifacts/DEPLOYED", NULL_PROPERTIES);
		this.eventAdmin.awaitPostingOfEvent(eventSystemArtifactsDeployed);
		
		initialArtifactDeployer.handleEvent(eventSystemArtifactsDeployed);
		this.eventAdmin.awaitPostingOfEvent(new Event("org/eclipse/virgo/kernel/userregion/userartifacts/DEPLOYED", NULL_PROPERTIES));
		
		verify(this.deployer);
	}
	
	@Test
	public void artifactDeployment() throws DeploymentException, InterruptedException {		
		
		InitialArtifactDeployer initialArtifactDeployer = new InitialArtifactDeployer(this.startedAwaiter, this.deployer, "repository:alpha/bravo/1, repository:alpha/bravo/2", "repository:charlie/delta,repository:echo/foxtrot/2", new StubDeployUriNormaliser(), this.eventAdmin, this.eventLogger, this.shutdown);				
		
		expect(this.deployer.deploy(URI.create("repository:alpha/bravo/1"), new DeploymentOptions(false, false, true))).andReturn(null);		
		expect(this.deployer.deploy(URI.create("repository:alpha/bravo/2"), new DeploymentOptions(false, false, true))).andReturn(null);
		replay(this.deployer);
		
		this.startedAwaiter.handleEvent(new Event("org/eclipse/virgo/kernel/STARTED", NULL_PROPERTIES));
		initialArtifactDeployer.deployArtifacts();
		
		Event eventSystemArtifactsDeployed = new Event("org/eclipse/virgo/kernel/userregion/systemartifacts/DEPLOYED", NULL_PROPERTIES);
		this.eventAdmin.awaitPostingOfEvent(eventSystemArtifactsDeployed);

		verify(this.deployer);
		
		reset(this.deployer);
		
		expect(this.deployer.deploy(URI.create("repository:charlie/delta"), new DeploymentOptions(false, false, true))).andReturn(null);		
		expect(this.deployer.deploy(URI.create("repository:echo/foxtrot/2"), new DeploymentOptions(false, false, true))).andReturn(null);
		
		replay(this.deployer);
		
		initialArtifactDeployer.handleEvent(eventSystemArtifactsDeployed);
		this.eventAdmin.awaitPostingOfEvent(new Event("org/eclipse/virgo/kernel/userregion/userartifacts/DEPLOYED", NULL_PROPERTIES));
		
		verify(this.deployer);
	}
	
	@Test
	public void failedDeploymentLogsMessageAndTriggersShutdown() throws DeploymentException, InterruptedException {
		InitialArtifactDeployer initialArtifactDeployer = new InitialArtifactDeployer(this.startedAwaiter, this.deployer, "repository:alpha/bravo/1", null, new StubDeployUriNormaliser(), this.eventAdmin, this.eventLogger, this.shutdown);				
		
		expect(this.deployer.deploy(URI.create("repository:alpha/bravo/1"), new DeploymentOptions(false, false, true))).andThrow(new DeploymentException("Deployment failed"));
		replay(this.deployer);
		
		this.shutdown.shutdown();
		replay(this.shutdown);
		
		this.startedAwaiter.handleEvent(new Event("org/eclipse/virgo/kernel/STARTED", NULL_PROPERTIES));
		
		initialArtifactDeployer.deployArtifacts();
		
		while (!this.eventLogger.isLogged("UR0002E")) {
			Thread.sleep(100);
		}
		
		Thread.sleep(100);
		
		verify(this.deployer, this.shutdown);
	}
	
	private final class StubEventAdmin implements EventAdmin {
		
		private final List<Event> postedEvents = new ArrayList<Event>();
		
		private final List<Event> sentEvents = new ArrayList<Event>();
		
		private final Object monitor = new Object();

		public void postEvent(Event event) {
			synchronized (this.monitor) {
				this.postedEvents.add(event);
			}
		}

		public void sendEvent(Event event) {
			synchronized (this.monitor) {
				this.sentEvents.add(event);
			}			
		}		
		
		public void awaitPostingOfEvent(Event event) {
			boolean eventSent = false;
			while (!eventSent) {				
				synchronized (this.monitor) {
					eventSent = this.postedEvents.contains(event);
				}
			}
		}
	}
	
	private static final class StubDeployUriNormaliser implements DeployUriNormaliser {
		public URI normalise(URI uri) throws DeploymentException {
			return uri;
		}
	}		
}
