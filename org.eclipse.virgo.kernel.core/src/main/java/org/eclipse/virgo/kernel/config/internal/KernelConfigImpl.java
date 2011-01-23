package org.eclipse.virgo.kernel.config.internal;

import org.eclipse.virgo.kernel.core.KernelConfig;
import org.osgi.service.component.ComponentContext;

public class KernelConfigImpl implements KernelConfig {
	
	ComponentContext context;
	
	private final String DOMAIN = "domain";

	protected void activate(ComponentContext context) {
		this.context = context;
		System.out
				.println("Activating KernelConfigImpl component with properties "
						+ context.getProperties());
	}

	protected void deactivate(ComponentContext context) {
		System.out.println("Deactivating KernelConfigImpl component");
		this.context = null;
	}

	public String getProperty(String name) {
		Object value = context.getProperties().get(name);
		System.out.println("KernelConfig.getProperty() is called with name ["
				+ name + "] value is [" + value + "]");
		if (value instanceof String) {
			return (String) value;
		} else if (value instanceof String[] && ((String[]) value).length > 0) {
			return ((String[]) value)[0];
		} else {
			return value.toString();
		}
	}
	
}
