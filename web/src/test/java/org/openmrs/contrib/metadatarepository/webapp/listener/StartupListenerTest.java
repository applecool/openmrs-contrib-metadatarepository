/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */

package org.openmrs.contrib.metadatarepository.webapp.listener;

import junit.framework.TestCase;

import org.openmrs.contrib.metadatarepository.Constants;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.Map;
import org.junit.Ignore;

/**
 * This class tests the StartupListener class to verify that variables are placed into the servlet
 * context.
 * 
 * @author <a href="mailto:matt@raibledesigns.com">Matt Raible</a>
 */
public class StartupListenerTest extends TestCase {
	
	private MockServletContext sc = null;
	
	private ServletContextListener listener = null;
	
	private ContextLoaderListener springListener = null;
	
	protected void setUp() throws Exception {
		super.setUp();
		sc = new MockServletContext("");
		sc.addInitParameter(Constants.CSS_THEME, "simplicity");
		
		// initialize Spring
		sc.addInitParameter(ContextLoader.CONFIG_LOCATION_PARAM, "classpath:/applicationContext-dao.xml, "
		        + "classpath:/applicationContext-service.xml, " + "classpath:/applicationContext-resources.xml");
		
		springListener = new ContextLoaderListener();
		springListener.contextInitialized(new ServletContextEvent(sc));
		listener = new StartupListener();
	}
	
	protected void tearDown() throws Exception {
		super.tearDown();
		springListener = null;
		listener = null;
		sc = null;
	}
	
	public void testContextInitialized() {
		listener.contextInitialized(new ServletContextEvent(sc));
		
		assertTrue(sc.getAttribute(Constants.CONFIG) != null);
		Map config = (Map) sc.getAttribute(Constants.CONFIG);
		assertEquals(config.get(Constants.CSS_THEME), "simplicity");
		
		assertTrue(sc.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE) != null);
		assertTrue(sc.getAttribute(Constants.AVAILABLE_ROLES) != null);
	}
}
