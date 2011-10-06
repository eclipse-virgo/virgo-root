/*******************************************************************************
 * Copyright (c) 2010 Oracle.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution. 
 * The Eclipse Public License is available at
 *     http://www.eclipse.org/legal/epl-v10.html
 * and the Apache License v2.0 is available at 
 *     http://www.opensource.org/licenses/apache2.0.php.
 * You may elect to redistribute this code under either of these licenses.
 *
 * Contributors:
 *     Hal Hildebrand - Initial JMX support 
 ******************************************************************************/

package org.eclipse.gemini.mgmt;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.StandardMBean;

import org.eclipse.gemini.mgmt.cm.ConfigAdminManager;
import org.eclipse.virgo.mgmt.BundleState;
import org.eclipse.gemini.mgmt.framework.CustomBundleStateMBean;
import org.eclipse.gemini.mgmt.framework.Framework;
import org.eclipse.gemini.mgmt.framework.PackageState;
import org.eclipse.gemini.mgmt.framework.ServiceState;
import org.eclipse.gemini.mgmt.permissionadmin.PermissionManager;
import org.eclipse.gemini.mgmt.provisioning.Provisioning;
import org.eclipse.gemini.mgmt.useradmin.UserManager;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.jmx.framework.FrameworkMBean;
import org.osgi.jmx.framework.BundleStateMBean;
import org.osgi.jmx.framework.PackageStateMBean;
import org.osgi.jmx.framework.ServiceStateMBean;
import org.osgi.jmx.service.cm.ConfigurationAdminMBean;
import org.osgi.jmx.service.permissionadmin.PermissionAdminMBean;
import org.osgi.jmx.service.provisioning.ProvisioningServiceMBean;
import org.osgi.jmx.service.useradmin.UserAdminMBean;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.packageadmin.PackageAdmin;
import org.osgi.service.permissionadmin.PermissionAdmin;
import org.osgi.service.provisioning.ProvisioningService;
import org.osgi.service.startlevel.StartLevel;
import org.osgi.service.useradmin.UserAdmin;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/**
 * The bundle activator which starts and stops the system, as well as providing
 * the service tracker which listens for the MBeanServer. When the MBeanServer
 * is found, the MBeans representing the OSGi services will be installed.
 * 
 */
public class Activator implements BundleActivator {

	private static final Logger log = Logger.getLogger(Activator.class.getCanonicalName());
	
	private MBeanServer server;
	
