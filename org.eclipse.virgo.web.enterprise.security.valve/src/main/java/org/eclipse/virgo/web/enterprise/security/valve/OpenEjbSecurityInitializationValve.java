package org.eclipse.virgo.web.enterprise.security.valve;

import java.io.IOException;

import javax.servlet.ServletException;

import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.Assembler;
import org.eclipse.virgo.web.enterprise.security.StandardSecurityService;

public class OpenEjbSecurityInitializationValve extends ValveBase {

	private StandardSecurityService securityService;

	@Override
	public void invoke(Request request, Response response) throws IOException,
			ServletException {
		Object oldState = null;
		Wrapper wrapper = (Wrapper) request.getMappingData().wrapper;
		
		if (getSecurityService() != null && wrapper != null) {
			oldState = securityService.enterWebApp(wrapper,
					request.getPrincipal(), wrapper.getRunAs());
		}

		try {
			getNext().invoke(request, response);
		} finally {
			if (securityService != null) {
				securityService.exitWebApp(oldState);
			}
		}

	}
	
	  private StandardSecurityService getSecurityService() {
	  	  if(securityService == null) {	  		
	  		Assembler assembler = (Assembler) SystemInstance.get().getComponent(org.apache.openejb.spi.Assembler.class);
	  		securityService = (StandardSecurityService)assembler.getSecurityService();
	  	  }
	  	  return securityService;
	    }

}
