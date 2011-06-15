/**
 * 
 */
package org.eclipse.virgo.medic.log.osgi;

import org.eclipse.equinox.log.LogFilter;
import org.osgi.framework.Bundle;

/**
 * @author cgfrost
 *
 */
public class VirgoLogFilter implements LogFilter {
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isLoggable(Bundle bundle, String loggerName, int logLevel) {
		return true; //!bundle.getSymbolicName().startsWith("org.eclipse.virgo") && (LogService.LOG_WARNING == logLevel || LogService.LOG_ERROR == logLevel);
	}

}