	protected List<MBeanServer> mbeanServers = new CopyOnWriteArrayList<MBeanServer>();
	protected StandardMBean bundleState;
	protected StandardMBean packageState;
	protected StandardMBean serviceState;
	protected BundleContext userRegionbundleContext;
	protected BundleContext bundleContext;
	protected ObjectName bundlesStateName;
	protected StandardMBean framework;
	protected ObjectName frameworkName;
	protected ObjectName packageStateName;
	protected ObjectName serviceStateName;
	protected ObjectName configAdminName;
	protected ObjectName permissionAdminName;
	protected ObjectName provisioningServiceName;
	protected ObjectName userAdminName;
	protected AtomicBoolean servicesRegistered = new AtomicBoolean(false);
	protected ServiceTracker configAdminTracker;
	protected ServiceTracker permissionAdminTracker;
	protected ServiceTracker provisioningServiceTracker;
	protected ServiceTracker userAdminTracker;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext bundleContext) throws Exception {
		this.userRegionbundleContext = bundleContext;
		this.bundleContext = bundleContext.getBundle(0).getBundleContext();
		frameworkName = new ObjectName(FrameworkMBean.OBJECTNAME);
		bundlesStateName = new ObjectName(CustomBundleStateMBean.OBJECTNAME);
		serviceStateName = new ObjectName(ServiceStateMBean.OBJECTNAME);
		packageStateName = new ObjectName(PackageStateMBean.OBJECTNAME);
		configAdminName = new ObjectName(ConfigurationAdminMBean.OBJECTNAME);
		permissionAdminName = new ObjectName(PermissionAdminMBean.OBJECTNAME);
		provisioningServiceName = new ObjectName(ProvisioningServiceMBean.OBJECTNAME);
		userAdminName = new ObjectName(UserAdminMBean.OBJECTNAME);
		
		this.server = ManagementFactory.getPlatformMBeanServer();
		this.mbeanServers.add(this.server);
		this.registerServices(this.server);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext arg0) throws Exception {
		for (MBeanServer mbeanServer : mbeanServers) {
			deregisterServices(mbeanServer);
		}
		mbeanServers.clear();
	}

	/**
     */
	protected synchronized void deregisterServices(MBeanServer mbeanServer) {
		if (!servicesRegistered.get()) {
			return;
		}
		log.fine("Deregistering framework with MBeanServer: " + mbeanServer);
		try {
			mbeanServer.unregisterMBean(frameworkName);
		} catch (InstanceNotFoundException e) {
			log.log(Level.FINE, "FrameworkMBean not found on deregistration", e);
		} catch (MBeanRegistrationException e) {
			log.log(Level.FINE, "FrameworkMBean deregistration problem", e);
		}
		framework = null;

		try {
			mbeanServer.unregisterMBean(bundlesStateName);
		} catch (InstanceNotFoundException e) {
			log.log(Level.FINEST,
					"OSGi BundleStateMBean not found on deregistration", e);
		} catch (MBeanRegistrationException e) {
			log.log(Level.FINE, 
					"OSGi BundleStateMBean deregistration problem", e);
		}
		bundleState = null;

		log.fine("Deregistering services monitor with MBeanServer: "
				+ mbeanServer);
		try {
			mbeanServer.unregisterMBean(serviceStateName);
		} catch (InstanceNotFoundException e) {
			log.log(Level.FINEST,
					"OSGi ServiceStateMBean not found on deregistration", e);
		} catch (MBeanRegistrationException e) {
			log.log(Level.FINE,
					"OSGi ServiceStateMBean deregistration problem", e);
		}
		serviceState = null;

		log.fine("Deregistering packages monitor with MBeanServer: "
				+ mbeanServer);
		try {
			mbeanServer.unregisterMBean(packageStateName);
		} catch (InstanceNotFoundException e) {
			log.log(Level.FINEST,
					"OSGi PackageStateMBean not found on deregistration", e);
		} catch (MBeanRegistrationException e) {
			log.log(Level.FINE,
					"OSGi PackageStateMBean deregistration problem", e);
		}
		packageState = null;

		log.fine("Deregistering config admin with MBeanServer: " + mbeanServer);
		configAdminTracker.close();
		try {
			mbeanServer.unregisterMBean(configAdminName);
		} catch (InstanceNotFoundException e) {
			log.log(Level.FINEST,
					"OSGi ConfigAdminMBean not found on deregistration", e);
		} catch (MBeanRegistrationException e) {
			log.log(Level.FINE, 
					"OSGi ConfigAdminMBean deregistration problem", e);
		}
		configAdminTracker = null;

		log.fine("Deregistering permission admin with MBeanServer: "
				+ mbeanServer);
		permissionAdminTracker.close();
		try {
			mbeanServer.unregisterMBean(permissionAdminName);
		} catch (InstanceNotFoundException e) {
			log.log(Level.FINEST,
					"OSGi PermissionAdminMBean not found on deregistration", e);
		} catch (MBeanRegistrationException e) {
			log.log(Level.FINE,
					"OSGi PermissionAdminMBean deregistration problem", e);
		}
		permissionAdminTracker = null;

		log.fine("Deregistering provisioning service admin with MBeanServer: "
				+ mbeanServer);
		provisioningServiceTracker.close();
		try {
			mbeanServer.unregisterMBean(provisioningServiceName);
		} catch (InstanceNotFoundException e) {
			log.log(Level.FINEST,
					"OSGi ProvisioningServiceMBean not found on deregistration", e);
		} catch (MBeanRegistrationException e) {
			log.log(Level.FINE,
					"OSGi ProvisioningServiceMBean deregistration problem", e);
		}
		provisioningServiceTracker = null;

		log.fine("Deregistering user admin with MBeanServer: " + mbeanServer);
		userAdminTracker.close();
		try {
			mbeanServer.unregisterMBean(userAdminName);
		} catch (InstanceNotFoundException e) {
			log.log(Level.FINEST,
					"OSGi UserAdminMBean not found on deregistration", e);
		} catch (MBeanRegistrationException e) {
			log.log(Level.FINE, "OSGi UserAdminMBean deregistration problem", e);
		}
		userAdminTracker = null;

		servicesRegistered.set(false);
	}

	/**
     */
	protected synchronized void registerServices(MBeanServer mbeanServer) {
		PackageAdmin admin = (PackageAdmin) bundleContext
				.getService(bundleContext
						.getServiceReference(PackageAdmin.class
								.getCanonicalName()));
		StartLevel sl = (StartLevel) bundleContext.getService(bundleContext
				.getServiceReference(StartLevel.class.getCanonicalName()));
		try {
			framework = new StandardMBean(new Framework(bundleContext, admin,
					sl), FrameworkMBean.class);
		} catch (NotCompliantMBeanException e) {
			log.log(Level.SEVERE,
					"Unable to create StandardMBean for Framework", e);
			return;
		}
		try {
			bundleState = new StandardMBean(new BundleState(bundleContext), BundleStateMBean.class);
		} catch (NotCompliantMBeanException e) {
			log.log(Level.SEVERE, "Unable to create StandardMBean for BundleState", e);
			return;
		}
		try {
			serviceState = new StandardMBean(new ServiceState(bundleContext),
					ServiceStateMBean.class);
		} catch (NotCompliantMBeanException e) {
			log.log(Level.SEVERE,
					"Unable to create StandardMBean for ServiceState", e);
			return;
		}
		try {
			packageState = new StandardMBean(new PackageState(bundleContext,
					admin), PackageStateMBean.class);
		} catch (NotCompliantMBeanException e) {
			log.log(Level.SEVERE,
					"Unable to create StandardMBean for PackageState", e);
			return;
		}

		log.fine("Registering Framework with MBeanServer: " + mbeanServer
				+ " with name: " + frameworkName);
		try {
			mbeanServer.registerMBean(framework, frameworkName);
		} catch (InstanceAlreadyExistsException e) {
			log.log(Level.FINE, "Cannot register OSGi framework MBean", e);
		} catch (MBeanRegistrationException e) {
			log.log(Level.SEVERE, "Cannot register OSGi framework MBean", e);
		} catch (NotCompliantMBeanException e) {
			log.log(Level.SEVERE, "Cannot register OSGi framework MBean", e);
		}

		log.fine("Registering bundle state monitor with MBeanServer: "
				+ mbeanServer + " with name: " + bundlesStateName);
		try {
			mbeanServer.registerMBean(bundleState, bundlesStateName);
		} catch (InstanceAlreadyExistsException e) {
			log.log(Level.FINE, "Cannot register OSGi BundleStateMBean", e);
		} catch (MBeanRegistrationException e) {
			log.log(Level.SEVERE, "Cannot register OSGi BundleStateMBean", e);
		} catch (NotCompliantMBeanException e) {
			log.log(Level.SEVERE, "Cannot register OSGi BundleStateMBean", e);
		}

		log.fine("Registering services monitor with MBeanServer: "
				+ mbeanServer + " with name: " + serviceStateName);
		try {
			mbeanServer.registerMBean(serviceState, serviceStateName);
		} catch (InstanceAlreadyExistsException e) {
			log.log(Level.FINE, "Cannot register OSGi ServiceStateMBean", e);
		} catch (MBeanRegistrationException e) {
			log.log(Level.SEVERE, "Cannot register OSGi ServiceStateMBean", e);
		} catch (NotCompliantMBeanException e) {
			log.log(Level.SEVERE, "Cannot register OSGi ServiceStateMBean", e);
		}

		log.fine("Registering packages monitor with MBeanServer: "
				+ mbeanServer + " with name: " + packageStateName);
		try {
			mbeanServer.registerMBean(packageState, packageStateName);
		} catch (InstanceAlreadyExistsException e) {
			log.log(Level.FINE, "Cannot register OSGi PackageStateMBean", e);
		} catch (MBeanRegistrationException e) {
			log.log(Level.SEVERE, "Cannot register OSGi PackageStateMBean", e);
		} catch (NotCompliantMBeanException e) {
			log.log(Level.SEVERE, "Cannot register OSGi PackageStateMBean", e);
		}

		configAdminTracker = new ServiceTracker(userRegionbundleContext,
				"org.osgi.service.cm.ConfigurationAdmin",
				new ConfigAdminTracker());
		permissionAdminTracker = new ServiceTracker(bundleContext,
				"org.osgi.service.permissionadmin.PermissionAdmin",
				new PermissionAdminTracker());
		provisioningServiceTracker = new ServiceTracker(bundleContext,
				"org.osgi.service.provisioning.ProvisioningService",
				new ProvisioningServiceTracker());
		userAdminTracker = new ServiceTracker(bundleContext,
				"org.osgi.service.useradmin.UserAdmin", new UserAdminTracker());
		configAdminTracker.open();
		permissionAdminTracker.open();
		provisioningServiceTracker.open();
		userAdminTracker.open();
		servicesRegistered.set(true);
	}

	class ConfigAdminTracker implements ServiceTrackerCustomizer {
		StandardMBean manager;

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.osgi.util.tracker.ServiceTrackerCustomizer#addingService(org.
		 * osgi.framework.ServiceReference)
		 */
		public Object addingService(ServiceReference reference) {
			ConfigurationAdmin admin;
			try {
				admin = (ConfigurationAdmin) userRegionbundleContext.getService(reference);
			} catch (ClassCastException e) {
				log.log(Level.SEVERE, "Incompatible class version for the Configuration Admin Manager", e);
				return userRegionbundleContext.getService(reference);
			}

			try {
				manager = new StandardMBean(new ConfigAdminManager(admin),
						ConfigurationAdminMBean.class);
			} catch (NotCompliantMBeanException e) {
				log.log(Level.SEVERE,
						"Unable to create Configuration Admin Manager", e);
				return admin;
			}
			for (MBeanServer mbeanServer : mbeanServers) {
				log.fine("Registering configuration admin with MBeanServer: "
						+ mbeanServer + " with name: " + configAdminName);
				try {
					mbeanServer.registerMBean(manager, configAdminName);
				} catch (InstanceAlreadyExistsException e) {
					log.log(Level.FINE,
							"Cannot register Configuration Manager MBean", e);
				} catch (MBeanRegistrationException e) {
					log.log(Level.SEVERE,
							"Cannot register Configuration Manager MBean", e);
				} catch (NotCompliantMBeanException e) {
					log.log(Level.SEVERE,
							"Cannot register Configuration Manager MBean", e);
				}
			}
			return admin;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.osgi.util.tracker.ServiceTrackerCustomizer#modifiedService(org
		 * .osgi.framework.ServiceReference, java.lang.Object)
		 */
		public void modifiedService(ServiceReference reference, Object service) {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.osgi.util.tracker.ServiceTrackerCustomizer#removedService(org
		 * .osgi.framework.ServiceReference, java.lang.Object)
		 */
		public void removedService(ServiceReference reference, Object service) {

			for (MBeanServer mbeanServer : mbeanServers) {
				log.fine("deregistering configuration admin from: "
						+ mbeanServer + " with name: " + configAdminName);
				try {
					mbeanServer.unregisterMBean(configAdminName);
				} catch (InstanceNotFoundException e) {
					log.fine("Configuration Manager MBean was never registered");
				} catch (MBeanRegistrationException e) {
					log.log(Level.SEVERE,
							"Cannot deregister Configuration Manager MBean", e);
				}
			}
		}
	}

	class PermissionAdminTracker implements ServiceTrackerCustomizer {
		StandardMBean manager;

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.osgi.util.tracker.ServiceTrackerCustomizer#addingService(org.
		 * osgi.framework.ServiceReference)
		 */
		public Object addingService(ServiceReference reference) {
			PermissionAdmin admin;
			try {
				admin = (PermissionAdmin) bundleContext.getService(reference);
			} catch (ClassCastException e) {
				log.log(Level.SEVERE,
						"Incompatible class version for the Permission Admin Manager", e);
				return bundleContext.getService(reference);
			}
			try {
				manager = new StandardMBean(new PermissionManager(admin),
						PermissionAdminMBean.class);
			} catch (NotCompliantMBeanException e) {
				log.log(Level.SEVERE, 
						"Unable to create Permission Admin Manager", e);
				return admin;
			}
			for (MBeanServer mbeanServer : mbeanServers) {
				log.fine("Registering permission admin with MBeanServer: "
						+ mbeanServer + " with name: " + permissionAdminName);
				try {
					mbeanServer.registerMBean(manager, permissionAdminName);
				} catch (InstanceAlreadyExistsException e) {
					log.log(Level.FINE,
							"Cannot register Permission Manager MBean", e);
				} catch (MBeanRegistrationException e) {
					log.log(Level.SEVERE,
							"Cannot register Permission Manager MBean", e);
				} catch (NotCompliantMBeanException e) {
					log.log(Level.SEVERE,
							"Cannot register Permission Manager MBean", e);
				}
			}
			return admin;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.osgi.util.tracker.ServiceTrackerCustomizer#modifiedService(org
		 * .osgi.framework.ServiceReference, java.lang.Object)
		 */
		public void modifiedService(ServiceReference reference, Object service) {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.osgi.util.tracker.ServiceTrackerCustomizer#removedService(org
		 * .osgi.framework.ServiceReference, java.lang.Object)
		 */
		public void removedService(ServiceReference reference, Object service) {
			for (MBeanServer mbeanServer : mbeanServers) {
				log.fine("deregistering permission admin with MBeanServer: "
						+ mbeanServer + " with name: " + permissionAdminName);
				try {
					mbeanServer.unregisterMBean(permissionAdminName);
				} catch (InstanceNotFoundException e) {
					log.fine("Permission Manager MBean was never registered");
				} catch (MBeanRegistrationException e) {
					log.log(Level.SEVERE,
							"Cannot deregister Permission Manager MBean", e);
				}
			}
		}
	}

	class ProvisioningServiceTracker implements ServiceTrackerCustomizer {
		StandardMBean provisioning;

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.osgi.util.tracker.ServiceTrackerCustomizer#addingService(org.
		 * osgi.framework.ServiceReference)
		 */
		public Object addingService(ServiceReference reference) {
			ProvisioningService service;
			try {
				service = (ProvisioningService) bundleContext
						.getService(reference);
			} catch (ClassCastException e) {
				log.log(Level.SEVERE,
						"Incompatible class version for the Provisioning service", e);
				return bundleContext.getService(reference);
			}
			try {
				provisioning = new StandardMBean(new Provisioning(service),
						ProvisioningServiceMBean.class);
			} catch (NotCompliantMBeanException e) {
				log.log(Level.SEVERE,
						"Unable to create Provisioning Service Manager", e);
				return service;
			}
			for (MBeanServer mbeanServer : mbeanServers) {
				log.fine("Registering provisioning service with MBeanServer: "
						+ mbeanServer + " with name: "
						+ provisioningServiceName);
				try {
					mbeanServer.registerMBean(provisioning,
							provisioningServiceName);
				} catch (InstanceAlreadyExistsException e) {
					log.log(Level.FINE,
							"Cannot register Provisioning Service MBean", e);
				} catch (MBeanRegistrationException e) {
					log.log(Level.SEVERE,
							"Cannot register Provisioning Service MBean", e);
				} catch (NotCompliantMBeanException e) {
					log.log(Level.SEVERE,
							"Cannot register Provisioning Service MBean", e);
				}
			}
			return service;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.osgi.util.tracker.ServiceTrackerCustomizer#modifiedService(org
		 * .osgi.framework.ServiceReference, java.lang.Object)
		 */
		public void modifiedService(ServiceReference reference, Object service) {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.osgi.util.tracker.ServiceTrackerCustomizer#removedService(org
		 * .osgi.framework.ServiceReference, java.lang.Object)
		 */
		public void removedService(ServiceReference reference, Object service) {
			for (MBeanServer mbeanServer : mbeanServers) {
				log.fine("deregistering provisioning service with MBeanServer: "
						+ mbeanServer + " with name: " + provisioningServiceName);
				try {
					mbeanServer.unregisterMBean(provisioningServiceName);
				} catch (InstanceNotFoundException e) {
					log.fine("Provisioning Service MBean was never registered");
				} catch (MBeanRegistrationException e) {
					log.log(Level.SEVERE,
							"Cannot deregister Provisioning Service MBean", e);
				}
			}
		}
	}

	class UserAdminTracker implements ServiceTrackerCustomizer {
		StandardMBean manager;

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.osgi.util.tracker.ServiceTrackerCustomizer#addingService(org.
		 * osgi.framework.ServiceReference)
		 */
		public Object addingService(ServiceReference reference) {
			UserAdmin admin;
			try {
				admin = (UserAdmin) bundleContext.getService(reference);
			} catch (ClassCastException e) {
				log.log(Level.SEVERE,
						"Incompatible class version for the User Admin manager", e);
				return bundleContext.getService(reference);
			}
			try {
				manager = new StandardMBean(new UserManager(admin),
						UserAdminMBean.class);
			} catch (NotCompliantMBeanException e1) {
				log.log(Level.SEVERE, "Unable to create User Admin Manager");
				return admin;
			}
			for (MBeanServer mbeanServer : mbeanServers) {
				log.fine("Registering user admin with MBeanServer: "
						+ mbeanServer + " with name: " + userAdminName);
				try {
					mbeanServer.registerMBean(manager, userAdminName);
				} catch (InstanceAlreadyExistsException e) {
					log.log(Level.FINE, "Cannot register User Manager MBean", e);
				} catch (MBeanRegistrationException e) {
					log.log(Level.SEVERE, "Cannot register User Manager MBean", e);
				} catch (NotCompliantMBeanException e) {
					log.log(Level.SEVERE, "Cannot register User Manager MBean", e);
				}
			}
			return admin;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.osgi.util.tracker.ServiceTrackerCustomizer#modifiedService(org
		 * .osgi.framework.ServiceReference, java.lang.Object)
		 */
		public void modifiedService(ServiceReference reference, Object service) {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.osgi.util.tracker.ServiceTrackerCustomizer#removedService(org
		 * .osgi.framework.ServiceReference, java.lang.Object)
		 */
		public void removedService(ServiceReference reference, Object service) {
			for (MBeanServer mbeanServer : mbeanServers) {
				log.fine("Deregistering user admin with MBeanServer: "
						+ mbeanServer + " with name: " + userAdminName);
				try {
					mbeanServer.unregisterMBean(userAdminName);
				} catch (InstanceNotFoundException e) {
					log.fine("User Manager MBean was never registered");
				} catch (MBeanRegistrationException e) {
					log.log(Level.SEVERE,
							"Cannot deregister User Manager MBean", e);
				}
			}
		}
	}
}
